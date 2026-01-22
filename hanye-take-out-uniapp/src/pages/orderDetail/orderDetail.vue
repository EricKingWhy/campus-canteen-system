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
        <text class="page-title">订单详情</text>
        <view class="placeholder"></view>
      </view>

      <!-- Loading -->
      <view class="loading-state" v-if="loading">
        <uni-icons type="spinner-cycle" size="32" color="#00b89c"></uni-icons>
        <text style="margin-left: 16rpx;">加载中...</text>
      </view>

      <!-- Main Content (Only render when order has id) -->
      <view v-else-if="order.id">

        <!-- Status Header -->
        <view class="status-header" :class="statusClass">
          <view class="status-icon-wrap">
            <view class="status-icon">
              <uni-icons :type="statusIcon" size="36" color="#fff"></uni-icons>
            </view>
          </view>
          <view class="status-info">
            <text class="status-title">{{ statusText }}</text>
            <text class="status-subtitle">{{ statusSubtitle }}</text>
          </view>
        </view>

        <!-- Pickup Ticket Card (Only for active orders, status 1-4) -->
        <view class="section-padding" v-if="order.status && order.status <= 4">
          <view class="ticket-card glass-panel receipt-edge">
            <!-- Top Section -->
            <view class="ticket-top">
              <view class="floor-tag">
                <uni-icons type="shop" size="14" color="#ea580c"></uni-icons>
                <text>{{ order.packAmount > 0 ? '打包 · 二楼' : '堂食 · 一楼' }}</text>
              </view>
              <view class="pickup-number">
                <text class="prefix">{{ order.packAmount > 0 ? 'B' : 'A' }}</text>
                <text class="num">{{ pickupNumber }}</text>
              </view>
              <view class="hint-box">
                <text>请留意 <text class="highlight">{{ order.packAmount > 0 ? '二楼' : '一楼' }}</text> 叫号屏</text>
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
                <text class="label">预计取餐时间</text>
                <text class="value accent">{{ estimatedTimeStr }}</text>
              </view>
              <view class="info-row">
                <text class="label">取餐地点</text>
                <text class="value">{{ order.address || (order.packAmount > 0 ? '智能食堂二楼取餐口' : '智能食堂一楼取餐口') }}</text>
              </view>
            </view>
          </view>
          <view class="ticket-shadow"></view>
        </view>

        <!-- Reminder for active orders -->
        <view class="section-padding" v-if="order.status === 3 || order.status === 4">
          <view class="reminder-box">
            <uni-icons type="info" size="18" color="#f59e0b"></uni-icons>
            <text>请留意叫号屏，取餐后管理员将完成订单</text>
          </view>
        </view>

        <!-- Order Items -->
        <view class="section-padding">
          <view class="glass-card">
            <view class="section-title">
              <uni-icons type="cart" size="20" color="#333"></uni-icons>
              <text>订单菜品</text>
              <text class="item-count">共 {{ orderDetailList.length }} 件</text>
            </view>
            <view class="item-list" v-if="orderDetailList.length > 0">
              <view class="order-item" v-for="(item, index) in orderDetailList" :key="index">
                <image class="item-img" :src="item.pic || item.image || '/static/default_dish.png'" mode="aspectFill"></image>
                <view class="item-info">
                  <text class="item-name">{{ item.name }}</text>
                  <text class="item-flavor">{{ item.dishFlavor || '正常' }}</text>
                </view>
                <view class="item-right">
                  <text class="item-price">¥{{ item.amount }}</text>
                  <text class="item-count-tag">x{{ item.number }}</text>
                </view>
              </view>
            </view>
            <view class="empty-list" v-else>
              <text>暂无菜品信息</text>
            </view>
          </view>
        </view>

        <!-- Order Info -->
        <view class="section-padding">
          <view class="glass-card">
            <view class="section-title">
              <uni-icons type="info" size="20" color="#333"></uni-icons>
              <text>订单信息</text>
            </view>
            <view class="info-list">
              <view class="info-row">
                <text class="label">备注</text>
                <text class="value">{{ order.remark || '无备注' }}</text>
              </view>
              <view class="info-row">
                <text class="label">餐具份数</text>
                <text class="value">{{ order.tablewareNumber || 1 }} 份</text>
              </view>
              <view class="info-row">
                <text class="label">订单编号</text>
                <text class="value selectable">{{ order.number }}</text>
              </view>
              <view class="info-row">
                <text class="label">下单时间</text>
                <text class="value">{{ orderTimeStr }}</text>
              </view>
              <view class="info-row total-row">
                <text class="label">实付金额</text>
                <text class="value price">¥{{ order.amount || '0.00' }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- Cancel Reason (for cancelled orders) -->
        <view class="section-padding" v-if="order.status === 6 && order.cancelReason">
          <view class="cancel-reason-box">
            <uni-icons type="closeempty" size="18" color="#ef4444"></uni-icons>
            <text>取消原因：{{ order.cancelReason }}</text>
          </view>
        </view>

        <!-- Action Buttons -->
        <view class="section-padding action-section" v-if="canCancel">
          <button class="btn-cancel" @click="cancelOrder">取消订单</button>
        </view>

        <view class="section-padding action-section" v-if="canReorder">
          <button class="btn-reorder" @click="reOrder">再来一单</button>
        </view>

        <!-- 【核心新增】用户完成取餐按钮 -->
        <view class="section-padding action-section" v-if="order.status === 4">
          <button class="btn-complete" @click="completeOrder">我已成功取餐</button>
        </view>

        <view style="height: 80rpx;"></view>
      </view>

      <!-- Error State -->
      <view class="error-state" v-else>
        <uni-icons type="info" size="48" color="#999"></uni-icons>
        <text>订单数据加载失败</text>
        <button class="btn-retry" @click="fetchOrderDetail">重试</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onPullDownRefresh, onShow } from '@dcloudio/uni-app'

