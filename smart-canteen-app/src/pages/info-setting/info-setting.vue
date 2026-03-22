<template>
  <view class="info-setting">
    <!-- Header -->
    <view class="header">
      <view class="back-btn" @click="handleBack">
        <text class="back-arrow">‹</text>
      </view>
      <text class="title">信息设置</text>
      <view v-if="currentStep === 2" class="save-btn" @click="handleSave">
        <text>保存</text>
      </view>
      <view v-else class="placeholder"></view>
    </view>

    <!-- Progress -->
    <view class="progress-section">
      <view class="progress-info">
        <text class="step-text">第 {{ currentStep }} 页</text>
        <text class="step-desc">{{ currentStep === 1 ? '基本信息' : '饮食偏好' }}</text>
      </view>
      <view class="progress-bar">
        <view class="progress-fill" :style="{ width: currentStep === 1 ? '50%' : '100%' }"></view>
      </view>
    </view>

    <!-- Step 1: 健康画像 -->
    <scroll-view v-if="currentStep === 1" class="content" scroll-y>
      <!-- 头像/昵称/手机号?-->
      <view class="profile-header-section">
        <view class="avatar-upload" @click="chooseAvatar">
          <image 
            class="avatar-preview" 
            :src="(!formData.avatar || formData.avatar.includes('photo-1599566150163-29194dcaad36') || formData.avatar.includes('images.unsplash.com') || (formData.avatar.startsWith('http') && !formData.avatar.includes('127.0.0.1') && !formData.avatar.includes('localhost'))) ? '/static/images/default_avatar.jpg' : formData.avatar" 
            mode="aspectFill"
          />
          <view class="avatar-overlay">
            <text>点击更换</text>
          </view>
        </view>
        <view class="profile-inputs">
          <view class="profile-input-item">
            <text class="input-label">昵称</text>
            <input 
              class="profile-input" 
              v-model="formData.nickname" 
              placeholder="请输入昵称"
              maxlength="20"
            />
          </view>
          <view class="profile-input-item">
            <text class="input-label">手机号</text>
            <input 
              class="profile-input" 
              v-model="formData.phone" 
              placeholder="请输入手机号"
              type="number"
              maxlength="11"
            />
          </view>
        </view>
      </view>

      <!-- 性别选择 -->
      <view class="section">
        <text class="section-title">基础数据</text>
        <view class="gender-selector">
          <view 
            class="gender-option" 
            :class="{ active: formData.gender === 1 }"
            @click="formData.gender = 1"
          >
            <text class="gender-icon">♂</text>
            <text>男</text>
          </view>
          <view 
            class="gender-option" 
            :class="{ active: formData.gender === 2 }"
            @click="formData.gender = 2"
          >
            <text class="gender-icon">♀</text>
            <text>女</text>
          </view>
        </view>
      </view>

      <!-- 年龄 -->
      <view class="card">
        <text class="card-label">年龄</text>
        <view class="stepper">
          <view class="stepper-btn" @click="formData.age = Math.max(10, (formData.age || 20) - 1)">
            <text></text>
          </view>
          <text class="stepper-value">{{ formData.age || 20 }}</text>
          <view class="stepper-btn" @click="formData.age = Math.min(80, (formData.age || 20) + 1)">
            <text>+</text>
          </view>
        </view>
      </view>

      <!-- 身高 -->
      <view class="card">
        <view class="card-header">
          <text class="card-label">身高</text>
          <view class="input-group">
            <input 
              type="number" 
              v-model="formData.height" 
              class="value-input"
              @input="onHeightInput"
            />
            <text class="unit">cm</text>
          </view>
        </view>
        <slider 
          :value="formData.height || 170" 
          :min="120" 
          :max="220" 
          activeColor="#34c759"
          @change="onHeightSliderChange"
        />
      </view>

      <!-- 体重 -->
      <view class="card">
        <view class="card-header">
          <text class="card-label">体重</text>
          <view class="input-group">
            <input 
              type="number" 
              v-model="formData.weight" 
              class="value-input"
              @input="onWeightInput"
            />
            <text class="unit">kg</text>
          </view>
        </view>
        <slider 
          :value="formData.weight || 65" 
          :min="30" 
          :max="200" 
          activeColor="#34c759"
          @change="onWeightSliderChange"
        />
      </view>

      <!-- 活动-->
      <view class="section">
        <text class="section-title">活动</text>
        <view class="activity-grid">
          <view 
            v-for="item in activityOptions" 
            :key="item.value"
            class="activity-card"
            :class="{ active: formData.activityLevel === item.value }"
            @click="formData.activityLevel = item.value"
          >
            <view class="activity-icon">{{ item.icon }}</view>
            <text class="activity-name">{{ item.name }}</text>
            <text class="activity-desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>

      <!-- 健康目标 -->
      <view class="section">
        <text class="section-title">健康目标</text>
        <view class="goal-grid">
          <view 
            v-for="item in goalOptions" 
            :key="item.value"
            class="goal-card"
            :class="{ active: formData.healthGoal === item.value }"
            @click="formData.healthGoal = item.value"
          >
            <image class="goal-icon" :src="goalIconMap[item.value]" mode="aspectFit" />
            <text class="goal-name">{{ item.name }}</text>
          </view>
        </view>
      </view>

      <!-- 健康画像预览看板 -->
      <view class="health-preview">
        <view class="preview-header">
          <text class="preview-title">健康画像预览</text>
          <view class="realtime-tag">实时计算</view>
        </view>
        
        <!-- BMI 进度-->
        <view class="bmi-section">
          <view class="bmi-header">
            <text class="bmi-label">BMI</text>
            <text class="bmi-value">
              {{ calculatedBMI || '--' }}
              <text class="bmi-category">({{ bmiCategory }})</text>
            </text>
          </view>
          <view class="bmi-bar">
            <view class="bmi-segment thin"></view>
            <view class="bmi-segment normal"></view>
            <view class="bmi-segment overweight"></view>
            <view class="bmi-segment obese"></view>
          </view>
          <view v-if="calculatedBMI" class="bmi-indicator" :style="{ left: bmiIndicatorPosition }"></view>
        </view>

        <!-- 三指-->
        <view class="metrics-grid">
          <view class="metric">
            <text class="metric-label">基础代谢</text>
            <text class="metric-value">{{ calculatedBMR || '--' }}</text>
            <text class="metric-unit">kcal</text>
          </view>
          <view class="metric">
            <text class="metric-label">总代</text>
            <text class="metric-value">{{ calculatedTDEE || '--' }}</text>
            <text class="metric-unit">kcal</text>
          </view>
          <view class="metric highlight">
            <text class="metric-label">建议摄入</text>
            <text class="metric-value">{{ suggestIntake || '--' }}</text>
            <text class="metric-unit">kcal</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Step 2: 饮食偏好 -->
    <scroll-view v-else class="content" scroll-y>
      <!-- 口味偏好 -->
      <view class="section">
        <text class="section-title">口味偏好</text>
        <text class="section-desc">您最喜欢什么口味？</text>
        <view class="tags-wrap">
          <view 
            v-for="tag in tasteOptions" 
            :key="tag"
            class="tag"
            :class="{ active: formData.tasteTags?.includes(tag) }"
            @click="toggleTag('tasteTags', tag)"
          >
            <text>{{ tag }}</text>
            <text v-if="formData.tasteTags?.includes(tag)" class="check"></text>
          </view>
        </view>
      </view>

      <!-- 忌口/过敏 -->
      <view class="section">
        <text class="section-title">忌口/过敏</text>
        <text class="section-desc">选择或添加您需要避免的食材</text>
        <view class="tags-wrap">
          <view 
            v-for="tag in avoidOptions" 
            :key="tag"
            class="tag"
            :class="{ active: formData.avoidTags?.includes(tag) }"
            @click="toggleTag('avoidTags', tag)"
          >
            <text>{{ tag }}</text>
            <text v-if="formData.avoidTags?.includes(tag)" class="close"></text>
          </view>
        </view>
        <view class="custom-input">
          <text class="add-icon">+</text>
          <input 
            v-model="customAvoid" 
            placeholder="添加自定义忌.."
            @confirm="addCustomAvoid"
          />
        </view>
      </view>

      <!-- 营养偏好 -->
      <view class="section">
        <text class="section-title">营养偏好</text>
        <text class="section-desc">选择您的饮食重点</text>
        <view class="tags-wrap">
          <view 
            v-for="item in nutritionOptions" 
            :key="item"
            class="tag"
            :class="{ active: formData.nutritionPref === item }"
            @click="formData.nutritionPref = item"
          >
            <text>{{ item }}</text>
            <text v-if="formData.nutritionPref === item" class="check"></text>
          </view>
        </view>
      </view>

      <!-- 每餐预算 -->
      <view class="section budget-section">
        <view class="budget-header">
          <view>
            <text class="section-title">每餐预算</text>
            <text class="section-desc">平均每餐</text>
          </view>
          <view class="budget-value">
            <text class="budget-num">{{ formData.mealBudget || 15 }}</text>
            <text class="budget-unit"></text>
          </view>
        </view>
        <slider 
          :value="formData.mealBudget || 15" 
          :min="5" 
          :max="100" 
          activeColor="#34c759"
          @change="onBudgetSliderChange"
        />
        <view class="slider-labels">
          <text>5</text>
          <text>100+</text>
        </view>
      </view>
    </scroll-view>

    <!-- Footer -->
    <view class="footer">
      <button v-if="currentStep === 1" class="primary-btn" @click="nextStep">
        下一页
        <text class="arrow"></text>
      </button>
      <template v-else>
        <button class="primary-btn" @click="handleSave" :loading="loading">
          保存完毕
        </button>
        <view class="skip-btn" @click="handleSkip">以后再说</view>
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserProfileStore } from '@/stores/modules/userProfile'
import type { UserProfileDTO } from '@/api/user'
import goalIconJianzhi from '@/assets/images/icons/jianzhi.png'
import goalIconZengji from '@/assets/images/icons/zengji.png'
import goalIconWeichi from '@/assets/images/icons/weichi.png'

