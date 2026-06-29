import request from '../utils/request'

export const getMyHostAppointments = (params: any) => request.get('/appointments/host/list', { params })
export const getMyVisitorAppointments = (params: any) => request.get('/appointments/visitor/list', { params })
export const getAdminAuditAppointments = (params: any) => request.get('/appointments/admin/list', { params })
export const createAppointment = (data: any) => request.post('/appointments', data)
export const auditByHost = (data: any) => request.post('/approval/first', data)
export const auditByAdmin = (data: any) => request.post('/approval/second', data)
export const searchHosts = (keyword: string) => request.get('/users/hosts', { params: { keyword } })

export const exportAppointments = (params: any) =>
  request.get('/appointment/export', { params, responseType: 'blob' })

export const getCalendarAppointments = (params: any) => request.get('/appointments/calendar', { params })
