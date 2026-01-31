<template>
  <!-- B站风格顶部导航栏 -->
  <header :class="['bili-header', { 'scrolled': isScrolled || !props.showBanner }]">
    <!-- Banner 背景层 - 仅在 showBanner 为 true 时显示 -->
    <div v-if="props.showBanner" :class="['header-banner', { 'banner-hidden': isScrolled }]">
      <img 
        src="/bg2.png" 
        alt="banner" 
        class="banner-img"
      />
    </div>
    
    <!-- 导航内容层 -->
    <div class="header-container">
      <div class="header-left">
        <!-- Logo -->
        <div class="logo" @click="$router.push('/')">
          <img src="/logo.png" alt="Logo" class="logo-icon" />
          <span class="logo-text">Fear and dreams</span>
        </div>

        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">首页</router-link>
          <router-link to="/videos" class="nav-item" :class="{ active: $route.path === '/videos' }">视频</router-link>
          <router-link v-if="userStore.isLoggedIn" to="/upload" class="nav-item" :class="{ active: $route.path === '/upload' }">投稿</router-link>
          <router-link v-if="userStore.isSystemAdmin" to="/admin" class="nav-item" :class="{ active: $route.path === '/admin' }">管理</router-link>
          <router-link v-else-if="userStore.isCityManager" to="/city-admin" class="nav-item" :class="{ active: $route.path === '/city-admin' }">管理</router-link>
        </nav>
      </div>

      <!-- 搜索框 -->
      <div class="header-center" v-if="showSearch">
        <div class="search-box">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="发现你的第一场Fear and dreams  "
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">
            <img src="/logo.png" alt="搜索" class="search-logo" />
          </button>
        </div>
      </div>

      <!-- 用户区域 -->
      <div class="header-right">
        <template v-if="!userStore.isLoggedIn">
          <button class="login-btn" @click="$router.push('/login')">登录</button>
          <button class="register-btn" @click="$router.push('/register')">注册</button>
        </template>
        <template v-else>
          <!-- 消息入口 -->
          <div class="message-entry" @click="$router.push('/profile?tab=messages')">
            <div class="message-icon-wrapper">
              <el-icon class="message-icon"><Bell /></el-icon>
              <el-badge 
                v-if="unreadCount > 0" 
                :value="unreadCount" 
                class="message-badge" 
                :max="99" 
              />
            </div>
            <span class="message-text">消息</span>
          </div>

          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-avatar">
              <img :src="avatarUrl" alt="avatar" />
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="upload">
                  <el-icon><Upload /></el-icon> 投稿管理
                </el-dropdown-item>
                <el-dropdown-item command="admin" v-if="userStore.isSystemAdmin">
                  <el-icon><Setting /></el-icon> 系统管理
                </el-dropdown-item>
                <el-dropdown-item command="cityAdmin" v-else-if="userStore.isCityManager">
                  <el-icon><Setting /></el-icon> 城市管理
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { messageAPI } from '../api/video'
import { ElMessage } from 'element-plus'
import { Search, User, Upload, Setting, SwitchButton, Bell } from '@element-plus/icons-vue'
import { getMediaUrl } from '../utils/mediaUrl'

// 滚动状态
const isScrolled = ref(false)
const scrollThreshold = 60 // 滚动超过60px后切换样式

const handleScroll = () => {
  isScrolled.value = window.scrollY > scrollThreshold
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  handleScroll() // 初始化检查
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const props = defineProps({
  showSearch: {
    type: Boolean,
    default: true
  },
  showBanner: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['search'])

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const searchKeyword = ref('')
const unreadCount = ref(0)

// 获取未读消息数
const loadUnreadCount = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const response = await messageAPI.getUnreadCount()
    if (response.data.success) {
      unreadCount.value = response.data.count
    }
  } catch (error) {
    console.error('获取消息未读数失败', error)
  }
}

// 监听路由变化，刷新未读数
watch(() => route.path, () => {
  loadUnreadCount()
})

onMounted(() => {
  loadUnreadCount()
})

// 计算头像URL
const avatarUrl = computed(() => {
  if (userStore.user?.avatar) {
    return getMediaUrl(userStore.user.avatar)
  }
  return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
})

// 搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    emit('search', searchKeyword.value)
    router.push({ path: '/videos', query: { keyword: searchKeyword.value } })
  }
}

// 用户菜单
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'upload':
      router.push('/upload')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'cityAdmin':
      router.push('/city-admin')
      break
    case 'logout':
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/')
      break
  }
}
</script>

<style scoped>
/* 顶部导航栏 */
.bili-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background-color: var(--bili-white, #fff);
  transition: all 0.3s ease;
}

/* Banner 容器 */
.header-banner {
  width: 100%;
  height: 180px;
  overflow: hidden;
  position: relative;
  background-color: #1a1a2e; /* 深色占位背景 */
  transition: height 0.3s ease, opacity 0.3s ease;
}

