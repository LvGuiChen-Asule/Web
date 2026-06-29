import request from '../utils/request'

export const upsertVehicle = (data: any) => request.post('/vehicle', data)
export const getVehicleByAppointment = (appointmentId: number) => request.get(`/vehicle/appointment/${appointmentId}`)
export const vehicleEntry = (appointmentId: number) => request.post('/vehicle/entry', { appointmentId })
export const vehicleExit = (appointmentId: number) => request.post('/vehicle/exit', { appointmentId })

