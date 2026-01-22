package fun.cyhgraph.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orders implements Serializable {
    // 【核心修复】全部升级为 Long
    private Long id;
    private String number;
    private Integer status;
    private Long userId; // Fixed: Integer -> Long
    private Long addressBookId; // Fixed: Integer -> Long
    private LocalDateTime orderTime;
    private LocalDateTime checkoutTime;
    private Integer payMethod;
    private Integer payStatus;
    private BigDecimal amount;
    private String remark;
    private String userName;
    private String phone;
    private String address;
    private String consignee;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private Integer deliveryStatus;
    private LocalDateTime deliveryTime; // 【新增】实际完成取餐时间
    private LocalDateTime estimatedDeliveryTime;
    private Integer packAmount;
    private Integer tablewareNumber;
    private Integer tablewareStatus;

    /**
     * 订单状态 (校园食堂语义 - Canonical Definition)
     * 1=待付款 2=待接单 3=制作中 4=待取餐 5=已完成 6=已取消
     * 注：本系统为校园食堂自取模式，无配送/派送概念
     */
    public static final Integer PENDING_PAYMENT = 1; // 待付款
    public static final Integer TO_BE_CONFIRMED = 2; // 待接单 (已支付)
    public static final Integer CONFIRMED = 3; // 制作中 (原"已接单")
    public static final Integer DELIVERY_IN_PROGRESS = 4; // 待取餐 (原"派送中", 保留字段名兼容)
    public static final Integer COMPLETED = 5; // 已完成
    public static final Integer CANCELLED = 6; // 已取消

    // 语义别名 (推荐使用)
    public static final Integer PREPARING = CONFIRMED; // 制作中
    public static final Integer READY_FOR_PICKUP = DELIVERY_IN_PROGRESS; // 待取餐

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    public static final Integer UN_PAID = 0;
    public static final Integer PAID = 1;
    public static final Integer REFUND = 2;
}
