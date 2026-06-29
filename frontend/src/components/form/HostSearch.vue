<template>
  <el-select
    v-model="modelValue"
    filterable
    remote
    reserve-keyword
    placeholder="搜索接待人（姓名/手机号）"
    :remote-method="remoteMethod"
    :loading="loading"
    style="width: 100%"
    @change="handleChange"
  >
    <el-option
      v-for="item in options"
      :key="item.id"
      :label="`${item.realName} (${item.phone})`"
      :value="item.id"
    />
  </el-select>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { searchHosts } from '../../api/appointments'

const props = defineProps(['modelValue'])
const emit = defineEmits(['update:modelValue', 'change'])

const loading = ref(false)
const options = ref<any[]>([])
const modelValue = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  modelValue.value = val
})

const remoteMethod = async (query: string) => {
  if (query) {
    loading.value = true
    try {
      const res: any = await searchHosts(query)
      options.value = res.data
    } finally {
      loading.value = false
    }
  } else {
    options.value = []
  }
}

const handleChange = (val: any) => {
  emit('update:modelValue', val)
  const selected = options.value.find(item => item.id === val)
  emit('change', selected)
}
</script>
