import request from '../utils/request'

export const getMyMessages = (params: any) => request.get('/message/list', { params })
export const markMessageRead = (id: number) => request.put(`/message/read/${id}`)
export const markAllRead = () => request.put('/message/read-all')
export const sendSystemMessage = (data: any) => request.post('/message/system', data)

