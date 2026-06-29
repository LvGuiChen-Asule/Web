<template>
  <div class="feedback-admin">
    <DataTable
      :data="tableData"
      :loading="loading"
      :total="total"
      v-model:page="page"
      v-model:size="size"
      @refresh="fetchData"
    >
      <template #search>
        <el-select v-model="featured" placeholder="精选" clearable style="width: 140px" @change="fetchData">
          <el-option label="已精选" :value="true" />
          <el-option label="未精选" :value="false" />
        </el-select>
        <el-button type="primary" icon="Search" @click="fetchData">查询</el-button>
      </template>

      <el-table-column prop="id" label="编号" width="90" />
      <el-table-column prop="appointmentId" label="预约ID" width="110" />
      <el-table-column prop="visitorName" label="访客" width="120" />
      <el-table-column prop="overall" label="总体" width="90" />
      <el-table-column prop="comment" label="留言" min-width="260" show-overflow-tooltip />
      <el-table-column prop="isAnonymous" label="匿名" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isAnonymous ? 'info' : 'success'">{{ row.isAnonymous ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isFeatured" label="精选" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isFeatured ? 'success' : 'info'">{{ row.isFeatured ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="toggle(row)" :loading="row._loading">
            {{ row.isFeatured ? '取消精选' : '设为精选' }}
          </el-button>
        </template>
      </el-table-column>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import DataTable from '../../components/data-table/DataTable.vue'
import { getFeedbackList, setFeedbackFeatured } from '../../api/feedback'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const featured = ref<boolean | undefined>(undefined)

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getFeedbackList({ current: page.value, size: size.value, featured: featured.value })
    tableData.value = (res.data.records || []).map((r: any) => ({ ...r, _loading: false }))
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const toggle = async (row: any) => {
  row._loading = true
  try {
    await setFeedbackFeatured(row.id, !row.isFeatured)
    row.isFeatured = !row.isFeatured
  } finally {
    row._loading = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.feedback-admin {
  width: 100%;
}
</style>

