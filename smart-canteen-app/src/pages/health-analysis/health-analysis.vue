<template>
  <view class="health-analysis-page">
    <!-- Header with Tab Toggle -->
    <view class="header">
      <view class="tab-toggle">
        <view 
          class="tab-item" 
          :class="{ active: activeTab === 'health' }"
          @click="activeTab = 'health'"
        >
          <text>健康分析</text>
        </view>
        <view 
          class="tab-item" 
          :class="{ active: activeTab === 'cost' }"
          @click="activeTab = 'cost'"
        >
          <text>餐费分析</text>
        </view>
      </view>
    </view>

    <!-- Content Area -->
    <scroll-view class="content" scroll-y>
      <!-- Variant 1: Incomplete Profile -->
      <view v-if="pageState === 'incomplete'" class="empty-state">
        <view class="empty-icon-wrap no-data-icon-wrap">
          <image class="empty-icon no-data-icon" src="/static/icons/no-analysis-hero.png" mode="aspectFill" />
        </view>
        <text class="empty-title">请先完善健康画像</text>
        <text class="empty-desc">填写身高、体重等信息后即可解锁详细的健康看板。</text>
        <button class="primary-btn" @click="goToInfoSetting">去完善信息</button>
      </view>

      <!-- Variant 2: No Order Data -->
      <view v-else-if="pageState === 'noData'" class="empty-state">
        <view class="empty-icon-wrap no-data-icon-wrap">
          <image class="empty-icon no-data-icon" src="/static/icons/no-analysis-hero.png" mode="aspectFill" />
        </view>
        <text class="empty-title">暂无分析数据</text>
        <text class="empty-desc">在食堂点餐后，我们将为您自动生成健康与消费报告。</text>
        <button class="primary-btn" @click="goToOrder">
          <text>🍴 去点餐</text>
        </button>
      </view>

      <!-- Normal: Health Tab -->
      <view v-else-if="activeTab === 'health'" class="normal-content">
        <!-- Health Overview Card -->
        <view class="card health-overview">
          <view class="card-header">
            <view>
              <text class="card-label">健康总览</text>
              <view class="remaining-kcal">
                <text class="big-num">{{ remainingKcal }}</text>
                <text class="unit">剩余千卡</text>
              </view>
            </view>
            <view class="activity-badge">
              <text>活动量等级: {{ activityLabel }}</text>
            </view>
          </view>
          
          <view class="intake-row">
            <text class="intake-label">今日摄入: {{ todayIntake }}</text>
            <text class="intake-label">目标: {{ goalKcal }}</text>
          </view>
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: intakeProgress + '%' }"></view>
          </view>
          
          <!-- BMI Section -->
          <view class="bmi-section">
            <view class="bmi-header">
              <text class="bmi-label">BMI 指数</text>
              <text class="bmi-value">{{ profileStore.calculatedBMI?.toFixed(1) || '--' }}</text>
            </view>
            <view class="bmi-scale">
              <view class="bmi-pointer" :style="{ left: bmiPointerPosition + '%' }"></view>
            </view>
            <view class="bmi-labels">
              <text>偏瘦</text>
              <text>正常</text>
              <text>偏胖</text>
            </view>
          </view>
        </view>

        <!-- Nutrition Structure Card -->
        <view class="card nutrition-card">
          <view class="card-header">
            <text class="card-title">营养结构</text>
            <image class="info-icon-image" :src="healthAnalysisIcon" mode="aspectFit" />
          </view>
          
          <view class="nutrition-content">
            <!-- Donut Chart Placeholder -->
            <view class="donut-chart" v-if="hasNutritionData">
              <view class="donut-ring" :style="donutStyle"></view>
              <view class="donut-center">
                <text class="donut-label">总热量</text>
                <text class="donut-value">{{ todayIntake }}</text>
              </view>
            </view>
            <view class="donut-placeholder" v-else>
              <text class="placeholder-text">暂无营养数据</text>
            </view>
            
            <!-- Legend -->
            <view class="nutrition-legend">
              <view class="legend-item">
                <view class="legend-dot protein"></view>
                <text class="legend-label">蛋白质 ({{ macros.proteinPct }}%)</text>
                <text class="legend-value">{{ macros.proteinG }}g</text>
              </view>
              <view class="legend-item">
                <view class="legend-dot carb"></view>
                <text class="legend-label">碳水 ({{ macros.carbPct }}%)</text>
                <text class="legend-value">{{ macros.carbG }}g</text>
              </view>
              <view class="legend-item">
                <view class="legend-dot fat"></view>
                <text class="legend-label">脂肪 ({{ macros.fatPct }}%)</text>
                <text class="legend-value">{{ macros.fatG }}g</text>
              </view>
            </view>
          </view>
          
          <!-- Suggestion -->
          <view class="suggestion-box" v-if="nutritionSuggestion">
            <image class="suggestion-icon" :src="promptIcon" mode="aspectFit" />
            <text class="suggestion-text">{{ nutritionSuggestion }}</text>
          </view>
          
          <button class="primary-btn recommend-btn" @click="goToRecommend">
            推荐补齐
          </button>
        </view>

        <!-- Health Trend Card -->
        <view class="card trend-card">
          <view class="card-header">
            <text class="card-title">健康趋势</text>
            <view class="trend-badge" v-if="trendChange">
              <text class="trend-value">{{ trendChange }}</text>
              <image class="trend-icon-image" :src="healthAnalysisIcon" mode="aspectFit" />
            </view>
          </view>
          
          <view class="trend-chart" v-if="weeklyHealthTrend.length">
            <!-- Simple trend visualization -->
            <view class="chart-area">
              <view 
                v-for="(point, index) in weeklyHealthTrend" 
                :key="index"
                class="chart-point"
                :style="{ height: point.heightPct + '%', left: (index * 14.28) + '%' }"
              ></view>
            </view>
            <view class="chart-labels">
              <text v-for="day in weekDays" :key="day">{{ day }}</text>
            </view>
          </view>
          <view class="no-trend" v-else>
            <text>暂无趋势数据</text>
          </view>
        </view>
      </view>

      <!-- Normal: Cost Tab -->
      <view v-else-if="activeTab === 'cost'" class="normal-content">
        <!-- Monthly Spending Card -->
        <view class="card spending-card">
          <view class="card-header">
            <text class="card-label">本月已花</text>
            <view class="warning-badge" v-if="isOverspending">
              <text>⚠️ 消费偏快</text>
            </view>
            <view class="ok-badge" v-else>
              <text>✓ 消费平稳</text>
            </view>
          </view>
          
          <text class="spending-amount">¥{{ formatMoney(monthSpent) }}</text>
          
          <view class="spending-progress">
            <view class="spending-bar">
              <view class="spending-fill" :style="{ width: spendingProgress + '%' }"></view>
            </view>
          </view>
          
          <view class="prediction-row">
            <image
              class="prediction-icon"
              src="@/assets/images/icons/icon_cost_prediction.png"
              mode="aspectFit"
            />
            <text class="prediction-text">预计月末消费 ¥{{ formatInt(predictedTotal) }}</text>
          </view>
        </view>

        <!-- Cost Trend Card -->
        <view class="card cost-trend-card">
          <view class="card-header">
            <text class="card-title">消费趋势</text>
            <view class="period-toggle">
              <text class="period" :class="{ active: selectedRange === '7days' }" @click="setRange('7days')">近7天</text>
              <text class="period" :class="{ active: selectedRange === '30days' }" @click="setRange('30days')">近30天</text>
            </view>
          </view>
          
          <view class="bar-chart" v-if="selectedRange === '7days' && weeklyCostTrend && weeklyCostTrend.length > 0">
            <view 
              v-for="(item, index) in weeklyCostTrend" 
              :key="index"
              class="bar-item"
              :class="{ highlight: item.isToday }"
            >
              <view class="bar-area">
                <view class="bar" :style="{ height: item.barHeight + 'rpx' }">
                  <text class="bar-tooltip">¥{{ item.value }}</text>
                </view>
              </view>
              <text class="bar-label">{{ item.day }}</text>
            </view>
          </view>
          <view class="area-chart" v-else-if="selectedRange === '30days' && monthlyCostTrend.length > 0">
            <view class="area-tooltip">{{ areaTooltipText }}</view>
            <canvas
              canvas-id="costTrendCanvas"
              id="costTrendCanvas"
              class="area-canvas"
              @touchstart="onAreaCanvasTouch"
            />
            <view class="area-x-axis">
              <text
                v-for="(item, index) in monthlyCostTrend"
                :key="`x-${index}`"
                class="area-x-label"
                :class="{ today: item.isToday }"
              >
                {{ item.displayLabel }}
              </text>
            </view>
          </view>
          <view v-else style="text-align: center; color: #999; line-height: 500rpx; height: 500rpx;">
            加载中或暂无数据...
          </view>
        </view>

        <!-- Cost Composition Card -->
        <view class="card composition-card">
          <text class="card-title">消费构成</text>
          
          <view class="composition-content" v-if="categoryBreakdown && categoryBreakdown.length > 0">
            <!-- Donut Chart -->
            <view class="composition-donut">
              <view class="composition-ring" :style="compositionDonutStyle"></view>
              <view class="composition-center">
                <text class="comp-label">占比最大</text>
                <text class="comp-value">{{ topCategory }}</text>
              </view>
            </view>
            
            <!-- Category List -->
            <view class="category-list">
              <view v-for="cat in categoryBreakdown" :key="cat.name" class="category-item">
                <view class="cat-dot" :style="{ background: cat.color }"></view>
                <text class="cat-name">{{ cat.name }}</text>
                <text class="cat-amount">¥{{ formatMoney(cat.amount) }}</text>
              </view>
            </view>
          </view>
          <view v-else style="text-align: center; color: #999; line-height: 500rpx; height: 500rpx;">
            加载中或暂无数据...
          </view>
          
          <!-- Tip -->
          <view class="tip-box" v-if="costTip">
            <text class="tip-icon">ℹ️</text>
            <text class="tip-text">{{ costTip }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserProfileStore } from '@/stores/modules/userProfile'
