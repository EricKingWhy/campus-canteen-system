<template>
  <view class="page-container">
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
            <text>{{ trendArrow }} {{ trendPercent }}%</text>
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
        <text class="summary-note">较上周{{ summaryTrendText }}支出 ¥{{ trendDiff }}</text>
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
          <text class="dist-main">{{ coreMealPeriod }}</text>
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
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getWeeklyAnalysisAPI, getWeeklyReportAPI } from '@/api/order'
import type { WeeklyMealPeriodItem, WeeklyReportVO, WeeklyTrendItem } from '@/types/order'

type WeekDayBar = {
  label: string
  percent: number
  amount: string
  isMax: boolean
}

type MealPeriodItem = {
  name: string
  percent: number
  color: string
}

const dayLabels = ['一', '二', '三', '四', '五', '六', '日']

const mealColorMap: Record<string, string> = {
  早餐: '#77574d',
  午餐: '#f68a2f',
  晚餐: '#ffdbcd',
}

const getWeekRange = () => {
  const now = new Date()
  const dayOfWeek = now.getDay() || 7
  const monday = new Date(now)
  monday.setDate(now.getDate() - dayOfWeek + 1)
  monday.setHours(0, 0, 0, 0)
  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)
  sunday.setHours(23, 59, 59, 999)
  return { monday, sunday }
}

const formatDate = (date: Date, separator = '-') => {
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  return `${yyyy}${separator}${mm}${separator}${dd}`
}

const weekRange = ref(getWeekRange())

const dateRange = computed(() => {
  return `${formatDate(weekRange.value.monday, '.')} - ${formatDate(weekRange.value.sunday, '.')}`
})

const reportData = ref<WeeklyReportVO>({
  totalAmount: 0,
  totalOrders: 0,
  orderCount: 0,
  dailyAverage: 0,
  lastWeekAmount: 0,
  diffAmount: 0,
  dailyTrend: [],
  mealPeriodDistribution: [],
})

const weeklyTotal = computed(() => Number(reportData.value.totalAmount || 0).toFixed(2))
const dailyAvg = computed(() => Number(reportData.value.dailyAverage || 0).toFixed(2))
const totalOrders = computed(() => Number(reportData.value.orderCount ?? reportData.value.totalOrders ?? 0))
const trendDiff = computed(() => Math.abs(Number(reportData.value.diffAmount || 0)).toFixed(2))
const trendUp = computed(() => Number(reportData.value.diffAmount || 0) >= 0)
const trendArrow = computed(() => {
  const diff = Number(reportData.value.diffAmount || 0)
  if (diff === 0) return '→'
  return diff > 0 ? '↑' : '↓'
})
const summaryTrendText = computed(() => {
  const diff = Number(reportData.value.diffAmount || 0)
  if (diff === 0) return '持平'
  return diff > 0 ? '多' : '少'
})
const trendPercent = computed(() => {
  const current = Number(reportData.value.totalAmount || 0)
  const last = Number(reportData.value.lastWeekAmount || 0)
  if (last <= 0) return current > 0 ? 100 : 0
  return Math.round(Math.abs(((current - last) / last) * 100))
})

const weekDays = ref<WeekDayBar[]>([])
const mealPeriods = ref<MealPeriodItem[]>([
  { name: '午餐', percent: 0, color: mealColorMap['午餐'] },
  { name: '晚餐', percent: 0, color: mealColorMap['晚餐'] },
  { name: '早餐', percent: 0, color: mealColorMap['早餐'] },
])

const coreMealPeriod = computed(() => {
  if (!mealPeriods.value.length) return '暂无'
  const sorted = [...mealPeriods.value].sort((a, b) => b.percent - a.percent)
  return sorted[0].percent > 0 ? sorted[0].name : '暂无'
})

const getTodayWeekIndex = () => {
  const jsDay = new Date().getDay()
  return jsDay === 0 ? 6 : jsDay - 1
}

const buildWeekDays = (trend: WeeklyTrendItem[] = []) => {
  const trendMap = new Map<string, number>()
  trend.forEach((item) => {
    if (item.day) {
      trendMap.set(item.day, Number(item.dailyTotal || 0))
    }
  })

  const bars: WeekDayBar[] = []
  const monday = new Date(weekRange.value.monday)
  for (let i = 0; i < 7; i++) {
    const current = new Date(monday)
    current.setDate(monday.getDate() + i)
    const key = formatDate(current, '-')
    const amount = Number(trendMap.get(key) || 0)
    bars.push({
      label: dayLabels[i],
      percent: 0,
      amount: amount.toFixed(2),
      isMax: false,
    })
  }

  const maxAmount = Math.max(...bars.map((item) => Number(item.amount)), 0)
  const todayIndex = getTodayWeekIndex()
  weekDays.value = bars.map((item, idx) => {
    const amount = Number(item.amount)
    let percent = 8
    if (maxAmount > 0) {
      percent = amount > 0 ? Math.max((amount / maxAmount) * 100, 15) : 8
    }
    return {
      ...item,
      percent,
      isMax: idx === todayIndex,
    }
  })
}

