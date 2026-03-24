<template>
  <view class="page-container">
    <!-- Custom Navbar -->
    <view class="custom-nav" :style="{ paddingTop: safeAreaTop + 'px' }">
      <view class="back-btn" @click="goBack">
        <uni-icons type="back" size="22" color="#333"></uni-icons>
      </view>
      <text class="page-title">支付</text>
      <view class="placeholder"></view>
    </view>

    <!-- Amount Section -->
    <view class="amount-section">
      <text class="amount-label">支付金额</text>
      <text class="amount-value">¥{{ amount }}</text>
    </view>

    <!-- Payment Methods -->
    <view class="payment-methods">
      <!-- WeChat Pay -->
      <view 
        class="method-item" 
        :class="{ active: payMethod === 1 }"
        @click="payMethod = 1"
      >
        <view class="method-left">
          <image class="method-icon" src="/static/wechat_pay.png" mode="aspectFit"></image>
          <view class="method-info">
            <text class="method-name">微信支付</text>
            <text class="method-desc">推荐使用微信支付</text>
          </view>
        </view>
        <view class="method-check" v-if="payMethod === 1">
          <uni-icons type="checkbox-filled" size="24" color="#07c160"></uni-icons>
        </view>
        <view class="method-check-empty" v-else>
          <view class="empty-circle"></view>
        </view>
      </view>

      <!-- Cash/Offline -->
      <view 
        class="method-item" 
        :class="{ active: payMethod === 2 }"
        @click="payMethod = 2"
      >
        <view class="method-left">
          <image class="method-icon" src="/static/cash_pay.png" mode="aspectFit"></image>
          <view class="method-info">
            <text class="method-name">线下支付(到付)</text>
            <text class="method-desc">取餐时付款</text>
          </view>
        </view>
        <view class="method-check" v-if="payMethod === 2">
          <uni-icons type="checkbox-filled" size="24" color="#f59e0b"></uni-icons>
        </view>
        <view class="method-check-empty" v-else>
          <view class="empty-circle"></view>
        </view>
      </view>
    </view>

    <!-- Order Info Preview -->
    <view class="order-preview" v-if="orderNumber">
      <view class="preview-row">
        <text class="preview-label">订单编号</text>
        <text class="preview-value">{{ orderNumber }}</text>
      </view>
    </view>

    <!-- Bottom Button -->
    <view class="bottom-bar" :style="{ paddingBottom: safeAreaBottom + 'px' }">
      <button class="pay-btn" :class="{ 'offline': payMethod === 2 }" @click="confirmPay">
        <text>{{ payMethod === 1 ? '确认支付' : '确认下单' }}</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const baseUrl = 'http://121.41.59.61:8081'
const safeAreaTop = ref(44)
const safeAreaBottom = ref(34)

const orderId = ref('')
const orderNumber = ref('')
const amount = ref('0.00')
const diningType = ref(1) // 1=堂食, 2=打包
const payMethod = ref(1) // 1=微信, 2=线下

onLoad((options: any) => {
  const sysInfo = uni.getSystemInfoSync()
  if (sysInfo.safeArea) {
    safeAreaTop.value = sysInfo.safeArea.top + 10
    safeAreaBottom.value = sysInfo.screenHeight - sysInfo.safeArea.bottom + 10
  }

  console.log('Pay page options:', options)
  if (options) {
    orderId.value = options.orderId || ''
    orderNumber.value = options.orderNumber || ''
    amount.value = options.amount || '0.00'
    diningType.value = parseInt(options.diningType) || 1
  }

  if (orderId.value) {
    fetchLatestOrderInfo(orderId.value)
  }
})

const goBack = () => uni.navigateBack()

const confirmPay = () => {
  if (!orderNumber.value) {
    uni.showToast({ title: '订单信息缺失', icon: 'none' })
    return
  }

  uni.showLoading({ title: '支付中...' })

  // 调用支付接口
  uni.request({
    url: `${baseUrl}/user/order/payment`,
    method: 'PUT',
    header: { 
      'authentication': uni.getStorageSync('token'),
      'Content-Type': 'application/json'
    },
    data: {
      orderNumber: orderNumber.value,
      payMethod: payMethod.value
    },
    success: (res: any) => {
      uni.hideLoading()
      console.log('Payment response:', res.data)
      
      if (res.data.code === 1 || res.data.code === 0) {
        uni.showToast({ title: '支付成功', icon: 'success' })
        
        // 跳转到成功页
        setTimeout(() => {
          uni.redirectTo({
            url: `/pages/pay/success?orderId=${orderId.value}&amount=${amount.value}&packAmount=${diningType.value === 2 ? 1 : 0}&orderTime=${new Date().toISOString()}`
          })
        }, 1000)
      } else {
        uni.showToast({ title: res.data.msg || '支付失败', icon: 'none' })
      }
    },
    fail: (err) => {
      uni.hideLoading()
      console.error('Payment failed:', err)
      uni.showToast({ title: '网络错误', icon: 'none' })
    }
  })
}

const fetchLatestOrderInfo = (id: string) => {
  uni.request({
    url: `${baseUrl}/user/order/orderDetail/${id}`,
    method: 'GET',
    header: { 'authentication': uni.getStorageSync('token') },
    success: (res: any) => {
      const data = res?.data?.data
      if ((res?.data?.code === 1 || res?.data?.code === 0) && data) {
        if (data.number) {
          orderNumber.value = String(data.number)
        }
        if (data.amount !== undefined && data.amount !== null) {
          amount.value = Number(data.amount).toFixed(2)
        }
        if (data.packAmount !== undefined && data.packAmount !== null) {
          diningType.value = Number(data.packAmount) > 0 ? 2 : 1
        }
      }
    },
  })
}
</script>

<style lang="scss">
.page-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.custom-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  background: #fff;
  border-bottom: 1px solid #eee;
}
.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.page-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}
.placeholder {
  width: 60rpx;
}

.amount-section {
  background: #fff;
  padding: 60rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20rpx;
}
.amount-label {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 16rpx;
}
.amount-value {
  font-size: 72rpx;
  font-weight: bold;
  color: #333;
}

.payment-methods {
  background: #fff;
  padding: 0 32rpx;
}
.method-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 0;
  border-bottom: 1px solid #f0f0f0;
}
.method-item:last-child {
  border-bottom: none;
}
.method-item.active {
  background: rgba(7, 193, 96, 0.02);
}
.method-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
}
.method-icon {
  width: 60rpx;
  height: 60rpx;
}
.method-info {
  display: flex;
  flex-direction: column;
}
.method-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}
.method-desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}
.empty-circle {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #ddd;
  border-radius: 50%;
}

.order-preview {
  background: #fff;
  margin-top: 20rpx;
  padding: 24rpx 32rpx;
}
.preview-row {
  display: flex;
  justify-content: space-between;
  font-size: 26rpx;
}
.preview-label {
  color: #999;
}
.preview-value {
  color: #333;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  padding: 20rpx 32rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}
.pay-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(90deg, #07c160, #10b981);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}
.pay-btn.offline {
  background: linear-gradient(90deg, #f59e0b, #fbbf24);
}
.pay-btn text {
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
}
</style>
