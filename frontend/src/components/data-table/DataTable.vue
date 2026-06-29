<template>
  <div class="data-table-container">
    <div class="search-card" v-if="$slots.search || $slots.actions">
      <div class="search-area">
        <slot name="search"></slot>
      </div>
      <div class="action-area">
        <slot name="actions"></slot>
      </div>
    </div>

    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="data"
        style="width: 100%"
        border
        stripe
        table-layout="auto"
        @selection-change="handleSelectionChange"
        :header-cell-style="{ background: 'var(--app-header-bg)', height: '48px', color: 'var(--app-text-1)' }"
        :row-style="{ height: '48px' }"
      >
        <el-table-column v-if="selectable" type="selection" width="55" />
        <slot></slot>
      </el-table>
    </div>

    <div class="pagination-container" v-if="showPagination">
      <div class="pagination-card">
        <div class="pagination-info">
          共 {{ total }} 条数据，当前第 {{ currentPage }}/{{ totalPages }} 页
        </div>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  total: {
    type: Number,
    default: 0
  },
  page: {
    type: Number,
    default: 1
  },
  size: {
    type: Number,
    default: 10
  },
  selectable: {
    type: Boolean,
    default: false
  },
  showPagination: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:page', 'update:size', 'refresh', 'selection-change'])

const currentPage = computed({
  get: () => props.page,
  set: (val) => emit('update:page', val)
})

const pageSize = computed({
  get: () => props.size,
  set: (val) => emit('update:size', val)
})

const totalPages = computed(() => {
  const s = pageSize.value || 10
  return Math.max(1, Math.ceil((props.total || 0) / s))
})

const handleSizeChange = (val: number) => {
  emit('update:size', val)
  emit('refresh')
}

const handleCurrentChange = (val: number) => {
  emit('update:page', val)
  emit('refresh')
}

const handleSelectionChange = (val: any[]) => {
  emit('selection-change', val)
}
</script>

<style scoped lang="scss">
.data-table-container {
  width: 100%;

  .search-card {
    background: var(--app-card);
    border: 0.5px solid var(--app-border);
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    display: flex;
    justify-content: space-between;
    gap: 16px;
    flex-wrap: wrap;
    margin-bottom: 16px;
  }

  .search-area {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    align-items: center;
    flex: 1;
  }

  .action-area {
    display: flex;
    gap: 12px;
    align-items: center;
  }

  .table-card {
    background: var(--app-card);
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    overflow: hidden;
  }

  :deep(.el-table__row:hover > td) {
    background-color: var(--app-primary-bg) !important;
  }

  :deep(.el-table__body tr:nth-child(even) > td) {
    background-color: #fdfdfe;
  }

  .pagination-container {
    margin-top: 16px;
  }

  .pagination-card {
    background: var(--app-card);
    border-radius: 8px;
    padding: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
  }

  .pagination-info {
    font-size: 12px;
    color: var(--app-text-2);
  }
}

@media (max-width: 768px) {
  .data-table-container {
    .table-card {
      overflow-x: auto;
    }

    .pagination-card {
      flex-direction: column;
      align-items: flex-start;
    }
  }
}
</style>
