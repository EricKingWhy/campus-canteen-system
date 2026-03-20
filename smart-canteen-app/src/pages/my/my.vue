<template>
  <view class="page-container">
    <!-- 1. User Header Card -->
    <view class="user-card">
      <view class="info-row">
        <!-- Avatar -->
        <image 
          class="avatar" 
          :src="profileStore.displayAvatar" 
          mode="aspectFill"
        />
        <!-- Text Info -->
        <view class="text-info">
           <view class="name-row">
             <text class="name">{{ profileStore.displayName }}</text>
             <image v-if="profileStore.profile.gender === 2" class="gender-icon" src="../../static/icon/girl.png" />
             <image v-else class="gender-icon" src="../../static/icon/boy.png" />
           </view>
           <text class="body-stats">{{ profileStore.bodyStats }}</text>
        </view>
        <!-- BMI Tag -->
        <view class="bmi-tag">
           <text>BMI {{ profileStore.calculatedBMI || '--' }} {{ profileStore.bmiCategory }}</text>
        </view>
      </view>
    </view>

    <!-- 2. Data Dashboard (2 Cols) -->
    <view class="dashboard-grid">
       <!-- 本月消费卡片 -->
       <view class="card spend-card">
          <view class="card-header">
             <view class="icon-bg orange"><text class="emoji">💳</text></view>
             <text class="card-title">本月消费</text>
          </view>
          <view class="spend-main">
             <text class="spend-symbol">¥</text>
             <text class="spend-amount">{{ monthlySpend }}</text>
          </view>
          <text class="spend-orders">共计 {{ monthlyOrders }} 单</text>
       </view>

       <!-- 今日饮食卡片 -->
       <view class="card diet-card">
          <view class="card-header">
             <view class="icon-bg orange"><text class="emoji">📊</text></view>
             <text class="card-title">今日饮食</text>
          </view>
          <view class="diet-list">
             <view class="diet-row">
                <text class="diet-label">热量: {{ todayCalories }} kcal</text>
                <view class="status-tag" :class="caloriesStatus.class">{{ caloriesStatus.text }}</view>
             </view>
             <view class="diet-row">
                <text class="diet-label">蛋白质: {{ todayProtein }}g</text>
                <view class="status-tag" :class="proteinStatus.class">{{ proteinStatus.text }}</view>
             </view>
          </view>
       </view>
    </view>

    <!-- 3. Common Functions (Grid) -->
    <view class="functions-section">
       <view class="section-title">常用功能</view>
       <view class="func-grid">
          <!-- History Order -->
          <view class="func-item" @click="goHistory">
             <view class="func-icon-box orange-bg">
                <image class="icon-img" src="../../static/icons/lishidingdan.png" mode="aspectFit"/>
             </view>
             <text class="func-name">历史订单</text>
          </view>

          <!-- Weekly Report -->
          <view class="func-item" @click="goWeeklyReport">
             <view class="func-icon-box blue-bg">
                <image class="icon-img" src="../../static/icons/xiaofeijilu.png" mode="aspectFit"/>
             </view>
             <text class="func-name">消费周报</text>
          </view>

          <!-- Favorites (Mock) -->
          <view class="func-item" @click="goFavorites">
             <view class="func-icon-box pink-bg">
                <image class="icon-img" src="../../static/icons/wodeshoucangyong.png" mode="aspectFit"/>
             </view>
             <text class="func-name">我的收藏</text>
          </view>

          <!-- Settings -->
          <view class="func-item" @click="goMyself">
             <view class="func-icon-box grey-bg">
                <image class="icon-img" src="../../static/icons/gerenxinxishezhi.png" mode="aspectFit"/>
             </view>
             <text class="func-name">信息设置</text>
          </view>
       </view>
    </view>
    
    <!-- 退出登录按钮 -->
    <view class="logout-section">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>

    <!-- Retained Hidden Components/Logic -->
    <pushMsg ref="childComp"></pushMsg>
  </view>
</template>

<script lang="ts" setup>
import pushMsg from '../../components/message/pushMsg.vue'
import {ref, reactive, computed} from 'vue'
import {onLoad, onReachBottom, onShow} from '@dcloudio/uni-app'
import {useUserStore} from '@/stores/modules/user'
import {useUserProfileStore} from '@/stores/modules/userProfile'
import {getUserInfoAPI} from '@/api/user'
import {getOrderPageAPI, reOrderAPI, urgeOrderAPI} from '@/api/order'
import {cleanCartAPI} from '@/api/cart'
import type {OrderPageDTO, OrderVO} from '@/types/order'

const userStore = useUserStore()
const profileStore = useUserProfileStore()
const childComp: any = ref(null)
const baseUrl = 'http://127.0.0.1:8081'

