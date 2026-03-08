<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { 
  getUserListAPI, 
  getUserProfileAPI, 
  getUserOrdersAPI,
  getUserSpendAnalyticsAPI, 
  getUserNutritionAnalyticsAPI,
  updateUserStatusAPI,
  exportUsersAPI 
} from '@/api/user-management'

// ============ 类型定义 ============
interface UserItem {
  id: number
  nickname: string
  avatar: string
  gender: number
  phone: string
  email: string
  bmi: number
  bmiLabel: string
  tdee: number
  monthSpend: number
  orderCount: number
  status: number
  createTime: string
}

interface Summary {
  totalUsers: number
  activeUsers: number
  totalSpend: number
}

// ============ 数据 ============
const userList = ref<UserItem[]>([])
const loading = ref(false)
const summary = ref<Summary>({ totalUsers: 0, activeUsers: 0, totalSpend: 0 })

const queryParams = reactive({
  keyword: '',
  status: null as number | null,
  bmiRange: '',
  page: 1,
  pageSize: 10,
  total: 0
})

// 详情 Drawer
const drawerVisible = ref(false)
const activeTab = ref('profile')
const currentUserId = ref<number | null>(null)
const userProfile = ref<any>(null)
const userOrders = ref<any[]>([])
const ordersTotal = ref(0)
const ordersPage = ref(1)
const ordersTimeRange = ref(7)
const spendAnalytics = ref<any>(null)
const nutritionAnalytics = ref<any>(null)
const analyticsTab = ref('spend')
const analyticsRange = ref(7)

// ============ 方法 ============
const fetchUserList = async () => {
  loading.value = true
  try {
    const { data: res } = await getUserListAPI({
      keyword: queryParams.keyword || undefined,
      status: queryParams.status ?? undefined,
      bmiRange: queryParams.bmiRange || undefined,
      page: queryParams.page,
      pageSize: queryParams.pageSize
    })
    if (res.code === 0) {
      userList.value = res.data.list
      queryParams.total = res.data.total
      summary.value = res.data.summary
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchUserList()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = null
  queryParams.bmiRange = ''
  queryParams.page = 1
  fetchUserList()
}

const handlePageChange = (page: number) => {
  queryParams.page = page
  fetchUserList()
}

const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  queryParams.page = 1
  fetchUserList()
}

const handleStatusChange = async (row: UserItem) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', { type: 'warning' })
    await updateUserStatusAPI(newStatus, row.id)
    ElMessage.success(`${action}成功`)
    fetchUserList()
  } catch {
    // 取消
  }
}

