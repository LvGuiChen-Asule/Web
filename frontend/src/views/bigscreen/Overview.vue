<template>
  <div class="bigscreen">
    <el-row :gutter="16" class="mb-16">
      <el-col :span="8">
        <el-card>
          <div class="kpi">
            <div class="kpi-title">当前在校访客</div>
            <div class="kpi-value">{{ overview.inCampusTotal }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <LineChart :x-axis-data="trendHours" :series-data="trendCounts" title="今日预约趋势（按开始时间小时）" height="260px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="24">
        <el-card>
          <HeatmapChart :x-axis-data="areaNames" :values="areaCounts" title="各区域在校访客分布热力图" height="320px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import LineChart from '../../components/charts/LineChart.vue'
import HeatmapChart from '../../components/charts/HeatmapChart.vue'
import { getBigScreenOverview } from '../../api/bigscreen'

const overview = ref<any>({
  inCampusTotal: 0,
  todayAppointmentTrend: [],
  inCampusByArea: []
})

const trendHours = ref<string[]>([])
const trendCounts = ref<number[]>([])
const areaNames = ref<string[]>([])
const areaCounts = ref<number[]>([])

let timer: number | undefined

const fetchData = async () => {
  const res: any = await getBigScreenOverview()
  overview.value = res.data || {}

  const trend = overview.value.todayAppointmentTrend || []
  trendHours.value = trend.map((t: any) => `${String(t.hour).padStart(2, '0')}:00`)
  trendCounts.value = trend.map((t: any) => Number(t.count || 0))

  const areas = overview.value.inCampusByArea || []
  areaNames.value = areas.map((a: any) => a.areaName || String(a.areaId))
  areaCounts.value = areas.map((a: any) => Number(a.count || 0))
}

onMounted(() => {
  fetchData()
  timer = window.setInterval(fetchData, 5000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
.bigscreen {
  width: 100%;
}
.mb-16 {
  margin-bottom: 16px;
}
.kpi {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 0;
}
.kpi-title {
  font-size: 14px;
  color: #606266;
}
.kpi-value {
  font-size: 40px;
  font-weight: 700;
  line-height: 1;
}
</style>
