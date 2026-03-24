<template>
  <view class="page-container">
    <!-- Animated Background Blobs -->
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
      <view class="status-header stagger-1">
        <view class="check-circle animate-check">
          <uni-icons type="checkmarkempty" size="52" color="#fff"></uni-icons>
        </view>
        <text class="status-title">支付成功</text>
        <text class="status-subtitle">厨房正在加速制作中...</text>
      </view>

      <!-- Ticket Card -->
      <view class="ticket-wrapper stagger-2">
        <view class="ticket-card glass-panel receipt-edge">
          <!-- Top Section -->
          <view class="ticket-top">
            <view class="floor-tag">
              <uni-icons type="shop-filled" size="16" color="#ea580c"></uni-icons>
              <text>{{ diningType === 1 ? '堂食 · 一楼' : '打包 · 二楼' }}</text>
            </view>
            <view class="pickup-label">取餐地点</view>
            <view class="pickup-number">
              <text class="prefix">{{ numberPrefix }}</text>
              <text class="num">{{ pickupNumber }}</text>
            </view>
            <view class="hint-box">
              <text>请留意<text class="highlight">{{ diningType === 1 ? '一楼' : '二楼' }}</text>叫号地点</text>
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
              <text class="label">预计取餐</text>
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
      <view class="action-buttons stagger-3">
        <button class="btn-primary" @click="viewDetail">
          <text>查看订单详情</text>
          <uni-icons type="arrowright" size="18" color="#fff"></uni-icons>
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
const baseUrl = 'http://121.41.59.61:8081'
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

const pad2 = (v: number) => String(v).padStart(2, '0')
const formatToHHmm = (value?: string) => {
  if (!value) return '--:--'
  const normalized = value.includes('T') ? value : value.replace(' ', 'T')
  const date = new Date(normalized)
  if (!Number.isNaN(date.getTime())) {
    return `${pad2(date.getHours())}:${pad2(date.getMinutes())}`
  }
  return value.length >= 16 ? value.substring(11, 16) : '--:--'
}

const fetchOrderDetail = () => {
  if (!orderId.value) return
  uni.request({
    url: `${baseUrl}/user/order/orderDetail/${orderId.value}`,
    method: 'GET',
    header: { authentication: uni.getStorageSync('token') },
    success: (res: any) => {
      const data = res?.data?.data
      if ((res?.data?.code === 1 || res?.data?.code === 0) && data) {
        if (data.amount !== undefined && data.amount !== null) {
          amount.value = Number(data.amount).toFixed(2)
        }
        if (data.packAmount !== undefined && data.packAmount !== null) {
          diningType.value = Number(data.packAmount) > 0 ? 2 : 1
        }
        estimatedTime.value = formatToHHmm(data.estimatedDeliveryTime)
        orderTime.value = formatToHHmm(data.orderTime)
      }
    },
  })
}

onLoad((options: any) => {
  const sysInfo = uni.getSystemInfoSync()
  if (sysInfo.safeArea) safeAreaTop.value = sysInfo.safeArea.top + 10

  if (options) {
    orderId.value = options.orderId || options.id || ''
    amount.value = options.amount || '0.00'
    diningType.value = parseInt(options.packAmount) > 0 ? 2 : 1
    
    if (options.estimatedTime) {
      estimatedTime.value = formatToHHmm(options.estimatedTime)
    }
    if (options.orderTime) {
      orderTime.value = formatToHHmm(options.orderTime)
    }
  }
  fetchOrderDetail()
})

const goBack = () => uni.navigateBack()
const goHome = () => uni.switchTab({ url: '/pages/index/index_v2' })
const viewDetail = () => {
  uni.redirectTo({ url: `/pages/orderDetail/orderDetail?id=${orderId.value}` })
}
</script>

