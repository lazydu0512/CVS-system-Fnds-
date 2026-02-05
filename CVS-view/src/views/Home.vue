<template>
  <div class="fnds-home">
    <!-- B站风格顶部导航栏 -->
    <AppHeader />

    <!-- 主要内容 -->
    <main class="fnds-main">
      <!-- 分类导航 -->
      <div class="category-section">
        <div class="container">
          <div class="category-tabs">
            <div
              v-for="cat in visibleCategories"
              :key="cat.value"
              :class="['category-item', { active: currentCategory === cat.value }]"
              @click="handleCategoryClick(cat)"
            >
              <el-icon><component :is="cat.icon" /></el-icon>
              <span>{{ cat.label }}</span>
            </div>
            <!-- 其他城市下拉菜单 -->
            <el-dropdown v-if="moreCategories.length > 0" trigger="click" @command="handleMoreCitySelect">
              <div :class="['category-item', { active: isMoreCategoryActive }]">
                <el-icon><MoreFilled /></el-icon>
                <span>更多</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="city in moreCategories"
                    :key="city.value"
                    :command="city.value"
                    :class="{ 'is-active': currentCategory === city.value }"
                  >
                    {{ city.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- 视频列表 -->
      <div class="video-section">
        <div class="container">
          <div class="section-header">
            <h2>{{ currentCategory === 'all' ? '推荐视频' : categories.find(c => c.value === currentCategory)?.label }}</h2>
            <div class="sort-tabs">
              <span
                v-for="sort in sortOptions"
                :key="sort.value"
                :class="['sort-item', { active: currentSort === sort.value }]"
                @click="currentSort = sort.value; loadVideos()"
              >
                {{ sort.label }}
              </span>
            </div>
          </div>

          <div class="video-grid" v-loading="loading">
            <div
              v-for="video in hotVideos"
              :key="video.id"
              class="video-card"
              @click="$router.push(`/videos/${video.id}`)"
            >
              <div class="video-cover">
                <img :src="getMediaUrl(video.thumbnailUrl) || 'https://via.placeholder.com/300x188'" :alt="video.title" />
                <div class="video-duration">{{ formatDuration(video.duration) }}</div>
                <div class="video-play-count">
                  <el-icon><VideoPlay /></el-icon>
                  {{ formatCount(video.viewCount) }}
                </div>
              </div>
              <div class="video-info">
                <h3 class="video-title">{{ video.title }}</h3>
                <div class="video-meta">
                  <span class="video-author">{{ video.uploaderName || '匿名用户' }}</span>
                  <span class="video-stats">
                    <el-icon><View /></el-icon> {{ formatCount(video.viewCount) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 空状态提示 -->
          <div v-if="!loading && hotVideos.length === 0" class="empty-state">
            <el-icon class="empty-icon"><VideoPlay /></el-icon>
            <p class="empty-text">还没有E粉发布该城市的视频，快去投稿吧</p>
            <el-button type="primary" @click="$router.push('/upload')">立即投稿</el-button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { videoAPI, cityAPI } from '../api/video'
import { ElMessage } from 'element-plus'
import { Star, Location, MoreFilled, VideoPlay, View } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import { getMediaUrl } from '../utils/mediaUrl'

const router = useRouter()
const userStore = useUserStore()

const currentCategory = ref('all')
const currentSort = ref('new') // 默认按最新排序
const hotVideos = ref([])
const loading = ref(false)

const bannerItems = ref([
  {
    id: 1,
    title: '分享你的演唱会精彩瞬间',
    description: '记录每一个难忘的音乐时刻',
    image: 'http://localhost:8080/static/banner/banner1.png'
  },
  {
    id: 2,
    title: '发现更多精彩演出',
    description: '探索不同城市的音乐盛事',
    image: 'http://localhost:8080/static/banner/banner1.png'
  }
])

const categories = ref([
  { label: '全部', value: 'all', icon: Star }
])

const sortOptions = ref([
  { label: '最新', value: 'new' },
  { label: '最多播放', value: 'view' }
])

// 可见的城市数量（显示在首行）
const MAX_VISIBLE_CITIES = 13

// 计算可见的分类（全部 + 最多显示的城市）
const visibleCategories = computed(() => {
  return categories.value.slice(0, MAX_VISIBLE_CITIES + 1) // +1 因为包含"全部"
})

// 更多城市（超出显示限制的城市）
const moreCategories = computed(() => {
  return categories.value.slice(MAX_VISIBLE_CITIES + 1)
})

// 是否选中"其他"中的城市
const isMoreCategoryActive = computed(() => {
  return moreCategories.value.some(city => city.value === currentCategory.value)
})

// 处理分类点击
const handleCategoryClick = (cat) => {
  currentCategory.value = cat.value
  loadVideos()
}

// 处理"其他"下拉菜单选择
const handleMoreCitySelect = (cityValue) => {
  currentCategory.value = cityValue
  loadVideos()
}

// 加载城市列表
const loadCities = async () => {
  try {
    const response = await cityAPI.getAllCities()
    if (response.data.success) {
      const cities = response.data.data
      cities.forEach(city => {
        categories.value.push({
          label: city.name,
          value: city.name,
          icon: Location
        })
      })
    }
  } catch (error) {
    console.error('加载城市列表失败:', error)
  }
}

// 加载视频列表
const loadVideos = async () => {
  loading.value = true
  try {
    const params = {
      current: 1,
      size: 20
    }

    if (currentCategory.value !== 'all') {
      params.city = currentCategory.value
    }

    // 添加排序参数
    params.sort = currentSort.value

    const response = await videoAPI.getVideoList(params)
    if (response.data.success) {
      hotVideos.value = response.data.data
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 格式化时长（支持秒和毫秒）
const formatDuration = (duration) => {
  if (!duration) return '00:00'

  // 如果时长大于10000，可能是毫秒，转换为秒
  let seconds = duration
  if (duration > 10000) {
    seconds = Math.floor(duration / 1000)
  }

  const hours = Math.floor(seconds / 3600)
  const mins = Math.floor((seconds % 3600) / 60)
  const secs = Math.floor(seconds % 60)

  if (hours > 0) {
    return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
  }
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

// 格式化数 
const formatCount = (count) => {
  if (!count) return '0'
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

onMounted(() => {
  loadCities()
  loadVideos()
})
</script>

<style scoped>
/* fns首页样式 */
.fnds-home {
  min-height: 100vh;
  background-color: var(--fnds-bg);
  background-image: url('http://localhost:8080/static/bg/bg1.png');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  position: relative;
}

/* 白色半透明蒙层 */
.fnds-home::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.7);
  z-index: 0;
  pointer-events: none;
}

/* 确保内容在蒙层之上 */
.fnds-home > * {
  position: relative;
  z-index: 1;
}

/* 主要内容 */
.fnds-main {
  padding-top: 20px;
}

/* 轮播图 */
.banner-section {
  max-width: 1400px;
  margin: 0 auto 20px;
  padding: 0 20px;
}

.banner-item {
  height: 200px;
  background-size: cover;
  background-position: center;
  border-radius: 12px;
  display: flex;
  align-items: center;
  padding: 0 60px;
  position: relative;
  overflow: hidden;
}

.banner-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, rgba(0, 0, 0, 0.5) 0%, transparent 100%);
}

.banner-content {
  position: relative;
  z-index: 1;
  color: white;
}

.banner-content h2 {
  font-size: 32px;
  margin-bottom: 10px;
  font-weight: 600;
}

.banner-content p {
  font-size: 16px;
  opacity: 0.9;
}

/* 分类导航 */
.category-section {
  padding: 20px 0;
  margin-bottom: 20px;
}

.category-tabs {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  padding: 0 10px;
  justify-content: center;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  color: var(--fnds-text-2);
}

.category-item:hover {
  background-color: var(--fnds-hover);
  color: var(--fnds-pink);
}

.category-item.active {
  border-radius: 8px;
  border: 1px solid var(--fnds-pink);
  background-color: #e6f4fa;
  color: var(--fnds-pink);
}

.category-item .el-icon {
  font-size: 24px;
}

.category-item span {
  font-size: 14px;
}

/* 视频区域 */
.video-section {
  padding-bottom: 40px;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--fnds-text);
}

.sort-tabs {
  display: flex;
  gap: 20px;
}

.sort-item {
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: var(--fnds-text-2);
  transition: all 0.3s;
}

.sort-item:hover {
  background-color: var(--fnds-hover);
  color: var(--fnds-pink);
}

.sort-item.active {
  background-color: #DFF6FD;
  color: var(--fnds-blue);
  font-weight: 500;
}

/* 视频网格 */
.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.video-card {
  background-color: var(--fnds-white);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.video-cover {
  position: relative;
  width: 100%;
  padding-top: 62.5%; /* 16:10 比例 */
  background-color: #f0f0f0;
  overflow: hidden;
}

.video-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.video-card:hover .video-cover img {
  transform: scale(1.05);
}

.video-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.video-play-count {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.video-info {
  padding: 12px;
}

.video-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--fnds-text);
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--fnds-text-3);
}

.video-author {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-stats {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-text {
  font-size: 16px;
  color: var(--fnds-text-2);
  margin-bottom: 24px;
  line-height: 1.6;
  font-weight: 600;
}

/* 响应式*/
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

  .video-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 10px;
  }
}
</style>
