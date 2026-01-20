<template>
  <view class="page-container">
    <!-- Background Blobs -->
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>
    <view class="blob blob-3"></view>

    <view class="content-wrapper">
      <!-- Navbar -->
      <view class="custom-nav" :style="{ paddingTop: safeAreaTop + 'px' }">
        <view class="back-btn glass-btn" @click="goBack">
          <uni-icons type="back" size="22" color="#333"></uni-icons>
        </view>
        <text class="page-title">订单状态</text>
        <view class="placeholder"></view>
      </view>

      <!-- Status Header -->
      <view class="status-header">
        <view class="check-circle animate-check">
          <uni-icons type="checkmarkempty" size="48" color="#fff"></uni-icons>
        </view>
        <text class="status-title">支付成功</text>
        <text class="status-subtitle">正在为您制作中...</text>
      </view>

      <!-- Ticket Card -->
      <view class="ticket-wrapper">
        <view class="ticket-card glass-panel receipt-edge">
          <!-- Top Section -->
          <view class="ticket-top">
            <view class="floor-tag">
              <uni-icons type="shop" size="14" color="#ea580c"></uni-icons>
              <text>{{ diningType === 1 ? '堂食 · 一楼' : '打包 · 二楼' }}</text>
            </view>
            <view class="pickup-number">
              <text class="prefix">{{ numberPrefix }}</text>
              <text class="num">{{ pickupNumber }}</text>
            </view>
            <view class="hint-box">
              <text>请留意 <text class="highlight">{{ diningType === 1 ? '一楼' : '二楼' }}</text> 叫号屏</text>
            </view>
          </view>

          <!-- Divider -->
          <view class="divider-row">
            <view class="divider-dot left"></view>
            <view class="divider-line"></view>
            <view class="divider-dot right"></view>
          </view>

          <!-- Bottom Section -->
          <view class="ticket-bottom">
            <view class="info-row">
              <text class="label">实付金额</text>
              <text class="value price">¥{{ amount }}</text>
            </view>
            <view class="info-row">
              <text class="label">预计取餐时间</text>
              <text class="value">{{ estimatedTime }}</text>
            </view>
            <view class="info-row">
              <text class="label">下单时间</text>
              <text class="value">{{ orderTime }}</text>
            </view>
          </view>
        </view>
        <view class="ticket-shadow"></view>
      </view>

      <view class="spacer"></view>

      <!-- Action Buttons -->
      <view class="action-buttons">
        <button class="btn-primary" @click="viewDetail">
          <text>查看订单详情</text>
          <uni-icons type="arrowright" size="16" color="#fff"></uni-icons>
        </button>
        <button class="btn-secondary" @click="goHome">返回首页</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const safeAreaTop = ref(44)
const orderId = ref<number | string>('')
const amount = ref('0.00')
const estimatedTime = ref('--:--')
const orderTime = ref('--:--')
const diningType = ref(1) // 1=堂食, 2=打包

const numberPrefix = computed(() => diningType.value === 1 ? 'A' : 'B')
const pickupNumber = computed(() => {
  const idStr = String(orderId.value)
  return idStr.slice(-5) || '00000'
})

onLoad((options: any) => {
  const sysInfo = uni.getSystemInfoSync()
  if (sysInfo.safeArea) safeAreaTop.value = sysInfo.safeArea.top + 10

  if (options) {
    orderId.value = options.orderId || options.id || ''
    amount.value = options.amount || '0.00'
    diningType.value = parseInt(options.packAmount) > 0 ? 2 : 1
    
    if (options.estimatedTime) {
      estimatedTime.value = options.estimatedTime.substring(11, 16)
    }
    if (options.orderTime) {
      orderTime.value = options.orderTime.substring(11, 16)
    }
  }
})

const goBack = () => uni.navigateBack()
const goHome = () => uni.switchTab({ url: '/pages/index/index_v2' })
const viewDetail = () => {
  uni.redirectTo({ url: `/pages/orderDetail/orderDetail?id=${orderId.value}` })
}
</script>

<style lang="scss">
$primary: #00b89c;
$primary-dark: #009680;
$orange: #ea580c;

.page-container {
  min-height: 100vh;
  background: #f0f9f6;
  position: relative;
  overflow: hidden;
}
.blob { position: fixed; border-radius: 50%; filter: blur(100rpx); z-index: 0; }
.blob-1 { top: -10%; left: -10%; width: 500rpx; height: 500rpx; background: rgba(0, 184, 156, 0.2); }
.blob-2 { top: 20%; right: -10%; width: 400rpx; height: 400rpx; background: rgba(255, 237, 213, 0.8); }
.blob-3 { bottom: -10%; left: 20%; width: 600rpx; height: 600rpx; background: rgba(204, 251, 241, 0.6); }

.content-wrapper { position: relative; z-index: 10; display: flex; flex-direction: column; min-height: 100vh; padding: 0 32rpx 60rpx; box-sizing: border-box; }