const user = reactive({
  id: userStore.profile?.id || 0,
  name: '',
  gender: 1,
  phone: '未设置',
  pic: '',
})

// ========== 数据看板响应式状态 ==========
const monthlySpend = ref('0.00')
const monthlyOrders = ref(0)
const todayCalories = ref(0)
const todayProtein = ref(0)

// 目标热量 (来自 profileStore 的 TDEE/suggestIntake)
const targetCalories = computed(() => profileStore.suggestIntake || 2000)

// 热量状态诊断
const caloriesStatus = computed(() => {
    if (todayCalories.value > targetCalories.value * 1.1) return { text: '超标 🔺', class: 'tag-over' }
    if (todayCalories.value < targetCalories.value * 0.8) return { text: '偏低 ⚠️', class: 'tag-low' }
    return { text: '达标 ✅', class: 'tag-ok' }
})

// 蛋白质状态诊断
const proteinStatus = computed(() => {
    if (todayProtein.value < 40) return { text: '偏低 ⚠️', class: 'tag-low' }
    return { text: '达标 ✅', class: 'tag-ok' }
})

// 获取消费摘要
const fetchCostSummary = async () => {
  const token = uni.getStorageSync('token')
  uni.request({
    url: baseUrl + '/analysis/cost/summary',
    method: 'GET',
    header: { authentication: token },
    success: (res: any) => {
      console.log("Cost API Response:", res.data);
      if (res.data && res.data.code === 0) {
        const data = res.data.data || {}
        const spend = data.monthSpent || data.totalAmount || data.amount || data.totalCost || data.cost || 0
        monthlySpend.value = spend.toFixed(2)
        monthlyOrders.value = data.totalOrders || data.ordersCount || data.orderCount || data.count || 0
      }
    },
    fail: (err: any) => {
      console.error('获取消费摘要失败:', err)
    }
  })
}

// 获取健康摘要
const fetchHealthSummary = async () => {
  const token = uni.getStorageSync('token')
  uni.request({
    url: baseUrl + '/analysis/health/summary',
    method: 'GET',
    header: { authentication: token },
    success: (res: any) => {
      console.log("Health API Response:", res.data);
      if (res.data && res.data.code === 0) {
        const data = res.data.data || {}
        todayCalories.value = data.todayIntakeKcal || data.todayCalories || data.calories || data.totalCalories || data.intake || 0
        const macros = data.macros || {}
        todayProtein.value = macros.proteinG || macros.todayProtein || macros.protein || macros.totalProtein || 0
      }
    },
    fail: (err: any) => {
      console.error('获取健康摘要失败:', err)
    }
  })
}

// 页面显示时刷新用户画像 + 看板数据
onShow(async () => {
  await profileStore.fetchProfile()
  fetchCostSummary()
  fetchHealthSummary()
})

// Original Logic Preserved
onLoad(async (options) => {
  if (user.id) {
     await getUserInfo(user.id)
  }
})

const getUserInfo = async (id: number) => {
  try {
    const res = await getUserInfoAPI(id)
    user.name = res.data.name as string
    user.gender = res.data.gender ?? 1
    user.phone = res.data.phone as string
    user.pic = res.data.pic as string
  } catch(e) {
    console.error(e)
  }
}

// Navigation Functions (Bound to New UI)
const goWeeklyReport = () => {
  uni.navigateTo({ url: '/pages/user/weekly-report' })
}

const goHistory = () => {
  uni.switchTab({ url: '/pages/history/history' }).catch(() => {
     // Fallback if history is not a tabbar page in some configs, though usually it is nice to check
     uni.navigateTo({ url: '/pages/history/history' })
  })
}

const goMyself = () => {
  uni.navigateTo({ url: '/pages/info-setting/info-setting' })
}

const goFavorites = () => {
   uni.navigateTo({ url: '/pages/favorite/favorite' })
}

// 退出登录
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出当前账号吗？',
    confirmColor: '#ff4d4f',
    success: function (res) {
      if (res.confirm) {
        // 1. 清除本地点点存储的 Token 和用户信息
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        // 2. 清 store
        userStore.clearProfile()
        // 3. 关闭所有页面，重启到登录页
        uni.reLaunch({ url: '/pages/login/login' })
      }
    }
  })
}

</script>

<style lang="scss" scoped>
/* Tokens */
$primary: #FF6B00;
$bg-page: #F7F8FA;
$text-main: #1A1A1A;

.page-container {
  min-height: 100vh;
  background-color: $bg-page;
  padding: 30rpx;
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica, sans-serif;
}