const profileStore = useUserProfileStore()

// 当前步骤
const currentStep = ref(1)
const loading = ref(false)
const customAvoid = ref('')

const goalIconMap: Record<number, string> = {
  1: goalIconJianzhi,
  2: goalIconZengji,
  3: goalIconWeichi
}

// 表单数据
const formData = ref<UserProfileDTO>({
  nickname: '',
  phone: '',
  avatar: '',
  gender: 1,
  age: 22,
  height: 170,
  weight: 65,
  activityLevel: 2,
  healthGoal: 3,
  tasteTags: [],
  avoidTags: [],
  nutritionPref: '',
  mealBudget: 15
})

// 选项配置
const activityOptions = [
  { value: 1, name: '久坐', desc: '极少运动', icon: '🪑' },
  { value: 2, name: '轻度', desc: '每周1-3天', icon: '🚶' },
  { value: 3, name: '中度', desc: '每周3-5天', icon: '🏃' },
  { value: 4, name: '重度', desc: '每周6-7天', icon: '💪' }
]

const goalOptions = [
  { value: 1, name: '减脂', icon: '📉' },
  { value: 2, name: '增肌', icon: '💪' },
  { value: 3, name: '维持', icon: '⚖️' }
]

const tasteOptions = ['清淡', '麻辣', '咸鲜', '酸甜']
const avoidOptions = ['花生', '麸质', '乳制品', '海鲜', '蛋类']
const nutritionOptions = ['高蛋白', '低碳', '生酮', '均衡']

