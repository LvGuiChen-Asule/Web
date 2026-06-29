<template>
  <el-dialog v-model="visible" :title="mode === 'create' ? '创建用户' : '编辑用户'" width="520px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
      <el-form-item v-if="mode === 'create'" label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item v-if="mode === 'create'" label="初始密码" prop="password">
        <el-input v-model="form.password" type="password" show-password placeholder="请输入初始密码" />
      </el-form-item>
      <el-form-item label="姓名" prop="realName">
        <el-input v-model="form.realName" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="form.idCard" placeholder="请输入身份证号" />
      </el-form-item>
      <el-form-item label="部门ID" prop="deptId">
        <el-input-number v-model="form.deptId" :min="0" style="width: 100%" />
      </el-form-item>
      <el-form-item v-if="mode === 'create'" label="状态" prop="status">
        <el-select v-model="form.status" style="width: 100%">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="mode === 'create'" label="角色" prop="roleCodes">
        <el-select v-model="form.roleCodes" multiple filterable style="width: 100%" placeholder="请选择角色">
          <el-option v-for="r in roleOptions" :key="r.roleCode" :label="r.roleName" :value="r.roleCode" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="onSubmit">
        {{ mode === 'create' ? '创建' : '保存' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

type RoleOption = { roleCode: string; roleName: string }

type EditMode = 'create' | 'edit'

type EditForm = {
  username: string
  password: string
  realName: string
  phone: string
  idCard: string
  deptId?: number
  status: number
  roleCodes: string[]
}

const props = defineProps<{ modelValue: boolean; mode: EditMode; roleOptions: RoleOption[]; initial?: Partial<EditForm> }>()
const emit = defineEmits<{ (e: 'update:modelValue', v: boolean): void; (e: 'submit', v: EditForm): void }>()

const loading = ref(false)
const visible = computed({
  get: () => props.modelValue,
  set: (v: boolean) => emit('update:modelValue', v),
})

const formRef = ref<FormInstance>()
const form = reactive<EditForm>({
  username: '',
  password: '',
  realName: '',
  phone: '',
  idCard: '',
  deptId: undefined,
  status: 1,
  roleCodes: [],
})

watch(
  () => [props.modelValue, props.mode, props.initial],
  () => {
    if (!props.modelValue) return
    form.username = props.initial?.username || ''
    form.password = ''
    form.realName = props.initial?.realName || ''
    form.phone = props.initial?.phone || ''
    form.idCard = props.initial?.idCard || ''
    form.deptId = props.initial?.deptId
    form.status = props.initial?.status ?? 1
    form.roleCodes = props.initial?.roleCodes ? [...props.initial.roleCodes] : []
  },
  { deep: true }
)

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为 3-50 位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 72, message: '密码长度为 6-72 位', trigger: 'blur' },
  ],
  roleCodes: [{ required: true, type: 'array', message: '请选择至少一个角色', trigger: 'change' }],
}

const onSubmit = async () => {
  if (!formRef.value) return
  loading.value = true
  try {
    await formRef.value.validate()
    emit('submit', { ...form, roleCodes: [...form.roleCodes] })
  } finally {
    loading.value = false
  }
}
</script>

