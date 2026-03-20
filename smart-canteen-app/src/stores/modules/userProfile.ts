import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserProfileAPI, updateUserProfileAPI } from '@/api/user'
import type { UserProfileVO, UserProfileDTO } from '@/api/user'

/**
 * 用户画像 Store
 * - 全局缓存用户画像数据
 * - 供推荐模块直接读取 tdee/healthGoal/tags 等
 */
export const useUserProfileStore = defineStore('userProfile', () => {
    // ====================== 状态 ======================
    const profile = ref<UserProfileVO>({})
    const loading = ref(false)
    const isLoaded = ref(false)

    // ====================== Getters (前端实时计算) ======================

    /**
     * 活动系数映射
     */
    const activityMultiplier = computed(() => {
        const multipliers: Record<number, number> = {
            1: 1.2,    // 久坐
            2: 1.375,  // 轻度
            3: 1.55,   // 中度
            4: 1.725   // 重度
        }
        return multipliers[profile.value.activityLevel || 1] || 1.2
    })

    /**
     * 计算 BMI (前端实时预览用)
     */
    const calculatedBMI = computed(() => {
        const { weight, height } = profile.value
        if (!weight || !height || height <= 0) return null
        const bmi = weight / Math.pow(height / 100, 2)
        return Math.round(bmi * 10) / 10
    })

    /**
     * BMI 分类
     */
    const bmiCategory = computed(() => {
        const bmi = calculatedBMI.value
        if (bmi === null) return '未知'
        if (bmi < 18.5) return '偏瘦'
        if (bmi < 24) return '正常'
        if (bmi < 28) return '超重'
        return '肥胖'
    })

    /**
     * 计算 BMR (Mifflin-St Jeor 公式)
     */
    const calculatedBMR = computed(() => {
        const { weight, height, age, gender } = profile.value
        if (!weight || !height || !age) return null
        let bmr = 10 * weight + 6.25 * height - 5 * age
        if (gender === 2) {
            bmr -= 161 // 女性
        } else {
            bmr += 5   // 男性
        }
        return Math.round(bmr)
    })

    /**
     * 计算 TDEE
     */
    const calculatedTDEE = computed(() => {
        const bmr = calculatedBMR.value
        if (bmr === null) return null
        return Math.round(bmr * activityMultiplier.value)
    })

    /**
     * 建议摄入 (根据健康目标调整)
     */
    const suggestIntake = computed(() => {
        const tdee = calculatedTDEE.value
        if (tdee === null) return null
        const goal = profile.value.healthGoal
        let adjustment = 0
        if (goal === 1) adjustment = -500  // 减脂
        if (goal === 2) adjustment = 300   // 增肌
        // goal === 3 维持，不调整
        return tdee + adjustment
    })

    /**
     * 展示用昵称 (供我的页/首页使用)
     */
    const displayName = computed(() => {
        return profile.value.nickname || '未设置昵称'
    })

    /**
     * 展示用头像 (供我的页/首页使用)
     */
    const displayAvatar = computed(() => {
        return profile.value.avatar || 'https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=100&h=100&fit=crop'
    })

    /**
     * 身体数据展示 (如 175cm / 68kg)
     */
    const bodyStats = computed(() => {
        const h = profile.value.height
        const w = profile.value.weight
        if (!h && !w) return '未设置'
        return `${h || '--'}cm / ${w || '--'}kg`
    })

    // ====================== Actions ======================

    /**
     * 获取用户画像 (从后端)
     */
    async function fetchProfile() {
        loading.value = true
        try {
            const res = await getUserProfileAPI()
            if (res.data) {
                profile.value = res.data
                isLoaded.value = true
            }
        } catch (error) {
            console.error('获取用户画像失败:', error)
        } finally {
            loading.value = false
        }
    }

    /**
     * 保存用户画像 (到后端)
     */
    async function saveProfile(data: UserProfileDTO) {
        loading.value = true
        try {
            const res = await updateUserProfileAPI(data)
            if (res.data) {
                profile.value = res.data // 更新本地缓存
                return { success: true, data: res.data }
            }
            return { success: false, message: '保存失败' }
        } catch (error: any) {
            console.error('保存用户画像失败:', error)
            return { success: false, message: error.message || '保存失败' }
        } finally {
            loading.value = false
        }
    }

    /**
     * 更新本地画像 (用于实时预览)
     */
    function updateLocal(data: Partial<UserProfileVO>) {
        profile.value = { ...profile.value, ...data }
    }

    /**
     * 重置画像
     */
    function reset() {
        profile.value = {}
        isLoaded.value = false
    }

    return {
        // State
        profile,
        loading,
        isLoaded,
        // Getters
        calculatedBMI,
        bmiCategory,
        calculatedBMR,
        calculatedTDEE,
        suggestIntake,
        activityMultiplier,
        displayName,
        displayAvatar,
        bodyStats,
        // Actions
        fetchProfile,
        saveProfile,
        updateLocal,
        reset
    }
})