const handleExport = async () => {
  try {
    const res = await exportUsersAPI({
      keyword: queryParams.keyword || undefined,
      status: queryParams.status ?? undefined,
      bmiRange: queryParams.bmiRange || undefined
    })
    const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'users_export.csv'
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

// 打开详情 Drawer
const openDrawer = async (row: UserItem) => {
  currentUserId.value = row.id
  activeTab.value = 'profile'
  drawerVisible.value = true
  await fetchProfile()
}

const fetchProfile = async () => {
  if (!currentUserId.value) return
  try {
    const { data: res } = await getUserProfileAPI(currentUserId.value)
    if (res.code === 0) {
      userProfile.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchOrders = async () => {
  if (!currentUserId.value) return
  try {
    const { data: res } = await getUserOrdersAPI(currentUserId.value, {
      timeRange: ordersTimeRange.value,
      page: ordersPage.value,
      pageSize: 10
    })
    if (res.code === 0) {
      userOrders.value = res.data.list
      ordersTotal.value = res.data.total
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchSpendAnalytics = async () => {
  if (!currentUserId.value) return
  try {
    const { data: res } = await getUserSpendAnalyticsAPI(currentUserId.value, analyticsRange.value)
    if (res.code === 0) {
      spendAnalytics.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchNutritionAnalytics = async () => {
  if (!currentUserId.value) return
  try {
    const { data: res } = await getUserNutritionAnalyticsAPI(currentUserId.value, analyticsRange.value)
    if (res.code === 0) {
      nutritionAnalytics.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const onTabChange = async (tab: string) => {
  if (tab === 'orders') {
    await fetchOrders()
  } else if (tab === 'analytics') {
    await fetchSpendAnalytics()
    await fetchNutritionAnalytics()
  }
}

// BMI Tag 类型
const getBmiTagType = (label: string) => {
  switch (label) {
    case '正常': return 'success'
    case '偏瘦': return 'info'
    case '超重': return 'warning'
    case '肥胖': return 'danger'
    default: return 'info'
  }
}

// 健康目标文案
const healthGoalText = computed(() => {
  if (!userProfile.value) return '-'
  switch (userProfile.value.healthGoal) {
    case 1: return '减脂'
    case 2: return '增肌'
    case 3: return '维持'
    default: return '未设置'
  }
})

// 活动量等级
const activityLevelText = computed(() => {
  if (!userProfile.value) return '-'
  const factor = userProfile.value.activityFactor
  if (!factor) return '未设置'
  if (factor <= 1.2) return '久坐'
  if (factor <= 1.375) return '轻度活动'
  if (factor <= 1.55) return '中度活动'
  return '重度活动'
})

// 初始化
fetchUserList()
</script>

<template>
  <div class="user-management">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-info">
            <span class="stat-label">总用户数</span>
            <span class="stat-value">{{ summary.totalUsers.toLocaleString() }}</span>
          </div>
          <el-icon class="stat-icon primary"><User /></el-icon>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-info">
            <span class="stat-label">本月活跃用户</span>
            <span class="stat-value">{{ summary.activeUsers.toLocaleString() }}</span>
          </div>
          <el-icon class="stat-icon success"><Check /></el-icon>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-info">
            <span class="stat-label">本月总消费</span>
            <span class="stat-value">¥ {{ Number(summary.totalSpend || 0).toLocaleString() }}</span>
          </div>
          <el-icon class="stat-icon warning"><Wallet /></el-icon>
        </div>
      </el-card>
    </div>

    <!-- 筛选区 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <div class="filter-item">
          <label>搜索用户</label>
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="昵称 / 手机号 / 用户ID" 
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="filter-item">
          <label>状态</label>
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="全部" :value="null" />
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </div>
        <div class="filter-item">
          <label>BMI 区间</label>
          <el-select v-model="queryParams.bmiRange" placeholder="全部" clearable>
            <el-option label="全部" value="" />
            <el-option label="偏瘦" value="underweight" />
            <el-option label="正常" value="normal" />
            <el-option label="超重" value="overweight" />
            <el-option label="肥胖" value="obese" />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <span class="table-title">用户列表</span>
          <el-button text @click="handleExport">
            <el-icon><Download /></el-icon> 导出数据
          </el-button>
        </div>
      </template>

      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="40" :src="row.avatar" />
              <div class="user-info">
                <span class="user-name">{{ row.nickname || '-' }}</span>
                <span class="user-id">ID: {{ row.id }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="性别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === 1 ? '' : 'danger'" size="small">
              {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="邮箱" width="160">
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="BMI" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.bmi" :type="getBmiTagType(row.bmiLabel)" round>
              {{ Number(row.bmi).toFixed(1) }} {{ row.bmiLabel }}
            </el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="TDEE" width="100" align="center">
          <template #default="{ row }">
            {{ row.tdee ? `${row.tdee} kcal` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="本月消费" width="100" align="right">
          <template #default="{ row }">
            ¥ {{ Number(row.monthSpend || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="总订单" width="80" align="center">
          <template #default="{ row }">
            {{ row.orderCount }} 单
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch 
              :model-value="row.status === 1" 
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDrawer(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="queryParams.total"
          :page-size="queryParams.pageSize"
          :current-page="queryParams.page"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 用户详情 Drawer -->
    <el-drawer 
      v-model="drawerVisible" 
      title="" 
      size="70%" 
      direction="rtl"
      :destroy-on-close="true"
    >
      <template #header>
        <div class="drawer-header" v-if="userProfile">
          <el-avatar :size="64" :src="userProfile.avatar" />
          <div class="drawer-user-info">
            <div class="drawer-user-name">
              {{ userProfile.nickname || userProfile.name || '未设置昵称' }}
              <el-tag :type="userProfile.status === 1 ? 'success' : 'danger'" size="small">
                {{ userProfile.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </div>
            <div class="drawer-user-meta">
              ID: {{ userProfile.id }} | {{ userProfile.phone || '-' }} | 
              {{ userProfile.createTime?.substring(0, 10) }} 注册
            </div>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- Tab 1: 档案与画像 -->
        <el-tab-pane label="档案与画像" name="profile">
          <div class="profile-tab" v-if="userProfile">
            <!-- 指标卡片 -->
            <div class="metrics-row">
              <el-card class="metric-card" shadow="hover">
                <div class="metric-label">BMI</div>
                <div class="metric-value">{{ userProfile.bmi?.toFixed(1) || '-' }}</div>
                <el-tag :type="getBmiTagType(userProfile.bmiLabel)" size="small">
                  {{ userProfile.bmiLabel || '未知' }}
                </el-tag>
              </el-card>
              <el-card class="metric-card" shadow="hover">
                <div class="metric-label">身高</div>
                <div class="metric-value">{{ userProfile.height || '-' }} <small>cm</small></div>
              </el-card>
              <el-card class="metric-card" shadow="hover">
                <div class="metric-label">体重</div>
                <div class="metric-value">{{ userProfile.weight || '-' }} <small>kg</small></div>
              </el-card>
              <el-card class="metric-card" shadow="hover">
                <div class="metric-label">TDEE</div>
                <div class="metric-value">{{ userProfile.tdee || '-' }} <small>kcal</small></div>
              </el-card>
            </div>

            <!-- 健康目标 -->
            <el-card class="section-card" shadow="never">
              <template #header>健康计划</template>
              <div class="goal-info">
                <div class="goal-item">
                  <span class="goal-label">健康目标</span>
                  <el-tag type="primary">{{ healthGoalText }}</el-tag>
                </div>
                <div class="goal-item">
                  <span class="goal-label">活动量等级</span>
                  <el-tag>{{ activityLevelText }}</el-tag>
                </div>
              </div>
            </el-card>

            <!-- 饮食偏好 -->
            <el-card class="section-card" shadow="never">
              <template #header>饮食偏好</template>
              <div class="pref-section">
                <div class="pref-group">
                  <span class="pref-title">口味偏好</span>
                  <div class="pref-tags">
                    <el-tag v-for="tag in (userProfile.tasteTags ? JSON.parse(userProfile.tasteTags) : [])" :key="tag">
                      {{ tag }}
                    </el-tag>
                    <span v-if="!userProfile.tasteTags" class="text-muted">未设置</span>
                  </div>
                </div>
                <div class="pref-group">
                  <span class="pref-title">忌口/过敏</span>
                  <div class="pref-tags">
                    <el-tag v-for="tag in (userProfile.avoidTags ? JSON.parse(userProfile.avoidTags) : [])" :key="tag" type="danger">
                      {{ tag }}
                    </el-tag>
                    <span v-if="!userProfile.avoidTags" class="text-muted">未设置</span>
                  </div>
                </div>
                <div class="pref-group">
                  <span class="pref-title">营养偏好</span>
                  <div class="pref-tags">
                    <el-tag v-if="userProfile.nutritionPref" type="success">{{ userProfile.nutritionPref }}</el-tag>
                    <span v-else class="text-muted">未设置</span>
                  </div>
                </div>
              </div>
            </el-card>

            <!-- 营养师建议 -->
            <el-card class="tip-card" shadow="never">
              <div class="tip-content">
                <el-icon class="tip-icon"><InfoFilled /></el-icon>
                <div>
                  <div class="tip-title">营养师建议</div>
                  <div class="tip-text">{{ userProfile.nutritionistTip }}</div>
                </div>
              </div>
            </el-card>

            <!-- 档案不完整提示 -->
            <el-empty v-if="!userProfile.profileComplete" description="档案不完整，缺少身高体重数据">
              <template #image>
                <el-icon :size="60" color="#909399"><WarningFilled /></el-icon>
              </template>
            </el-empty>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 点餐记录 -->
        <el-tab-pane label="点餐记录" name="orders">
          <div class="orders-tab">
            <div class="orders-filter">
              <el-radio-group v-model="ordersTimeRange" @change="fetchOrders">
                <el-radio-button :label="7">近7天</el-radio-button>
                <el-radio-button :label="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
            <el-table :data="userOrders" stripe>
              <el-table-column prop="orderTime" label="下单时间" width="180" />
              <el-table-column prop="number" label="订单号" />
              <el-table-column label="菜品" min-width="200">
                <template #default="{ row }">
                  <div v-for="d in row.details" :key="d.name" class="order-dish">
                    {{ d.name }} x{{ d.number }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="金额" width="100" align="right">
                <template #default="{ row }">
                  ¥ {{ Number(row.amount || 0).toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 5 ? 'success' : 'warning'" size="small">
                    {{ row.status === 5 ? '已完成' : '进行中' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="orders-pagination"
              background
              layout="prev, pager, next"
              :total="ordersTotal"
              :page-size="10"
              v-model:current-page="ordersPage"
              @current-change="fetchOrders"
            />
            <el-empty v-if="userOrders.length === 0" description="暂无点餐记录" />
          </div>
        </el-tab-pane>

        <!-- Tab 3: 数据分析 -->
        <el-tab-pane label="数据分析" name="analytics">
          <div class="analytics-tab">
            <!-- 二级切换 -->
            <el-radio-group v-model="analyticsTab" class="analytics-switch">
              <el-radio-button label="spend">餐费分析</el-radio-button>
              <el-radio-button label="nutrition">健康分析</el-radio-button>
            </el-radio-group>

            <!-- 餐费分析 -->
            <div v-if="analyticsTab === 'spend' && spendAnalytics" class="spend-section">
              <div class="spend-overview">
                <el-card class="spend-main" shadow="hover">
                  <div class="spend-label">本月已花</div>
                  <div class="spend-value">¥ {{ Number(spendAnalytics.monthSpend || 0).toFixed(2) }}</div>
                  <el-tag :type="spendAnalytics.statusLabel === '消费平稳' ? 'success' : 'warning'">
                    {{ spendAnalytics.statusLabel }}
                  </el-tag>
                </el-card>
                <el-card class="spend-forecast" shadow="hover">
                  <div class="spend-label">预计月末消费</div>
                  <div class="spend-value">¥ {{ Number(spendAnalytics.forecastMonthEnd || 0).toFixed(2) }}</div>
                  <el-progress 
                    :percentage="spendAnalytics.forecastMonthEnd > 0 ? Math.min(100, Math.round((spendAnalytics.monthSpend / spendAnalytics.forecastMonthEnd) * 100)) : 0" 
                    :stroke-width="10"
                  />
                </el-card>
              </div>

              <!-- 消费趋势 -->
              <el-card class="chart-card" shadow="never">
                <template #header>消费趋势</template>
                <div class="chart-placeholder" v-if="spendAnalytics.dailySpendTrend && spendAnalytics.dailySpendTrend.length > 0">
                  <div v-for="item in spendAnalytics.dailySpendTrend" :key="item.date" class="trend-bar">
                    <div class="bar" :style="{ height: Math.max(10, (item.amount || 0) * 2) + 'px' }"></div>
                    <span class="bar-label">{{ item.date.substring(5) }}</span>
                  </div>
                </div>
                <el-empty v-else description="暂无趋势数据" :image-size="60" style="padding: 10px;" />
              </el-card>

              <!-- 消费构成 -->
              <el-card class="chart-card" shadow="never">
                <template #header>消费构成</template>
                <div class="composition-list" v-if="spendAnalytics.spendComposition && spendAnalytics.spendComposition.length > 0">
                  <div v-for="item in spendAnalytics.spendComposition" :key="item.name" class="comp-item">
                    <span class="comp-name">{{ item.name }}</span>
                    <el-progress :percentage="item.percent" :stroke-width="8" />
                    <span class="comp-amount">¥{{ Number(item.amount).toFixed(0) }}</span>
                  </div>
                </div>
                <el-empty v-else description="暂无消费数据" :image-size="60" style="padding: 10px;" />
              </el-card>

              <!-- 助手建议 -->
              <el-alert :title="spendAnalytics.assistantTip" type="info" show-icon :closable="false" />
            </div>

            <!-- 健康分析 -->
            <div v-if="analyticsTab === 'nutrition' && nutritionAnalytics" class="nutrition-section">
              <template v-if="nutritionAnalytics.hasData">
                <div class="nutrition-overview">
                  <el-card shadow="hover">
                    <div class="metric-label">今日摄入</div>
                    <div class="metric-value">{{ nutritionAnalytics.todayActual }} <small>kcal</small></div>
                  </el-card>
                  <el-card shadow="hover">
                    <div class="metric-label">目标摄入</div>
                    <div class="metric-value">{{ nutritionAnalytics.todayTarget }} <small>kcal</small></div>
                  </el-card>
                  <el-card shadow="hover">
                    <div class="metric-label">剩余</div>
                    <div class="metric-value">{{ nutritionAnalytics.todayRemaining }} <small>kcal</small></div>
                  </el-card>
                  <el-card shadow="hover">
                    <div class="metric-label">进度</div>
                    <el-progress type="circle" :percentage="nutritionAnalytics.todayProgress" :width="60" />
                  </el-card>
                </div>

                <!-- 营养结构 -->
                <el-card class="chart-card" shadow="never">
                  <template #header>营养结构</template>
                  <div class="macro-list">
                    <div class="macro-item">
                      <span class="macro-name">碳水化合物</span>
                      <el-progress :percentage="nutritionAnalytics.macroBreakdown?.carbsPct || 0" color="#409EFF" />
                      <span class="macro-value">{{ nutritionAnalytics.macroBreakdown?.carbs || 0 }}g</span>
                    </div>
                    <div class="macro-item">
                      <span class="macro-name">蛋白质</span>
                      <el-progress :percentage="nutritionAnalytics.macroBreakdown?.proteinPct || 0" color="#67C23A" />
                      <span class="macro-value">{{ nutritionAnalytics.macroBreakdown?.protein || 0 }}g</span>
                    </div>
                    <div class="macro-item">
                      <span class="macro-name">脂肪</span>
                      <el-progress :percentage="nutritionAnalytics.macroBreakdown?.fatPct || 0" color="#E6A23C" />
                      <span class="macro-value">{{ nutritionAnalytics.macroBreakdown?.fat || 0 }}g</span>
                    </div>
                  </div>
                </el-card>

                <!-- 热量趋势 -->
                <el-card class="chart-card" shadow="never">
                  <template #header>七日热量趋势</template>
                  <div class="chart-placeholder">
                    <div v-for="item in nutritionAnalytics.caloriesTrend" :key="item.date" class="trend-bar">
                      <div class="bar actual" :style="{ height: Math.max(10, item.actual / 20) + 'px' }"></div>
                      <span class="bar-label">{{ item.day }}</span>
                    </div>
                  </div>
                </el-card>
              </template>

              <!-- 空态 -->
              <el-empty v-else :description="nutritionAnalytics.emptyReason || '暂无分析数据'">
                <template #image>
                  <el-icon :size="60" color="#409EFF"><PieChart /></el-icon>
                </template>
              </el-empty>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>
  </div>
</template>

<style lang="less" scoped>
.user-management {
  padding: 20px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  .stat-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .stat-info {
    display: flex;
    flex-direction: column;
  }
  .stat-label {
    font-size: 12px;
    color: #909399;
    text-transform: uppercase;
  }
  .stat-value {
    font-size: 28px;
    font-weight: 700;
    margin-top: 8px;
  }
  .stat-icon {
    font-size: 32px;
    padding: 12px;
    border-radius: 8px;
    &.primary { background: #ecf5ff; color: #409eff; }
    &.success { background: #f0f9eb; color: #67c23a; }
    &.warning { background: #fdf6ec; color: #e6a23c; }
  }
}

.filter-card {
  margin-bottom: 20px;
  .filter-row {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    align-items: flex-end;
  }
  .filter-item {
    display: flex;
    flex-direction: column;
    gap: 6px;
    label { font-size: 13px; color: #606266; }
    .el-input, .el-select { width: 200px; }
  }
  .filter-actions {
    margin-left: auto;
  }
}

.table-card {
  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .table-title { font-weight: 600; }
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  .user-info {
    display: flex;
    flex-direction: column;
  }
  .user-name { font-weight: 500; }
  .user-id { font-size: 12px; color: #909399; font-family: monospace; }
}

.text-muted { color: #c0c4cc; }

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

// Drawer 样式
.drawer-header {
  display: flex;
  align-items: center;
  gap: 16px;
  .drawer-user-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
  .drawer-user-name {
    font-size: 20px;
    font-weight: 700;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .drawer-user-meta {
    font-size: 13px;
    color: #909399;
  }
}

.metrics-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.metric-card {
  text-align: center;
  .metric-label { font-size: 13px; color: #909399; }
  .metric-value { 
    font-size: 28px; 
    font-weight: 700; 
    margin: 8px 0;
    small { font-size: 14px; color: #909399; }
  }
}

.section-card {
  margin-bottom: 16px;
}

.goal-info {
  display: flex;
  gap: 32px;
  .goal-item {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .goal-label { color: #606266; }
}

.pref-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.pref-group {
  .pref-title { font-size: 12px; color: #909399; display: block; margin-bottom: 8px; }
  .pref-tags { display: flex; flex-wrap: wrap; gap: 8px; }
}

.tip-card {
  background: linear-gradient(135deg, #f0f9eb, #e1f3d8);
  border: 1px solid #c2e7b0;
  .tip-content {
    display: flex;
    gap: 12px;
    align-items: flex-start;
  }
  .tip-icon { font-size: 24px; color: #67c23a; }
  .tip-title { font-weight: 600; color: #67c23a; }
  .tip-text { color: #606266; margin-top: 4px; }
}

.orders-tab {
  .orders-filter { margin-bottom: 16px; }
  .orders-pagination { margin-top: 16px; justify-content: center; }
  .order-dish { font-size: 12px; color: #606266; }
}

.analytics-tab {
  .analytics-switch { margin-bottom: 20px; }
}

.spend-section, .nutrition-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.spend-overview, .nutrition-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.spend-main, .spend-forecast {
  .spend-label { font-size: 12px; color: #909399; }
  .spend-value { font-size: 32px; font-weight: 700; margin: 8px 0; }
}

.chart-card {
  .chart-placeholder {
    display: flex;
    justify-content: space-around;
    align-items: flex-end;
    height: 120px;
    padding: 16px 0;
    border-bottom: 1px solid #ebeef5;
  }
  .trend-bar {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    .bar {
      width: 24px;
      background: #409eff;
      border-radius: 4px 4px 0 0;
      &.actual { background: #67c23a; }
    }
    .bar-label { font-size: 11px; color: #909399; }
  }
}

.composition-list, .macro-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comp-item, .macro-item {
  display: flex;
  align-items: center;
  gap: 12px;
  .comp-name, .macro-name { width: 80px; font-size: 13px; }
  .el-progress { flex: 1; }
  .comp-amount, .macro-value { width: 60px; text-align: right; font-weight: 500; }
}
</style>
