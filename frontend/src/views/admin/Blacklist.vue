<template>
  <div class="blacklist-page">
    <DataTable
      :data="tableData"
      :loading="loading"
      :total="total"
      v-model:page="page"
      v-model:size="size"
      @refresh="fetchData"
    >
      <template #search>
        <el-input v-model="keyword" placeholder="身份证号" clearable style="width: 220px" @keyup.enter="fetchData" />
        <el-button type="primary" icon="Search" @click="fetchData">查询</el-button>
      </template>

      <template #actions>
        <el-button type="danger" icon="Plus" @click="openAdd">添加黑名单</el-button>
      </template>

      <el-table-column prop="id" label="编号" width="90" />
      <el-table-column prop="visitorIdCard" label="身份证号" min-width="180" />
      <el-table-column prop="visitorName" label="姓名" width="120" />
      <el-table-column prop="reason" label="原因" min-width="240" show-overflow-tooltip />
      <el-table-column prop="expireTime" label="到期时间" width="180">
        <template #default="{ row }">
          <span>{{ row.expireTime || '永久' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" @click="remove(row)">解除</el-button>
        </template>
      </el-table-column>
    </DataTable>

    <el-dialog v-model="dialogVisible" title="添加黑名单" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="身份证号" required>
          <el-input v-model="form.visitorIdCard" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.visitorName" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="form.reason" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="到期时间">
          <el-date-picker
            v-model="form.expireTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="为空表示永久"
            clearable
          />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import DataTable from '../../components/data-table/DataTable.vue'
import { addBlacklist, getBlacklist, removeBlacklist } from '../../api/blacklist'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')

const dialogVisible = ref(false)
const form = reactive({
  visitorIdCard: '',
  visitorName: '',
  reason: '',
  expireTime: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getBlacklist({ current: page.value, size: size.value, idCard: keyword.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  form.visitorIdCard = ''
  form.visitorName = ''
  form.reason = ''
  form.expireTime = ''
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.visitorIdCard) return
  submitLoading.value = true
  try {
    await addBlacklist({
      visitorIdCard: form.visitorIdCard,
      visitorName: form.visitorName,
      reason: form.reason,
      expireTime: form.expireTime || null
    })
    ElMessage.success('已添加')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const remove = async (row: any) => {
  await ElMessageBox.confirm('确认解除该黑名单记录？', '提示', { type: 'warning' })
  await removeBlacklist(row.id)
  ElMessage.success('已解除')
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.blacklist-page {
  width: 100%;
}
</style>
