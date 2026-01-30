// 完整版本的 main.js - 在安装完所有依赖后替换 src/main.js

import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'

// 路由配置
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'Home', component: () => import('./views/Home.vue') },
    { path: '/login', name: 'Login', component: () => import('./views/Login.vue') },
    { path: '/register', name: 'Register', component: () => import('./views/Register.vue') },
    { path: '/videos', name: 'VideoList', component: () => import('./views/VideoList.vue') },
    { path: '/videos/:id', name: 'VideoDetail', component: () => import('./views/VideoDetail.vue') },
    { path: '/upload', name: 'VideoUpload', component: () => import('./views/VideoUpload.vue'), meta: { requiresAuth: true } },
    { path: '/profile', name: 'Profile', component: () => import('./views/Profile.vue'), meta: { requiresAuth: true } },
    { path: '/admin', name: 'Admin', component: () => import('./views/Admin.vue'), meta: { requiresAuth: true, requiresAdmin: true } }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.requiresAdmin && user?.role !== 1) {
    next('/')
  } else {
    next()
  }
})

// 创建应用
const app = createApp(App)
const pinia = createPinia()

app.use(router)
app.use(pinia)
app.use(ElementPlus)

app.mount('#app')