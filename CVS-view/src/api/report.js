import request from '../utils/request'

/**
 * 举报相关API
 */
export const reportAPI = {
  /**
   * 提交举报
   */
  submitReport: (data) => request.post('/report/submit', data),

  /**
   * 获取举报列表（管理员）
   */
  getReportList: (params) => request.get('/report/list', { params }),

  /**
   * 审核通过举报
   */
  approveReport: (id, data) => request.post(`/report/approve/${id}`, data),

  /**
   * 驳回举报
   */
  rejectReport: (id, data) => request.post(`/report/reject/${id}`, data)
}
