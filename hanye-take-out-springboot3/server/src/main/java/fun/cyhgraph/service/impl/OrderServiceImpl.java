package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.cyhgraph.constant.MessageConstant;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.*;
import fun.cyhgraph.entity.*;
import fun.cyhgraph.exception.OrderBusinessException;
import fun.cyhgraph.exception.ShoppingCartBusinessException;
import fun.cyhgraph.mapper.OrderDetailMapper;
import fun.cyhgraph.mapper.OrderMapper;
import fun.cyhgraph.mapper.ShoppingCartMapper;
import fun.cyhgraph.mapper.UserMapper;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.service.OrderService;
import fun.cyhgraph.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private fun.cyhgraph.mapper.DishMapper dishMapper; // 【新增】用于销量更新
    @Autowired
    private fun.cyhgraph.websocket.WebSocketServer webSocketServer; // 【审计修复】WebSocket推送

    /**
     * 用户下单
     */
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 1. 处理异常情况（购物车为空）
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectList(wrapper);

        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 2. 查询当前用户 (获取手机号和昵称)
        User user = userMapper.selectById(userId);

        // 3. 构造订单数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        // 【核心修复】这里不再强转类型，因为 Orders.userId 已经是 Long 了
        orders.setUserId(userId);

        // 【核心修复】优先使用 DTO 传入的地址信息，否则使用默认值
        if (ordersSubmitDTO.getAddress() != null) {
            orders.setAddress(ordersSubmitDTO.getAddress());
        } else {
            orders.setAddress("食堂堂食");
        }

        // 手机号和收货人优先使用 DTO (部分情况可能用户填写), 否则用 User 信息
        if (ordersSubmitDTO.getPhone() != null) {
            orders.setPhone(ordersSubmitDTO.getPhone());
        } else {
            orders.setPhone(user.getPhone());
        }

        if (ordersSubmitDTO.getConsignee() != null) {
            orders.setConsignee(ordersSubmitDTO.getConsignee());
        } else {
            orders.setConsignee(user.getName() == null ? "同学" : user.getName());
        }

        orders.setUserName(user.getName());

        // 4. 插入订单表
        this.save(orders);

        // 5. 插入订单明细表
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId()); // ID 匹配 (Long -> Long)
            orderDetailList.add(orderDetail);

            // 【核心新增】更新菜品销量 - 每下单一次销量+1
            if (cart.getDishId() != null && cart.getNumber() != null) {
                dishMapper.incrementSold(cart.getDishId(), cart.getNumber());
            }
        }

        // 批量插入
        for (OrderDetail detail : orderDetailList) {
            orderDetailMapper.insert(detail);
        }

        // 6. 清空购物车
        shoppingCartMapper.delete(wrapper);

        // 7. 【审计修复】WebSocket推送通知管理端
        try {
            java.util.Map<String, Object> wsMap = new java.util.HashMap<>();
            wsMap.put("type", 1); // 1表示来单提醒
            wsMap.put("orderId", orders.getId());
            wsMap.put("content", "订单号：" + orders.getNumber());
            String json = com.alibaba.fastjson.JSON.toJSONString(wsMap);
            webSocketServer.sendToAllClient(json);
            log.info("WebSocket推送成功: {}", json);
        } catch (Exception e) {
            log.warn("WebSocket推送失败: {}", e.getMessage());
        }

        // 8. 返回VO
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
    }

    /**
     * 订单支付
     * 【核心修复】实际更新订单状态为"待接单"
     */
    public OrderPaymentVO payment(OrderPaymentDTO orderPaymentDTO) {
        // 1. 根据订单号查询订单
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getNumber, orderPaymentDTO.getOrderNumber());
        Orders order = orderMapper.selectOne(queryWrapper);

        if (order == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 2. 更新订单状态为"待接单"，支付状态为"已支付"
        order.setStatus(Orders.TO_BE_CONFIRMED); // 2: 待接单
        order.setPayStatus(Orders.PAID); // 1: 已支付
        order.setCheckoutTime(LocalDateTime.now());
        order.setPayMethod(orderPaymentDTO.getPayMethod());

        orderMapper.updateById(order);

        log.info("订单支付成功，订单号：{}，状态更新为：待接单", orderPaymentDTO.getOrderNumber());

        // 3. 返回空VO（实际业务不需要签名信息）
        return new OrderPaymentVO();
    }

    /**
     * 历史订单查询
     */
    public PageResult pageQuery4User(int page, int pageSize, Integer status) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        if (status != null) {
            queryWrapper.eq(Orders::getStatus, status);
        }
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderMapper.selectPage(pageInfo, queryWrapper);

        List<OrderVO> list = new ArrayList<>();

        if (pageInfo.getRecords() != null && pageInfo.getRecords().size() > 0) {
            for (Orders orders : pageInfo.getRecords()) {
                Long orderId = orders.getId();
                List<OrderDetail> orderDetails = getOrderDetail(orderId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                // 【核心修复】计算订单总菜品份数
                int totalNum = 0;
                if (orderDetails != null) {
                    for (OrderDetail detail : orderDetails) {
                        totalNum += (detail.getNumber() != null ? detail.getNumber() : 1);
                    }
                }
                orderVO.setTotalNum(totalNum);

                list.add(orderVO);
            }
        }
        return new PageResult(pageInfo.getTotal(), list);
    }

    private List<OrderDetail> getOrderDetail(Long orderId) {
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orderId);
        return orderDetailMapper.selectList(queryWrapper);
    }

    // 兼容旧接口命名
    public PageResult userPage(int page, int pageSize, Integer status) {
        return pageQuery4User(page, pageSize, status);
    }

    /**
     * 查询订单详情
     * 【核心修复】增加空值检查防止NPE
     */
    @Override
    public OrderVO details(Long id) {
        Orders orders = orderMapper.selectById(id);
        // 【核心修复】防御空订单
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        List<OrderDetail> orderDetailList = getOrderDetail(orders.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        // 【核心修复】计算订单总菜品份数
        int totalNum = 0;
        if (orderDetailList != null) {
            for (OrderDetail detail : orderDetailList) {
                totalNum += (detail.getNumber() != null ? detail.getNumber() : 1);
            }
        }
        orderVO.setTotalNum(totalNum);

        return orderVO;
    }

    /**
     * 用户取消订单
     */
    @Override
    public void userCancelById(Long id) {
        Orders orders = orderMapper.selectById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (orders.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(orders);
    }

    /**
     * 再来一单
     */
    @Override
    public void reOrder(Long id) {
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> orderDetailList = getOrderDetail(id);
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        for (ShoppingCart cart : shoppingCartList) {
            shoppingCartMapper.insert(cart);
        }
    }

    /**
     * 条件搜索
     */
    public PageResult conditionSearch(OrderPageDTO orderPageDTO) {
        Page<Orders> page = new Page<>(orderPageDTO.getPage(), orderPageDTO.getPageSize());
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        if (orderPageDTO.getNumber() != null) {
            queryWrapper.eq(Orders::getNumber, orderPageDTO.getNumber());
        }
        if (orderPageDTO.getStatus() != null) {
            queryWrapper.eq(Orders::getStatus, orderPageDTO.getStatus());
        }
        // 【核心修复】增加时间范围查询
        if (orderPageDTO.getBeginTime() != null) {
            queryWrapper.ge(Orders::getOrderTime, orderPageDTO.getBeginTime());
        }
        if (orderPageDTO.getEndTime() != null) {
            queryWrapper.le(Orders::getOrderTime, orderPageDTO.getEndTime());
        }
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderMapper.selectPage(page, queryWrapper);
        return new PageResult(page.getTotal(), page.getRecords());
    }

    /**
     * 各个状态的订单数量统计
     */
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper
                .selectCount(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, Orders.TO_BE_CONFIRMED)).intValue();
        Integer confirmed = orderMapper
                .selectCount(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, Orders.CONFIRMED)).intValue();
        Integer deliveryInProgress = orderMapper
                .selectCount(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS))
                .intValue();

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 接单
     */
    // 先写一个通用更新状态方法
    private void updateStatus(Long id, Integer status) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(status);
        orderMapper.updateById(orders);
    }

    // --- 接口方法实现 ---
    @Override
    public void confirm(OrderConfirmDTO dto) {
        updateStatus(Long.valueOf(dto.getId()), Orders.CONFIRMED);
    }

    @Override
    public void reject(OrderRejectionDTO orderRejectionDTO) {
        Orders orders = orderMapper.selectById(orderRejectionDTO.getId());
        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders updateOrder = new Orders();
        updateOrder.setId(orders.getId());
        updateOrder.setStatus(Orders.CANCELLED);
        updateOrder.setCancelReason("商家拒单: " + orderRejectionDTO.getRejectionReason());
        updateOrder.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(updateOrder);
    }

    @Override
    public void cancel(OrderCancelDTO orderCancelDTO) { // 商家取消
        Orders orders = orderMapper.selectById(orderCancelDTO.getId());
        Orders updateOrder = new Orders();
        updateOrder.setId(orders.getId());
        updateOrder.setStatus(Orders.CANCELLED);
        updateOrder.setCancelReason(orderCancelDTO.getCancelReason());
        updateOrder.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(updateOrder);
    }

    @Override
    public void delivery(Long id) {
        // 【核心修复】使用 Long ID 更新状态为 4 (派送中/待取餐)
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orders.setDeliveryTime(LocalDateTime.now()); // 【新增】记录出餐时间
        orderMapper.updateById(orders);
    }

    @Override
    public void complete(Long id) {
        updateStatus(id, Orders.COMPLETED);
        // 【核心新增】订单完成时累加销量
        incrementSales(id);
    }

    /**
     * 辅助方法：完成订单后，原子化自增菜品销量
     */
    private void incrementSales(Long orderId) {
        List<OrderDetail> details = getOrderDetail(orderId);
        if (details != null) {
            for (OrderDetail detail : details) {
                if (detail.getDishId() != null && detail.getNumber() != null) {
                    dishMapper.incrementSold(detail.getDishId(), detail.getNumber());
                }
            }
        }
    }

    @Override
    public void reminder(Long id) {
        // 催单逻辑
    }

    @Override
    public Integer unPayOrderCount() {
        // 待付款订单数量
        return orderMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, Orders.PENDING_PAYMENT)
                .eq(Orders::getUserId, BaseContext.getCurrentId()))
                .intValue();
    }

    // Interface doesn't have paySuccess
    public void paySuccess(String outTradeNo) {
        // ...
    }

    // 处理 payment 接口 VO 返回
    // public OrderPaymentVO payment(OrderPaymentDTO orderPaymentDTO) 已在上面

    // 处理 repayment
    public void repayment(String orderNumber) {
        // ...
    }

    /**
     * 用户完成取餐
     */
    public void userComplete(Long id) {
        // 1. 根据id查询订单
        Orders orders = orderMapper.selectById(id);

        // 2. 校验存在的订单
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 3. 校验状态 (必须是待取餐/派送中状态 4 才能点击完成)
        if (!orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 4. 更新状态为完成
        orders.setStatus(Orders.COMPLETED);
        orderMapper.updateById(orders);

        // 【核心新增】订单完成时累加销量
        incrementSales(id);
    }

}
