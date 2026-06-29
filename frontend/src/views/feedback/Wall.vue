<template>
  <div class="feedback-wall">
    <el-card>
      <template #header>
        <div class="header">
          <span>访客留言墙</span>
          <el-button type="primary" @click="fetchData" :loading="loading">刷新</el-button>
        </div>
      </template>

      <div v-if="loading">加载中...</div>
      <div v-else-if="items.length === 0" class="empty">暂无精选留言</div>
      <el-row v-else :gutter="16">
        <el-col v-for="(it, idx) in items" :key="idx" :span="8">
          <el-card class="item-card">
            <div class="item-title">
              <span>{{ it.displayName }}</span>
              <span class="item-time">{{ it.createTime }}</span>
            </div>
            <div class="rates">
              <div class="rate-row">
                <span class="rate-label">审批速度</span>
                <el-rate v-model="it.approvalSpeed" disabled />
              </div>
              <div class="rate-row">
                <span class="rate-label">门岗态度</span>
                <el-rate v-model="it.guardAttitude" disabled />
              </div>
              <div class="rate-row">
                <span class="rate-label">校园环境</span>
                <el-rate v-model="it.environment" disabled />
              </div>
              <div class="rate-row">
                <span class="rate-label">总体满意度</span>
                <el-rate v-model="it.overall" disabled />
              </div>
            </div>
            <div class="comment">{{ it.comment || '（无留言）' }}</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFeedbackWall } from '../../api/feedback'

const loading = ref(false)
const items = ref<any[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getFeedbackWall({ limit: 30 })
    items.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.feedback-wall {
  width: 100%;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.empty {
  color: #909399;
}
.item-card {
  margin-bottom: 16px;
}
.item-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  margin-bottom: 10px;
}
.item-time {
  font-weight: normal;
  color: #909399;
  font-size: 12px;
}
.rates {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}
.rate-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}
.rate-label {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}
.comment {
  font-size: 13px;
  color: #303133;
  white-space: pre-wrap;
}
</style>

