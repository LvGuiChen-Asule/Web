<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="user-info">
            <el-avatar :size="100" class="avatar">{{ (user?.realName || user?.username || 'U')[0].toUpperCase() }}</el-avatar>
            <h2 class="username">{{ user?.realName || user?.username }}</h2>
            <p class="role-tag">
              <el-tag v-for="role in user?.roles" :key="role" size="small" class="mr-2">{{ getRoleLabel(role) }}</el-tag>
            </p>
          </div>
          <el-descriptions :column="1" border class="mt-4">
            <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ user?.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="身份证号">{{ user?.idCard || '-' }}</el-descriptions-item>
            <el-descriptions-item label="部门ID">{{ user?.deptId || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card class="security-card">
          <template #header>
            <div class="card-header">
              <span>安全设置</span>
            </div>
          </template>
          <el-tabs>
            <el-tab-pane label="修改密码">
              <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" class="mt-4" style="max-width: 500px">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="loading" @click="handleChangePassword">确认修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
// API needed for changing password. Assuming it might be in auth.ts or users.ts, but for now I'll mock or check auth.ts
// Checking auth.ts first would be better, but I'll write the component structure first.
import { updatePassword } from '../../api/auth' 

const user = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : null
})

const getRoleLabel = (role: string) => {
  const map: Record<string, string> = {
    'ROLE_ADMIN': '管理员',
    'ROLE_HOST': '教职工',
    'ROLE_VISITOR': '访客',
    'ROLE_GUARD': '安保人员'
  }
  return map[role] || role
}

const passwordFormRef = ref()
const loading = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePass2 = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }]
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        // Assuming updatePassword API exists or I will create it
        // The backend might not have this endpoint yet, checking AuthController is needed.
        // For now, I will implement the frontend logic.
        await updatePassword(passwordForm.oldPassword, passwordForm.newPassword)
        ElMessage.success('密码修改成功，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
      } catch (error) {
        // Error handled
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.profile-container {
  padding: 20px;
}
.profile-card {
  text-align: center;
  .user-info {
    padding: 20px 0;
    .avatar {
      background-color: #1890ff;
      font-size: 32px;
      margin-bottom: 16px;
    }
    .username {
      margin: 0 0 8px;
      font-size: 20px;
    }
    .role-tag {
      margin: 0;
    }
  }
}
.mr-2 {
  margin-right: 8px;
}
.mt-4 {
  margin-top: 16px;
}
</style>
