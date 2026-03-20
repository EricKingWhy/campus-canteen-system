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

    void delivery(Long id); // 派送

    void complete(Long id); // 完成

    OrderVO details(Long id); // 详情

    // --- 用户端方法 ---
    OrderSubmitVO submit(OrdersSubmitDTO orderSubmitDTO); // 用户下单

    OrderPaymentVO payment(OrderPaymentDTO orderPaymentDTO); // 订单支付

    void userCancelById(Long id); // 用户取消

    void repayment(String orderNumber); // 重新支付

    PageResult userPage(int page, int pageSize, Integer status); // 用户历史订单

    void reminder(Long id); // 催单

    void reOrder(Long id); // 再来一单

    Integer unPayOrderCount(); // 待支付数量

    void userComplete(Long id); // 用户完成取餐

    fun.cyhgraph.vo.WeeklyReportVO getWeeklyReport(java.time.LocalDate startDate, java.time.LocalDate endDate);
}