import promptIcon from '@/assets/images/icons/prompt.png'
import healthAnalysisIcon from '@/assets/images/icons/icon_health_analysis_new.png'

const profileStore = useUserProfileStore()

// ============ 安全格式化工具函数 ============
const toNum = (v: any, d = 0): number => {
  if (v === null || v === undefined) return d
  const n = Number(v)
  return isNaN(n) ? d : n
}
const formatMoney = (v: any): string => toNum(v).toFixed(2)
const formatInt = (v: any): string => Math.round(toNum(v)).toString()

// Tab state
const activeTab = ref<'health' | 'cost'>('health')

// Page state: 'incomplete' | 'noData' | 'normal'
const pageState = computed(() => {
  const p = profileStore.profile
  // Variant 1: Profile incomplete
  if (!p.gender || !p.age || !p.height || !p.weight) {
    return 'incomplete'
  }
  // Variant 2: No order data (check from cost summary)
  if (!hasAnyData.value) {
    return 'noData'
  }
  return 'normal'
})

// ============ Health Tab Data ============
const todayIntake = ref(0) // 今日摄入 kcal
const goalKcal = computed(() => profileStore.suggestIntake || 2200)
const remainingKcal = computed(() => Math.max(0, goalKcal.value - todayIntake.value))
const intakeProgress = computed(() => Math.min(100, (todayIntake.value / goalKcal.value) * 100))

