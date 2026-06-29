<template>
  <el-dialog v-model="visible" title="分配角色" width="520px">
    <el-form label-width="96px">
      <el-form-item label="目标用户">
        <el-input :model-value="username" disabled />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="roleCodes" multiple filterable style="width: 100%" placeholder="请选择角色">
          <el-option v-for="r in roleOptions" :key="r.roleCode" :label="r.roleName" :value="r.roleCode" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

type RoleOption = { roleCode: string; roleName: string }

const props = defineProps<{ modelValue: boolean; username: string; roleOptions: RoleOption[]; initialRoleCodes: string[] }>()
const emit = defineEmits<{ (e: 'update:modelValue', v: boolean): void; (e: 'submit', v: string[]): void }>()

const loading = ref(false)
const roleCodes = ref<string[]>([])

watch(
  () => props.modelValue,
  (v) => {
    if (!v) return
    roleCodes.value = props.initialRoleCodes ? [...props.initialRoleCodes] : []
  }
)

const visible = computed({
  get: () => props.modelValue,
  set: (v: boolean) => emit('update:modelValue', v),
})

const submit = async () => {
  if (!roleCodes.value || roleCodes.value.length === 0) {
    ElMessage.warning('请至少选择一个角色')
    return
  }
  loading.value = true
  try {
    emit('submit', [...roleCodes.value])
  } finally {
    loading.value = false
  }
}
</script>