.custom-nav { display: flex; align-items: center; justify-content: space-between; padding-bottom: 20rpx; }
.back-btn { width: 70rpx; height: 70rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.glass-btn { background: rgba(255,255,255,0.5); backdrop-filter: blur(8px); }
.page-title { font-size: 34rpx; font-weight: bold; color: #1f2937; }
.placeholder { width: 70rpx; }

.status-header { display: flex; flex-direction: column; align-items: center; margin-bottom: 48rpx; gap: 16rpx; }
.check-circle { 
  width: 140rpx; height: 140rpx; border-radius: 50%; 
  background: $primary; 
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 20rpx 40rpx rgba(0, 184, 156, 0.3);
  margin-bottom: 16rpx;
}
@keyframes scale-in { 0% { transform: scale(0.5); opacity: 0; } 100% { transform: scale(1); opacity: 1; } }
.animate-check { animation: scale-in 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards; }
.status-title { font-size: 44rpx; font-weight: bold; color: #111827; }
.status-subtitle { font-size: 28rpx; color: #6b7280; }

.ticket-wrapper { position: relative; margin-bottom: 40rpx; }
.ticket-card { 
  background: linear-gradient(135deg, rgba(255,255,255,0.85), rgba(255,255,255,0.5));
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.8);
  border-radius: 32rpx 32rpx 0 0;
  padding-bottom: 20rpx;
}
.receipt-edge {
  clip-path: polygon(
    0 0, 100% 0, 100% 100%, 
    97% 98%, 94% 100%, 91% 98%, 88% 100%, 85% 98%, 82% 100%, 79% 98%, 76% 100%, 73% 98%, 70% 100%, 67% 98%, 64% 100%, 61% 98%, 58% 100%, 55% 98%, 52% 100%, 49% 98%, 46% 100%, 43% 98%, 40% 100%, 37% 98%, 34% 100%, 31% 98%, 28% 100%, 25% 98%, 22% 100%, 19% 98%, 16% 100%, 13% 98%, 10% 100%, 7% 98%, 4% 100%, 1% 98%, 0 100%
  );
}
.ticket-shadow { position: absolute; bottom: -16rpx; left: 32rpx; right: 32rpx; height: 32rpx; background: rgba(0,0,0,0.08); filter: blur(16rpx); border-radius: 50%; z-index: -1; }

.ticket-top { display: flex; flex-direction: column; align-items: center; padding: 48rpx 32rpx 36rpx; }
.floor-tag { 
  display: inline-flex; align-items: center; gap: 8rpx; 
  padding: 8rpx 20rpx; border-radius: 100rpx; 
  background: #fff7ed; color: $orange; 
  font-size: 22rpx; font-weight: bold;
  border: 1px solid rgba(234, 88, 12, 0.2);
  margin-bottom: 24rpx;
}
.pickup-number { display: flex; align-items: baseline; margin-bottom: 16rpx; }
.pickup-number .prefix { font-size: 56rpx; font-weight: 800; color: rgba(0, 184, 156, 0.7); margin-right: 8rpx; }
.pickup-number .num { font-size: 96rpx; font-weight: 800; color: $primary; letter-spacing: -4rpx; text-shadow: 0 4rpx 8rpx rgba(0,184,156,0.2); }
.hint-box { background: rgba(255,255,255,0.6); padding: 12rpx 24rpx; border-radius: 16rpx; font-size: 26rpx; color: #4b5563; }
.hint-box .highlight { font-weight: bold; color: $primary-dark; }

.divider-row { position: relative; display: flex; align-items: center; margin: 16rpx 0; }
.divider-dot { position: absolute; width: 36rpx; height: 36rpx; background: #f0f9f6; border-radius: 50%; box-shadow: inset 0 2rpx 8rpx rgba(0,0,0,0.08); }
.divider-dot.left { left: -18rpx; }
.divider-dot.right { right: -18rpx; }
.divider-line { flex: 1; border-bottom: 2rpx dashed #d1d5db; margin: 0 48rpx; }

.ticket-bottom { padding: 24rpx 48rpx; display: flex; flex-direction: column; gap: 20rpx; }
.info-row { display: flex; justify-content: space-between; align-items: center; font-size: 26rpx; }
.info-row .label { color: #6b7280; }
.info-row .value { color: #111827; font-weight: bold; font-size: 28rpx; }
.info-row .value.price { font-size: 32rpx; }

.spacer { flex: 1; min-height: 40rpx; }

.action-buttons { display: flex; flex-direction: column; gap: 24rpx; }
.btn-primary { 
  display: flex; align-items: center; justify-content: center; gap: 12rpx;
  width: 100%; height: 96rpx; border-radius: 100rpx; 
  background: linear-gradient(90deg, $primary, #26c6da);
  color: white; font-size: 30rpx; font-weight: bold;
  border: none;
}
.btn-secondary { 
  width: 100%; height: 96rpx; border-radius: 100rpx;
  background: transparent; border: 2rpx solid #d1d5db;
  color: #4b5563; font-size: 30rpx; font-weight: bold;
}
</style>
