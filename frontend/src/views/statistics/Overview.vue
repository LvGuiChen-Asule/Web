<template>
  <div class="stats-overview">
    <el-card class="mb-20">
      <template #header>
        <div class="card-header">
          <span>访客趋势</span>
          <div class="filter-area">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              :shortcuts="shortcuts"
              @change="fetchTrend"
            />
          </div>
        </div>
      </template>
      <div class="chart-wrapper">
        <LineChart
          :x-axis-data="trendData.dates"
          :series-data="trendData.counts"
          title="每日访客数"
        />
      </div>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>接待人排行</span>
        </div>
      </template>
      <div class="chart-wrapper">
        <BarChart
          :x-axis-data="rankingData.names"
          :series-data="rankingData.counts"
          title="来访次数 Top 10 接待人"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import LineChart from '../../components/charts/LineChart.vue'
import BarChart from '../../components/charts/BarChart.vue'
import { getVisitorTrend, getHostRanking } from '../../api/stats'
import dayjs from 'dayjs'

const dateRange = ref<[string, string]>([
  dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD')
])

const trendData = ref({ dates: [], counts: [] })
const rankingData = ref({ names: [], counts: [] })

const shortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '最近一月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    },
  },
]

const fetchTrend = async () => {
  if (!dateRange.value) return
  const res: any = await getVisitorTrend({
    startDate: `${dateRange.value[0]} 00:00:00`,
    endDate: `${dateRange.value[1]} 23:59:59`
  })
  trendData.value = {
    dates: res.data.map((item: any) => item.date),
    counts: res.data.map((item: any) => item.count)
  }
}

const fetchRanking = async () => {
  const res: any = await getHostRanking(10)
  rankingData.value = {
    names: res.data.map((item: any) => item.hostName),
    counts: res.data.map((item: any) => item.visitCount)
  }
}

onMounted(() => {
  fetchTrend()
  fetchRanking()
})
</script>

<style scoped>
.stats-overview {
  .mb-20 {
    margin-bottom: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .chart-wrapper {
    padding: 10px;
  }
}
</style>