const activityLabel = computed(() => {
  const level = profileStore.profile.activityLevel
  const labels = ['久坐', '轻度', '中等', '重度']
  return labels[(level || 1) - 1] || '未知'
})

// BMI pointer position (scale: 16-30 -> 0%-100%)
const bmiPointerPosition = computed(() => {
  const bmi = profileStore.calculatedBMI || 22
  // Map BMI 16-30 to 0-100%
  return Math.min(100, Math.max(0, ((bmi - 16) / 14) * 100))
})

// Nutrition data
const hasNutritionData = ref(false)
const macros = ref({
  proteinG: 0, carbG: 0, fatG: 0,
  proteinPct: 30, carbPct: 50, fatPct: 20
})
const donutStyle = computed(() => {
  const { proteinPct, carbPct, fatPct } = macros.value
  return {
    background: `conic-gradient(#34c759 0% ${proteinPct}%, #4A90E2 ${proteinPct}% ${proteinPct + carbPct}%, #FFB347 ${proteinPct + carbPct}% 100%)`
  }
})
const nutritionSuggestion = ref('')

// Health trend
const weeklyHealthTrend = ref<{ value: number; heightPct: number }[]>([])
const trendChange = ref('')
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

// ============ Cost Tab Data ============
const monthSpent = ref(0)
const predictedTotal = ref(0)
const baseline = ref(0)
const isOverspending = computed(() => predictedTotal.value > baseline.value && baseline.value > 0)
const spendingProgress = computed(() => {
  if (baseline.value <= 0) return 50
  return Math.min(100, (monthSpent.value / baseline.value) * 100)
})

type TrendPoint = {
  day: string
  date: string
  value: number
  barHeight: number
  isToday: boolean
  displayLabel: string
}

const selectedRange = ref<'7days' | '30days'>('7days')
const weeklyCostTrend = ref<TrendPoint[]>([])
const monthlyCostTrend = ref<TrendPoint[]>([])
const selectedAreaIndex = ref(0)
const areaCanvasRect = ref<{ left: number; width: number } | null>(null)
const categoryBreakdown = ref<{ name: string; amount: number; color: string }[]>([])
const topCategory = computed(() => {
  if (!categoryBreakdown.value.length) return '--'
  return categoryBreakdown.value.reduce((a, b) => a.amount > b.amount ? a : b).name
})
const compositionDonutStyle = computed(() => {
  if (!categoryBreakdown.value.length) return { background: '#eee' }
  let acc = 0
  const gradientParts = categoryBreakdown.value.map(cat => {
    const start = acc
    acc += cat.amount / monthSpent.value * 100
    return `${cat.color} ${start}% ${acc}%`
  })
  return { background: `conic-gradient(${gradientParts.join(', ')})` }
})
const costTip = ref('')
const areaTooltipText = computed(() => {
  const point = monthlyCostTrend.value[selectedAreaIndex.value]
  if (!point) return ''
  return `${point.date}  ¥${formatMoney(point.value)}`
})

