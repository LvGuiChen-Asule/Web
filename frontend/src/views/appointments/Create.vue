<template>
  <div class="create-appointment">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>新建预约申请</span>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" class="form-content">
        <el-form-item label="接待人" prop="hostId">
          <HostSearch v-model="form.hostId" @change="handleHostChange" />
        </el-form-item>

        <el-form-item label="访问区域" prop="areaId">
          <el-select v-model="form.areaId" placeholder="请选择区域（可选）" clearable style="width: 100%">
            <el-option v-for="a in areas" :key="a.id" :label="a.areaName" :value="a.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="来访时间" prop="visitTime">
          <el-date-picker
            v-model="form.visitTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
            @change="handleTimeChange"
          />
        </el-form-item>

        <el-form-item label="来访事由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="携带物品">
          <div class="items">
            <div v-for="(it, idx) in form.items" :key="idx" class="item-row">
              <el-input v-model="it.itemName" placeholder="物品名称" class="item-name" />
              <el-input-number v-model="it.quantity" :min="1" :max="99" class="item-qty" />
              <el-input v-model="it.note" placeholder="备注" class="item-note" />
              <el-button type="danger" plain @click="removeItem(idx)">删除</el-button>
            </div>
            <el-button type="primary" plain @click="addItem">添加物品</el-button>
          </div>
        </el-form-item>

        <el-form-item label="车牌号" prop="licensePlate">
          <el-input v-model="form.licensePlate" placeholder="选填（用于车辆登记）" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submitForm">提交申请</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import HostSearch from '../../components/form/HostSearch.vue'
import { createAppointment } from '../../api/appointments'
import { listAreas } from '../../api/area'
import { upsertVehicle } from '../../api/vehicle'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const areas = ref<any[]>([])

const form = reactive({
  hostId: undefined,
  areaId: undefined,
  visitTime: [],
  visitStartTime: '',
  visitEndTime: '',
  reason: '',
  items: [] as Array<{ itemName: string; quantity: number; note: string }>,
  licensePlate: ''
})

const rules = {
  hostId: [{ required: true, message: '请选择接待人', trigger: 'change' }],
  visitTime: [{ required: true, message: '请选择来访时间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入来访事由', trigger: 'blur' }]
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7
}

const handleHostChange = (host: any) => {
  console.log('Selected host:', host)
}

const handleTimeChange = (val: any) => {
  if (val && val.length === 2) {
    form.visitStartTime = val[0]
    form.visitEndTime = val[1]
  } else {
    form.visitStartTime = ''
    form.visitEndTime = ''
  }
}

const addItem = () => {
  form.items.push({ itemName: '', quantity: 1, note: '' })
}

const removeItem = (idx: number) => {
  form.items.splice(idx, 1)
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        const res: any = await createAppointment({
          hostId: form.hostId,
          areaId: form.areaId,
          visitStartTime: form.visitStartTime,
          visitEndTime: form.visitEndTime,
          reason: form.reason,
          items: form.items.filter((i) => i.itemName && i.itemName.trim() !== '').map((i) => ({
            itemName: i.itemName,
            quantity: i.quantity,
            note: i.note
          }))
        })
        const appointmentId = res.data
        if (form.licensePlate && appointmentId) {
          await upsertVehicle({ appointmentId, licensePlate: form.licensePlate })
        }
        ElMessage.success('预约提交成功')
        router.push('/appointments')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(async () => {
  const res: any = await listAreas()
  areas.value = res.data || []
})
</script>

<style scoped>
.create-appointment {
  max-width: 800px;
  margin: 0 auto;
}
.items {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.item-row {
  display: grid;
  grid-template-columns: 1.4fr 0.7fr 1.4fr auto;
  gap: 10px;
  align-items: center;
}
.item-name {
  width: 100%;
}
.item-qty {
  width: 100%;
}
.item-note {
  width: 100%;
}
</style>
