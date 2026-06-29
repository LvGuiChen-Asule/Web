<template>
  <div ref="chartRef" class="chart-container" :style="{ width: width, height: height }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: '350px'
  },
  title: {
    type: String,
    default: ''
  },
  xAxisData: {
    type: Array,
    default: () => []
  },
  seriesData: {
    type: Array,
    default: () => []
  }
})

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const initChart = () => {
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    setOptions()
  }
}

const setOptions = () => {
  if (!chart) return
  
  chart.setOption({
    title: {
      text: props.title,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: props.xAxisData,
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '来访次数',
        type: 'bar',
        barWidth: '60%',
        data: props.seriesData,
        itemStyle: {
          color: '#52c41a'
        }
      }
    ]
  })
}

watch([() => props.xAxisData, () => props.seriesData], () => {
  setOptions()
})

const handleResize = () => {
  chart?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.chart-container {
  width: 100%;
}
</style>
