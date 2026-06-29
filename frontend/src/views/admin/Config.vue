<template>
  <div class="config-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>系统配置</span>
          <el-button type="primary" @click="fetchData" :loading="loading">刷新</el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="configKey" label="配置项" min-width="220" />
        <el-table-column prop="description" label="说明" min-width="260" show-overflow-tooltip />
        <el-table-column prop="configValue" label="配置值" min-width="240" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑配置" width="520px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="配置项">
          <el-input v-model="editForm.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-model="editForm.configValue" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitLoading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listConfigDetailed, updateConfig } from '../../api/config'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<any[]>([])

const dialogVisible = ref(false)
const editForm = reactive({
  configKey: '',
  configValue: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await listConfigDetailed()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const openEdit = (row: any) => {
  editForm.configKey = row.configKey
  editForm.configValue = row.configValue
  dialogVisible.value = true
}

const submit = async () => {
  submitLoading.value = true
  try {
    await updateConfig(editForm.configKey, editForm.configValue)
    ElMessage.success('已更新')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.config-page {
  width: 100%;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