const baseUrl = 'http://localhost:8081'
const safeAreaTop = ref(44)
const loading = ref(true)
const orderId = ref('')

const order = ref<any>({})
const orderDetailList = ref<any[]>([])

// Status configurations
const statusConfig: any = {
  1: { text: '待付款', subtitle: '请尽快完成支付', icon: 'wallet', class: 'status-pending' },
  2: { text: '待接单', subtitle: '订单等待商家接单', icon: 'loop', class: 'status-active' },
  3: { text: '后厨制作中', subtitle: '🔥 后厨制作中，请耐心等待', icon: 'fire', class: 'status-active' },
  4: { text: '待取餐', subtitle: '🟢 餐品已备好，请前往窗口取餐', icon: 'flag', class: 'status-ready' },
  5: { text: '已完成', subtitle: '感谢您的光临，期待下次再见', icon: 'checkmarkempty', class: 'status-success' },
  6: { text: '已取消', subtitle: '订单已取消', icon: 'closeempty', class: 'status-cancelled' }
}

const statusText = computed(() => statusConfig[order.value.status]?.text || '未知状态')
const statusSubtitle = computed(() => statusConfig[order.value.status]?.subtitle || '')
const statusIcon = computed(() => statusConfig[order.value.status]?.icon || 'info')
const statusClass = computed(() => statusConfig[order.value.status]?.class || 'status-pending')

const pickupNumber = computed(() => {
  const num = order.value.number || order.value.id
  return String(num).slice(-5) || '00000'
})
const estimatedTimeStr = computed(() => {
  if (!order.value.estimatedDeliveryTime) return '--:--'
  return String(order.value.estimatedDeliveryTime).substring(11, 16)
})
const orderTimeStr = computed(() => {
  if (!order.value.orderTime) return '--'
  return String(order.value.orderTime).replace('T', ' ').substring(0, 16)
})
const canCancel = computed(() => order.value.status === 1 || order.value.status === 2)
const canReorder = computed(() => order.value.status >= 4)