const buildMealPeriods = (distribution: WeeklyMealPeriodItem[] = []) => {
  const percentageMap: Record<string, number> = {}
  distribution.forEach((item) => {
    if (item.mealPeriod) {
      percentageMap[item.mealPeriod] = Number(item.percentage || 0)
    }
  })

  mealPeriods.value = ['午餐', '晚餐', '早餐'].map((name) => ({
    name,
    percent: Number((percentageMap[name] || 0).toFixed(1)),
    color: mealColorMap[name],
  }))
}

const analysisData = ref({
   maxAmount: 0,
   maxDishName: '',
   topDishName: '',
   topDishCount: 0,
   avgIntervalHours: 0,
})

// === Analysis Items ===
const analysisItems = computed(() => {
   const maxSpendValue = analysisData.value.maxAmount > 0
      ? `¥${analysisData.value.maxAmount.toFixed(2)} (${analysisData.value.maxDishName || '暂无数据'})`
      : '暂无数据'
   const topDishValue = analysisData.value.topDishName
      ? `${analysisData.value.topDishName} (${analysisData.value.topDishCount || 0}次)`
      : '暂无数据'
   const avgIntervalValue = analysisData.value.avgIntervalHours > 0
      ? `${analysisData.value.avgIntervalHours.toFixed(1)} 小时`
      : '暂无数据'

   return [
      { label: '最高单笔消费', value: maxSpendValue, emoji: '📊', bgColor: '#ffdcc5' },
      { label: '下单最多菜品', value: topDishValue, emoji: '❤️', bgColor: '#ffdbd0' },
      { label: '平均下单间隔', value: avgIntervalValue, emoji: '⏱️', bgColor: '#ffdbcd' },
   ]
})

// === Fetch Real Data ===
const fetchWeeklyData = async () => {
  try {
    const res = await getWeeklyReportAPI({
      start_date: formatDate(weekRange.value.monday, '-'),
      end_date: formatDate(weekRange.value.sunday, '-'),
    })
    if (res.code === 0 && res.data) {
      const data = res.data
      reportData.value = {
        ...reportData.value,
        ...data,
        totalAmount: Number(data.totalAmount || 0),
        totalOrders: Number(data.totalOrders || 0),
        orderCount: Number(data.orderCount ?? data.totalOrders ?? 0),
        dailyAverage: Number(data.dailyAverage || 0),
        lastWeekAmount: Number(data.lastWeekAmount || 0),
        diffAmount: Number(data.diffAmount || 0),
      }
      buildWeekDays(data.dailyTrend || [])
      buildMealPeriods(data.mealPeriodDistribution || [])
    }
  } catch (error) {
    console.error('获取周报数据失败:', error)
  }
}

const fetchWeeklyAnalysis = async () => {
   try {
      const res = await getWeeklyAnalysisAPI()
      if (res.code === 0 && res.data) {
         analysisData.value = {
            maxAmount: Number(res.data.maxAmount || 0),
            maxDishName: res.data.maxDishName || '',
            topDishName: res.data.topDishName || '',
            topDishCount: Number(res.data.topDishCount || 0),
            avgIntervalHours: Number(res.data.avgIntervalHours || 0),
         }
      }
   } catch (error) {
      console.error('获取本周餐点分析失败:', error)
   }
}

onLoad(() => {
  weekRange.value = getWeekRange()
  buildWeekDays([])
  buildMealPeriods([])
  fetchWeeklyData()
  fetchWeeklyAnalysis()
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
   box-sizing: border-box;
}

.main-scroll {
   height: calc(100vh - 100rpx);
   padding: 0 24rpx;
   box-sizing: border-box;
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
   box-sizing: border-box;
   
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
   box-sizing: border-box;
   
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
   box-sizing: border-box;
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
   box-sizing: border-box;
   
   .suggestion-icon { font-size: 32rpx; }
   .suggestion-text { font-size: 24rpx; color: $on-surface-variant; font-weight: 500; line-height: 1.6; font-style: italic; }
}
</style>