// ========== 计算属(实时预览) ==========
const activityMultiplier = computed(() => {
  const multipliers: Record<number, number> = { 1: 1.2, 2: 1.375, 3: 1.55, 4: 1.725 }
  return multipliers[formData.value.activityLevel || 1] || 1.2
})

const calculatedBMI = computed(() => {
  const { weight, height } = formData.value
  if (!weight || !height || height <= 0) return null
  return Math.round(weight / Math.pow(height / 100, 2) * 10) / 10
})

const bmiCategory = computed(() => {
  const bmi = calculatedBMI.value
  if (bmi === null) return '未知'
  if (bmi < 18.5) return '偏瘦'
  if (bmi < 24) return '正常'
  if (bmi < 28) return '超重'
  return '肥胖'
})

const bmiIndicatorPosition = computed(() => {
  const bmi = calculatedBMI.value
  if (bmi === null) return '0%'
  // BMI 15-35 映射0-100%
  const percent = Math.min(100, Math.max(0, ((bmi - 15) / 20) * 100))
  return `${percent}%`
})

const calculatedBMR = computed(() => {
  const { weight, height, age, gender } = formData.value
  if (!weight || !height || !age) return null
  let bmr = 10 * weight + 6.25 * height - 5 * age
  if (gender === 2) bmr -= 161
  else bmr += 5
  return Math.round(bmr)
})

const calculatedTDEE = computed(() => {
  const bmr = calculatedBMR.value
  if (bmr === null) return null
  return Math.round(bmr * activityMultiplier.value)
})

const suggestIntake = computed(() => {
  const tdee = calculatedTDEE.value
  if (tdee === null) return null
  const goal = formData.value.healthGoal
  let adjustment = 0
  if (goal === 1) adjustment = -500
  if (goal === 2) adjustment = 300
  return tdee + adjustment
})