onLoad((options: any) => {
  const sysInfo = uni.getSystemInfoSync()
  if (sysInfo.safeArea) safeAreaTop.value = sysInfo.safeArea.top + 10

  // 【核心修复】兼容两种参数名：id 和 orderId
  const paramId = options?.id || options?.orderId
  console.log('OrderDetail onLoad - options:', options)
  console.log('OrderDetail onLoad - paramId:', paramId)
  
  if (paramId) {
    orderId.value = String(paramId) // 确保是字符串
    fetchOrderDetail()
  } else {
    uni.showToast({ title: '订单ID缺失', icon: 'none' })
    loading.value = false
  }
})

onPullDownRefresh(() => {
  fetchOrderDetail()
})

// 【核心修复】用 onShow 实现实时状态同步
// 每次返回此页面时刷新，确保管理员取消的状态同步显示
onShow(() => {
  if (orderId.value) {
    console.log('OrderDetail onShow - 刷新订单状态')
    fetchOrderDetail()
  }
})

const fetchOrderDetail = () => {
  loading.value = true
  uni.request({
    url: `${baseUrl}/user/order/orderDetail/${orderId.value}`,
    method: 'GET',
    header: { 'authentication': uni.getStorageSync('token') },
    success: (res: any) => {
      console.log('Order Detail Response:', res.data)
      if (res.data.code === 1 || res.data.code === 0) {
        const data = res.data.data
        order.value = data || {}
        orderDetailList.value = data?.orderDetailList || []
        console.log('Order:', order.value)
        console.log('Detail List:', orderDetailList.value)
      } else {
        uni.showToast({ title: res.data.msg || '加载失败', icon: 'none' })
      }
    },
    fail: (err) => {
      console.error('Fetch order detail failed:', err)
      uni.showToast({ title: '网络错误', icon: 'none' })
    },
    complete: () => {
      loading.value = false
      uni.stopPullDownRefresh()
    }
  })
}

const cancelOrder = () => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消此订单吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '取消中...' })
        uni.request({
          url: `${baseUrl}/user/order/cancel/${orderId.value}`,
          method: 'PUT',
          header: { 'authentication': uni.getStorageSync('token') },
          success: (res: any) => {
            uni.hideLoading()
            if (res.data.code === 1 || res.data.code === 0) {
              uni.showToast({ title: '已取消', icon: 'success' })
              // 【关键联动】不返回上一页，而是刷新当前页状态
              fetchOrderDetail()
            } else {
              uni.showToast({ title: res.data.msg || '取消失败', icon: 'none' })
            }
          },
          fail: () => {
            uni.hideLoading()
            uni.showToast({ title: '网络错误', icon: 'none' })
          }
        })
      }
    }
  })
}

const reOrder = () => {
  uni.showLoading({ title: '加载中...' })
  uni.request({
    url: `${baseUrl}/user/order/repetition/${orderId.value}`,
    method: 'POST',
    header: { 'authentication': uni.getStorageSync('token') },
    success: (res: any) => {
      uni.hideLoading()
      if (res.data.code === 1 || res.data.code === 0) {
        uni.showToast({ title: '已加入购物车', icon: 'success' })
        setTimeout(() => uni.switchTab({ url: '/pages/index/index_v2' }), 1000)
      } else {
        uni.showToast({ title: res.data.msg || '操作失败', icon: 'none' })
      }
    }
  })
}

const completeOrder = () => {
  uni.showModal({
    title: '确认取餐',
    content: '您确认已经收到餐品了吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '处理中...' })
        uni.request({
          url: `${baseUrl}/user/order/complete/${orderId.value}`,
          method: 'PUT',
          header: { 'authentication': uni.getStorageSync('token') },
          success: (res: any) => {
            uni.hideLoading()
            if (res.data.code === 1 || res.data.code === 0) {
              uni.showToast({ title: '取餐成功', icon: 'success' })
              // 【关键链动】立即刷新订单状态
              fetchOrderDetail()
            } else {
              uni.showToast({ title: res.data.msg || '操作失败', icon: 'none' })
            }
          },
          fail: () => {
            uni.hideLoading()
            uni.showToast({ title: '网络错误', icon: 'none' })
          }
        })
      }
    }
  })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss">
$primary: #00b89c;
$primary-dark: #009680;
$orange: #ea580c;