/* 1. Header Card */
.user-card {
  background: white;
  border-radius: 40rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.03);

  .info-row {
     display: flex;
     align-items: center;
     
     .avatar {
        width: 120rpx;
        height: 120rpx;
        border-radius: 50%;
        margin-right: 30rpx;
        border: 4rpx solid #F0F0F0;
     }
     
     .text-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: center;
        
        .name-row {
           display: flex;
           align-items: center;
           margin-bottom: 8rpx;
           
           .name { font-size: 36rpx; font-weight: 800; color: $text-main; margin-right: 12rpx; }
           .gender-icon { width: 32rpx; height: 32rpx; }
        }
        
        .body-stats {
           font-size: 26rpx;
           color: #999;
        }
     }
     
     .bmi-tag {
        background: rgba(0, 185, 107, 0.1);
        padding: 8rpx 20rpx;
        border-radius: 30rpx;
        text {
           font-size: 24rpx;
           color: #00B96B;
           font-weight: 600;
        }
     }
  }
}

/* 2. Dashboard Grid — aligned with Stitch iOS-style design */
.dashboard-grid {
   display: flex;
   justify-content: space-between;
   gap: 24rpx;
   margin-bottom: 30rpx;
   
   .card {
      flex: 1;
      background: white;
      border-radius: 40rpx;
      padding: 32rpx;
      min-height: 280rpx;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      box-shadow: 0 8rpx 40rpx -4rpx rgba(0,0,0,0.05);
      
      .card-header {
         display: flex;
         align-items: center;
         gap: 12rpx;
         
         .icon-bg {
            display: flex; align-items: center; justify-content: center;
            &.orange { color: #FF8A00; }
            .emoji { font-size: 32rpx; }
         }
         .card-title { font-size: 28rpx; font-weight: 500; color: #666; }
      }
   }
   
   /* 本月消费卡片 */
   .spend-card {
      .spend-main {
         display: flex;
         align-items: baseline;
         margin-top: 16rpx;
         .spend-symbol { font-size: 28rpx; color: $text-main; font-weight: 700; letter-spacing: -0.025em; }
         .spend-amount { font-size: 48rpx; font-weight: 700; color: $text-main; font-family: 'Inter', 'DIN', -apple-system, sans-serif; line-height: 1; margin-left: 8rpx; letter-spacing: -0.025em; }
      }
      .spend-orders {
         font-size: 24rpx;
         color: #999;
         margin-top: auto;
      }
   }
   
   /* 今日饮食卡片 */
   .diet-card {
      .diet-list {
         display: flex;
         flex-direction: column;
         gap: 20rpx;
         margin-top: 16rpx;
         
         .diet-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            
            .diet-label { font-size: 24rpx; color: #374151; }
            .status-tag {
               font-size: 20rpx; padding: 6rpx 16rpx; border-radius: 999rpx; font-weight: 700;
               /* 达标 */
               &.tag-ok { background: #E8F5E9; color: #2E7D32; }
               /* 偏低 */
               &.tag-low { background: #FEF3C7; color: #D97706; }
               /* 超标 */
               &.tag-over { background: #FEE2E2; color: #EF4444; }
            }
         }
      }
   }
}

/* 3. Common Functions */
.logout-section {
   margin-top: 40rpx;
   padding: 0 20rpx;
   
   .logout-btn {
      width: 100%;
      height: 90rpx;
      line-height: 90rpx;
      background-color: #fff;
      color: #ff4d4f;
      border: 2rpx solid #ff4d4f;
      border-radius: 50rpx;
      font-size: 32rpx;
      font-weight: 500;
   }
}

.functions-section {
   background: white;
   border-radius: 40rpx;
   padding: 40rpx;
   box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.03);
   
   .section-title {
      font-size: 32rpx;
      font-weight: 800;
      color: $text-main;
      margin-bottom: 40rpx;
   }
   
   .func-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 30rpx;
      
      .func-item {
         display: flex;
         flex-direction: column;
         align-items: center;
         
         .func-icon-box {
            width: 110rpx;
            height: 110rpx;
            border-radius: 36rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 20rpx;
            
            .icon-img { width: 56rpx; height: 56rpx; }
            
            &.orange-bg { background: linear-gradient(135deg, #FFF0E6 0%, #FFE0CC 100%); }
            &.blue-bg { background: linear-gradient(135deg, #E6F7FF 0%, #CCEEFF 100%); }
            &.pink-bg { background: linear-gradient(135deg, #FFF0F5 0%, #FFE0EB 100%); }
            &.grey-bg { background: linear-gradient(135deg, #F5F5F5 0%, #EBEBEB 100%); }
         }
         
         .func-name {
            font-size: 24rpx;
            color: $text-main;
            font-weight: 500;
         }
      }
   }
}
</style>