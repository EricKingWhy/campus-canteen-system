<template>
  <div class="container">
    <h2 class="chartTitle">营业额统计</h2>
    <div class="charBox">
      <div ref="chartDomRef" style="width: 100%; height: 320px"></div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { markRaw, nextTick, onBeforeUnmount, onMounted, shallowRef, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  turnoverdata: {
    dateList: string[]
    turnoverList: number[]
  }
}>()

const chartDomRef = shallowRef<HTMLElement | null>(null)
const chartInstance = shallowRef<echarts.ECharts | null>(null)

const createOption = () => {
  const isSingleDay = props.turnoverdata.dateList.length <= 1
  const singleValue = props.turnoverdata.turnoverList[0] ?? 0

  return {
    tooltip: {
      trigger: 'axis',
    },
    grid: {
      top: '15%',
      left: '6%',
      right: '10%',
      bottom: '12%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisLabel: {
        textStyle: {
          color: '#666',
          fontSize: '12px',
        },
      },
      axisLine: {
        lineStyle: {
          color: '#E5E4E4',
          width: 1,
        },
      },
      data: props.turnoverdata.dateList,
    },
    yAxis: {
      type: 'value',
      min: 0,
      axisLabel: {
        textStyle: {
          color: '#666',
          fontSize: '12px',
        },
      },
    },
    legend: {
      data: ['营业额（元）'],
      bottom: '4%',
      icon: 'rect',
      itemWidth: 20,
      itemHeight: 2,
      textStyle: {
        fontSize: 12,
        color: '#666',
      },
    },
    series: [
      {
        name: '营业额（元）',
        type: 'line',
        smooth: false,
        showSymbol: isSingleDay,
        symbol: 'circle',
        symbolSize: 10,
        lineStyle: {
          width: 2,
        },
        itemStyle: {
          normal: {
            color: '#00ccff',
            lineStyle: {
              color: '#00ccff',
            },
          },
          emphasis: {
            color: '#fff',
            borderWidth: 5,
            borderColor: '#00ccff',
          },
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: 'rgba(0, 221, 255, 1)',
            },
            {
              offset: 1,
              color: 'rgba(0, 221, 255, 0)',
            },
          ]),
        },
        markLine: isSingleDay
          ? {
              symbol: 'none',
              silent: true,
              label: { show: false },
              lineStyle: {
                color: '#00ccff',
                width: 1.5,
                type: 'solid',
                opacity: 0.45,
              },
              data: [{ yAxis: singleValue }],
            }
          : undefined,
        data: props.turnoverdata.turnoverList,
      },
    ],
  }
}

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
  () => props.turnoverdata,
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

.legendLine {
  display: flex;
  justify-content: center;
  margin-top: 10px;

  li {
    position: relative;
    padding-left: 10px;
  }

  li::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 5px;
    height: 2px;
    background-color: red;
  }
}
</style>
