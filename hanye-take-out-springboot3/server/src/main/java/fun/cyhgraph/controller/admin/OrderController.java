package fun.cyhgraph.controller.admin;

import fun.cyhgraph.dto.OrderCancelDTO;
import fun.cyhgraph.dto.OrderConfirmDTO;
import fun.cyhgraph.dto.OrderPageDTO;
import fun.cyhgraph.dto.OrderRejectionDTO;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.OrderService;
import fun.cyhgraph.vo.OrderStatisticsVO;
import fun.cyhgraph.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrderPageDTO orderPageDTO) {
        PageResult pageResult = orderService.conditionSearch(orderPageDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics() {
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查询订单详情
     * 【修复】Integer -> Long 支持大订单号
     */
    @GetMapping("/details/{id}")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        log.info("管理端查询订单详情，订单ID: {}", id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     */
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrderConfirmDTO orderConfirmDTO) {
        log.info("管理端接单，订单ID: {}", orderConfirmDTO.getId());
        orderService.confirm(orderConfirmDTO);
        return Result.success();
    }

    /**
     * 拒单
     */
    @PutMapping("/rejection")
    public Result reject(@RequestBody OrderRejectionDTO orderRejectionDTO) throws Exception {
        log.info("管理端拒单，订单ID: {}", orderRejectionDTO.getId());
        orderService.reject(orderRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrderCancelDTO orderCancelDTO) throws Exception {
        log.info("管理端取消订单，订单ID: {}", orderCancelDTO.getId());
        orderService.cancel(orderCancelDTO);
        return Result.success();
    }

    /**
     * 制作完成 (通知取餐)
     * 状态: 3(制作中) -> 4(待取餐)
     */
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable("id") Long id) {
        log.info("管理端制作完成，订单ID: {}", id);
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单 (用户已取餐)
     * 状态: 4(待取餐) -> 5(已完成)
     */
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable("id") Long id) {
        log.info("管理端完成订单，订单ID: {}", id);
        orderService.complete(id);
        return Result.success();
    }
}
