import axios from 'axios'
import { ElMessage } from 'element-plus'

type ApiResult<T = any> = {
  code: number
  message: string
  data: T
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor
request.interceptors.response.use(
  (response) => {
    if (response.config?.responseType === 'blob') return response
    const result = response.data as ApiResult
    if (result?.code === 200) return result

    const url = response.config?.url || ''

    if (result?.code === 401) {
      if (url.includes('/auth/login')) {
        ElMessage.error('用户名或密码错误')
        return Promise.reject(new Error('用户名或密码错误'))
      }

      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
      return Promise.reject(new Error(result?.message || '登录已失效，请重新登录'))
    }

    ElMessage.error(result?.message || '请求失败')
    return Promise.reject(new Error(result?.message || '请求失败'))
  },
  (error) => {
    const url = error.config?.url || ''
    if (error.response?.status === 401) {
      if (url.includes('/auth/login')) {
        ElMessage.error('用户名或密码错误')
        return Promise.reject(error)
      }

      // Handle authentication failure
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    ElMessage.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  }
)

export default request
