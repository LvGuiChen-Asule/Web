import request from '../utils/request'

export type SysRole = {
  id: number
  roleCode: string
  roleName: string
  description?: string
}

export type AdminUser = {
  id: number
  username: string
  realName?: string
  phone?: string
  idCard?: string
  deptId?: number
  status: number
  roles: string[]
  createTime?: string
  updateTime?: string
}

export const listAdminRoles = () => request.get('/admin/roles')

export const pageAdminUsers = (params: any) => request.get('/admin/users', { params })

export const createAdminUser = (data: any) => request.post('/admin/users', data)

export const updateAdminUser = (id: number, data: any) => request.put(`/admin/users/${id}`, data)

export const setAdminUserStatus = (id: number, status: number) => request.patch(`/admin/users/${id}/status`, { status })

export const resetAdminUserPassword = (id: number, newPassword: string) =>
  request.post(`/admin/users/${id}/reset-password`, { newPassword })

export const updateAdminUserRoles = (id: number, roleCodes: string[]) => request.put(`/admin/users/${id}/roles`, { roleCodes })

