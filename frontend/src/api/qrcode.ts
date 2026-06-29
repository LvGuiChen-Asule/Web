import request from '../utils/request'

export const generateQrCode = (appointmentId: number) => request.get(`/qrcode/generate/${appointmentId}`)
export const verifyQrCode = (qrContent: string, checkUsed?: boolean) => request.post('/qrcode/verify', { qrContent, checkUsed })

