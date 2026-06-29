<template>
  <div class="message-list">
    <DataTable
      :data="tableData"
      :loading="loading"
      :total="total"
      v-model:page="page"
      v-model:size="size"
      @refresh="fetchData"
    >
      <template #actions>
        <el-button type="primary" @click="markAll" :loading="markAllLoading">全部已读</el-button>
      </template>

      <el-table-column prop="id" label="编号" width="90" />
      <el-table-column prop="type" label="类型" width="140" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="content" label="内容" min-width="280" show-overflow-tooltip />
      <el-table-column prop="isRead" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isRead ? 'info' : 'warning'">{{ row.isRead ? '已读' : '未读' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="!row.isRead" link type="primary" @click="markRead(row)">标为已读</el-button>
        </template>
      </el-table-column>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import DataTable from '../../components/data-table/DataTable.vue'
import { getMyMessages, markAllRead, markMessageRead } from '../../api/messages'

const loading = ref(false)
const markAllLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getMyMessages({ current: page.value, size: size.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const markRead = async (row: any) => {
  await markMessageRead(row.id)
  ElMessage.success('已标记为已读')
  fetchData()
}

const markAll = async () => {
  markAllLoading.value = true
  try {
    await markAllRead()
    ElMessage.success('已全部标记为已读')
    fetchData()
  } finally {
    markAllLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.message-list {
  width: 100%;
}
</style>
