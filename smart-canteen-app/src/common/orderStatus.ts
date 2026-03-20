/**
 * 订单状态枚举 - 校园食堂语义 (Single Source of Truth)
 * 
 * 本系统为校园食堂自取模式，无配送/派送概念。
 * 所有页面（列表页/详情页）必须使用此统一映射。
 */

// 状态码定义
export const ORDER_STATUS = {
    PENDING_PAYMENT: 1,      // 待付款
    TO_BE_CONFIRMED: 2,      // 待接单 (已支付)
    PREPARING: 3,            // 制作中 (原"已接单")
    READY_FOR_PICKUP: 4,     // 待取餐 (原"派送中")
    COMPLETED: 5,            // 已完成
    CANCELLED: 6,            // 已取消
} as const

// 状态文案映射 (唯一真相)
export const ORDER_STATUS_TEXT: Record<number, string> = {
    [ORDER_STATUS.PENDING_PAYMENT]: '待付款',
    [ORDER_STATUS.TO_BE_CONFIRMED]: '待接单',
    [ORDER_STATUS.PREPARING]: '制作中',
    [ORDER_STATUS.READY_FOR_PICKUP]: '待取餐',
    [ORDER_STATUS.COMPLETED]: '已完成',
    [ORDER_STATUS.CANCELLED]: '已取消',
}

// 状态样式映射 (可选)
export const ORDER_STATUS_CLASS: Record<number, string> = {
    [ORDER_STATUS.PENDING_PAYMENT]: 'status-pending',
    [ORDER_STATUS.TO_BE_CONFIRMED]: 'status-waiting',
    [ORDER_STATUS.PREPARING]: 'status-preparing',
    [ORDER_STATUS.READY_FOR_PICKUP]: 'status-ready',
    [ORDER_STATUS.COMPLETED]: 'status-completed',
    [ORDER_STATUS.CANCELLED]: 'status-cancelled',
}

/**
 * 获取订单状态文案
 * @param status 状态码
 * @returns 中文文案
 */
export function getOrderStatusText(status: number): string {
    return ORDER_STATUS_TEXT[status] || '未知状态'
}

/**
 * 判断订单是否可取消
 * @param status 状态码
 * @returns 是否可取消
 */
export function canCancel(status: number): boolean {
    return status === ORDER_STATUS.PENDING_PAYMENT || status === ORDER_STATUS.TO_BE_CONFIRMED
}

/**
 * 判断订单是否可再来一单
 * @param status 状态码
 * @returns 是否可再来一单
 */
export function canReorder(status: number): boolean {
    return status >= ORDER_STATUS.READY_FOR_PICKUP
}

/**
 * 判断用户是否可确认取餐
 * @param status 状态码
 * @returns 是否可确认取餐
 */
export function canUserComplete(status: number): boolean {
    return status === ORDER_STATUS.READY_FOR_PICKUP
}