// ========== 方法 ==========
const onHeightInput = (e: { detail: { value: string } }) => {
  formData.value.height = Number(e.detail.value) || 170
}

const onWeightInput = (e: { detail: { value: string } }) => {
  formData.value.weight = Number(e.detail.value) || 65
}

const onHeightSliderChange = (e: { detail: { value: number } }) => {
  formData.value.height = e.detail.value
}

const onWeightSliderChange = (e: { detail: { value: number } }) => {
  formData.value.weight = e.detail.value
}

const onBudgetSliderChange = (e: { detail: { value: number } }) => {
  formData.value.mealBudget = e.detail.value
}

const toggleTag = (field: 'tasteTags' | 'avoidTags', tag: string) => {
  const tags = formData.value[field] || []
  const index = tags.indexOf(tag)
  if (index > -1) {
    tags.splice(index, 1)
  } else {
    tags.push(tag)
  }
  formData.value[field] = [...tags]
}

const addCustomAvoid = () => {
  if (customAvoid.value.trim()) {
    const tags = formData.value.avoidTags || []
    if (!tags.includes(customAvoid.value.trim())) {
      tags.push(customAvoid.value.trim())
      formData.value.avoidTags = [...tags]
    }
    customAvoid.value = ''
  }
}

const nextStep = () => {
  currentStep.value = 2
}

const handleBack = () => {
  if (currentStep.value === 2) {
    currentStep.value = 1
  } else {
    uni.navigateBack()
  }
}

const handleSave = async () => {
  loading.value = true
  try {
    const result = await profileStore.saveProfile(formData.value)
    if (result.success) {
      uni.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 1000)
    } else {
      uni.showToast({ title: result.message || '保存失败', icon: 'none' })
    }
  } catch (error: any) {
    uni.showToast({ title: error.message || '保存失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const handleSkip = () => {
  uni.navigateBack()
}

// 选择头像
const chooseAvatar = () => {
  uni.chooseMedia({
    count: 1,
    mediaType: ['image'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFiles[0].tempFilePath
      // base64 存储 (小程序端)
      // @ts-ignore
      wx.getFileSystemManager().readFile({
        filePath: tempFilePath,
        encoding: 'base64',
        success: (fileRes: { data: string }) => {
          formData.value.avatar = 'data:image/png;base64,' + fileRes.data
        },
        fail: () => {
          // fallback: 直接使用临时路径
          formData.value.avatar = tempFilePath
        }
      })
    }
  })
}

// 页面加载时获取已保存的画
onMounted(async () => {
  await profileStore.fetchProfile()
  if (profileStore.isLoaded && profileStore.profile) {
    const p = profileStore.profile
    formData.value = {
      nickname: p.nickname || '',
      phone: p.phone || '',
      avatar: p.avatar || '',
      gender: p.gender || 1,
      age: p.age || 22,
      height: p.height || 170,
      weight: p.weight || 65,
      activityLevel: p.activityLevel || 2,
      healthGoal: p.healthGoal || 3,
      tasteTags: p.tasteTags || [],
      avoidTags: p.avoidTags || [],
      nutritionPref: p.nutritionPref || '',
      mealBudget: p.mealBudget || 15
    }
  }
})
</script>

<style lang="scss" scoped>
$primary: #ff8c42;
$bg: #fffaf5;
$card-bg: #ffffff;
$text-primary: #2d241f;
$text-secondary: #6e6159;
$text-muted: #a4978f;

// ===== Profile Header Section =====
.profile-header-section {
  display: flex;
  gap: 32rpx;
  padding: 32rpx;
  background: $card-bg;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 10rpx 28rpx rgba(45, 36, 31, 0.05);

  .avatar-upload {
    position: relative;
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    overflow: hidden;
    flex-shrink: 0;

    .avatar-preview {
      width: 100%;
      height: 100%;
    }

    .avatar-overlay {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      background: rgba(0,0,0,0.5);
      padding: 8rpx;
      text-align: center;
      
      text {
        color: #fff;
        font-size: 20rpx;
      }
    }
  }

  .profile-inputs {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 16rpx;

    .profile-input-item {
      display: flex;
      align-items: center;
      background: #f5f5f5;
      border-radius: 12rpx;
      padding: 0 20rpx;
      height: 72rpx;

      .input-label {
        font-size: 26rpx;
        color: $text-secondary;
        width: 100rpx;
        flex-shrink: 0;
      }

      .profile-input {
        flex: 1;
        font-size: 28rpx;
        color: $text-primary;
      }
    }
  }
}

.info-setting {
  min-height: 100vh;
  background: $bg;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: calc(env(safe-area-inset-top) + 12rpx) 32rpx 18rpx;
  background: rgba(255, 250, 245, 0.96);
  backdrop-filter: blur(20rpx);
  position: sticky;
  top: 0;
  z-index: 100;

  .back-btn {
    width: 76rpx;
    height: 76rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 38rpx;
    background: #ffffff;
    box-shadow: 0 6rpx 18rpx rgba(45, 36, 31, 0.08);

    .back-arrow {
      color: #5c4f47;
      font-size: 46rpx;
      line-height: 1;
      transform: translateX(-2rpx);
    }
  }

  .title {
    font-size: 36rpx;
    font-weight: 700;
    color: $text-primary;
  }

  .save-btn {
    color: $primary;
    font-size: 32rpx;
    font-weight: 600;
  }

  .placeholder {
    width: 80rpx;
  }
}

.progress-section {
  padding: 8rpx 32rpx 24rpx;

  .progress-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12rpx;

    .step-text {
      font-size: 28rpx;
      font-weight: 600;
      color: $text-primary;
    }

    .step-desc {
      font-size: 24rpx;
      color: $text-muted;
    }
  }

  .progress-bar {
    height: 16rpx;
    background: #f1e9df;
    border-radius: 8rpx;
    overflow: hidden;

    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #ffb17a 0%, $primary 100%);
      transition: width 0.3s;
    }
  }
}

