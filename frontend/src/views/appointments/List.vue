<template>
  <div class="appointment-list">
    <DataTable
      :data="tableData"
      :loading="loading"
      :total="total"
      v-model:page="page"
      v-model:size="size"
      @refresh="fetchData"
    >
      <template #search>
        <div class="filters">
          <el-select v-model="status" placeholder="状态" clearable class="filter-item">
            <el-option value="PENDING" label="待审批">
              <el-tag type="warning">待审批</el-tag>
            </el-option>
            <el-option value="FIRST_APPROVED" label="被访人已通过">
              <el-tag type="warning">被访人已通过</el-tag>
            </el-option>
            <el-option value="APPROVED" label="已通过">
              <el-tag type="success">已通过</el-tag>
            </el-option>
            <el-option value="REJECTED" label="已拒绝">
              <el-tag type="danger">已拒绝</el-tag>
            </el-option>
            <el-option value="CANCELLED" label="已取消">
              <el-tag type="info">已取消</el-tag>
            </el-option>
            <el-option value="CHECKED_IN" label="已入校">
              <el-tag type="info">已入校</el-tag>
            </el-option>
            <el-option value="CHECKED_OUT" label="已离校">
              <el-tag type="info">已离校</el-tag>
            </el-option>
          </el-select>

          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            class="filter-item"
            range-separator="~"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :shortcuts="shortcuts"
            clearable
          />

          <el-input v-model="keyword" class="filter-item" placeholder="请输入访客姓名/手机号/编号搜索" clearable>
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <div class="filter-actions">
            <el-button class="btn-primary" type="primary" @click="onSearch">查询</el-button>
            <el-button class="btn-secondary" @click="onReset">重置</el-button>
          </div>
        </div>
      </template>

      <template #actions>
        <el-button v-if="isVisitor" type="success" icon="Plus" @click="$router.push('/appointments/create')">新建预约</el-button>
        <el-button type="primary" plain @click="$router.push('/appointments/calendar')">日历视图</el-button>
        <el-button v-if="isAdmin" type="primary" @click="handleExport" :loading="exportLoading">导出</el-button>
      </template>

      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="visitorName" label="访客" width="100" />
      <el-table-column prop="visitorPhone" label="手机号" width="130" />
      <el-table-column prop="hostId" label="被访人ID" width="100" />
      <el-table-column prop="areaId" label="区域ID" width="80" />
      <el-table-column prop="visitStartTime" label="开始时间" width="160" />
      <el-table-column prop="visitEndTime" label="结束时间" width="160" />
      <el-table-column prop="reason" label="来访事由" min-width="260" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tooltip :content="getStatusTooltip(row.status)" placement="top">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="aiRiskLevel" label="AI风险等级" width="120">
        <template #default="{ row }">
          <AiRiskTag :risk-level="row.aiRiskLevel" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
          <el-button v-if="canAudit(row)" link type="success" @click="handleAudit(row)">审核</el-button>
        </template>
      </el-table-column>
    </DataTable>

    <el-card v-if="!loading && total === 0" class="empty-card">
      <div class="empty-inner">
        <div class="empty-icon">📭</div>
        <div class="empty-title">暂无访客数据</div>
        <div class="empty-sub">可以调整筛选条件，或创建新的预约</div>
        <el-button v-if="isVisitor" type="primary" @click="$router.push('/appointments/create')">去新增访客</el-button>
      </div>
    </el-card>

    <el-dialog v-model="auditDialogVisible" title="审核预约" width="500px">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.passed">
            <el-radio :label="true">通过</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="auditForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit" :loading="auditLoading">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <el-drawer v-model="detailDrawerVisible" title="访客详情" size="520px">
      <el-descriptions :column="1" border class="detail-desc">
        <el-descriptions-item label="预约编号">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="访客姓名">{{ detailData.visitorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="访客手机号">{{ detailData.visitorPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="访客身份证">{{ maskIdCard(detailData.visitorIdCard) }}</el-descriptions-item>
        <el-descriptions-item label="被访人ID">{{ detailData.hostId }}</el-descriptions-item>
        <el-descriptions-item label="区域ID">{{ detailData.areaId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="来访时间">{{ detailData.visitStartTime }} ~ {{ detailData.visitEndTime }}</el-descriptions-item>
        <el-descriptions-item label="来访事由">{{ detailData.reason }}</el-descriptions-item>
        <el-descriptions-item label="携带物品">
          <div v-if="detailItems.length === 0">无</div>
          <el-table v-else :data="detailItems" border stripe size="small">
            <el-table-column prop="itemName" label="物品" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="note" label="备注" />
          </el-table>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)">{{ getStatusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="二维码" v-if="detailData.status === 'APPROVED' || detailData.status === 'CHECKED_IN' || detailData.status === 'CHECKED_OUT'">
          <div v-if="detailData.qrcodeUrl" class="qr-code-box">
            <img class="qr-image" :src="toQrImg(detailData.qrcodeUrl)" alt="qr" />
            <div class="qr-code-tip">请向门岗出示此码</div>
          </div>
          <div v-else class="qr-code-tip">二维码未生成</div>
          <el-button v-if="isAdmin && detailData.status === 'APPROVED' && !detailData.qrcodeUrl" type="primary" @click="generateQr" :loading="qrLoading">生成二维码</el-button>
          <el-button v-if="detailData.qrcodeUrl" type="primary" plain @click="downloadQr">保存二维码</el-button>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import DataTable from '../../components/data-table/DataTable.vue'
import { getMyHostAppointments, getMyVisitorAppointments, getAdminAuditAppointments, auditByHost, auditByAdmin, exportAppointments } from '../../api/appointments'
import { generateQrCode } from '../../api/qrcode'
import { getAppointmentItems } from '../../api/appointmentItems'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const route = useRoute()

const parseStatusQuery = (val: unknown) => {
  return typeof val === 'string' && val.trim() !== '' ? val : undefined
}

const status = ref<string | undefined>(parseStatusQuery(route.query.status))
const keyword = ref('')
const timeRange = ref<string[]>([])
const shortcuts = [
  {
    text: '今日',
    value: () => {
      const start = dayjs().startOf('day')
      const end = dayjs().endOf('day')
      return [start.toDate(), end.toDate()]
    }
  },
  {
    text: '本周',
    value: () => {
      const start = dayjs().startOf('week')
      const end = dayjs().endOf('week')
      return [start.toDate(), end.toDate()]
    }
  },
  {
    text: '本月',
    value: () => {
      const start = dayjs().startOf('month')
      const end = dayjs().endOf('month')
      return [start.toDate(), end.toDate()]
    }
  }
]

const auditDialogVisible = ref(false)
const auditLoading = ref(false)
const currentRow = ref<any>(null)
const auditForm = ref({
  passed: true,
  remark: ''
})

const detailDrawerVisible = ref(false)
const detailData = ref<any>({})
const detailItems = ref<any[]>([])
const exportLoading = ref(false)
const qrLoading = ref(false)

const user = JSON.parse(localStorage.getItem('user') || 'null')
const roles: string[] = user?.roles || []

const isAdmin = computed(() => roles.includes('ROLE_ADMIN'))
const isHost = computed(() => roles.includes('ROLE_HOST'))
const isVisitor = computed(() => roles.includes('ROLE_VISITOR'))

const fetchData = async () => {
  loading.value = true
  try {
    const startTime = timeRange.value?.[0]
    const endTime = timeRange.value?.[1]
    let res: any
    if (isAdmin.value) {
      res = await getAdminAuditAppointments({ current: page.value, size: size.value, status: status.value, keyword: keyword.value, startTime, endTime })
    } else if (isVisitor.value) {
      res = await getMyVisitorAppointments({ current: page.value, size: size.value, status: status.value, keyword: keyword.value, startTime, endTime })
    } else {
      res = await getMyHostAppointments({ current: page.value, size: size.value, status: status.value, keyword: keyword.value, startTime, endTime })
    }
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const getStatusLabel = (s: string) => {
  const map: any = {
    PENDING: '待审批',
    FIRST_APPROVED: '被访人已通过',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    CANCELLED: '已取消',
    CHECKED_IN: '已入校',
    CHECKED_OUT: '已离校'
  }
  return map[s] || s || '未知'
}

const getStatusType = (s: string) => {
  if (s === 'APPROVED') return 'success'
  if (s === 'REJECTED' || s === 'CANCELLED') return 'danger'
  if (s === 'CHECKED_IN' || s === 'CHECKED_OUT') return 'info'
  return 'warning'
}

const getStatusTooltip = (s: string) => {
  const map: any = {
    PENDING: '待审批：等待被访人审核',
    FIRST_APPROVED: '被访人已通过：等待管理员审批',
    APPROVED: '已通过：可在有效时间内入校',
    REJECTED: '已拒绝：预约未通过',
    CANCELLED: '已取消：访客取消预约',
    CHECKED_IN: '已入校：访客已通过门岗核验进入校园',
    CHECKED_OUT: '已离校：访客已通过门岗核验离开校园'
  }
  return map[s] || '状态说明暂无'
}

const canAudit = (row: any) => {
  if (isHost.value && row.status === 'PENDING') return true
  if (isAdmin.value && row.status === 'FIRST_APPROVED') return true
  return false
}

const handleDetail = (row: any) => {
  detailData.value = row
  detailItems.value = []
  detailDrawerVisible.value = true
  getAppointmentItems(row.id).then((res: any) => {
    detailItems.value = res.data || []
  })
}

const handleAudit = (row: any) => {
  currentRow.value = row
  auditForm.value = { passed: true, remark: '' }
  auditDialogVisible.value = true
}

const submitAudit = async () => {
  auditLoading.value = true
  try {
    const payload = {
      id: currentRow.value.id,
      ...auditForm.value
    }
    if (isHost.value && currentRow.value.status === 'PENDING') {
      await auditByHost(payload)
    } else if (isAdmin.value && currentRow.value.status === 'FIRST_APPROVED') {
      await auditByAdmin(payload)
    }
    ElMessage.success('审核完成')
    auditDialogVisible.value = false
    fetchData()
  } finally {
    auditLoading.value = false
  }
}

const maskIdCard = (s: string) => {
  if (!s) return '-'
  if (s.length <= 8) return s
  return s.slice(0, 4) + '****' + s.slice(-4)
}

const toQrImg = (base64: string) => {
  if (!base64) return ''
  if (base64.startsWith('data:image')) return base64
  return `data:image/png;base64,${base64}`
}

const downloadQr = () => {
  const b64 = detailData.value?.qrcodeUrl
  if (!b64) return
  const raw = b64.startsWith('data:image') ? b64.split(',')[1] : b64
  const bin = atob(raw)
  const bytes = new Uint8Array(bin.length)
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i)
  const blob = new Blob([bytes], { type: 'image/png' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `appointment_${detailData.value.id}_qrcode.png`
  a.click()
  window.URL.revokeObjectURL(url)
}

const generateQr = async () => {
  qrLoading.value = true
  try {
    const res: any = await generateQrCode(detailData.value.id)
    detailData.value.qrcodeUrl = res.data
    ElMessage.success('二维码已生成')
  } finally {
    qrLoading.value = false
  }
}

const handleExport = async () => {
  exportLoading.value = true
  try {
    const res: any = await exportAppointments({ status: status.value })
    const blob = res.data
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'appointments.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
  } finally {
    exportLoading.value = false
  }
}

const onSearch = () => {
  page.value = 1
  fetchData()
}

const onReset = () => {
  status.value = undefined
  keyword.value = ''
  timeRange.value = []
  page.value = 1
  fetchData()
}

onMounted(() => {
  fetchData()
})

watch(
  () => route.query.status,
  (val) => {
    const nextStatus = parseStatusQuery(val)
    if (nextStatus === status.value) return
    status.value = nextStatus
    page.value = 1
    fetchData()
  }
)
</script>

<style scoped>
.filters {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.filter-item {
  width: calc((100% - 32px) / 3);
  min-width: 260px;
}

.filter-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-primary,
.btn-secondary {
  height: 32px;
  border-radius: 6px;
}

.empty-card {
  margin-top: 16px;
  border-radius: 8px;
}

.empty-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 48px;
}

.empty-icon {
  font-size: 40px;
}

.empty-title {
  font-size: 18px;
  font-weight: 700;
  color: #333333;
}

.empty-sub {
  font-size: 12px;
  color: #999999;
}

.detail-desc {
  width: 100%;
}

.qr-code-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.qr-code-box {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.qr-image {
  width: 220px;
  height: 220px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

@media (min-width: 768px) and (max-width: 1200px) {
  .filter-item {
    width: calc((100% - 16px) / 2);
    min-width: 220px;
  }
}

@media (max-width: 768px) {
  .filter-item {
    width: 100%;
    min-width: 0;
  }
}
</style>