<style lang="scss">
$primary: #00b89c;
$primary-gradient: linear-gradient(135deg, #00b89c 0%, #009688 100%);
$orange: #f97316;
$dark-text: #1f2937;
$light-text: #6b7280;

.page-container {
  min-height: 100vh;
  background: #f0fdfa;
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

/* Background Animations */
@keyframes float { 0%, 100% { transform: translate(0, 0) rotate(0deg); } 33% { transform: translate(30rpx, -50rpx) rotate(10deg); } 66% { transform: translate(-20rpx, 20rpx) rotate(-5deg); } }
.blob { position: fixed; border-radius: 50%; filter: blur(80rpx); z-index: 0; opacity: 0.6; animation: float 10s infinite ease-in-out; }
.blob-1 { top: -10%; left: -20%; width: 600rpx; height: 600rpx; background: rgba(5, 184, 156, 0.25); animation-delay: 0s; }
.blob-2 { top: 30%; right: -20%; width: 500rpx; height: 500rpx; background: rgba(251, 146, 60, 0.15); animation-delay: -2s; }
.blob-3 { bottom: -10%; left: 10%; width: 700rpx; height: 700rpx; background: rgba(45, 212, 191, 0.2); animation-delay: -5s; }

.content-wrapper { position: relative; z-index: 10; display: flex; flex-direction: column; min-height: 100vh; padding: 0 40rpx 60rpx; box-sizing: border-box; }

/* Navbar */
.custom-nav { display: flex; align-items: center; justify-content: space-between; padding-bottom: 20rpx; }
.back-btn { width: 80rpx; height: 80rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; transition: transform 0.2s; }
.back-btn:active { transform: scale(0.95); }
.glass-btn { background: rgba(255,255,255,0.6); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.8); box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05); }
.page-title { font-size: 36rpx; font-weight: 700; color: $dark-text; }
.placeholder { width: 80rpx; }

/* Status Header */
.status-header { display: flex; flex-direction: column; align-items: center; margin: 40rpx 0 60rpx; }
.check-circle { 
  width: 160rpx; height: 160rpx; border-radius: 50%; 
  background: $primary-gradient; 
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 20rpx 60rpx rgba(0, 184, 156, 0.35);
  margin-bottom: 24rpx;
  position: relative;
}
.check-circle::after { content: ''; position: absolute; inset: -10rpx; border-radius: 50%; border: 2rpx solid rgba(0, 184, 156, 0.3); opacity: 0.5; animation: pulse 2s infinite; }
@keyframes pulse { 0% { transform: scale(1); opacity: 0.5; } 100% { transform: scale(1.2); opacity: 0; } }

.status-title { font-size: 48rpx; font-weight: 800; color: $dark-text; letter-spacing: -1rpx; margin-bottom: 8rpx; }
.status-subtitle { font-size: 30rpx; color: $light-text; font-weight: 500; }

/* Entrance Animations */
@keyframes slideUpFade { from { opacity: 0; transform: translateY(40rpx); } to { opacity: 1; transform: translateY(0); } }
.stagger-1 { animation: slideUpFade 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) forwards; }
.stagger-2 { opacity: 0; animation: slideUpFade 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) 0.2s forwards; }
.stagger-3 { opacity: 0; animation: slideUpFade 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) 0.4s forwards; }
.animate-check uni-icons { animation: scaleCheck 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) 0.3s backwards; }
@keyframes scaleCheck { from { transform: scale(0); opacity: 0; } to { transform: scale(1); opacity: 1; } }

/* Ticket Card */
.ticket-wrapper { position: relative; margin-bottom: 40rpx; }
.ticket-card { 
  background: rgba(255,255,255,0.75);
  backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.9);
  border-radius: 40rpx 40rpx 0 0;
  box-shadow: 0 24rpx 64rpx rgba(0, 0, 0, 0.08);
  padding-bottom: 30rpx;
  position: relative; overflow: hidden;
}
.ticket-card::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 200rpx; background: linear-gradient(180deg, rgba(255,255,255,0.8), transparent); pointer-events: none; }

