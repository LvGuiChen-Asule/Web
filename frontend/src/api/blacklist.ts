import request from '../utils/request'

export const getBlacklist = (params: any) => request.get('/blacklist', { params })
export const addBlacklist = (data: any) => request.post('/blacklist', data)
export const removeBlacklist = (id: number) => request.delete(`/blacklist/${id}`)

