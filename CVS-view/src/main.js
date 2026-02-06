import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './style.css'
import App from './App.vue'
import { useUserStore } from './stores/user'

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
    { path: '/admin', name: 'Admin', component: () => import('./views/Admin.vue'), meta: { requiresAuth: true, requiresSystemAdmin: true } },
    { path: '/admin/review', name: 'AdminReview', component: () => import('./views/Admin.vue'), meta: { requiresAuth: true, requiresSystemAdmin: true } },
    { path: '/city-admin', name: 'CityManagerAdmin', component: () => import('./views/CityManagerAdmin.vue'), meta: { requiresAuth: true, requiresCityManager: true } },
    { path: '/admin/preview/:id', name: 'VideoPreview', component: () => import('./views/VideoPreview.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
    { path: '/user/:userId', name: 'UserSpace', component: () => import('./views/UserSpace.vue') }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = 'Fear And Dreams'
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.requiresSystemAdmin && user?.role !== 1) {
    // 系统管理员页面：城市管理员重定向到城市管理员后台
    if (user?.role === 2) {
      next('/city-admin')
    } else {
      next('/')
    }
  } else if (to.meta.requiresCityManager && user?.role !== 2) {
    // 城市管理员页面：系统管理员可以访问，普通用户不能
    if (user?.role === 1) {
      next('/admin')
    } else {
      next('/')
    }
  } else if (to.meta.requiresAdmin && user?.role !== 1 && user?.role !== 2) {
    // 通用管理页面（如视频预览）：两种管理员都可以访问
    next('/')
  } else {
    next()
  }
})

// 创建应用
const app = createApp(App)
const pinia = createPinia()

app.use(pinia) // Ensure Pinia is installed before router usage if stores are used in guards (though here they are not directly)
app.use(router)
app.use(ElementPlus)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 初始化用户状态
const userStore = useUserStore()
userStore.init()

app.mount('#app')