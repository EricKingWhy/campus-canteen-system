package fun.cyhgraph.controller.user;

import fun.cyhgraph.dto.OrderPaymentDTO;
import fun.cyhgraph.dto.OrdersSubmitDTO;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.OrderService;
import fun.cyhgraph.vo.OrderPaymentVO;
import fun.cyhgraph.vo.OrderSubmitVO;
import fun.cyhgraph.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO orderSubmitDTO) {
        OrderSubmitVO orderSubmitVO = orderService.submit(orderSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @PutMapping("/payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrderPaymentDTO orderPaymentDTO) throws Exception {
        OrderPaymentVO orderPaymentVO = orderService.payment(orderPaymentDTO);
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/historyOrders")
    public Result<PageResult> page(int page, int pageSize, Integer status) {
        PageResult pageResult = orderService.userPage(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     * 【核心修复】参数类型从 Integer 改为 Long，支持大订单号
     */
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        log.info("查询订单详情，订单ID: {}", id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     * 用户取消订单
     * 【核心修复】参数类型从 Integer 改为 Long
     */
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable("id") Long id) throws Exception {
        log.info("用户取消订单，订单ID: {}", id);
        orderService.userCancelById(id);
        return Result.success();
    }

    /**
     * 再来一单
     */
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id) {
        orderService.reOrder(id);
        return Result.success();
    }

    /**
     * 催单
     */
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable("id") Long id) {
        orderService.reminder(id);
        return Result.success();
    }

    /**
     * 用户端点击完成取餐
     */
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id) {
        log.info("用户点击完成取餐，订单id：{}", id);
        orderService.userComplete(id);
        return Result.success();
    }

    @GetMapping("/weekly-report")
    public Result<fun.cyhgraph.vo.WeeklyReportVO> getWeeklyReport(
            @RequestParam("start_date") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate start_date,
            @RequestParam("end_date") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate end_date) {
        log.info("获取消费周报: {} - {}", start_date, end_date);
        fun.cyhgraph.vo.WeeklyReportVO vo = orderService.getWeeklyReport(start_date, end_date);
        return Result.success(vo);
    }
}