.page-container { min-height: 100vh; background: #f0f9f6; position: relative; overflow: hidden; }
.blob { position: fixed; border-radius: 50%; filter: blur(100rpx); z-index: 0; }
.blob-1 { top: -10%; left: -10%; width: 400rpx; height: 400rpx; background: rgba(0, 184, 156, 0.15); }
.blob-2 { top: 20%; right: -15%; width: 500rpx; height: 500rpx; background: rgba(255, 237, 213, 0.5); }
.blob-3 { bottom: -10%; left: 30%; width: 600rpx; height: 600rpx; background: rgba(204, 251, 241, 0.4); }

.content-wrapper { position: relative; z-index: 10; min-height: 100vh; }
.section-padding { padding: 0 32rpx; margin-bottom: 24rpx; }

.custom-nav { display: flex; align-items: center; justify-content: space-between; padding: 20rpx 32rpx; }
.back-btn { width: 70rpx; height: 70rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.glass-btn { background: rgba(255,255,255,0.6); backdrop-filter: blur(8px); }
.page-title { font-size: 34rpx; font-weight: bold; color: #1f2937; }
.placeholder { width: 70rpx; }

.loading-state, .error-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 200rpx 0; color: #6b7280; gap: 24rpx; }
.btn-retry { margin-top: 24rpx; padding: 16rpx 48rpx; background: $primary; color: #fff; border-radius: 100rpx; font-size: 28rpx; }

/* Status Header */
.status-header { 
  display: flex; align-items: center; gap: 24rpx; 
  margin: 0 32rpx 32rpx; padding: 40rpx; border-radius: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.1);
}
.status-pending { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.status-active { background: linear-gradient(135deg, #00b89c, #26c6da); }
.status-ready { background: linear-gradient(135deg, #f97316, #fb923c); }
.status-success { background: linear-gradient(135deg, #10b981, #34d399); }
.status-cancelled { background: linear-gradient(135deg, #6b7280, #9ca3af); }

.status-icon-wrap { margin-right: 8rpx; }
.status-icon { width: 100rpx; height: 100rpx; border-radius: 50%; background: rgba(255,255,255,0.25); display: flex; align-items: center; justify-content: center; }
.status-info { flex: 1; }
.status-title { font-size: 40rpx; font-weight: bold; color: #fff; display: block; }
.status-subtitle { font-size: 26rpx; color: rgba(255,255,255,0.85); margin-top: 8rpx; display: block; }

/* Ticket Card */
.ticket-card { 
  background: linear-gradient(135deg, rgba(255,255,255,0.9), rgba(255,255,255,0.6));
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
.ticket-shadow { position: relative; top: -10rpx; margin: 0 32rpx; height: 20rpx; background: rgba(0,0,0,0.06); filter: blur(12rpx); border-radius: 50%; }

.ticket-top { display: flex; flex-direction: column; align-items: center; padding: 40rpx 32rpx 32rpx; }
.floor-tag { 
  display: inline-flex; align-items: center; gap: 8rpx; 
  padding: 10rpx 24rpx; border-radius: 100rpx; 
  background: #fff7ed; color: $orange; 
  font-size: 24rpx; font-weight: bold;
  border: 1px solid rgba(234, 88, 12, 0.2);
  margin-bottom: 20rpx;
}
.pickup-number { display: flex; align-items: baseline; margin-bottom: 16rpx; }
.pickup-number .prefix { font-size: 52rpx; font-weight: 800; color: rgba(0, 184, 156, 0.6); margin-right: 8rpx; }
.pickup-number .num { font-size: 88rpx; font-weight: 800; color: $primary; letter-spacing: -4rpx; text-shadow: 0 4rpx 8rpx rgba(0,184,156,0.15); }
.hint-box { background: rgba(255,255,255,0.7); padding: 12rpx 28rpx; border-radius: 16rpx; font-size: 26rpx; color: #4b5563; }
.hint-box .highlight { font-weight: bold; color: $primary-dark; }

.divider-row { position: relative; display: flex; align-items: center; margin: 16rpx 0; }
.divider-dot { position: absolute; width: 36rpx; height: 36rpx; background: #f0f9f6; border-radius: 50%; box-shadow: inset 0 2rpx 8rpx rgba(0,0,0,0.06); }
.divider-dot.left { left: -18rpx; }
.divider-dot.right { right: -18rpx; }
.divider-line { flex: 1; border-bottom: 2rpx dashed #d1d5db; margin: 0 48rpx; }

.ticket-bottom { padding: 20rpx 40rpx; display: flex; flex-direction: column; gap: 16rpx; }
.ticket-bottom .info-row { display: flex; justify-content: space-between; font-size: 26rpx; }
.ticket-bottom .label { color: #6b7280; }
.ticket-bottom .value { color: #1f2937; font-weight: 600; }
.ticket-bottom .value.accent { color: $primary; font-weight: bold; }

/* Reminder & Cancel Reason */
.reminder-box, .cancel-reason-box { 
  display: flex; align-items: center; gap: 12rpx; 
  padding: 24rpx 32rpx; border-radius: 16rpx; font-size: 26rpx;
}
.reminder-box { background: #fffbeb; color: #92400e; border: 1px solid #fde68a; }
.cancel-reason-box { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }

/* Glass Card */
.glass-card { 
  background: rgba(255,255,255,0.92); 
  backdrop-filter: blur(12px); 
  border-radius: 24rpx; 
  padding: 32rpx;
  border: 1px solid rgba(255,255,255,0.6);
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.04);
}

/* Section Title */
.section-title { 
  display: flex; align-items: center; gap: 12rpx; 
  font-size: 30rpx; font-weight: bold; color: #1f2937; 
  margin-bottom: 24rpx; padding-bottom: 20rpx; 
  border-bottom: 1px solid #f1f5f9; 
}
.section-title .item-count { margin-left: auto; font-size: 24rpx; color: #9ca3af; font-weight: normal; }

/* Order Items */
.item-list { display: flex; flex-direction: column; gap: 24rpx; }
.order-item { display: flex; align-items: center; gap: 20rpx; }
.item-img { width: 120rpx; height: 120rpx; border-radius: 20rpx; background: #f3f4f6; flex-shrink: 0; }
.item-info { flex: 1; min-width: 0; }
.item-name { font-size: 30rpx; font-weight: 600; color: #1f2937; display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-flavor { font-size: 24rpx; color: #9ca3af; margin-top: 8rpx; }
.item-right { text-align: right; flex-shrink: 0; }
.item-price { font-size: 30rpx; font-weight: bold; color: #ef4444; display: block; }
.item-count-tag { font-size: 24rpx; color: #9ca3af; margin-top: 4rpx; }

.empty-list { padding: 40rpx; text-align: center; color: #9ca3af; }

/* Info List */
.info-list { display: flex; flex-direction: column; gap: 24rpx; }
.info-row { display: flex; justify-content: space-between; font-size: 28rpx; }
.info-row .label { color: #6b7280; flex-shrink: 0; }
.info-row .value { color: #1f2937; font-weight: 500; text-align: right; word-break: break-all; }
.info-row .value.price { color: #ef4444; font-size: 36rpx; font-weight: bold; }
.info-row.total-row { padding-top: 24rpx; border-top: 1px dashed #e5e7eb; margin-top: 8rpx; }
.selectable { user-select: text; -webkit-user-select: text; }

/* Buttons */
.action-section { margin-top: 16rpx; }
.btn-cancel { 
  width: 100%; height: 96rpx; border-radius: 48rpx;
  background: #fff; border: 2rpx solid #ef4444; 
  color: #ef4444; font-size: 30rpx; font-weight: bold;
}
.btn-reorder { 
  width: 100%; height: 96rpx; border-radius: 48rpx;
  background: linear-gradient(90deg, $primary, #26c6da);
  color: #fff; font-size: 30rpx; font-weight: bold; border: none;
}
.btn-complete {
  width: 100%; height: 96rpx; border-radius: 48rpx;
  background: linear-gradient(90deg, #10b981, #34d399); 
  color: #fff; font-size: 30rpx; font-weight: bold; border: none;
  box-shadow: 0 4rpx 12rpx rgba(16, 185, 129, 0.4);
}
</style>
