<template>
  <div class="feedback-submit">
    <el-card>
      <template #header>
        <div class="header">
          <span>满意度评价</span>
        </div>
      </template>

      <div v-if="loading">加载中...</div>
      <div v-else-if="eligibleAppointments.length === 0" class="empty">暂无可评价的离校记录</div>
      <el-table v-else :data="eligibleAppointments" border stripe>
        <el-table-column prop="id" label="预约ID" width="110" />
        <el-table-column prop="visitStartTime" label="开始时间" width="170" />
        <el-table-column prop="visitEndTime" label="结束时间" width="170" />
        <el-table-column prop="hostId" label="被访人ID" width="110" />
        <el-table-column prop="areaId" label="区域ID" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">去评价</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="提交评价" width="520px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="审批速度">
          <el-rate v-model="form.approvalSpeed" />
        </el-form-item>
        <el-form-item label="门岗态度">
          <el-rate v-model="form.guardAttitude" />
        </el-form-item>
        <el-form-item label="校园环境">
          <el-rate v-model="form.environment" />
        </el-form-item>
        <el-form-item label="总体满意度">
          <el-rate v-model="form.overall" />
        </el-form-item>
        <el-form-item label="留言">
          <el-input v-model="form.comment" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="匿名展示">
          <el-switch v-model="form.anonymous" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitLoading">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEligibleFeedbackAppointments, submitFeedback } from '../../api/feedback'
import { getMyVisitorAppointments } from '../../api/appointments'

const loading = ref(false)
const submitLoading = ref(false)
const eligibleAppointments = ref<any[]>([])
const dialogVisible = ref(false)
const currentAppointmentId = ref<number | null>(null)

const form = reactive({
  approvalSpeed: 5,
  guardAttitude: 5,
  environment: 5,
  overall: 5,
  comment: '',
  anonymous: true
})

const fetchData = async () => {
  loading.value = true
  try {
    const eligibleRes: any = await getEligibleFeedbackAppointments()
    const eligibleIds: number[] = eligibleRes.data || []
    if (eligibleIds.length === 0) {
      eligibleAppointments.value = []
      return
    }
    const apptRes: any = await getMyVisitorAppointments({ current: 1, size: 100, status: 'CHECKED_OUT' })
    const list: any[] = apptRes.data.records || []
    eligibleAppointments.value = list.filter((a) => eligibleIds.includes(a.id))
  } finally {
    loading.value = false
  }
}

const openDialog = (row: any) => {
  currentAppointmentId.value = row.id
  form.approvalSpeed = 5
  form.guardAttitude = 5
  form.environment = 5
  form.overall = 5
  form.comment = ''
  form.anonymous = true
  dialogVisible.value = true
}

const submit = async () => {
  if (!currentAppointmentId.value) return
  submitLoading.value = true
  try {
    await submitFeedback({
      appointmentId: currentAppointmentId.value,
      approvalSpeed: form.approvalSpeed,
      guardAttitude: form.guardAttitude,
      environment: form.environment,
      overall: form.overall,
      comment: form.comment,
      anonymous: form.anonymous
    })
    ElMessage.success('提交成功')
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
.feedback-submit {
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
</style>

