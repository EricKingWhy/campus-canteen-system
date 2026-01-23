package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import fun.cyhgraph.entity.Orders;
import fun.cyhgraph.entity.OrderDetail;
import fun.cyhgraph.entity.User;
import fun.cyhgraph.mapper.OrderMapper;
import fun.cyhgraph.mapper.OrderDetailMapper;
import fun.cyhgraph.mapper.UserMapper;
import fun.cyhgraph.service.ReportService;
import fun.cyhgraph.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     */
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();

        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current.toString());

            // 查询当天已完成订单的营业额
            LocalDateTime dayStart = LocalDateTime.of(current, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(current, LocalTime.MAX);

            LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Orders::getOrderTime, dayStart);
            wrapper.le(Orders::getOrderTime, dayEnd);
            wrapper.eq(Orders::getStatus, 5); // 已完成

            List<Orders> orders = orderMapper.selectList(wrapper);
            Double turnover = orders.stream()
                    .mapToDouble(o -> o.getAmount() != null ? o.getAmount().doubleValue() : 0)
                    .sum();
            turnoverList.add(String.valueOf(turnover));

            current = current.plusDays(1);
        }

        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateList))
                .turnoverList(String.join(",", turnoverList))
                .build();
    }

    /**
     * 用户统计
     */
    public UserReportVO getUser(LocalDate begin, LocalDate end) {
        List<String> dateList = new ArrayList<>();
        List<String> totalUserList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();

        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current.toString());

            LocalDateTime dayEnd = LocalDateTime.of(current, LocalTime.MAX);
            LocalDateTime dayStart = LocalDateTime.of(current, LocalTime.MIN);

            // 总用户数（截止当天）
            LambdaQueryWrapper<User> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.le(User::getCreateTime, dayEnd);
            Long totalCount = userMapper.selectCount(totalWrapper);
            totalUserList.add(String.valueOf(totalCount));

            // 新增用户数（当天）
            LambdaQueryWrapper<User> newWrapper = new LambdaQueryWrapper<>();
            newWrapper.ge(User::getCreateTime, dayStart);
            newWrapper.le(User::getCreateTime, dayEnd);
            Long newCount = userMapper.selectCount(newWrapper);
            newUserList.add(String.valueOf(newCount));

            current = current.plusDays(1);
        }

        return UserReportVO.builder()
                .dateList(String.join(",", dateList))
                .totalUserList(String.join(",", totalUserList))
                .newUserList(String.join(",", newUserList))
                .build();
    }

    /**
     * 订单统计
     */
    public OrderReportVO getOrder(LocalDate begin, LocalDate end) {
        List<String> dateList = new ArrayList<>();
        List<String> orderCountList = new ArrayList<>();
        List<String> validOrderCountList = new ArrayList<>();

        int totalOrderCount = 0;
        int validOrderCount = 0;

        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current.toString());

            LocalDateTime dayStart = LocalDateTime.of(current, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(current, LocalTime.MAX);

            // 当天订单总数
            LambdaQueryWrapper<Orders> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.ge(Orders::getOrderTime, dayStart);
            totalWrapper.le(Orders::getOrderTime, dayEnd);
            Long dayTotal = orderMapper.selectCount(totalWrapper);
            orderCountList.add(String.valueOf(dayTotal));
            totalOrderCount += dayTotal;

            // 当天有效订单数 (已完成)
            LambdaQueryWrapper<Orders> validWrapper = new LambdaQueryWrapper<>();
            validWrapper.ge(Orders::getOrderTime, dayStart);
            validWrapper.le(Orders::getOrderTime, dayEnd);
            validWrapper.eq(Orders::getStatus, 5);
            Long dayValid = orderMapper.selectCount(validWrapper);
            validOrderCountList.add(String.valueOf(dayValid));
            validOrderCount += dayValid;

            current = current.plusDays(1);
        }

        Double orderCompletionRate = totalOrderCount > 0
                ? (double) validOrderCount / totalOrderCount
                : 0.0;

        return OrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(String.join(",", orderCountList))
                .validOrderCountList(String.join(",", validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量Top10统计
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 查询时间范围内已完成的订单
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.ge(Orders::getOrderTime, beginTime);
        orderWrapper.le(Orders::getOrderTime, endTime);
        orderWrapper.eq(Orders::getStatus, 5); // 已完成
        List<Orders> orders = orderMapper.selectList(orderWrapper);

        if (orders.isEmpty()) {
            return SalesTop10ReportVO.builder()
                    .nameList("")
                    .numberList("")
                    .build();
        }

        // 获取订单ID列表
        List<Long> orderIds = orders.stream().map(Orders::getId).collect(Collectors.toList());

        // 查询订单详情
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.in(OrderDetail::getOrderId, orderIds);
        List<OrderDetail> details = orderDetailMapper.selectList(detailWrapper);

        // 按菜品名称分组统计销量
        Map<String, Integer> salesMap = new HashMap<>();
        for (OrderDetail detail : details) {
            String name = detail.getName();
            Integer number = detail.getNumber();
            salesMap.put(name, salesMap.getOrDefault(name, 0) + number);
        }

        // 排序取Top10
        List<Map.Entry<String, Integer>> sortedList = salesMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        List<String> nameList = sortedList.stream().map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> numberList = sortedList.stream().map(e -> String.valueOf(e.getValue()))
                .collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList))
                .build();
    }

    /**
     * 导出运营数据 (暂不实现，返回空)
     */
    public void exportBusinessData(HttpServletResponse response) {
        // TODO: 实现Excel导出
    }
}
