<template>
  <view class="page-container">
    <!-- Decorational Blobs -->
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>
    <view class="blob blob-3"></view>

    <!-- Main Content -->
    <view class="content-wrapper">
      
      <!-- 1. Header (Custom Navbar) -->
      <view class="custom-nav" :style="{ paddingTop: safeAreaTop + 'px' }">
        <view class="back-btn glass-panel" @click="goBack">
          <uni-icons type="back" size="24" color="#333"></uni-icons>
        </view>
        <text class="page-title">提交订单</text>
        <view class="placeholder"></view>
      </view>

      <!-- 2. Dining Toggle -->
      <view class="section-padding">
        <view class="dining-toggle glass-panel">
          <view class="toggle-item" :class="{ active: diningType === 1 }" @click="diningType = 1">
            <view class="active-bg" v-if="diningType === 1"></view>
            <text class="toggle-text">🍽️ 堂食</text>
          </view>
          <view class="toggle-item" :class="{ active: diningType === 2 }" @click="diningType = 2">
            <view class="active-bg" v-if="diningType === 2"></view>
            <text class="toggle-text">🥡 打包自取</text>
          </view>
        </view>
      </view>

      <!-- 3. Smart Pickup Card -->
      <view class="section-padding">
        <view class="glass-card smart-card">
          <view class="card-header">
            <view>
              <text class="sub-label">取餐地点</text>
              <text class="main-value">{{ pickupLocation }}</text>
            </view>
            <view class="edit-btn">
               <uni-icons type="location-filled" size="20" color="#00BA9D"></uni-icons>
            </view>
          </view>
          
          <view class="divider"></view>

          <!-- Time Selector (Native Picker) -->
          <picker mode="selector" :range="timeSlots" @change="onTimeChange">
            <view class="time-trigger">
              <view class="icon-circle">
                 <uni-icons type="calendar" size="20" color="#00BA9D"></uni-icons>
              </view>
              <view class="time-info">
                <text class="sub-label">预计取餐时间</text>
                <text class="main-value highlight">
                  {{ selectedTimeStr }} 
                  <text class="hint" v-if="selectedTimeStr === '立即取餐'"> (预计 {{ estimatedTimeStr }})</text>
                </text>
              </view>
              <uni-icons type="right" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>
      </view>

      <!-- 4. Order List -->
      <view class="section-padding">
        <view class="glass-card list-card">
          <view class="cart-item" v-for="(item, index) in cartList" :key="index">
            <image class="item-img" :src="item.image || item.pic" mode="aspectFill"></image>
            <view class="item-info">
              <view class="info-top">
                <text class="item-name">{{ item.name }}</text>
                <text class="item-price">¥{{ item.amount }}</text>
              </view>
              <text class="item-desc">{{ item.dishFlavor || '正常' }}</text>
              <view class="item-count">
                <text class="count-tag">x{{ item.number }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 5. Options -->
      <view class="section-padding">
        <view class="glass-card options-card">
          <view class="option-row">
             <uni-icons type="compose" size="24" color="#94a3b8"></uni-icons>
             <input class="input-field" type="text" v-model="remark" placeholder="添加备注 (如: 不要香菜)" />
             <text class="field-label">备注</text>
          </view>
          <view class="divider"></view>
          <view class="option-row">
            <view class="left-group">
               <uni-icons type="staff-filled" size="24" color="#94a3b8"></uni-icons>
               <text class="row-label" style="margin-left: 10rpx;">餐具份数</text>
            </view>
            <view class="stepper">
              <view class="step-btn" @click="updateTableware(-1)">-</view>
              <text class="step-val">{{ tablewareNumber }}</text>
              <view class="step-btn active" @click="updateTableware(1)">+</view>
            </view>
          </view>
        </view>
      </view>

      <view style="height: 180rpx;"></view>
    </view>

    <!-- 6. Footer -->
    <view class="footer-wrapper">
      <view class="glass-bar">
        <view class="total-info">
           <text class="total-label">合计 Total</text>
           <view class="price-display">
             <text class="symbol">¥</text>
             <text class="amount">{{ totalPrice }}</text>
           </view>
        </view>
        <button class="pay-btn" @click="submitOrder">
          <text class="btn-text">立即支付</text>
          <uni-icons type="arrowright" size="18" color="#fff" style="margin-left: 8rpx;"></uni-icons>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'

const diningType = ref(1) 
const cartList = ref<any[]>([])
const remark = ref('')
const tablewareNumber = ref(1)
const selectedTimeStr = ref('立即取餐')
const timeSlots = ref<string[]>([])
const baseUrl = 'http://localhost:8081'
const safeAreaTop = ref(40)

const pickupLocation = computed(() => {
  return diningType.value === 1 ? '智能食堂一楼取餐口' : '智能食堂二楼取餐口(打包)'
})

const totalPrice = computed(() => {
  let sum = 0
  cartList.value.forEach(item => sum += (item.amount * item.number))
  return sum.toFixed(2)
})

const estimatedTimeStr = computed(() => {
   const now = new Date();
   now.setMinutes(now.getMinutes() + 15);
   const h = now.getHours().toString().padStart(2, '0');
   const m = now.getMinutes().toString().padStart(2, '0');
   return `${h}:${m}`;
})

onLoad(() => {
   const sysInfo = uni.getSystemInfoSync();
   if (sysInfo.safeArea) {
      safeAreaTop.value = sysInfo.safeArea.top + 10;
   }
})

onShow(() => {
  loadCartData();
  generateTimeSlots();
})

const goBack = () => uni.navigateBack()

const loadCartData = () => {
   uni.request({
      url: baseUrl + '/user/shoppingCart/list',
      method: 'GET',
      header: { 'authentication': uni.getStorageSync('token') },
      success: (res: any) => {
         if (res.data.code === 0 || res.data.code === 1) {
            cartList.value = res.data.data || []
         }
      }
   })
}

const generateTimeSlots = () => {
   const slots = ['立即取餐'];
   const now = new Date();
   let m = Math.ceil(now.getMinutes() / 10) * 10;
   now.setMinutes(m);
   for (let i = 0; i < 12; i++) {
       now.setMinutes(now.getMinutes() + 10);
       const h = now.getHours().toString().padStart(2, '0');
       const min = now.getMinutes().toString().padStart(2, '0');
       slots.push(`${h}:${min}`);
   }
   timeSlots.value = slots;
}

const onTimeChange = (e: any) => {
   const index = e.detail.value;
   selectedTimeStr.value = timeSlots.value[index];
}

const updateTableware = (delta: number) => {
   const newVal = tablewareNumber.value + delta
   if (newVal >= 1 && newVal <= 10) tablewareNumber.value = newVal
}

const submitOrder = () => {
   if (cartList.value.length === 0) return;
   uni.showLoading({ title: '提交中...' });
   
   const now = new Date();
   const y = now.getFullYear();
   const mo = (now.getMonth()+1).toString().padStart(2,'0');
   const d = now.getDate().toString().padStart(2,'0');
   let timePart = estimatedTimeStr.value + ":00"; 
   if (selectedTimeStr.value !== '立即取餐') timePart = selectedTimeStr.value + ":00";
   
   const deliveryTimeStr = `${y}-${mo}-${d}T${timePart}`;

   const payload = {
      addressBookId: null,
      payMethod: 1,
      remark: remark.value,
      amount: parseFloat(totalPrice.value),
      address: pickupLocation.value,
      estimatedDeliveryTime: deliveryTimeStr,
      packAmount: diningType.value === 2 ? 1 : 0,
      tablewareNumber: tablewareNumber.value,
      tablewareStatus: 1
   }

   uni.request({
      url: baseUrl + '/user/order/submit',
      method: 'POST',
      data: payload,
      header: { 
         'authentication': uni.getStorageSync('token'),
         'Content-Type': 'application/json' 
      },
      success: (res: any) => {
         uni.hideLoading()
         if (res.data.code === 0 || res.data.code === 1) {
            const orderData = res.data.data
            const orderId = orderData?.id || ''
            const orderNumber = orderData?.orderNumber || ''
            // 【修改】跳转到收银台页面，而不是直接成功页
            uni.redirectTo({ 
               url: `/pages/pay/pay?orderId=${orderId}&orderNumber=${orderNumber}&amount=${totalPrice.value}&diningType=${diningType.value}` 
            })
         } else {
            uni.showToast({ title: res.data.msg || '失败', icon: 'none' })
         }
      }
   })
}
</script>

<style lang="scss">
.page-container {
  min-height: 100vh;
  background: #f0fdfa; /* Fallback */
  background: radial-gradient(circle at 10% 20%, #d4fcfa 0%, transparent 40%),
              radial-gradient(circle at 90% 10%, #fff4e6 0%, transparent 40%),
              #f0fdfa;
  padding-bottom: 200rpx;
}
.blob { position: fixed; border-radius: 50%; filter: blur(80rpx); z-index: 1; opacity: 0.6; }
.blob-1 { top: -100rpx; left: -100rpx; width: 400rpx; height: 400rpx; background: rgba(0, 184, 156, 0.2); }
.blob-2 { top: 200rpx; right: -100rpx; width: 500rpx; height: 500rpx; background: rgba(255, 107, 107, 0.1); }
.content-wrapper { position: relative; z-index: 10; }
.section-padding { padding: 0 32rpx; margin-bottom: 24rpx; }
.glass-panel, .glass-card, .glass-bar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.05);
}
.glass-bar { background: rgba(20, 20, 20, 0.95); border: none; }
.custom-nav { display: flex; align-items: center; justify-content: space-between; padding: 20rpx 32rpx; }
.back-btn { width: 70rpx; height: 70rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.page-title { font-size: 34rpx; font-weight: bold; color: #0c1d1a; }
.placeholder { width: 70rpx; }
.dining-toggle { display: flex; height: 90rpx; border-radius: 45rpx; padding: 8rpx; position: relative; }
.toggle-item { flex: 1; display: flex; align-items: center; justify-content: center; border-radius: 40rpx; position: relative; z-index: 2; }
.active-bg { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: #00b89c; border-radius: 40rpx; z-index: -1; }
.toggle-text { font-size: 28rpx; font-weight: bold; color: #64748b; }
.toggle-item.active .toggle-text { color: #fff; }
.smart-card { border-radius: 32rpx; padding: 40rpx; display: flex; flex-direction: column; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.sub-label { font-size: 24rpx; color: #64748b; font-weight: bold; display: block; margin-bottom: 8rpx;}
.main-value { font-size: 34rpx; font-weight: bold; color: #0c1d1a; }
.edit-btn { padding: 10rpx; background: rgba(0,184,156,0.1); border-radius: 50%; }
.divider { height: 1rpx; background: #e2e8f0; margin: 30rpx 0; }
.time-trigger { display: flex; align-items: center; gap: 20rpx; }
.icon-circle { width: 70rpx; height: 70rpx; border-radius: 50%; background: rgba(0,184,156,0.1); display: flex; align-items: center; justify-content: center; }
.time-info { flex: 1; }
.highlight { color: #00b89c; }
.hint { font-size: 24rpx; color: #94a3b8; font-weight: normal; }
.list-card { border-radius: 32rpx; padding: 16rpx; }
.cart-item { display: flex; gap: 24rpx; padding: 24rpx; border-bottom: 1px solid #f1f5f9; }
.item-img { width: 120rpx; height: 120rpx; border-radius: 16rpx; background: #eee; }
.item-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.info-top { display: flex; justify-content: space-between; }
.item-name { font-size: 30rpx; font-weight: bold; }
.item-price { color: #FF6B6B; font-weight: bold; }
.item-desc { font-size: 24rpx; color: #999; }
.count-tag { font-size: 22rpx; color: #999; background: #f8fafc; padding: 4rpx 12rpx; border-radius: 8rpx; align-self: flex-start; }
.options-card { border-radius: 32rpx; padding: 32rpx; }
.option-row { display: flex; align-items: center; gap: 20rpx; height: 80rpx; }
.input-field { flex: 1; text-align: right; font-size: 28rpx; }
.field-label, .row-label { font-size: 28rpx; font-weight: bold; color: #333; }
.left-group { display: flex; align-items: center; flex: 1; }
.stepper { display: flex; background: #f1f5f9; border-radius: 30rpx; padding: 4rpx; }
.step-btn { width: 50rpx; height: 50rpx; display: flex; align-items: center; justify-content: center; border-radius: 50%; background: #fff; font-weight: bold; }
.step-btn.active { background: #00b89c; color: white; }
.step-val { width: 60rpx; text-align: center; line-height: 50rpx; font-weight: bold; }
.footer-wrapper { position: fixed; bottom: 40rpx; left: 32rpx; right: 32rpx; z-index: 100; }
.glass-bar { border-radius: 100rpx; padding: 16rpx 16rpx 16rpx 48rpx; display: flex; justify-content: space-between; align-items: center; }
.total-label { font-size: 20rpx; color: #ccc; display: block; }
.symbol { color: #FF6B6B; font-size: 28rpx; font-weight: bold; }
.amount { color: #FF6B6B; font-size: 40rpx; font-weight: bold; }
.pay-btn { background: #00b89c; border-radius: 100rpx; height: 90rpx; padding: 0 40rpx; display: flex; align-items: center; color: white; border: none; }
.btn-text { font-size: 30rpx; font-weight: bold; }
</style>