/* Banner 隐藏状态 - 使用高度过渡避免滚动弹跳 */
.header-banner.banner-hidden {
  height: 0;
  opacity: 0;
}

/* Banner 图片 */
.header-banner .banner-img {
  width: 100%;
  object-fit: cover;
  object-position: center;
}

/* 滚动后的白色背景状态 */
.bili-header.scrolled {
  background-color: var(--bili-white, #fff);
  border-bottom: 1px solid var(--bili-border, #e3e5e7);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

/* 导航内容层 - 未滚动时覆盖在 banner 上并垂直居中 */
.bili-header:not(.scrolled) .header-container {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  z-index: 10;
}

/* 未滚动时的文字颜色调整 */
.bili-header:not(.scrolled) .logo-text,
.bili-header:not(.scrolled) .nav-item {
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.bili-header:not(.scrolled) .nav-item:hover,
.bili-header:not(.scrolled) .nav-item.active {
  color: #fff;
  border-bottom-color: #fff;
}

.bili-header:not(.scrolled) .search-box {
  background-color: rgba(255, 255, 255, 0.9);
}

.bili-header:not(.scrolled) .login-btn {
  color: #fff;
  border-color: #fff;
  background-color: transparent;
}

.bili-header:not(.scrolled) .login-btn:hover {
  background-color: #c23b3b;
  border-color: #c23b3b;
  color: #fff;
}

.bili-header:not(.scrolled) .message-entry {
  color: #fff;
}

.bili-header:not(.scrolled) .message-entry:hover {
  color: rgba(255, 255, 255, 0.8);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: 65px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.logo-icon {
  width: 36px;
  height: 36px;
  object-fit: contain;
}

.logo-text {
  /* 引用字体 */
  font-family: 'Cormorant Garamond', serif;
  font-size: 22px; /* 衬线体通常偏小，建议稍微调大 */
  font-weight: 500;
  color: var(--bili-text, #18191c);
  
  /* 艺术处理 */
  text-transform: uppercase; /* 全大写 */
  letter-spacing: 0.15em;    /* 增加字间距是艺术感的灵魂 */
  -webkit-font-smoothing: antialiased;
}

.nav-menu {
  display: flex;
  gap: 30px;
}

.nav-item {
  color: var(--bili-text-2, #61666d);
  font-size: 15px;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
  text-decoration: none;
}

.nav-item:hover,
.nav-item.active {
  color: var(--bili-pink, #2b86c3);
  border-bottom-color: var(--bili-pink, #2b86c3);
}

/* 搜索框 */
.header-center {
  flex: 1;
  max-width: 500px;
  margin: 0 40px;
}

.search-box {
  display: flex;
  align-items: center;
  background-color: var(--bili-hover, #f1f2f3);
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid transparent;
  transition: all 0.3s;
}

.search-box:hover,
.search-box:focus-within {
  background-color: var(--bili-white, #fff);
  border-color: #2b86c3;
}

.search-box input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  padding: 10px 15px;
  font-size: 14px;
  color: var(--bili-text, #18191c);
}

.search-box input::placeholder {
  color: var(--bili-text-3, #9499a0);
}

.search-btn {
  padding: 6px 12px;
  background-color: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.search-btn .search-logo {
  width: 32px;
  height: 32px;
  object-fit: contain;
  transition: transform 0.4s ease;
}

/* 气球上升动画 */
.search-btn:hover .search-logo {
  animation: balloon-float 0.6s ease-in-out infinite alternate;
}

@keyframes balloon-float {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-6px);
  }
}

/* 用户区域 */
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.login-btn,
.register-btn {
  padding: 8px 20px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

/* 登录按钮 - 红气球主题色 */
.login-btn {
  background-color:#c23b3b !important;
  color: #fff;
  border: 1px solid #c23b3b !important;
}

.login-btn:hover {
  background-color: #9a091a !important;
  color: white;
}

/* 注册按钮 - 蓝气球主题色 */
.register-btn {
  background-color: #2b86c3;
  color: white;
}

.register-btn:hover {
  background-color: #1e6a9e;
}

.message-entry {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0 10px;
  color: var(--bili-text-2, #61666d);
  transition: all 0.3s;
}

.message-entry:hover {
  color: var(--bili-pink, #2b86c3);
}

.message-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 24px;
}

.message-icon {
  font-size: 20px;
}

.message-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  transform: scale(0.8);
}

.message-text {
  font-size: 12px;
  margin-top: 2px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.3s;
}

.user-avatar:hover {
  border-color: var(--bili-pink, #2b86c3);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 响应式 */
@media (max-width: 768px) {
  .header-container {
    padding: 0 10px;
  }

  .header-left {
    gap: 20px;
  }

  .nav-menu {
    display: none;
  }

  .header-center {
    margin: 0 20px;
  }
}
</style>

