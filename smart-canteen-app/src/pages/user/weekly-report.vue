<template>
  <view class="page-container">
    <!-- TopAppBar -->
    <view class="top-bar">
      <view class="left" @click="goBack">
        <text class="back-icon">←</text>
        <text class="bar-title">Weekly Report</text>
      </view>
    </view>

    <scroll-view scroll-y class="main-scroll">
      <!-- Header Section -->
      <view class="section-header">
        <text class="page-title">消费周报 (Weekly Report)</text>
        <text class="page-subtitle">统计周期: {{ dateRange }} (本周)</text>
      </view>

      <!-- Summary Card -->
      <view class="summary-card">
        <view class="summary-label">本周总餐费</view>
        <view class="summary-amount-row">
          <text class="currency">¥</text>
          <text class="amount">{{ weeklyTotal }}</text>
          <view class="trend-badge" :class="trendUp ? 'up' : 'down'">
            <text>{{ trendUp ? '↑' : '↓' }} {{ trendPercent }}%</text>
          </view>
        </view>
        <view class="summary-grid">
          <view class="grid-item">
            <text class="grid-label">日均餐费</text>
            <text class="grid-value">¥{{ dailyAvg }}</text>
          </view>
          <view class="grid-item">
            <text class="grid-label">累计下单</text>
            <text class="grid-value">{{ totalOrders }}次</text>
          </view>
        </view>
        <text class="summary-note">较上周{{ trendUp ? '多' : '少' }}支出 ¥{{ trendDiff }}</text>
      </view>

      <!-- Weekly Spend Trend Card -->
      <view class="trend-card">
        <view class="trend-header">
          <text class="trend-title">七日餐费趋势</text>
          <text class="trend-icon">📅</text>
        </view>
        <view class="bar-chart">
          <view class="bar-col" v-for="(day, idx) in weekDays" :key="idx">
            <view class="bar-fill" :style="{ height: day.percent + '%' }" :class="{ highlight: day.isMax }">
              <text v-if="day.isMax" class="bar-tooltip">¥{{ day.amount }}</text>
            </view>
            <text class="bar-label" :class="{ 'label-highlight': day.isMax }">{{ day.label }}</text>
          </view>
        </view>
      </view>

      <!-- Meal Period Distribution -->
      <view class="dist-card">
        <text class="card-title">用餐段分布</text>
        <view class="dist-center-label">
          <text class="dist-small">核心时段</text>
          <text class="dist-main">午餐</text>
        </view>
        <view class="dist-list">
          <view class="dist-row" v-for="m in mealPeriods" :key="m.name">
            <view class="dist-dot" :style="{ background: m.color }"></view>
            <text class="dist-name">{{ m.name }}</text>
            <text class="dist-pct">{{ m.percent }}%</text>
          </view>
        </view>
      </view>

      <!-- Analysis Card -->
      <view class="analysis-card">
        <text class="card-title">本周餐点分析</text>
        <view class="analysis-item" v-for="item in analysisItems" :key="item.label">
          <view class="analysis-icon" :style="{ background: item.bgColor }">
            <text>{{ item.emoji }}</text>
          </view>
          <view class="analysis-text">
            <text class="analysis-label">{{ item.label }}</text>
            <text class="analysis-value">{{ item.value }}</text>
          </view>
        </view>
      </view>

      <!-- Intelligent Suggestion Card -->
      <view class="suggestion-card">
        <text class="suggestion-icon">💡</text>
        <text class="suggestion-text">提示: 本周午餐消费占比较高，建议合理控制日均开支，注意营养均衡。增加蔬菜摄入可让饮食更健康。</text>
      </view>

      <!-- Bottom spacing -->
      <view style="height: 60rpx;"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const baseUrl = 'http://127.0.0.1:8081'

// === Summary Data ===
const weeklyTotal = ref('138.50')
const dailyAvg = ref('19.79')
const totalOrders = ref(20)
const trendUp = ref(true)
const trendPercent = ref(8)
const trendDiff = ref('10.30')

// Date range
const dateRange = computed(() => {
   const now = new Date()
   const dayOfWeek = now.getDay() || 7
   const monday = new Date(now)
   monday.setDate(now.getDate() - dayOfWeek + 1)
   const sunday = new Date(monday)
   sunday.setDate(monday.getDate() + 6)
   const fmt = (d: Date) => `${d.getFullYear()}.${String(d.getMonth()+1).padStart(2,'0')}.${String(d.getDate()).padStart(2,'0')}`
   return `${fmt(monday)} - ${fmt(sunday)}`
})

