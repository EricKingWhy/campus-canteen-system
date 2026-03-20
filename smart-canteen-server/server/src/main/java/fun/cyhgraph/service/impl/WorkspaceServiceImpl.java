package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.entity.Orders;
import fun.cyhgraph.entity.Setmeal;
import fun.cyhgraph.mapper.DishMapper;
import fun.cyhgraph.mapper.OrderMapper;
import fun.cyhgraph.mapper.SetmealMapper;
import fun.cyhgraph.service.WorkspaceService;
import fun.cyhgraph.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 获取今日经营数据
     */
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        // 如果未传入时间，默认为今天
        if (begin == null) {
            begin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        }
        if (end == null) {
            end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        }

        // 查询今日订单
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Orders::getOrderTime, begin);
        queryWrapper.le(Orders::getOrderTime, end);
        List<Orders> orderList = orderMapper.selectList(queryWrapper);

        // 计算营业额 (已完成订单的总金额)
        Double turnover = orderList.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() == 5) // 已完成
                .mapToDouble(o -> o.getAmount() != null ? o.getAmount().doubleValue() : 0.0)
                .sum();

        // 有效订单数 (已完成)
        Integer validOrderCount = (int) orderList.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() == 5)
                .count();

        // 订单完成率
        Double orderCompletionRate = orderList.size() > 0
                ? (double) validOrderCount / orderList.size()
                : 0.0;

        // 平均客单价
        Double unitPrice = validOrderCount > 0
                ? BigDecimal.valueOf(turnover / validOrderCount).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // 新增用户 (暂时设为0，因为校园食堂不需要此统计)
        Integer newUsers = 0;

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    /**
     * 获取订单概览数据
     */
    public OrderOverViewVO getOverviewOrders() {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        // 今日订单
        queryWrapper.ge(Orders::getOrderTime, LocalDateTime.of(LocalDate.now(), LocalTime.MIN));
        queryWrapper.le(Orders::getOrderTime, LocalDateTime.of(LocalDate.now(), LocalTime.MAX));
        List<Orders> orderList = orderMapper.selectList(queryWrapper);

        // 待接单 (status = 2)
        Integer waitingOrders = (int) orderList.stream().filter(o -> o.getStatus() != null && o.getStatus() == 2)
                .count();
        // 待派送/制作中 (status = 3)
        Integer deliveredOrders = (int) orderList.stream().filter(o -> o.getStatus() != null && o.getStatus() == 3)
                .count();
        // 已完成 (status = 5)
        Integer completedOrders = (int) orderList.stream().filter(o -> o.getStatus() != null && o.getStatus() == 5)
                .count();
        // 已取消 (status = 6)
        Integer cancelledOrders = (int) orderList.stream().filter(o -> o.getStatus() != null && o.getStatus() == 6)
                .count();
        // 全部订单
        Integer allOrders = orderList.size();

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 获取菜品总览
     */
    public DishOverViewVO getOverviewDishes() {
        // 已启售 (status = 1)
        LambdaQueryWrapper<Dish> soldWrapper = new LambdaQueryWrapper<>();
        soldWrapper.eq(Dish::getStatus, 1);
        Integer sold = Math.toIntExact(dishMapper.selectCount(soldWrapper));

        // 已停售 (status = 0)
        LambdaQueryWrapper<Dish> discontinuedWrapper = new LambdaQueryWrapper<>();
        discontinuedWrapper.eq(Dish::getStatus, 0);
        Integer discontinued = Math.toIntExact(dishMapper.selectCount(discontinuedWrapper));

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 获取套餐总览
     */
    public SetmealOverViewVO getOverviewSetmeals() {
        // 已启售 (status = 1)
        LambdaQueryWrapper<Setmeal> soldWrapper = new LambdaQueryWrapper<>();
        soldWrapper.eq(Setmeal::getStatus, 1);
        Integer sold = Math.toIntExact(setmealMapper.selectCount(soldWrapper));

        // 已停售 (status = 0)
        LambdaQueryWrapper<Setmeal> discontinuedWrapper = new LambdaQueryWrapper<>();
        discontinuedWrapper.eq(Setmeal::getStatus, 0);
        Integer discontinued = Math.toIntExact(setmealMapper.selectCount(discontinuedWrapper));

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