.content {
  flex: 1;
  padding: 0 28rpx 200rpx;
}

.section {
  margin-bottom: 32rpx;

  .section-title {
    font-size: 32rpx;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 8rpx;
    display: block;
  }

  .section-desc {
    font-size: 26rpx;
    color: $text-muted;
    margin-bottom: 20rpx;
    display: block;
  }
}

.gender-selector {
  display: flex;
  background: #f0f0f0;
  border-radius: 16rpx;
  padding: 8rpx;

  .gender-option {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    padding: 24rpx;
    border-radius: 12rpx;
    color: $text-secondary;
    transition: all 0.2s;

    &.active {
      background: $card-bg;
      color: $text-primary;
      box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1);
    }

    .gender-icon {
      font-size: 36rpx;
    }
  }
}

.card {
  background: $card-bg;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.04);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
  }

  .card-label {
    font-size: 30rpx;
    font-weight: 500;
    color: $text-primary;
  }

  .input-group {
    display: flex;
    align-items: baseline;
    gap: 8rpx;

    .value-input {
      width: 120rpx;
      text-align: right;
      font-size: 36rpx;
      font-weight: 700;
      border-bottom: 2rpx solid #e0e0e0;
      padding: 0 8rpx;
    }

    .unit {
      font-size: 24rpx;
      color: $text-muted;
    }
  }
}

.stepper {
  display: flex;
  align-items: center;
  gap: 32rpx;

  .stepper-btn {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    border: 2rpx solid #e0e0e0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40rpx;
    color: $text-secondary;

    &:active {
      background: rgba($primary, 0.1);
      border-color: $primary;
      color: $primary;
    }
  }

  .stepper-value {
    font-size: 44rpx;
    font-weight: 700;
    min-width: 80rpx;
    text-align: center;
  }
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;

  .activity-card {
    background: $card-bg;
    border-radius: 24rpx;
    padding: 30rpx;
    border: 2rpx solid transparent;
    transition: all 0.2s;

    &.active {
      border-color: rgba(255, 140, 66, 0.55);
      background: rgba(255, 140, 66, 0.07);

      .activity-icon {
        background: $card-bg;
      }
    }

    .activity-icon {
      width: 80rpx;
      height: 80rpx;
      background: #f8f2ea;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36rpx;
      margin-bottom: 16rpx;
    }

    .activity-name {
      font-size: 28rpx;
      font-weight: 700;
      display: block;
      margin-bottom: 4rpx;
    }

    .activity-desc {
      font-size: 22rpx;
      color: $text-muted;
    }
  }
}

.goal-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;

  .goal-card {
    background: $card-bg;
    border-radius: 24rpx;
    padding: 24rpx;
    text-align: center;
    border: 2rpx solid transparent;
    height: 160rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    &.active {
      border-color: rgba(255, 140, 66, 0.55);
      background: rgba(255, 140, 66, 0.07);
    }

    .goal-icon {
      width: 48rpx;
      height: 48rpx;
      margin-bottom: 12rpx;
    }

    .goal-name {
      font-size: 26rpx;
      font-weight: 700;
    }
  }
}