// Has any data check
const hasAnyData = computed(() => monthSpent.value > 0 || todayIntake.value > 0)

// ============ Navigation ============
const goToInfoSetting = () => {
  uni.navigateTo({ url: '/pages/info-setting/info-setting' })
}

const goToOrder = () => {
  uni.switchTab({ url: '/pages/category/category' })
}

const goToRecommend = () => {
  uni.switchTab({ url: '/pages/index/index_v2' })
}

// ============ Data Fetching ============
const fetchHealthSummary = async () => {
  try {
    const res = await uni.request({
      url: 'http://127.0.0.1:8081/analysis/health/summary',
      method: 'GET',
      header: { 'authentication': uni.getStorageSync('token') }
    })
    const data = (res as any).data?.data
    if (data) {
      todayIntake.value = data.todayIntakeKcal || 0
      if (data.macros) {
        hasNutritionData.value = true
        macros.value = data.macros
      } else {
        hasNutritionData.value = false
      }
      nutritionSuggestion.value = data.suggestion || ''
    }
  } catch (e) {
    console.error('获取健康分析数据失败:', e)
  }
}

// 获取健康趋势（新接口）
const fetchHealthTrend = async () => {
  try {
    const res = await uni.request({
      url: 'http://127.0.0.1:8081/analysis/health/trend?range=7',
      method: 'GET',
      header: { 'authentication': uni.getStorageSync('token') }
    })
    const data = (res as any).data?.data
    if (data && Array.isArray(data) && data.length > 0) {
      const maxIntake = Math.max(...data.map((t: any) => t.intakeKcal || 0))
      weeklyHealthTrend.value = data.map((t: any) => ({
        value: t.intakeKcal || 0,
        day: t.day,
        heightPct: maxIntake > 0 ? ((t.intakeKcal || 0) / maxIntake) * 80 + 10 : 10
      }))
      // 计算趋势变化
      if (data.length >= 2) {
        const latest = data[data.length - 1]?.completionRate || 0
        const prev = data[data.length - 2]?.completionRate || 0
        if (prev > 0) {
          const change = ((latest - prev) / prev * 100).toFixed(1)
          trendChange.value = change.startsWith('-') ? change + '%' : '+' + change + '%'
        }
      }
    }
  } catch (e) {
    console.error('获取健康趋势失败:', e)
  }
}

// ============ 日期工具 ============
const getDayName = (dateStr: string): string => {
  try {
    const d = new Date(dateStr)
    const names = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return names[d.getDay()] || dateStr
  } catch { return dateStr }
}

const isToday = (dateStr: string): boolean => {
  try {
    const d = new Date(dateStr)
    const now = new Date()
    return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate()
  } catch { return false }
}

const formatMonthDay = (dateStr: string): string => {
  try {
    const d = new Date(dateStr)
    const mm = d.getMonth() + 1
    const dd = String(d.getDate()).padStart(2, '0')
    return `${mm}-${dd}`
  } catch {
    return dateStr
  }
}

const rpxToPx = (rpx: number): number => {
  const { windowWidth } = uni.getSystemInfoSync()
  return Math.round((windowWidth / 750) * rpx)
}

const queryAreaCanvasRect = () => {
  uni.createSelectorQuery()
    .select('#costTrendCanvas')
    .boundingClientRect((rect: any) => {
      if (rect && rect.width) {
        areaCanvasRect.value = { left: rect.left, width: rect.width }
      }
    })
    .exec()
}

