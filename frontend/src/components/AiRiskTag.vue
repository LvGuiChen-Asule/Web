<template>
  <el-tag :type="tagType" size="small">
    {{ displayText }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  riskLevel?: string
}>()

const tagType = computed(() => {
  if (props.riskLevel === 'HIGH_RISK') {
    return 'danger'
  } else if (props.riskLevel === 'NORMAL') {
    return 'success'
  } else {
    return 'info'
  }
})

const displayText = computed(() => {
  if (props.riskLevel === 'HIGH_RISK') {
    return '高风险'
  } else if (props.riskLevel === 'NORMAL') {
    return '正常'
  } else {
    return '未知'
  }
})
</script>