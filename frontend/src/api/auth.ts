import request from '../utils/request'

export const login = (data: any) => request.post('/auth/login', data)
export const register = (data: any) => request.post('/auth/register', data)
export const updatePassword = (oldPassword: string, newPassword: string) => request.post('/users/password', { oldPassword, newPassword })
export const forgotPassword = (data: any) => request.post('/auth/forgot-password', data)
