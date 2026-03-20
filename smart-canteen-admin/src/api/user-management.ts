import request from '@/utils/request'

/**
 * 获取用户分页列表（含统计卡片）
 */
export const getUserListAPI = (params: {
    keyword?: string
    status?: number
    bmiRange?: string
    page: number
    pageSize: number
}) => {
    return request({
        url: '/admin/users',
        method: 'get',
        params
    })
}

/**
 * 获取用户详情（档案与画像）
 */
export const getUserProfileAPI = (id: number) => {
    return request({
        url: `/admin/users/${id}/profile`,
        method: 'get'
    })
}

/**
 * 获取用户点餐记录
 */
export const getUserOrdersAPI = (id: number, params: {
    timeRange?: number
    page?: number
    pageSize?: number
}) => {
    return request({
        url: `/admin/users/${id}/orders`,
        method: 'get',
        params
    })
}

/**
 * 获取用户餐费分析
 */
export const getUserSpendAnalyticsAPI = (id: number, range: number = 7) => {
    return request({
        url: `/admin/users/${id}/analytics/spend`,
        method: 'get',
        params: { range }
    })
}

/**
 * 获取用户健康营养分析
 */
export const getUserNutritionAnalyticsAPI = (id: number, range: number = 7) => {
    return request({
        url: `/admin/users/${id}/analytics/nutrition`,
        method: 'get',
        params: { range }
    })
}

/**
 * 更新用户状态（启用/禁用）
 */
export const updateUserStatusAPI = (status: number, id: number) => {
    return request({
        url: `/admin/users/status/${status}`,
        method: 'post',
        params: { id }
    })
}

/**
 * 导出用户数据 CSV
 */
export const exportUsersAPI = (params: {
    keyword?: string
    status?: number
    bmiRange?: string
}) => {
    return request({
        url: '/admin/users/export',
        method: 'get',
        params,
        responseType: 'blob'
    })
}

