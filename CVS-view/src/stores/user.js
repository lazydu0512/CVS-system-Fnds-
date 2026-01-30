import { defineStore } from 'pinia'
import axios from 'axios'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    token: null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isSystemAdmin: (state) => state.user?.role === 1,
    isCityManager: (state) => state.user?.role === 2,
    isAdmin: (state) => state.user?.role === 1 || state.user?.role === 2
  },

  actions: {
    // 初始化用户状态
    init() {
      const token = localStorage.getItem('token')
      const user = localStorage.getItem('user')
      if (token && user) {
        this.token = token
        this.user = JSON.parse(user)
        // 设置 axios 默认头部
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      }
    },

    // 登录
    async login(credentials) {
      try {
        const response = await axios.post('/api/user/login', credentials)
        if (response.data.success) {
          this.user = response.data.user
          this.token = response.data.token
          localStorage.setItem('token', this.token)
          localStorage.setItem('user', JSON.stringify(this.user))
          axios.defaults.headers.common['Authorization'] = `Bearer ${this.token}`
          return { success: true }
        } else {
          return { success: false, message: response.data.message }
        }
      } catch (error) {
        return { success: false, message: error.response?.data?.message || '登录失败' }
      }
    },

    // 注册
    async register(userData) {
      try {
        const response = await axios.post('/api/user/register', userData)
        return response.data
      } catch (error) {
        return { success: false, message: error.response?.data?.message || '注册失败' }
      }
    },

    // 登出
    logout() {
      this.user = null
      this.token = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      delete axios.defaults.headers.common['Authorization']
    },

    // 更新用户信息
    async updateUser(userData) {
      try {
        const response = await axios.put(`/api/user/${this.user.id}`, userData)
        if (response.data.success) {
          this.user = { ...this.user, ...userData }
          localStorage.setItem('user', JSON.stringify(this.user))
          return { success: true }
        } else {
          return { success: false, message: response.data.message }
        }
      } catch (error) {
        return { success: false, message: error.response?.data?.message || '更新失败' }
      }
    },

    // 设置用户信息
    setUser(user) {
      this.user = user
      localStorage.setItem('user', JSON.stringify(this.user))
    }
  }
})