// === Bar Chart Data ===
const weekDays = ref([
   { label: '一', percent: 40, amount: '15.50', isMax: false },
   { label: '二', percent: 55, amount: '21.00', isMax: false },
   { label: '三', percent: 90, amount: '28.50', isMax: true },
   { label: '四', percent: 45, amount: '17.00', isMax: false },
   { label: '五', percent: 65, amount: '24.00', isMax: false },
   { label: '六', percent: 30, amount: '18.50', isMax: false },
   { label: '日', percent: 25, amount: '14.00', isMax: false },
])

// === Meal Period Distribution ===
const mealPeriods = ref([
   { name: '午餐', percent: 45, color: '#f68a2f' },
   { name: '晚餐', percent: 35, color: '#ffdbcd' },
   { name: '早餐', percent: 20, color: '#77574d' },
])

// === Analysis Items ===
const analysisItems = ref([
   { label: '最高单笔消费', value: '¥18.00 (牛肉板面 - 餐点, 周三)', emoji: '📊', bgColor: '#ffdcc5' },
   { label: '下单最多菜品', value: '排骨瓦罐汤 (4次)', emoji: '❤️', bgColor: '#ffdbd0' },
   { label: '平均下单间隔', value: '4.2 小时', emoji: '⏱️', bgColor: '#ffdbcd' },
])

// === Fetch Real Data ===
const fetchWeeklyData = () => {
   const token = uni.getStorageSync('token')
   // Fetch cost trend
   uni.request({
      url: baseUrl + '/analysis/cost/summary',
      method: 'GET',
      header: { 'authentication': token },
      success: (res: any) => {
         console.log('Weekly cost summary:', res.data)
         if (res.data && res.data.code === 0 && res.data.data) {
            const data = res.data.data
            if (data.weekSpent) weeklyTotal.value = data.weekSpent.toFixed(2)
            if (data.totalOrders) totalOrders.value = data.totalOrders
         }
      },
      fail: (err) => {
         console.error('获取周报数据失败:', err)
      }
   })
   
   // Fetch health trend for 7 days
   uni.request({
      url: baseUrl + '/analysis/health/trend?range=7',
      method: 'GET',
      header: { 'authentication': token },
      success: (res: any) => {
         console.log('Weekly health trend:', res.data)
      },
      fail: (err) => {
         console.error('获取健康趋势失败:', err)
      }
   })
}

const goBack = () => {
   uni.navigateBack()
}

onLoad(() => {
   fetchWeeklyData()
})
</script>

<style lang="scss" scoped>
$primary: #944a00;
$primary-container: #f68a2f;
$surface: #fbf9f5;
$on-surface: #1b1c1a;
$on-surface-variant: #554337;
$outline: #887365;
$surface-container: #efeeea;
$tertiary: #77574d;

.page-container {
   min-height: 100vh;
   background: $surface;
}

.top-bar {
   position: sticky;
   top: 0;
   z-index: 50;
   background: $surface;
   display: flex;
   align-items: center;
   justify-content: space-between;
   padding: 24rpx 32rpx;
   padding-top: calc(var(--status-bar-height, 44px) + 12rpx);
   
   .left {
      display: flex;
      align-items: center;
      gap: 16rpx;
      .back-icon { font-size: 40rpx; color: $primary-container; }
      .bar-title { font-size: 36rpx; font-weight: 700; color: $on-surface; }
   }
}

.main-scroll {
   height: calc(100vh - 100rpx);
   padding: 0 24rpx;
}

.section-header {
   margin-bottom: 32rpx;
   .page-title { display: block; font-size: 48rpx; font-weight: 800; color: $on-surface; margin-bottom: 8rpx; }
   .page-subtitle { display: block; font-size: 24rpx; color: $on-surface-variant; font-weight: 500; }
}

