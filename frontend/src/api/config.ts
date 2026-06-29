import request from '../utils/request'

export const getConfig = (key: string) => request.get(`/config/${encodeURIComponent(key)}`)
export const updateConfig = (key: string, value: string) => request.put(`/config/${encodeURIComponent(key)}`, { value })
export const listConfig = () => request.get('/config/list')
export const listConfigDetailed = () => request.get('/config/list-detailed')

