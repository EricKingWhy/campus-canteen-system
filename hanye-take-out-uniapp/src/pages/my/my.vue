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
       <!-- Budget Card -->
       <view class="card budget-card">
          <view class="card-header">
             <view class="icon-bg orange"><text class="emoji">💴</text></view>
             <text class="card-title">本月消费</text>
          </view>
          <view class="budget-main">
             <text class="currency">¥</text>
             <text class="amount">320</text>
             <text class="suffix">剩余</text>
          </view>
          <view class="progress-box">
             <view class="label-row">
                <text>进度</text>
                <text>70%</text>
             </view>
             <view class="progress-track">
                <view class="progress-bar"></view>
             </view>
          </view>
       </view>

       <!-- Analysis Card -->
       <view class="card analysis-card">
          <view class="card-header">
             <view class="icon-bg blue"><text class="emoji">📊</text></view>
             <text class="card-title">周饮食分析</text>
          </view>
          <view class="analysis-list">
             <view class="analysis-item">
                <text class="label">蛋白质</text>
                <view class="tag warning">偏低 ⚠️</view>
             </view>
             <view class="analysis-item">
                <text class="label">碳水</text>
                <view class="tag success">达标 ✅</view>
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
                <image class="icon-img" src="../../static/icon/history.png" mode="aspectFit"/>
             </view>
             <text class="func-name">历史订单</text>
          </view>

          <!-- Address -->
          <view class="func-item" @click="goAddress">
             <view class="func-icon-box blue-bg">
                <image class="icon-img" src="../../static/icon/address.png" mode="aspectFit"/>
             </view>
             <text class="func-name">我的地址</text>
          </view>

          <!-- Favorites (Mock) -->
          <view class="func-item" @click="goFavorites">
             <view class="func-icon-box pink-bg">
                 <!-- Using text emoji as placeholder or similar icon -->
                <text class="icon-text">❤</text> 
             </view>
             <text class="func-name">我的收藏</text>
          </view>

          <!-- Settings -->
          <view class="func-item" @click="goMyself">
             <view class="func-icon-box grey-bg">
                <image class="icon-img" src="../../static/icon/my.png" mode="aspectFit"/>
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
import {ref, reactive} from 'vue'
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

const user = reactive({
  id: userStore.profile?.id || 0,
  name: '',
  gender: 1,
  phone: '未设置',
  pic: '',
})

// 页面显示时刷新用户画像 (实时同步)
onShow(async () => {
  await profileStore.fetchProfile()
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
const goAddress = () => {
  uni.navigateTo({ url: '/pages/address/address' }) // Optimized to navigateTo
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
        // 1. 清除本地存储的 Token 和用户信息
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        // 2. 清空 store
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

/* 2. Dashboard Grid */
.dashboard-grid {
   display: flex;
   gap: 24rpx;
   margin-bottom: 30rpx;
   
   .card {
      flex: 1;
      background: white;
      border-radius: 32rpx;
      padding: 30rpx;
      box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.02);
      
      .card-header {
         display: flex;
         align-items: center;
         margin-bottom: 24rpx;
         
         .icon-bg {
            width: 48rpx; height: 48rpx; border-radius: 12rpx; 
            display: flex; align-items: center; justify-content: center; margin-right: 12rpx;
            &.orange { background: rgba(255,107,0,0.1); }
            &.blue { background: rgba(24,144,255,0.1); }
            .emoji { font-size: 24rpx; }
         }
         .card-title { font-size: 28rpx; font-weight: bold; color: $text-main; }
      }
   }
   
   .budget-card {
      .budget-main {
         margin-bottom: 20rpx;
         .currency { font-size: 24rpx; color: $text-main; vertical-align: bottom; }
         .amount { font-size: 48rpx; font-weight: 800; color: $text-main; font-family: 'DIN', sans-serif; line-height: 1; margin: 0 8rpx; }
         .suffix { font-size: 22rpx; color: #999; }
      }
      .progress-box {
         .label-row {
            display: flex; justify-content: space-between; font-size: 20rpx; color: #999; margin-bottom: 8rpx;
         }
         .progress-track {
            height: 12rpx; background: #F5F5F5; border-radius: 6rpx; overflow: hidden;
            .progress-bar { width: 70%; height: 100%; background: $primary; border-radius: 6rpx; }
         }
      }
   }
   
   .analysis-card {
      .analysis-list {
         display: flex;
         flex-direction: column;
         gap: 20rpx;
         
         .analysis-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            
            .label { font-size: 26rpx; color: #666; }
            .tag {
               font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 8rpx; font-weight: 500;
               &.warning { background: #FFF0E5; color: $primary; }
               &.success { background: #E6FFFB; color: #00B96B; }
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
            width: 100rpx;
            height: 100rpx;
            border-radius: 30rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 16rpx;
            
            .icon-img { width: 48rpx; height: 48rpx; }
            .icon-text { font-size: 40rpx; color: #FF4B4B; }
            
            &.orange-bg { background: rgba(255,107,0,0.08); }
            &.blue-bg { background: rgba(24,144,255,0.08); }
            &.pink-bg { background: rgba(255,75,75,0.08); }
            &.grey-bg { background: rgba(26,26,26,0.06); }
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
