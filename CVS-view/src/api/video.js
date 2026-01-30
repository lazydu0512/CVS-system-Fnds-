import axios from 'axios'
import { uploadVideoToOSS, uploadCoverToOSS, uploadAvatarToOSS } from '../utils/ossUpload'

// 创建一个带token的axios实例
const createAuthRequest = () => {
  const token = localStorage.getItem('token')
  return axios.create({
    headers: token ? {
      'Authorization': `Bearer ${token}`
    } : {}
  })
}

// 视频相关API
export const videoAPI = {
  // 获取视频列表
  getVideoList(params = {}) {
    return axios.get('/api/video/list', { params })
  },

  // 获取视频详情
  getVideoDetail(id) {
    return axios.get(`/api/video/${id}`)
  },

  // 上传视频
  uploadVideo(videoData) {
    const token = localStorage.getItem('token')
    return axios.post('/api/video/upload', videoData, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 更新视频
  updateVideo(videoData) {
    const token = localStorage.getItem('token')
    return axios.put('/api/video/update', videoData, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 删除视频
  deleteVideo(videoId) {
    const token = localStorage.getItem('token')
    return axios.delete(`/api/video/${videoId}`, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 下架视频
  offlineVideo(videoId, reason) {
    const token = localStorage.getItem('token')
    return axios.post(`/api/video/${videoId}/offline`, { reason }, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 重新上架视频
  onlineVideo(videoId) {
    const token = localStorage.getItem('token')
    return axios.post(`/api/video/${videoId}/online`, {}, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 获取用户上传的视频
  getUserVideos(userId) {
    return axios.get(`/api/video/user/${userId}`)
  },

  // 获取待审核视频（管理员）
  getPendingVideos() {
    const token = localStorage.getItem('token')
    return axios.get('/api/video/pending', {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 管理员预览视频（可查看任意状态的视频）
  previewVideo(id) {
    const token = localStorage.getItem('token')
    return axios.get(`/api/video/preview/${id}`, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 审核视频（管理员）
  reviewVideo(id, reviewData) {
    const token = localStorage.getItem('token')
    return axios.post(`/api/video/${id}/review`, reviewData, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 上传视频文件
  uploadVideoFile(file, onProgress) {
    const formData = new FormData()
    formData.append('file', file)
    const token = localStorage.getItem('token')
    return axios.post('/api/file/upload-video', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      onUploadProgress: onProgress
    })
  },

  // 上传封面图片
  uploadCoverFile(file, onProgress) {
    const formData = new FormData()
    formData.append('file', file)
    const token = localStorage.getItem('token')
    return axios.post('/api/file/upload-cover', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      onUploadProgress: onProgress
    })
  },

  // 增加播放量
  increaseViewCount(id) {
    return axios.post(`/api/video/${id}/view`)
  },

  // 上传视频文件到OSS（前端直传）
  uploadVideoToOSS(file, onProgress) {
    return uploadVideoToOSS(file, onProgress)
  },

  // 上传封面图片到OSS（前端直传）
  uploadCoverToOSS(file, onProgress) {
    return uploadCoverToOSS(file, onProgress)
  }
}

// 评论相关API
export const commentAPI = {
  // 添加评论
  addComment(commentData) {
    const token = localStorage.getItem('token')
    return axios.post('/api/comment', commentData, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 获取视频评论
  getVideoComments(videoId) {
    return axios.get(`/api/comment/video/${videoId}`)
  },

  // 删除评论
  deleteComment(id) {
    const token = localStorage.getItem('token')
    return axios.delete(`/api/comment/${id}`, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 获取用户的所有评论
  getUserComments() {
    const token = localStorage.getItem('token')
    return axios.get('/api/comment/user', {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  }
}

// 点赞相关API
export const likeAPI = {
  // 点赞/取消点赞
  toggleLike(videoId) {
    const token = localStorage.getItem('token')
    return axios.post(`/api/video-like/${videoId}`, {}, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 检查点赞状态
  checkLikeStatus(videoId) {
    const token = localStorage.getItem('token')
    return axios.get(`/api/video-like/${videoId}/status`, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  }
}

// 收藏相关API
export const collectAPI = {
  // 收藏/取消收藏
  toggleCollect(videoId) {
    const token = localStorage.getItem('token')
    return axios.post(`/api/video-collect/${videoId}`, {}, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 检查收藏状态
  checkCollectStatus(videoId) {
    const token = localStorage.getItem('token')
    return axios.get(`/api/video-collect/${videoId}/status`, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 获取用户收藏列表
  getUserCollects() {
    const token = localStorage.getItem('token')
    return axios.get('/api/video-collect/user', {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  }
}

// 用户相关API
export const userAPI = {
  // 上传头像
  uploadAvatar(file, onProgress) {
    const formData = new FormData()
    formData.append('file', file)
    const token = localStorage.getItem('token')
    return axios.post('/api/file/upload-avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      onUploadProgress: onProgress
    })
  },

  // 上传头像到OSS（前端直传）
  uploadAvatarToOSS(file, onProgress) {
    return uploadAvatarToOSS(file, onProgress)
  },

  // 更新用户信息
  updateUser(userId, userData) {
    const token = localStorage.getItem('token')
    return axios.put(`/api/user/${userId}`, userData, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 获取用户信息
  getUser(userId) {
    return axios.get(`/api/user/${userId}`)
  },

  // 获取所有用户列表（管理员）
  getAllUsers(params = {}) {
    const token = localStorage.getItem('token')
    return axios.get('/api/user/list', {
      params,
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 切换用户状态（管理员）
  toggleUserStatus(userId) {
    const token = localStorage.getItem('token')
    return axios.post(`/api/user/${userId}/toggle-status`, {}, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 修改密码（需验证原密码）
  changePassword(oldPassword, newPassword) {
    const token = localStorage.getItem('token')
    return axios.post('/api/user/change-password', { oldPassword, newPassword }, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 管理员查看用户密码
  getUserPassword(userId) {
    const token = localStorage.getItem('token')
    return axios.get(`/api/user/${userId}/password`, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 管理员修改用户密码
  adminChangePassword(userId, newPassword) {
    const token = localStorage.getItem('token')
    return axios.put(`/api/user/${userId}/password`, { newPassword }, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  }
}

// 管理员统计API
export const adminAPI = {
  // 获取统计数据
  getStats() {
    const token = localStorage.getItem('token')
    return axios.get('/api/admin/stats', {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 获取所有视频（管理员）
  getAllVideos(params = {}) {
    const token = localStorage.getItem('token')
    return axios.get('/api/admin/videos', {
      params,
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 删除视频（管理员）
  deleteVideo(videoId) {
    const token = localStorage.getItem('token')
    return axios.delete(`/api/admin/video/${videoId}`, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  }
}

// 消息通知API
export const messageAPI = {
  // 获取消息列表
  getMessages() {
    const token = localStorage.getItem('token')
    return axios.get('/api/message/list', {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 获取未读消息数量
  getUnreadCount() {
    const token = localStorage.getItem('token')
    return axios.get('/api/message/unread-count', {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 标记单条消息为已读
  markAsRead(messageId) {
    const token = localStorage.getItem('token')
    return axios.post(`/api/message/${messageId}/read`, {}, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 标记所有消息为已读
  markAllAsRead() {
    const token = localStorage.getItem('token')
    return axios.post('/api/message/read-all', {}, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  }
}

// 城市管理API
export const cityAPI = {
  // 获取所有城市
  getAllCities() {
    return axios.get('/api/city')
  },

  // 添加城市（管理员）
  addCity(city) {
    const token = localStorage.getItem('token')
    return axios.post('/api/city', city, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 更新城市（管理员）
  updateCity(id, city) {
    const token = localStorage.getItem('token')
    return axios.put(`/api/city/${id}`, city, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 删除城市（管理员）
  deleteCity(id) {
    const token = localStorage.getItem('token')
    return axios.delete(`/api/city/${id}`, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  }
}

// 城市管理员API
export const cityManagerAPI = {
  // 获取所有城市管理员及其管理的城市
  getAllCityManagers() {
    const token = localStorage.getItem('token')
    return axios.get('/api/city-manager/all', {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 获取指定管理员管理的城市
  getManagedCities(userId) {
    const token = localStorage.getItem('token')
    return axios.get(`/api/city-manager/${userId}/cities`, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 分配城市给管理员
  assignCity(userId, cityId) {
    const token = localStorage.getItem('token')
    return axios.post('/api/city-manager/assign', { userId, cityId }, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 移除管理员的城市权限
  removeCity(userId, cityId) {
    const token = localStorage.getItem('token')
    return axios.delete(`/api/city-manager/${userId}/city/${cityId}`, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 设置用户为城市管理员
  setUserAsCityManager(userId) {
    const token = localStorage.getItem('token')
    return axios.post(`/api/city-manager/set-role/${userId}`, {}, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  },

  // 取消用户的城市管理员角色
  removeCityManagerRole(userId) {
    const token = localStorage.getItem('token')
    return axios.delete(`/api/city-manager/remove-role/${userId}`, {
      headers: token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
  }
}