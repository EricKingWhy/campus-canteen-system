import { http } from '@/utils/http'
import type { ProfileDetail } from '@/types/user'

// 用户画像接口响应类型
export interface UserProfileVO {
  id?: number
  nickname?: string
  avatar?: string
  phone?: string
  gender?: number        // 1男 2女
  age?: number
  height?: number        // cm
  weight?: number        // kg
  activityLevel?: number // 1久坐 2轻度 3中度 4重度
  healthGoal?: number    // 1减脂 2增肌 3维持
  bmi?: number
  bmiCategory?: string   // 偏瘦/正常/超重/肥胖
  bmr?: number
  tdee?: number
  suggestIntake?: number
  tasteTags?: string[]
  avoidTags?: string[]
  nutritionPref?: string
  mealBudget?: number
}

// 用户画像更新参数类型
export interface UserProfileDTO {
  nickname?: string
  avatar?: string
  phone?: string
  gender?: number
  age?: number
  height?: number
  weight?: number
  activityLevel?: number
  healthGoal?: number
  tasteTags?: string[]
  avoidTags?: string[]
  nutritionPref?: string
  mealBudget?: number
}

// 根据id查询用户信息
export const getUserInfoAPI = (id: number) => {
  return http<ProfileDetail>({
    url: `/user/user/${id}`,
    method: 'GET',
  })
}

// 更新用户信息
export const updateUserAPI = (params: any) => {
  return http({
    url: '/user/user',
    method: 'PUT',
    data: params,
  })
}

// ====================== 用户画像接口 ======================

/**
 * 获取当前登录用户画像
 * - 无需传 userId，后端从 JWT 获取
 */
export const getUserProfileAPI = () => {
  return http<UserProfileVO>({
    url: '/user/user/profile',
    method: 'GET',
  })
}

/**
 * 更新当前登录用户画像
 * - 支持部分更新：只传需要修改的字段
 */
export const updateUserProfileAPI = (data: UserProfileDTO) => {
  return http<UserProfileVO>({
    url: '/user/user/profile',
    method: 'PUT',
    data,
  })
}
