<template>
  <div class="container">
    <h2 class="chartTitle">销量排名TOP10</h2>
    <div class="charBox">
      <div ref="chartDomRef" style="width: 100%; height: 300px"></div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { markRaw, nextTick, onBeforeUnmount, onMounted, shallowRef, watch } from 'vue'
import * as echarts from 'echarts'

interface Top10Data {
  nameList: string[]
  numberList: number[]
}

const props = defineProps<{
  top10data: Top10Data
}>()

const chartDomRef = shallowRef<HTMLElement | null>(null)
const chartInstance = shallowRef<echarts.ECharts | null>(null)

const createOption = () => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: '#fff',
    borderRadius: 2,
    textStyle: {
      color: '#333',
      fontSize: 12,
      fontWeight: 300,
    },
  },
  grid: {
    top: '5%',
    left: '6%',
    right: '10%',
    bottom: '10%',
    containLabel: true,
  },
  xAxis: {
    show: false,
  },
  yAxis: {
    axisLine: {
      show: false,
    },
    axisTick: {
      show: false,
      alignWithLabel: true,
    },
    type: 'category',
    axisLabel: {
      textStyle: {
        color: '#666',
        fontSize: '12px',
      },
    },
    data: props.top10data.nameList,
  },
  series: [
    {
      data: props.top10data.numberList,
      type: 'bar',
      showBackground: true,
      backgroundStyle: {
        color: '#F3F4F7',
      },
      barWidth: 20,
      barGap: '80%',
      barCategoryGap: '80%',
      itemStyle: {
        emphasis: {
          barBorderRadius: 30,
        },
        normal: {
          barBorderRadius: [0, 10, 10, 0],
          color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
            { offset: 0, color: '#00aaff' },
            { offset: 1, color: '#22ccff' },
          ]),
          label: {
            show: true,
            formatter: '{c}',
            color: '#333',
            position: ['8', '5'],
          },
        },
      },
    },
  ],
})

const renderChart = async () => {
  await nextTick()
  if (!chartDomRef.value) return

  if (!chartInstance.value) {
    chartInstance.value = markRaw(echarts.init(chartDomRef.value))
  } else {
    chartInstance.value.clear()
  }

  chartInstance.value.setOption(createOption(), true)
}

watch(
  () => props.top10data,
  () => {
    void renderChart()
  },
  { deep: true }
)

onMounted(() => {
  void renderChart()
})

onBeforeUnmount(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
})
</script>

<style lang="less" scoped>
.chartTitle {
  font-size: 16px;
  color: #333;
  margin: 10px 20px;
}
</style>
