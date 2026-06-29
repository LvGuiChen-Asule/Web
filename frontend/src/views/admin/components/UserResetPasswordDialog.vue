<template>
  <el-dialog v-model="visible" title="重置密码" width="520px">
    <el-form ref="formRef" :model="model" :rules="rules" label-width="96px">
      <el-form-item label="目标用户">
        <el-input :model-value="username" disabled />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="model.newPassword" type="password" show-password placeholder="请输入新密码" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="model.confirmPassword" type="password" show-password placeholder="请再次输入" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">确认重置</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

const props = defineProps<{ modelValue: boolean; username: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', v: boolean): void; (e: 'submit', v: string): void }>()

const visible = computed({
  get: () => props.modelValue,
  set: (v: boolean) => emit('update:modelValue', v),
})

const model = reactive({
  newPassword: '',
  confirmPassword: '',
})

watch(
  () => props.modelValue,
  (v) => {
    if (!v) return
    model.newPassword = ''
    model.confirmPassword = ''
  }
)

const formRef = ref<FormInstance>()
const loading = ref(false)

const rules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 72, message: '密码长度为 6-72 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== model.newPassword) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

const submit = async () => {
  if (!formRef.value) return
  loading.value = true
  try {
    await formRef.value.validate()
    emit('submit', model.newPassword)
  } finally {
    loading.value = false
  }
}
</script>

