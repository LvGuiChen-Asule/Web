<template>
  <div class="appointment-calendar">
    <el-card>
      <template #header>
        <div class="header">
          <span>预约日历</span>
          <div class="actions">
            <el-button @click="$router.push('/appointments')">列表视图</el-button>
          </div>
        </div>
      </template>

      <el-calendar v-model="currentDate">
        <template #date-cell="{ data }">
          <div class="cell" @click="openDay(data.day)">
            <div class="cell-top">
              <span class="cell-day">{{ data.day.split('-').slice(2).join('') }}</span>
              <el-tag v-if="dayMap[data.day]?.length" size="small" type="success">{{ dayMap[data.day].length }}</el-tag>
            </div>
            <div class="cell-body" v-if="dayMap[data.day]?.length">
              <div v-for="a in dayMap[data.day].slice(0, 2)" :key="a.id" class="cell-item">
                <span class="cell-item-time">{{ toHHmm(a.visitStartTime) }}</span>
                <span class="cell-item-status">{{ a.status }}</span>
              </div>
              <div v-if="dayMap[data.day].length > 2" class="cell-more">更多...</div>
            </div>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="520px">
      <el-table :data="drawerList" border stripe>
        <el-table-column prop="id" label="编号" width="90" />
        <el-table-column prop="visitStartTime" label="开始时间" width="170" />
        <el-table-column prop="visitEndTime" label="结束时间" width="170" />
        <el-table-column prop="status" label="状态" width="140" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { getCalendarAppointments } from '../../api/appointments'

const currentDate = ref(new Date())
const dayMap = ref<Record<string, any[]>>({})

const drawerVisible = ref(false)
const drawerTitle = ref('')
const drawerList = ref<any[]>([])

const pad2 = (n: number) => String(n).padStart(2, '0')

const formatDateTime = (d: Date) => {
  const y = d.getFullYear()
  const m = pad2(d.getMonth() + 1)
  const dd = pad2(d.getDate())
  const hh = pad2(d.getHours())
  const mm = pad2(d.getMinutes())
  const ss = pad2(d.getSeconds())
  return `${y}-${m}-${dd} ${hh}:${mm}:${ss}`
}

const formatDate = (d: Date) => {
  const y = d.getFullYear()
  const m = pad2(d.getMonth() + 1)
  const dd = pad2(d.getDate())
  return `${y}-${m}-${dd}`
}

const toHHmm = (s: string) => {
  if (!s) return ''
  const parts = s.split(' ')
  if (parts.length < 2) return s
  return parts[1].slice(0, 5)
}

const fetchMonth = async () => {
  const d = currentDate.value
  const start = new Date(d.getFullYear(), d.getMonth(), 1, 0, 0, 0)
  const end = new Date(d.getFullYear(), d.getMonth() + 1, 1, 0, 0, 0)
  const res: any = await getCalendarAppointments({
    startTime: formatDateTime(start),
    endTime: formatDateTime(end)
  })
  const list: any[] = res.data || []
  const map: Record<string, any[]> = {}
  list.forEach((a) => {
    const day = (a.visitStartTime || '').split(' ')[0]
    if (!day) return
    if (!map[day]) map[day] = []
    map[day].push(a)
  })
  Object.keys(map).forEach((k) => {
    map[k].sort((x, y) => String(x.visitStartTime).localeCompare(String(y.visitStartTime)))
  })
  dayMap.value = map
}

const openDay = (day: string) => {
  drawerTitle.value = `当天预约：${day}`
  drawerList.value = dayMap.value[day] || []
  drawerVisible.value = true
}

watch(
  () => currentDate.value,
  () => {
    fetchMonth()
  }
)

onMounted(() => {
  fetchMonth()
})
</script>

<style scoped>
.appointment-calendar {
  width: 100%;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cell {
  min-height: 72px;
  padding: 6px;
  cursor: pointer;
}
.cell-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 6px;
}
.cell-day {
  font-weight: 600;
}
.cell-body {
  margin-top: 6px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.cell-item {
  font-size: 12px;
  display: flex;
  justify-content: space-between;
  gap: 8px;
  color: #606266;
}
.cell-more {
  font-size: 12px;
  color: #909399;
}
</style>