.receipt-edge {
  clip-path: polygon(
    0 0, 100% 0, 100% 100%, 
    97% 99%, 94% 100%, 91% 99%, 88% 100%, 85% 99%, 82% 100%, 79% 99%, 76% 100%, 73% 99%, 70% 100%, 67% 99%, 64% 100%, 61% 99%, 58% 100%, 55% 99%, 52% 100%, 49% 99%, 46% 100%, 43% 99%, 40% 100%, 37% 99%, 34% 100%, 31% 99%, 28% 100%, 25% 99%, 22% 100%, 19% 99%, 16% 100%, 13% 99%, 10% 100%, 7% 99%, 4% 100%, 1% 99%, 0 100%
  );
}
.ticket-shadow { position: absolute; bottom: -20rpx; left: 40rpx; right: 40rpx; height: 40rpx; background: #000; opacity: 0.15; filter: blur(20rpx); border-radius: 50%; z-index: -1; }

.ticket-top { display: flex; flex-direction: column; align-items: center; padding: 60rpx 32rpx 40rpx; position: relative; }
.floor-tag { 
  display: inline-flex; align-items: center; gap: 10rpx; 
  padding: 10rpx 24rpx; border-radius: 100rpx; 
  background: #fff7ed; color: $orange; 
  font-size: 24rpx; font-weight: 700;
  border: 1px solid rgba(249, 115, 22, 0.2);
  margin-bottom: 20rpx;
}
.pickup-label { font-size: 26rpx; color: $light-text; letter-spacing: 4rpx; text-transform: uppercase; margin-bottom: 4rpx; }
.pickup-number { display: flex; align-items: baseline; margin-bottom: 30rpx; }
.pickup-number .prefix { font-size: 60rpx; font-weight: 800; color: #cbd5e1; margin-right: 12rpx; transform: translateY(-8rpx); }
.pickup-number .num { 
  font-size: 110rpx; font-weight: 900; 
  letter-spacing: -6rpx; line-height: 1;
  background: linear-gradient(135deg, $primary 0%, #0d9488 100%);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 4rpx 10rpx rgba(0, 184, 156, 0.25));
}
.hint-box { background: rgba(255,255,255,0.6); padding: 14rpx 30rpx; border-radius: 20rpx; font-size: 26rpx; color: $dark-text; border: 1px solid rgba(0,0,0,0.03); }
.hint-box .highlight { font-weight: 800; color: $primary; }

.divider-row { position: relative; display: flex; align-items: center; margin: 10rpx 0; }
.divider-dot { position: absolute; width: 40rpx; height: 40rpx; background: #f0fdfa; border-radius: 50%; box-shadow: inset 0 2rpx 6rpx rgba(0,0,0,0.1); z-index: 5; }
.divider-dot.left { left: -20rpx; }
.divider-dot.right { right: -20rpx; }
.divider-line { flex: 1; border-bottom: 4rpx dashed #e2e8f0; margin: 0 50rpx; opacity: 0.6; }

.ticket-bottom { padding: 40rpx 50rpx; display: flex; flex-direction: column; gap: 24rpx; }
.info-row { display: flex; justify-content: space-between; align-items: center; font-size: 28rpx; }
.info-row .label { color: $light-text; }
.info-row .value { color: $dark-text; font-weight: 600; font-size: 30rpx; font-family: 'DIN Alternate', sans-serif; }
.info-row .value.price { font-size: 36rpx; color: $dark-text; }

.spacer { flex: 1; min-height: 40rpx; }

/* Buttons */
.action-buttons { display: flex; flex-direction: column; gap: 24rpx; padding-bottom: 20rpx; }
.btn-primary { 
  display: flex; align-items: center; justify-content: center; gap: 12rpx;
  width: 100%; height: 108rpx; border-radius: 36rpx; 
  background: $primary-gradient;
  color: white; font-size: 32rpx; font-weight: 700;
  border: none;
  box-shadow: 0 12rpx 36rpx rgba(0, 184, 156, 0.4);
  transition: transform 0.2s, box-shadow 0.2s;
}
.btn-primary:active { transform: scale(0.98); box-shadow: 0 6rpx 20rpx rgba(0, 184, 156, 0.3); }

.btn-secondary { 
  display: flex; align-items: center; justify-content: center;
  width: 100%; height: 108rpx; border-radius: 36rpx;
  background: rgba(255,255,255,0.6); border: 2rpx solid #e5e7eb;
  color: $light-text; font-size: 30rpx; font-weight: 600;
  backdrop-filter: blur(4px);
}
.btn-secondary:active { background: rgba(0,0,0,0.05); }
</style>
