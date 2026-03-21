<script setup lang="ts">
import TurnoverStatistics from './components/TurnoverStatistics.vue'
import UserStatistics from './components/UserStatistics.vue'
import OrderStatistics from './components/OrderStatistics.vue'
import Top from './components/Top10.vue'

import { onMounted, ref } from 'vue'
import {
  getTurnoverStatisticsAPI,
  getUserStatisticsAPI,
  getOrderStatisticsAPI,
  getTop10StatisticsAPI,
  exportInforAPI,
} from '@/api/statistics'
import { ElMessage, ElMessageBox } from 'element-plus'

interface TurnoverData {
  dateList: string[]
  turnoverList: number[]
}
interface UserData {
  dateList: string[]
  totalUserList: number[]
  newUserList: number[]
}
interface OrderData {
  orderCompletionRate: number
  validOrderCount: number
  totalOrderCount: number
  data: {
    dateList: string[]
    orderCountList: number[]
    validOrderCountList: number[]
  }
}
interface Top10Data {
  nameList: string[]
  numberList: number[]
}

const overviewData = ref({})
const tateData = ref<string[]>([])
const beginTime = ref('')
const endTime = ref('')
const nowIndex = ref(0)
const tabsParam = ['昨日', '近7日', '近30日', '本周', '本月']

const turnoverData = ref<TurnoverData>({
  dateList: [],
  turnoverList: [],
})
const userData = ref<UserData>({
  dateList: [],
  totalUserList: [],
  newUserList: [],
})
const orderData = ref<OrderData>({
  orderCompletionRate: 0,
  validOrderCount: 0,
  totalOrderCount: 0,
  data: {
    dateList: [],
    orderCountList: [],
    validOrderCountList: [],
  },
})
const top10Data = ref<Top10Data>({
  nameList: [],
  numberList: [],
})