.health-preview {
  background: #1a1a1a;
  border-radius: 32rpx;
  padding: 36rpx;
  margin-top: 20rpx;
  color: #fff;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -80rpx;
    right: -80rpx;
    width: 280rpx;
    height: 280rpx;
    background: rgba($primary, 0.2);
    border-radius: 50%;
    filter: blur(60rpx);
  }

  .preview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 28rpx;
    position: relative;

    .preview-title {
      font-size: 34rpx;
      font-weight: 700;
    }

    .realtime-tag {
      font-size: 20rpx;
      padding: 8rpx 16rpx;
      background: rgba(255,255,255,0.1);
      border: 1rpx solid rgba($primary, 0.3);
      border-radius: 8rpx;
      color: $primary;
    }
  }

  .bmi-section {
    margin-bottom: 28rpx;
    position: relative;

    .bmi-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 12rpx;
      font-size: 24rpx;

      .bmi-label {
        color: #888;
      }

      .bmi-value {
        font-weight: 700;
      }

      .bmi-category {
        color: $primary;
        margin-left: 8rpx;
      }
    }

    .bmi-bar {
      display: flex;
      height: 16rpx;
      border-radius: 8rpx;
      overflow: hidden;

      .bmi-segment {
        &.thin { flex: 20; background: #60a5fa; opacity: 0.5; }
        &.normal { flex: 40; background: $primary; }
        &.overweight { flex: 25; background: #fbbf24; opacity: 0.5; }
        &.obese { flex: 15; background: #f87171; opacity: 0.5; }
      }
    }

    .bmi-indicator {
      position: absolute;
      bottom: 0;
      width: 6rpx;
      height: 24rpx;
      background: #fff;
      border-radius: 3rpx;
      transform: translateX(-50%);
    }
  }

  .metrics-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    text-align: center;
    border-top: 1rpx solid rgba(255,255,255,0.1);
    padding-top: 20rpx;

    .metric {
      .metric-label {
        font-size: 20rpx;
        color: #888;
        display: block;
        margin-bottom: 8rpx;
        text-transform: uppercase;
      }

      .metric-value {
        font-size: 36rpx;
        font-weight: 700;
        display: block;
      }

      .metric-unit {
        font-size: 20rpx;
        color: #666;
      }

      &.highlight {
        .metric-label, .metric-value {
          color: $primary;
        }
      }
    }
  }
}

.tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .tag {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 16rpx 28rpx;
    background: #f0f0f0;
    border-radius: 999rpx;
    font-size: 28rpx;
    transition: all 0.2s;

    &.active {
      background: $primary;
      color: $text-primary;
      font-weight: 600;
    }

    .check, .close {
      font-size: 24rpx;
    }
  }
}

.custom-input {
  display: flex;
  align-items: center;
  background: #f8f2ea;
  border-radius: 24rpx;
  padding: 0 24rpx;
  margin-top: 20rpx;
  height: 88rpx;

  .add-icon {
    font-size: 36rpx;
    color: #999;
    margin-right: 16rpx;
  }

  input {
    flex: 1;
    font-size: 28rpx;
  }
}

.budget-section {
  .budget-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 32rpx;
  }

  .budget-value {
    display: flex;
    align-items: baseline;
    background: #f8f2ea;
    padding: 12rpx 24rpx;
    border-radius: 16rpx;

    .budget-num {
      font-size: 44rpx;
      font-weight: 700;
    }

    .budget-unit {
      font-size: 28rpx;
      color: $text-muted;
      margin-left: 8rpx;
    }
  }

  .slider-labels {
    display: flex;
    justify-content: space-between;
    font-size: 24rpx;
    color: $text-muted;
    margin-top: 12rpx;
  }
}

.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(20rpx);
  border-top: 1rpx solid #f3e9de;

  .primary-btn {
    width: 100%;
    height: 100rpx;
    background: $primary;
    border-radius: 24rpx;
    font-size: 34rpx;
    font-weight: 700;
    color: $text-primary;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    border: none;

    .arrow {
      font-size: 28rpx;
    }
  }

  .skip-btn {
    text-align: center;
    padding: 20rpx;
    color: $text-muted;
    font-size: 28rpx;
  }
}
</style>