const draw30DayAreaChart = () => {
  if (selectedRange.value !== '30days' || !monthlyCostTrend.value.length) return

  const ctx = uni.createCanvasContext('costTrendCanvas')
  const width = rpxToPx(610)
  const height = rpxToPx(320)
  const padding = {
    top: rpxToPx(24),
    right: rpxToPx(16),
    bottom: rpxToPx(22),
    left: rpxToPx(16)
  }
  const chartWidth = width - padding.left - padding.right
  const chartHeight = height - padding.top - padding.bottom
  const baseY = height - padding.bottom

  const values = monthlyCostTrend.value.map(item => item.value)
  const max = Math.max(...values, 1)
  const min = Math.min(...values, 0)
  const range = Math.max(max - min, 1)

  const points = monthlyCostTrend.value.map((item, index, arr) => {
    const x = padding.left + (arr.length === 1 ? 0 : (index / (arr.length - 1)) * chartWidth)
    const y = padding.top + (1 - (item.value - min) / range) * chartHeight
    return { x, y }
  })

  ctx.clearRect(0, 0, width, height)

  if (points.length > 1) {
    // 平滑面积填充（莫兰迪紫渐变）
    const areaGradient = ctx.createLinearGradient(0, padding.top, 0, baseY)
    areaGradient.addColorStop(0, 'rgba(142, 124, 195, 0.30)')
    areaGradient.addColorStop(1, 'rgba(142, 124, 195, 0.03)')
    ctx.beginPath()
    ctx.moveTo(points[0].x, baseY)
    ctx.lineTo(points[0].x, points[0].y)
    for (let i = 1; i < points.length; i++) {
      const prev = points[i - 1]
      const curr = points[i]
      const cx = (prev.x + curr.x) / 2
      const cy = (prev.y + curr.y) / 2
      ctx.quadraticCurveTo(prev.x, prev.y, cx, cy)
    }
    const last = points[points.length - 1]
    ctx.lineTo(last.x, last.y)
    ctx.lineTo(last.x, baseY)
    ctx.closePath()
    ctx.setFillStyle(areaGradient)
    ctx.fill()

    // 平滑主线
    ctx.beginPath()
    ctx.moveTo(points[0].x, points[0].y)
    for (let i = 1; i < points.length; i++) {
      const prev = points[i - 1]
      const curr = points[i]
      const cx = (prev.x + curr.x) / 2
      const cy = (prev.y + curr.y) / 2
      ctx.quadraticCurveTo(prev.x, prev.y, cx, cy)
    }
    const lineGradient = ctx.createLinearGradient(0, 0, width, 0)
    lineGradient.addColorStop(0, '#9E8CD6')
    lineGradient.addColorStop(1, '#8F77D0')
    ctx.setStrokeStyle(lineGradient)
    ctx.setLineWidth(rpxToPx(4))
    ctx.setLineCap('round')
    ctx.setLineJoin('round')
    ctx.stroke()
  }

  // 今日/选中点高亮
  const focusIndex = Math.min(selectedAreaIndex.value, points.length - 1)
  if (focusIndex >= 0 && points[focusIndex]) {
    const p = points[focusIndex]
    ctx.beginPath()
    ctx.arc(p.x, p.y, rpxToPx(7), 0, 2 * Math.PI)
    ctx.setFillStyle('#8F77D0')
    ctx.fill()
    ctx.beginPath()
    ctx.arc(p.x, p.y, rpxToPx(11), 0, 2 * Math.PI)
    ctx.setStrokeStyle('rgba(143, 119, 208, 0.28)')
    ctx.setLineWidth(rpxToPx(3))
    ctx.stroke()
  }

  ctx.draw()
}

const setRange = async (range: '7days' | '30days') => {
  if (selectedRange.value === range) return
  selectedRange.value = range
  const dayRange = range === '30days' ? 30 : 7
  await fetchCostTrend(dayRange)
  if (range === '30days') {
    await nextTick()
    queryAreaCanvasRect()
    draw30DayAreaChart()
  }
}

const onAreaCanvasTouch = (e: any) => {
  if (!monthlyCostTrend.value.length || !areaCanvasRect.value) return
  const touchX = e?.changedTouches?.[0]?.x
  if (typeof touchX !== 'number') return
  const ratio = Math.min(1, Math.max(0, (touchX - areaCanvasRect.value.left) / areaCanvasRect.value.width))
  const index = Math.round(ratio * (monthlyCostTrend.value.length - 1))
  selectedAreaIndex.value = index
  draw30DayAreaChart()
}

const fetchCostTrend = async (range: 7 | 30 = 7) => {
  try {
    const res = await uni.request({
      url: `http://127.0.0.1:8081/analysis/cost/trend?range=${range}`,
      method: 'GET',
      header: { 'authentication': uni.getStorageSync('token') }
    })

    const data = (res as any).data?.data
    if (!Array.isArray(data) || data.length === 0) {
      if (range === 7) weeklyCostTrend.value = []
      if (range === 30) monthlyCostTrend.value = []
      return
    }

    const maxValue = Math.max(...data.map((t: any) => parseFloat(t.amount || t.value || 0))) || 1
    const MAX_BAR_RPX = 380
    const MIN_BAR_RPX = 20

    const points: TrendPoint[] = data.map((t: any, index: number) => {
      const amount = parseFloat(t.amount || t.value || 0)
      const ratio = maxValue > 0 ? amount / maxValue : 0
      const barHeight = amount > 0 ? Math.max(MIN_BAR_RPX, Math.round(ratio * MAX_BAR_RPX)) : MIN_BAR_RPX
      const date = t.date || ''
      const showTick = index === 0 || index === data.length - 1 || index % 5 === 0
      return {
        day: getDayName(date),
        date,
        value: amount,
        barHeight,
        isToday: isToday(date),
        displayLabel: range === 30 ? (showTick ? formatMonthDay(date) : '') : getDayName(date)
      }
    })

    if (range === 7) {
      weeklyCostTrend.value = points
    } else {
      monthlyCostTrend.value = points
      const todayIndex = points.findIndex(point => point.isToday)
      selectedAreaIndex.value = todayIndex >= 0 ? todayIndex : points.length - 1
    }
  } catch (e) {
    console.error(`获取${range}天餐费趋势失败:`, e)
  }
}

