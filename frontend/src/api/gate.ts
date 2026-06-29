import request from '../utils/request'

export const checkIn = (qrContent: string) => request.post('/gate/check-in', { qrContent })
export const checkOut = (qrContent: string) => request.post('/gate/check-out', { qrContent })