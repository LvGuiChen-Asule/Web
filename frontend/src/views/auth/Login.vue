<template>
  <div class="auth-container">
    <div class="auth-content">
      <div class="auth-left">
        <div class="illustration">
          <img 
            src="/illustrations/login.svg" 
            alt="Smart Campus" 
            class="illustration-img"
          />
        </div>
        <div class="brand-intro">
          <h2>智慧校园访客系统</h2>
          <p>Smart Campus Visitor Management System</p>
          <ul class="features">
            <li><el-icon><Check /></el-icon> 智能预约审批</li>
            <li><el-icon><Check /></el-icon> 实时访客追踪</li>
            <li><el-icon><Check /></el-icon> 数据可视化分析</li>
          </ul>
        </div>
      </div>
      
      <div class="auth-box">
        <div class="auth-header">
          <h2 class="title">欢迎登录</h2>
          <p class="subtitle">请使用您的账号密码登录系统</p>
        </div>
        
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top" size="large">
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名" 
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              show-password 
              :prefix-icon="Lock"
            />
          </el-form-item>
          
          <div class="form-actions">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
          </div>

          <el-button type="primary" class="w-full submit-btn" :loading="loading" @click="handleLogin">
            立即登录
          </el-button>
          
          <div class="footer">
            还没有账号？<router-link to="/register" class="register-link">立即注册</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Check } from '@element-plus/icons-vue'
import { login } from '../../api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        const res: any = await login({
          username: form.username.trim(),
          password: form.password
        })
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data))
        ElMessage.success('登录成功')
        router.push('/dashboard')
      } catch (error) {
        // Error handled in interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: url('/bg.png') center center / cover no-repeat fixed;
  position: relative;
  overflow: hidden;
}

.auth-content {
  display: flex;
  width: 900px;
  height: 520px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  z-index: 1;
  position: relative;
}

.auth-left {
  flex: 1;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: white;
  position: relative;

  .illustration {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .illustration-img {
      width: 80%;
      height: auto;
      border-radius: 12px;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
      transition: transform 0.3s ease;
      
      &:hover {
        transform: scale(1.02);
      }
    }
  }

  .brand-intro {
    margin-top: 20px;
    text-align: center;

    h2 {
      font-size: 24px;
      margin-bottom: 8px;
      font-weight: 600;
    }

    p {
      font-size: 14px;
      opacity: 0.8;
      margin-bottom: 24px;
    }

    .features {
      list-style: none;
      padding: 0;
      display: flex;
      justify-content: center;
      gap: 16px;
      
      li {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        opacity: 0.9;
        
        .el-icon {
          font-size: 16px;
        }
      }
    }
  }
}

.auth-box {
  flex: 1;
  padding: 40px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: white;

  .auth-header {
    margin-bottom: 32px;
    text-align: left;

    .title {
      font-size: 28px;
      color: #1f1f1f;
      margin-bottom: 8px;
      font-weight: 600;
    }

    .subtitle {
      color: #8c8c8c;
      font-size: 14px;
    }
  }
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .forgot-link {
    color: #1890ff;
    font-size: 14px;
    text-decoration: none;
    transition: color 0.3s;
    
    &:hover {
      color: #40a9ff;
    }
  }
}

.submit-btn {
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-weight: 500;
  letter-spacing: 1px;
}

.footer {
  text-align: center;
  font-size: 14px;
  color: #666;
  
  .register-link {
    color: #1890ff;
    font-weight: 500;
    text-decoration: none;
    transition: color 0.3s;
    
    &:hover {
      color: #40a9ff;
      text-decoration: underline;
    }
  }
}

.w-full {
  width: 100%;
}
</style>