// ============ 【核心修复】统一从 /cost/summary 获取所有餐费数据 ============
// /cost/summary 后端已返回 trendData + byCategory，无需额外接口
const fetchCostSummary = async () => {
  try {
    const res = await uni.request({
      url: 'http://127.0.0.1:8081/analysis/cost/summary',
      method: 'GET',
      header: { 'authentication': uni.getStorageSync('token') }
    })
    const data = (res as any).data?.data
    console.log('===== 餐费分析完整响应 =====', JSON.stringify(data))

    if (data) {
      // 1) 金额摘要
      monthSpent.value = toNum(data.monthSpent)
      predictedTotal.value = toNum(data.predictedMonthTotal)
      baseline.value = toNum(data.baseline)
      costTip.value = data.tip || ''

      // 2) 消费构成 - 直接从 summary 响应中提取 byCategory
      const catArr = data.byCategory
      console.log('===== 构成原始数据 =====', JSON.stringify(catArr))
      if (catArr && Array.isArray(catArr) && catArr.length > 0) {
        const colorPalette = ['#4A90E2', '#34C759', '#FF9500', '#FF3B30', '#AF52DE', '#5AC8FA']
        categoryBreakdown.value = JSON.parse(JSON.stringify(
          catArr.map((c: any, index: number) => ({
            name: c.name || c.categoryName || '其他',
            amount: parseFloat(c.amount || c.value || 0),
            color: c.color || colorPalette[index % colorPalette.length]
          }))
        ))
        console.log('===== 构成组装结果 =====', JSON.stringify(categoryBreakdown.value))
      } else {
        categoryBreakdown.value = []
      }
    }
  } catch (e) {
    console.error('获取餐费分析数据失败:', e)
  }
}

// 统一加载所有数据
const loadAllData = async () => {
  await profileStore.fetchProfile()
  // 并行加载所有分析数据
  await Promise.all([
    fetchHealthSummary(),
    fetchHealthTrend(),
    fetchCostSummary(),
    fetchCostTrend(7)
  ])
}

watch(selectedRange, async (range) => {
  if (range !== '30days' || !monthlyCostTrend.value.length) return
  await nextTick()
  queryAreaCanvasRect()
  draw30DayAreaChart()
})

watch(selectedAreaIndex, () => {
  if (selectedRange.value === '30days') {
    draw30DayAreaChart()
  }
})

// ============ Lifecycle ============
onShow(() => {
  loadAllData()
})

onMounted(() => {
  loadAllData()
})
</script>

<style lang="scss" scoped>
.health-analysis-page {
  min-height: 100vh;
  background: #f7f8fa;
  display: flex;
  flex-direction: column;
  padding-bottom: 120rpx;
}

// Header
.header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: rgba(247, 248, 250, 0.95);
  backdrop-filter: blur(10px);
  padding: 20rpx 32rpx;
  padding-top: calc(env(safe-area-inset-top) + 20rpx);
}

.tab-toggle {
  display: flex;
  height: 88rpx;
  background: #edf0f4;
  border-radius: 44rpx;
  padding: 8rpx;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 500;
  color: #666666;
  transition: all 0.3s;
  
  &.active {
    background: #fff;
    color: #111813;
    font-weight: 700;
    box-shadow: 0 12rpx 28rpx rgba(0, 0, 0, 0.05);
  }
}

// Content
.content {
  flex: 1;
  padding: 0 32rpx;
}

// Empty States
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 60rpx;
  text-align: center;
}

.empty-icon-wrap {
  width: 320rpx;
  height: 320rpx;
  background: #eef1f4;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 60rpx;
  position: relative;
}

.empty-icon {
  width: 160rpx;
  height: 160rpx;
  opacity: 0.5;
}

.no-data-icon-wrap {
  width: 320rpx;
  height: 320rpx;
  border-radius: 50%;
  background: #eef1f4;
  margin-bottom: 60rpx;
  overflow: hidden;
}

.no-data-icon {
  width: 100%;
  height: 100%;
  opacity: 1;
  border-radius: 50%;
}