const DAY_MS = 24 * 60 * 60 * 1000
const pad = (num: number) => String(num).padStart(2, '0')
const formatDate = (date: Date) => {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}
const formatDateTime = (date: Date) => {
  return `${formatDate(date)} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}
const startOfDay = (date: Date) => new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0)
const endOfDay = (date: Date) => new Date(date.getFullYear(), date.getMonth(), date.getDate(), 23, 59, 59)

const getRangeByTab = (tabType: number) => {
  const now = new Date()
  const todayStart = startOfDay(now)
  const todayEnd = endOfDay(now)

  switch (tabType) {
    case 1: {
      const yesterday = new Date(todayStart.getTime() - DAY_MS)
      return {
        beginDate: startOfDay(yesterday),
        endDate: endOfDay(yesterday),
      }
    }
    case 2:
      return {
        beginDate: new Date(todayStart.getTime() - 7 * DAY_MS),
        endDate: new Date(todayStart.getTime() - 1000),
      }
    case 3:
      return {
        beginDate: new Date(todayStart.getTime() - 30 * DAY_MS),
        endDate: new Date(todayStart.getTime() - 1000),
      }
    case 4: {
      const day = now.getDay()
      const mondayOffset = day === 0 ? 6 : day - 1
      const weekStart = new Date(todayStart.getTime() - mondayOffset * DAY_MS)
      return {
        beginDate: weekStart,
        endDate: todayEnd,
      }
    }
    case 5: {
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0)
      return {
        beginDate: monthStart,
        endDate: todayEnd,
      }
    }
    default:
      return {
        beginDate: new Date(todayStart.getTime() - 7 * DAY_MS),
        endDate: new Date(todayStart.getTime() - 1000),
      }
  }
}

const init = (begin: string, end: string) => {
  getTurnoverStatisticsData(begin, end)
  getUserStatisticsData(begin, end)
  getOrderStatisticsData(begin, end)
  getTopData(begin, end)
}

const parseStringCsv = (raw: unknown) => {
  if (typeof raw !== 'string') return []
  return raw
    .split(',')
    .map((item) => item.trim())
    .filter((item) => item.length > 0)
}

const parseNumberCsv = (raw: unknown) => {
  return parseStringCsv(raw).map((item) => {
    const num = Number(item)
    return Number.isFinite(num) ? num : 0
  })
}

const getTurnoverStatisticsData = async (begin: string, end: string) => {
  const { data } = await getTurnoverStatisticsAPI({ begin, end })
  turnoverData.value = {
    dateList: parseStringCsv(data?.data?.dateList),
    turnoverList: parseNumberCsv(data?.data?.turnoverList),
  }
}

const getUserStatisticsData = async (begin: string, end: string) => {
  const { data: res } = await getUserStatisticsAPI({ begin, end })
  userData.value = {
    dateList: parseStringCsv(res?.data?.dateList),
    totalUserList: parseNumberCsv(res?.data?.totalUserList),
    newUserList: parseNumberCsv(res?.data?.newUserList),
  }
}

const getOrderStatisticsData = async (begin: string, end: string) => {
  const { data: res } = await getOrderStatisticsAPI({ begin, end })
  orderData.value = {
    data: {
      dateList: parseStringCsv(res?.data?.dateList),
      orderCountList: parseNumberCsv(res?.data?.orderCountList),
      validOrderCountList: parseNumberCsv(res?.data?.validOrderCountList),
    },
    totalOrderCount: res.data.totalOrderCount,
    validOrderCount: res.data.validOrderCount,
    orderCompletionRate: res.data.orderCompletionRate,
  }
}

const getTopData = async (begin: string, end: string) => {
  const { data: res } = await getTop10StatisticsAPI({ begin, end })
  top10Data.value = {
    // 后端已按销量降序返回，前端不再 reverse，避免顺序颠倒
    nameList: parseStringCsv(res?.data?.nameList),
    numberList: parseNumberCsv(res?.data?.numberList),
  }
}

const getTitleNum = (tabType: number) => {
  const { beginDate, endDate } = getRangeByTab(tabType)

  beginTime.value = formatDateTime(beginDate)
  endTime.value = formatDateTime(endDate)
  tateData.value = [formatDate(beginDate), formatDate(endDate)]

  // 后端接口当前使用 yyyy-MM-dd 参数，内部状态仍保留精确到秒的 beginTime/endTime
  init(tateData.value[0], tateData.value[1])
}

const toggleTabs = (index: number) => {
  nowIndex.value = index
  getTitleNum(index + 1)
}

const handleExport = async () => {
  try {
    const confirm = await ElMessageBox.confirm('是否导出最近30天运营数据?', '导出数据', {
      confirmButtonText: 'OK',
      cancelButtonText: 'Cancel',
      type: 'warning',
    })

    if (confirm) {
      const { data } = await exportInforAPI()
      const url = window.URL.createObjectURL(data)
      const a = document.createElement('a')
      document.body.appendChild(a)
      a.href = url
      a.download = '运营数据统计报表.xlsx'
      a.click()
      window.URL.revokeObjectURL(url)
      ElMessage({
        type: 'success',
        message: '导出成功',
      })
    }
  } catch (error) {
    if (error === 'cancel') {
      ElMessage({
        type: 'info',
        message: '取消导出',
      })
    } else {
      console.error('导出失败:', error)
      ElMessage({
        type: 'error',
        message: '导出失败',
      })
    }
  }
}

onMounted(() => {
  getTitleNum(nowIndex.value + 1)
})
</script>

<template>
  <div class="title-index">
    <div class="tab-change">
      <div
        class="tab-item"
        v-for="(item, index) in tabsParam"
        @click="toggleTabs(index)"
        :class="{ active: index === nowIndex }"
        :key="index"
      >
        <div class="item">{{ item }}</div>
      </div>
      <div class="get-time">
        <p> 已选时间：{{ tateData[0] }} 至 {{ tateData[tateData.length - 1] }} </p>
      </div>
    </div>
    <el-button type="success" @click="handleExport">数据导出</el-button>
  </div>
  <div class="page">
    <el-row :gutter="20">
      <div class="turnover">
        <TurnoverStatistics :turnoverdata="turnoverData" />
      </div>
      <div class="user">
        <UserStatistics :userdata="userData" />
      </div>
    </el-row>
    <el-row :gutter="20">
      <div class="order">
        <OrderStatistics :orderdata="orderData" :overviewData="overviewData" />
      </div>
      <div class="top10">
        <Top :top10data="top10Data" />
      </div>
    </el-row>
  </div>
</template>

<style lang="less" scoped>
.page {
  margin: 20px;
  padding: 0;
  background-color: #e9f5ff;
}

.title-index {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 30px 0 20px;

  .tab-change {
    display: flex;
    border-radius: 4px;

    .tab-item {
      width: 100px;
      height: 40px;
      text-align: center;
      line-height: 40px;
      color: #333;
      border: 1px solid #e5e4e4;
      background-color: white;
      border-left: none;
      cursor: pointer;

      .special-item {
        .el-badge__content {
          width: 20px;
          padding: 0 5px;
        }
      }
    }

    .get-time {
      width: 300px;
      height: 40px;
      line-height: 40px;
      text-align: center;
      align-items: center;
      font-size: 14px;
      color: #333;
    }

    .active {
      background-color: #22ccff;
      font-weight: bold;
    }

    .tab-item:first-child {
      border-left: 1px solid #e5e4e4;
    }
  }
}

.el-select {
  margin: 20px;
  width: 100px;
  float: right;
  right: 40px;
}

.turnover {
  display: inline-block;
  width: 48%;
  height: 440px;
  margin: 10px;
  padding: 20px;
  background-color: #fff;
  border-radius: 10px;
}

.user {
  display: inline-block;
  width: 48%;
  height: 440px;
  margin: 10px;
  padding: 20px;
  background-color: #fff;
  border-radius: 10px;
  vertical-align: top;
}

.order {
  display: inline-block;
  width: 48%;
  height: 450px;
  margin: 10px;
  padding: 20px;
  background-color: #fff;
  border-radius: 10px;
}

.top10 {
  display: inline-block;
  width: 48%;
  height: 450px;
  margin: 10px;
  padding: 20px;
  background-color: #fff;
  border-radius: 10px;
  vertical-align: top;
}
</style>

<style>
.my-card {
  margin: 20px;
  padding: 20px;
  border-radius: 10px;
}

.pagination {
  justify-content: center;
}
</style>
