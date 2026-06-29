import request from '../utils/request'

export const getVisitorTrend = (params: { startDate: string, endDate: string }) => request.get('/stats/visitor-trend', { params })
export const getHostRanking = (limit: number = 10) => request.get('/stats/host-ranking', { params: { limit } })