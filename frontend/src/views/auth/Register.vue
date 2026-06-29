<template>
  <div class="auth-container">
    <div class="auth-content">
      <div class="auth-left">
        <div class="illustration">
          <img 
            src="/illustrations/register.svg" 
            alt="Visitor Registration" 
            class="illustration-img"
          />
        </div>
        <div class="brand-intro">
          <h2>访客自助注册</h2>
          <p>Visitor Self-Service Registration</p>
          <ul class="features">
            <li><el-icon><Check /></el-icon> 实名认证保障</li>
            <li><el-icon><Check /></el-icon> 快捷通行授权</li>
            <li><el-icon><Check /></el-icon> 隐私安全保护</li>
          </ul>
        </div>
      </div>

      <div class="auth-box">
        <div class="auth-header">
          <h2 class="title">创建账号</h2>
          <p class="subtitle">填写以下信息完成注册</p>
        </div>
        
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top" size="large">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="form.realName" placeholder="请输入姓名" :prefix-icon="User" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Iphone" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" placeholder="请输入身份证号" :prefix-icon="Postcard" />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请设置密码" show-password :prefix-icon="Lock" />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password :prefix-icon="Lock" />
          </el-form-item>
          
          <el-button type="primary" class="w-full submit-btn" :loading="loading" @click="handleRegister">
            立即注册
          </el-button>
          
          <div class="footer">
            已有账号？<router-link to="/login" class="login-link">直接登录</router-link>
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
import { User, Lock, Check, Iphone, Postcard } from '@element-plus/icons-vue'
import { register } from '../../api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  realName: '',
  phone: '',
  idCard: '',
  password: '',
  confirmPassword: ''
})

const validatePass2 = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致！'))
  } else {
    callback()
  }
}

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }]
}

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        await register({
          username: form.phone,
          password: form.password,
          realName: form.realName,
          idCard: form.idCard
        })
        ElMessage.success('注册成功，请登录')
        router.push('/login')
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
  width: 960px;
  height: 580px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  z-index: 1;
  position: relative;
}

.auth-left {
  flex: 0 0 40%;
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
      width: 85%;
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
    margin-bottom: 24px;
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

.submit-btn {
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
  margin-top: 16px;
  margin-bottom: 16px;
  font-weight: 500;
  letter-spacing: 1px;
}

.footer {
  text-align: center;
  font-size: 14px;
  color: #666;
  
  .login-link {
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
