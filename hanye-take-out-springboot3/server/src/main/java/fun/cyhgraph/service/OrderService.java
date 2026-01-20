package fun.cyhgraph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.cyhgraph.dto.*;
import fun.cyhgraph.entity.Orders;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.vo.OrderPaymentVO;
import fun.cyhgraph.vo.OrderStatisticsVO;
import fun.cyhgraph.vo.OrderSubmitVO;
import fun.cyhgraph.vo.OrderVO;

public interface OrderService extends IService<Orders> {

    // --- 管理端方法 ---
    PageResult conditionSearch(OrderPageDTO orderPageDTO); // 搜单

    OrderStatisticsVO statistics(); // 统计

    void confirm(OrderConfirmDTO orderConfirmDTO); // 接单

    void reject(OrderRejectionDTO orderRejectionDTO); // 拒单

    void cancel(OrderCancelDTO orderCancelDTO); // 取消

    void delivery(Integer id); // 派送

    void complete(Integer id); // 完成

    OrderVO details(Integer id); // 详情 (兼容旧版)

    OrderVO details(Long id); // 详情 (支持大ID)

    // --- 用户端方法 ---
    OrderSubmitVO submit(OrdersSubmitDTO orderSubmitDTO); // 用户下单

    OrderPaymentVO payment(OrderPaymentDTO orderPaymentDTO); // 订单支付

    void userCancelById(Integer id); // 用户取消 (兼容)

    void userCancelById(Long id); // 用户取消 (支持大ID)

    void repayment(String orderNumber); // 重新支付

    PageResult userPage(int page, int pageSize, Integer status); // 用户历史订单

    void reminder(Integer id); // 催单

    void reOrder(Integer id); // 再来一单

    Integer unPayOrderCount(); // 待支付数量
}
