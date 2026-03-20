import request from '@/utils/request' // 引入自定义的axios函数

// 订单管理
export const getOrderDataAPI = () => {
  return request({
    url: `/admin/workspace/overviewOrders`,
    method: 'get'
  })
}
// 菜品总览
export const getOverviewDishesAPI = () => {
  return request({
    url: `/admin/workspace/overviewDishes`,
    method: 'get'
  })
}

// 套餐总览
export const getSetMealStatisticsAPI = () => {
  return request({
    url: `/admin/workspace/overviewSetmeals`,
    method: 'get'
  })
}

// 营业数据
export const getBusinessDataAPI = () => {
  return request({
    url: `/admin/workspace/businessData`,
    method: 'get'
  })
}