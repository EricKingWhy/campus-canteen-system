import type { DishItem, HealthStats } from '@/types/dish'
import { http } from '@/utils/http'

/**
 * 根据菜品分类id获取菜品列表
 */
export const getDishListAPI = (id: number) => {
  return http<DishItem[]>({
    method: 'GET',
    url: `/user/dish/list/${id}`,
  })
}

/**
 * 分类列表-小程序
 */
export const getDishByIdAPI = (id: number) => {
  return http<DishItem>({
    method: 'GET',
    url: `/user/dish/dish/${id}`,
  })
}

/**
 * 健康看板
 */
export const getHealthStatsAPI = () => {
  return http<HealthStats>({
    method: 'GET',
    url: '/user/health/stats',
  })
}

/**
 * 智能推荐
 */
export const getRecommendDishAPI = () => {
  return http<DishItem[]>({
    method: 'GET',
    url: '/user/dish/recommend',
  })
}
