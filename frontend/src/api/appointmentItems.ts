import request from '../utils/request'

export const getAppointmentItems = (appointmentId: number) => request.get(`/appointment/items/${appointmentId}`)
export const replaceAppointmentItems = (appointmentId: number, items: any[]) =>
  request.put(`/appointment/items/${appointmentId}`, items)

