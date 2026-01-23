import { http } from '@/utils/http'

/**
 * 添加收藏
 * @param dishId 菜品ID
 */
export const favoriteAddAPI = (dishId: number) => {
    return http({
        url: `/user/favorite/add?dishId=${dishId}`,
        method: 'POST'
    })
}

/**
 * 取消收藏
 * @param dishId 菜品ID
 */
export const favoriteRemoveAPI = (dishId: number) => {
    return http({
        url: `/user/favorite/remove?dishId=${dishId}`,
        method: 'POST'
    })
}

/**
 * 获取收藏列表
 */
export const favoriteListAPI = () => {
    return http<any[]>({
        url: `/user/favorite/list`,
        method: 'GET'
    })
}

/**
 * 检查是否已收藏
 * @param dishId 菜品ID
 */
export const favoriteCheckAPI = (dishId: number) => {
    return http<boolean>({
        url: `/user/favorite/check/${dishId}`,
        method: 'GET'
    })
}