.summary-card {
   background: white;
   border-radius: 48rpx;
   padding: 48rpx;
   margin-bottom: 32rpx;
   box-shadow: 0 8rpx 32rpx rgba(27,28,26,0.04);
   position: relative;
   overflow: hidden;
   
   .summary-label { font-size: 22rpx; font-weight: 600; color: $on-surface-variant; text-transform: uppercase; letter-spacing: 4rpx; margin-bottom: 12rpx; }
   .summary-amount-row {
      display: flex;
      align-items: baseline;
      gap: 8rpx;
      margin-bottom: 24rpx;
      .currency { font-size: 36rpx; font-weight: 800; color: $primary; }
      .amount { font-size: 80rpx; font-weight: 800; color: $primary; font-family: 'DIN', sans-serif; line-height: 1; }
      .trend-badge {
         font-size: 20rpx; font-weight: 700; padding: 4rpx 12rpx; border-radius: 999rpx;
         &.up { background: rgba($primary, 0.1); color: $primary; }
         &.down { background: rgba(0,185,107,0.1); color: #00B96B; }
      }
   }
   .summary-grid {
      display: flex;
      gap: 32rpx;
      padding-top: 24rpx;
      border-top: 1rpx solid rgba($on-surface-variant, 0.15);
      margin-bottom: 16rpx;
      .grid-item { flex: 1; }
      .grid-label { display: block; font-size: 22rpx; color: $on-surface-variant; font-weight: 600; }
      .grid-value { display: block; font-size: 32rpx; font-weight: 700; color: $tertiary; margin-top: 4rpx; }
   }
   .summary-note { font-size: 20rpx; color: $outline; font-style: italic; font-weight: 500; }
}

.trend-card {
   background: #f5f3ef;
   border-radius: 48rpx;
   padding: 48rpx;
   margin-bottom: 32rpx;
   
   .trend-header {
      display: flex; justify-content: space-between; align-items: center; margin-bottom: 32rpx;
      .trend-title { font-size: 36rpx; font-weight: 700; color: $on-surface; }
      .trend-icon { font-size: 36rpx; }
   }
   
   .bar-chart {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      height: 300rpx;
      gap: 12rpx;
      padding: 0 12rpx;
      
      .bar-col {
         flex: 1;
         display: flex;
         flex-direction: column;
         align-items: center;
         gap: 16rpx;
         height: 100%;
         justify-content: flex-end;
         
         .bar-fill {
            width: 100%;
            background: $surface-container;
            border-radius: 16rpx 16rpx 0 0;
            transition: all 0.3s;
            position: relative;
            
            &.highlight {
               background: linear-gradient(to top, $primary, $primary-container);
               box-shadow: 0 8rpx 20rpx rgba($primary, 0.2);
            }
            
            .bar-tooltip {
               position: absolute;
               top: -60rpx;
               left: 50%;
               transform: translateX(-50%);
               background: $on-surface;
               color: white;
               font-size: 18rpx;
               font-weight: 700;
               padding: 6rpx 12rpx;
               border-radius: 12rpx;
               white-space: nowrap;
            }
         }
         
         .bar-label { font-size: 22rpx; font-weight: 700; color: $outline; }
         .label-highlight { color: $primary; }
      }
   }
}

.dist-card, .analysis-card {
   background: white;
   border-radius: 48rpx;
   padding: 36rpx;
   margin-bottom: 32rpx;
   box-shadow: 0 4rpx 24rpx rgba(27,28,26,0.03);
   border: 1rpx solid $surface-container;
}

.card-title { display: block; font-size: 32rpx; font-weight: 700; color: $on-surface; margin-bottom: 24rpx; }

.dist-center-label {
   text-align: center;
   margin-bottom: 24rpx;
   .dist-small { display: block; font-size: 18rpx; font-weight: 700; color: $outline; text-transform: uppercase; letter-spacing: 2rpx; }
   .dist-main { display: block; font-size: 30rpx; font-weight: 700; color: $on-surface; }
}

.dist-list {
   .dist-row {
      display: flex; align-items: center; justify-content: space-between; padding: 8rpx 0;
      .dist-dot { width: 16rpx; height: 16rpx; border-radius: 50%; margin-right: 12rpx; }
      .dist-name { flex: 1; font-size: 24rpx; font-weight: 500; }
      .dist-pct { font-size: 24rpx; font-weight: 700; }
   }
}

.analysis-item {
   display: flex;
   gap: 20rpx;
   margin-bottom: 28rpx;
   &:last-child { margin-bottom: 0; }
   
   .analysis-icon {
      width: 72rpx; height: 72rpx; border-radius: 20rpx;
      display: flex; align-items: center; justify-content: center;
      text { font-size: 32rpx; }
   }
   .analysis-text {
      flex: 1;
      .analysis-label { display: block; font-size: 18rpx; font-weight: 700; color: $outline; text-transform: uppercase; margin-bottom: 4rpx; }
      .analysis-value { display: block; font-size: 24rpx; font-weight: 700; color: $on-surface; line-height: 1.4; }
   }
}

.suggestion-card {
   background: rgba($primary, 0.05);
   border-radius: 48rpx;
   padding: 32rpx;
   border-left: 8rpx solid $primary;
   display: flex;
   gap: 16rpx;
   margin-bottom: 32rpx;
   
   .suggestion-icon { font-size: 32rpx; }
   .suggestion-text { font-size: 24rpx; color: $on-surface-variant; font-weight: 500; line-height: 1.6; font-style: italic; }
}
</style>