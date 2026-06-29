import request from '../utils/request'

export const submitFeedback = (data: any) => request.post('/feedback', data)
export const getEligibleFeedbackAppointments = () => request.get('/feedback/eligible')
export const getFeedbackWall = (params?: any) => request.get('/feedback/wall', { params })
export const getFeedbackList = (params: any) => request.get('/feedback/list', { params })
export const setFeedbackFeatured = (id: number, featured: boolean) => request.put(`/feedback/feature/${id}`, { featured })

