<template>
  <div ref="chartRef" class="chart-container" :style="{ width: width, height: height }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  width: { type: String, default: '100%' },
  height: { type: String, default: '300px' },
  title: { type: String, default: '' },
  xAxisData: { type: Array, default: () => [] },
  values: { type: Array, default: () => [] }
})

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const initChart = () => {
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  setOptions()
}

const setOptions = () => {
  if (!chart) return
  const x = props.xAxisData as any[]
  const v = props.values as any[]
  const data = x.map((_, idx) => [idx, 0, Number(v[idx] || 0)])
  const max = Math.max(0, ...data.map((d) => d[2]))

  chart.setOption({
    title: { text: props.title, left: 'center' },
    tooltip: {
      position: 'top',
      formatter: (p: any) => `${x[p.data[0]]}：${p.data[2]}`
    },
    grid: { top: 50, left: 60, right: 20, bottom: 30, containLabel: true },
    xAxis: { type: 'category', data: x, axisLabel: { rotate: 30 } },
    yAxis: { type: 'category', data: ['区域'] },
    visualMap: {
      min: 0,
      max,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 0
    },
    series: [
      {
        type: 'heatmap',
        data,
        label: { show: true },
        emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } }
      }
    ]
  })
}

watch([() => props.xAxisData, () => props.values], () => setOptions())

const handleResize = () => chart?.resize()

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