.edit-badge, .receipt-badge {
  position: absolute;
  bottom: 40rpx;
  right: 40rpx;
  background: #fff;
  padding: 16rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
  font-size: 36rpx;
}

.empty-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #111813;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 28rpx;
  color: #61896f;
  line-height: 1.6;
  margin-bottom: 60rpx;
  max-width: 480rpx;
}

.primary-btn {
  background: #ff8c42;
  color: #ffffff;
  font-weight: 700;
  font-size: 30rpx;
  padding: 24rpx 64rpx;
  border-radius: 48rpx;
  border: none;
  box-shadow: 0 16rpx 40rpx rgba(255, 140, 66, 0.2);
}

// Cards
.card {
  background: #fff;
  border-radius: 32rpx;
  padding: 40rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24rpx;
}

.card-label {
  font-size: 26rpx;
  color: #61896f;
  font-weight: 600;
}

.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #111813;
}

.info-icon-image {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
}

// Health Overview
.remaining-kcal {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-top: 8rpx;
}

.big-num {
  font-size: 56rpx;
  font-weight: 900;
  color: #111813;
}

.unit {
  font-size: 28rpx;
  font-weight: 600;
  color: #61896f;
}

.activity-badge {
  background: rgba(255, 140, 66, 0.14);
  color: #b85b00;
  padding: 12rpx 20rpx;
  border-radius: 32rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.intake-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.intake-label {
  font-size: 24rpx;
  color: #61896f;
}

.progress-bar {
  height: 16rpx;
  background: #f0f4f2;
  border-radius: 8rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #ff8c42;
  border-radius: 8rpx;
  transition: width 0.8s ease-out;
}

// BMI Section
.bmi-section {
  margin-top: 40rpx;
  padding-top: 24rpx;
  border-top: none;
}

.bmi-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.bmi-label, .bmi-value {
  font-size: 24rpx;
  font-weight: 700;
  color: #111813;
}

.bmi-scale {
  height: 12rpx;
  background: linear-gradient(to right, #9ec5ff, #ffbd7a, #ff8c42);
  border-radius: 6rpx;
  position: relative;
  opacity: 0.8;
}

.bmi-pointer {
  position: absolute;
  top: -4rpx;
  width: 8rpx;
  height: 20rpx;
  background: #111;
  border-radius: 4rpx;
  transform: translateX(-50%);
  box-shadow: 0 0 0 4rpx #fff;
}

.bmi-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8rpx;
  font-size: 20rpx;
  color: #61896f;
}

// Nutrition Card
.nutrition-content {
  display: flex;
  gap: 32rpx;
  align-items: center;
}

.donut-chart, .donut-placeholder {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  position: relative;
  flex-shrink: 0;
}

.donut-ring {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.donut-center {
  position: absolute;
  inset: 24rpx;
  background: #fff;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.donut-label {
  font-size: 22rpx;
  color: #61896f;
}

.donut-value {
  font-size: 28rpx;
  font-weight: 700;
  color: #111813;
}

.donut-placeholder {
  background: #f0f4f2;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-text {
  font-size: 24rpx;
  color: #999;
}

.nutrition-legend {
  flex: 1;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.legend-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  
  &.protein { background: #34c759; }
  &.carb { background: #4A90E2; }
  &.fat { background: #FFB347; }
}

.legend-label {
  flex: 1;
  font-size: 26rpx;
  color: #61896f;
}

.legend-value {
  font-size: 26rpx;
  font-weight: 700;
  color: #111813;
}

.suggestion-box {
  background: #f0f4f2;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-top: 24rpx;
  display: flex;
  gap: 16rpx;
}

.suggestion-icon {
  width: 34rpx;
  height: 34rpx;
  flex-shrink: 0;
  margin-top: 2rpx;
}

.suggestion-text {
  font-size: 26rpx;
  color: #111813;
  line-height: 1.5;
}

.recommend-btn {
  margin-top: 24rpx;
  width: 100%;
}

// Trend Card
.trend-card {
  padding-bottom: 24rpx;
}

.trend-badge {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #34c759;
  font-size: 24rpx;
  font-weight: 600;
}

.trend-icon-image {
  width: 30rpx;
  height: 30rpx;
  flex-shrink: 0;
}

.chart-area {
  height: 200rpx;
  position: relative;
  margin: 24rpx 0;
}

.chart-point {
  position: absolute;
  bottom: 0;
  width: 8rpx;
  background: #34c759;
  border-radius: 4rpx 4rpx 0 0;
}

.chart-labels {
  display: flex;
  justify-content: space-between;
  font-size: 20rpx;
  color: #61896f;
}

.no-trend {
  padding: 60rpx;
  text-align: center;
  color: #999;
  font-size: 26rpx;
}

// Cost Tab Styles
.spending-card {
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -80rpx;
    right: -80rpx;
    width: 200rpx;
    height: 200rpx;
    background: rgba(255, 140, 66, 0.12);
    border-radius: 50%;
    filter: blur(40rpx);
  }
}

.warning-badge {
  background: #fef3c7;
  color: #b45309;
  padding: 8rpx 16rpx;
  border-radius: 32rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.ok-badge {
  background: #d1fae5;
  color: #065f46;
  padding: 8rpx 16rpx;
  border-radius: 32rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.spending-amount {
  font-size: 64rpx;
  font-weight: 900;
  color: #111813;
  margin: 16rpx 0;
}

.spending-progress {
  margin: 24rpx 0;
}

.spending-bar {
  height: 12rpx;
  background: #f0f4f2;
  border-radius: 6rpx;
  overflow: hidden;
}

.spending-fill {
  height: 100%;
  background: #fbbf24;
  border-radius: 6rpx;
}

.prediction-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 16rpx;
}

.prediction-icon {
  width: 34rpx;
  height: 34rpx;
  flex-shrink: 0;
}

.prediction-text {
  font-size: 28rpx;
  color: #111813;
}

// Bar Chart
.bar-chart {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-top: 40rpx;
  padding-bottom: 16rpx;
}

.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.bar-area {
  width: 100%;
  height: 400rpx;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.bar {
  width: 36rpx;
  max-width: 48rpx;
  min-height: 20rpx;
  background: rgba(255, 140, 66, 0.28);
  border-radius: 8rpx 8rpx 0 0;
  position: relative;
  transition: all 0.3s;
  
  .highlight & {
    background: #ff8c42;
    box-shadow: 0 12rpx 30rpx rgba(255, 140, 66, 0.25);
  }
}

.bar-tooltip {
  position: absolute;
  top: -48rpx;
  left: 50%;
  transform: translateX(-50%);
  background: #333;
  color: #fff;
  font-size: 20rpx;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  white-space: nowrap;
  opacity: 0;
  
  .bar-item:active & {
    opacity: 1;
  }
}

.bar-label {
  font-size: 20rpx;
  color: #61896f;
  
  .highlight & {
    color: #111813;
    font-weight: 700;
  }
}

// 30-day smooth area chart
.area-chart {
  position: relative;
  padding-top: 24rpx;
  padding-bottom: 8rpx;
}

.area-tooltip {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 14rpx;
  border-radius: 20rpx;
  background: rgba(143, 119, 208, 0.12);
  color: #6f5aa4;
  font-size: 22rpx;
  font-weight: 600;
  margin-bottom: 14rpx;
}

.area-canvas {
  width: 610rpx;
  height: 320rpx;
}

.area-x-axis {
  margin-top: 10rpx;
  display: flex;
  justify-content: space-between;
}

.area-x-label {
  flex: 1;
  text-align: center;
  font-size: 18rpx;
  color: #9ca3af;
  min-height: 26rpx;
}

.area-x-label.today {
  color: #6f5aa4;
  font-weight: 700;
}

// Composition
.composition-content {
  display: flex;
  gap: 32rpx;
  align-items: center;
  margin-top: 24rpx;
  height: 500rpx !important;
  min-height: 500rpx !important;
}

.composition-donut {
  width: 240rpx;
  height: 240rpx;
  border-radius: 50%;
  position: relative;
}

.composition-ring {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.composition-center {
  position: absolute;
  inset: 36rpx;
  background: #fff;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.comp-label {
  font-size: 22rpx;
  color: #999;
}

.comp-value {
  font-size: 32rpx;
  font-weight: 700;
  color: #111813;
}

.category-list {
  flex: 1;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.cat-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
}

.cat-name {
  flex: 1;
  font-size: 26rpx;
  color: #374151;
}

.cat-amount {
  font-size: 26rpx;
  font-weight: 700;
  color: #111813;
}

.tip-box {
  background: #eff6ff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-top: 24rpx;
  display: flex;
  gap: 12rpx;
  align-items: flex-start;
}

.tip-icon {
  font-size: 28rpx;
  color: #2563eb;
}

.tip-text {
  font-size: 26rpx;
  color: #1e40af;
  line-height: 1.5;
}

.period-toggle {
  display: flex;
  background: #f3f4f6;
  border-radius: 12rpx;
  padding: 4rpx;
}

.period {
  padding: 12rpx 20rpx;
  font-size: 22rpx;
  color: #6b7280;
  border-radius: 10rpx;
  transition: all 0.2s ease;
  
  &.active {
    background: #fff;
    color: #111813;
    font-weight: 600;
    box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.04);
  }
}
</style>

