<template>
  <div class="admin-users">
    <el-card class="mb-16">
      <div class="toolbar">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索用户名/姓名/手机号"
          clearable
          style="width: 260px"
          @keyup.enter="onSearch"
        />

        <el-select v-model="filters.roleCode" clearable placeholder="角色" style="width: 200px">
          <el-option v-for="r in roleOptions" :key="r.roleCode" :label="r.roleName" :value="r.roleCode" />
        </el-select>

        <el-select v-model="filters.status" clearable placeholder="状态" style="width: 160px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>

        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="onReset">重置</el-button>

        <div class="spacer" />

        <el-button type="primary" @click="openCreate">创建用户</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column label="角色" min-width="220">
          <template #default="scope">
            <el-tag v-for="code in scope.row.roles" :key="code" class="mr-8" size="small">
              {{ roleNameByCode(code) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" min-width="280" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="primary" @click="openRole(scope.row)">分配角色</el-button>
            <el-button link type="primary" @click="openResetPassword(scope.row)">重置密码</el-button>
            <el-button
              link
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :total="total"
          :page-size="page.size"
          :current-page="page.current"
          :page-sizes="[10, 20, 50]"
          @current-change="(v:number) => { page.current = v; fetchUsers() }"
          @size-change="(v:number) => { page.size = v; page.current = 1; fetchUsers() }"
        />
      </div>
    </el-card>

    <UserEditDialog
      v-model="editVisible"
      :mode="editMode"
      :role-options="roleOptions"
      :initial="editInitial"
      @submit="handleEditSubmit"
    />

    <UserRolesDialog
      v-model="rolesVisible"
      :username="selectedUser?.username || ''"
      :role-options="roleOptions"
      :initial-role-codes="selectedUser?.roles || []"
      @submit="handleRolesSubmit"
    />

    <UserResetPasswordDialog
      v-model="pwdVisible"
      :username="selectedUser?.username || ''"
      @submit="handleResetPasswordSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminUser,
  listAdminRoles,
  pageAdminUsers,
  resetAdminUserPassword,
  setAdminUserStatus,
  updateAdminUser,
  updateAdminUserRoles,
  type AdminUser,
  type SysRole,
} from '../../api/adminUsers'
import UserEditDialog from './components/UserEditDialog.vue'
import UserRolesDialog from './components/UserRolesDialog.vue'
import UserResetPasswordDialog from './components/UserResetPasswordDialog.vue'

const loading = ref(false)
const tableData = ref<AdminUser[]>([])
const total = ref(0)

const page = reactive({
  current: 1,
  size: 10,
})

const filters = reactive<{ keyword: string; status?: number; roleCode?: string }>({
  keyword: '',
  status: undefined,
  roleCode: undefined,
})

const roleOptions = ref<SysRole[]>([])
const roleNameByCode = (code: string) => {
  const hit = roleOptions.value.find((r) => r.roleCode === code)
  return hit ? hit.roleName : code
}

const fetchRoles = async () => {
  const res: any = await listAdminRoles()
  roleOptions.value = res.data || []
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const params: any = {
      current: page.current,
      size: page.size,
    }
    if (filters.keyword.trim()) params.keyword = filters.keyword.trim()
    if (filters.status !== undefined && filters.status !== null) params.status = filters.status
    if (filters.roleCode) params.roleCode = filters.roleCode

    const res: any = await pageAdminUsers(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  page.current = 1
  fetchUsers()
}

const onReset = () => {
  filters.keyword = ''
  filters.status = undefined
  filters.roleCode = undefined
  page.current = 1
  fetchUsers()
}

const selectedUser = ref<AdminUser | null>(null)

const editVisible = ref(false)
const editMode = ref<'create' | 'edit'>('create')
const editInitial = ref<any>({})

const rolesVisible = ref(false)
const pwdVisible = ref(false)

const openCreate = () => {
  editMode.value = 'create'
  editInitial.value = { status: 1, roleCodes: [] }
  editVisible.value = true
}

const openEdit = (row: AdminUser) => {
  selectedUser.value = row
  editMode.value = 'edit'
  editInitial.value = {
    username: row.username,
    realName: row.realName || '',
    phone: row.phone || '',
    idCard: row.idCard || '',
    deptId: row.deptId,
    status: row.status,
    roleCodes: row.roles || [],
  }
  editVisible.value = true
}

const openRole = (row: AdminUser) => {
  selectedUser.value = row
  rolesVisible.value = true
}

const openResetPassword = (row: AdminUser) => {
  selectedUser.value = row
  pwdVisible.value = true
}

const handleEditSubmit = async (form: any) => {
  if (editMode.value === 'create') {
    await createAdminUser({
      username: String(form.username || '').trim(),
      password: form.password,
      realName: form.realName || undefined,
      phone: form.phone || undefined,
      idCard: form.idCard || undefined,
      deptId: form.deptId,
      status: form.status,
      roleCodes: form.roleCodes,
    })
    ElMessage.success('创建成功')
  } else {
    if (!selectedUser.value) return
    await updateAdminUser(selectedUser.value.id, {
      realName: form.realName || undefined,
      phone: form.phone || undefined,
      idCard: form.idCard || undefined,
      deptId: form.deptId,
    })
    ElMessage.success('保存成功')
  }
  editVisible.value = false
  fetchUsers()
}

const handleRolesSubmit = async (codes: string[]) => {
  if (!selectedUser.value) return
  await updateAdminUserRoles(selectedUser.value.id, codes)
  ElMessage.success('角色已更新')
  rolesVisible.value = false
  fetchUsers()
}

const handleResetPasswordSubmit = async (newPassword: string) => {
  if (!selectedUser.value) return
  await resetAdminUserPassword(selectedUser.value.id, newPassword)
  ElMessage.success('密码已重置')
  pwdVisible.value = false
}

const toggleStatus = async (row: AdminUser) => {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确认要${actionText}用户“${row.username}”吗？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: nextStatus === 1 ? 'success' : 'warning',
  })
  await setAdminUserStatus(row.id, nextStatus)
  ElMessage.success(`用户已${actionText}`)
  fetchUsers()
}

onMounted(async () => {
  await fetchRoles()
  await fetchUsers()
})
</script>

<style scoped>
.admin-users {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mb-16 {
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.spacer {
  flex: 1;
}

.mr-8 {
  margin-right: 8px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
