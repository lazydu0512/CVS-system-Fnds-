<template>
  <div class="video-list-page">
    <!-- 顶部导航 -->
    <AppHeader @search="handleHeaderSearch" />

    <div class="video-list-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">
          <el-icon><VideoCamera /></el-icon>
          好歌献给你
        </h1>
        <p class="page-subtitle">开启你的第一次Fear and dreams </p>
      </div>

      <!-- 搜索和筛选 -->
      <div class="filters-section">
        <div class="filters">
          <el-input
            v-model="searchForm.title"
            placeholder="搜索视频标题..."
            class="search-input"
            size="large"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-select 
            v-model="searchForm.city" 
            placeholder="选择城市" 
            class="city-select"
            size="large"
            clearable
          >
            <el-option label="全部城市" value="" />
            <el-option
              v-for="city in cities"
              :key="city.id"
              :label="city.name"
              :value="city.name"
            />
          </el-select>

          <el-button type="primary" size="large" @click="handleSearch" class="search-btn">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </div>

        <!-- 结果统计 -->
        <div class="result-info" v-if="!loading">
          <span v-if="pagination.total > 0">共找到 <strong>{{ pagination.total }}</strong> 个视频</span>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="content-area" v-loading="loading">
        <!-- 有视频时显示视频网格 -->
        <div v-if="videos.length > 0" class="video-grid">
          <div
            v-for="video in videos"
            :key="video.id"
            class="video-card"
            @click="$router.push(`/videos/${video.id}`)"
          >
            <div class="video-cover">
              <img :src="getMediaUrl(video.thumbnailUrl) || 'https://via.placeholder.com/300x188'" :alt="video.title" />
              <div class="video-duration">{{ formatDuration(video.duration) }}</div>
              <div class="video-overlay">
                <el-icon :size="48"><VideoPlay /></el-icon>
              </div>
            </div>
            <div class="video-info">
              <h3 class="video-title">{{ video.title }}</h3>
              <div class="video-meta">
                <span class="video-city">
                  <el-icon><Location /></el-icon>
                  {{ video.city }}
                </span>
                <span class="video-views">
                  <el-icon><View /></el-icon>
                  {{ formatCount(video.viewCount) }}
                </span>
              </div>
              <p class="video-description" v-if="video.description">{{ video.description }}</p>
            </div>
          </div>
        </div>

        <!-- 没有视频时显示空状态 -->
        <div v-else-if="!loading" class="empty-state">
          <div class="empty-icon">
            <el-icon :size="80"><VideoCamera /></el-icon>
          </div>
          <h2 class="empty-title">没有找到相关的视频</h2>
          <p class="empty-description">
            试试更换搜索关键词或选择其他城市
          </p>
          <el-button type="primary" @click="resetSearch">
            <el-icon><RefreshRight /></el-icon>
            重置搜索
          </el-button>
        </div>
      </div>

      <!-- 分页 - 始终显示在底部 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[8, 16, 24, 32]"
          :total="pagination.total"
          :disabled="loading"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { videoAPI, cityAPI } from '../api/video'
import { ElMessage } from 'element-plus'
import { Search, Location, View, VideoPlay, VideoCamera, RefreshRight } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import { getMediaUrl } from '../utils/mediaUrl'

const route = useRoute()

const videos = ref([])
const cities = ref([])
const loading = ref(false)

const searchForm = reactive({
  title: '',
  city: ''
})

const pagination = reactive({
  current: 1,
  size: 16,
  total: 0
})

// 加载城市列表
const loadCities = async () => {
  try {
    const response = await cityAPI.getAllCities()
    if (response.data.success) {
      cities.value = response.data.data
    }
  } catch (error) {
    console.error('加载城市列表失败:', error)
  }
}

const loadVideos = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }

    const response = await videoAPI.getVideoList(params)
    if (response.data.success) {
      videos.value = response.data.data
      pagination.total = response.data.total
    }
  } catch (error) {
    ElMessage.error('加载视频列表失败')
    console.error('加载视频列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadVideos()
}

const resetSearch = () => {
  searchForm.title = ''
  searchForm.city = ''
  pagination.current = 1
  loadVideos()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadVideos()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadVideos()
}

const formatDuration = (seconds) => {
  if (!seconds) return '00:00'
  // 如果时长大于10000，可能是毫秒
  if (seconds > 10000) {
    seconds = Math.floor(seconds / 1000)
  }
  const hours = Math.floor(seconds / 3600)
  const mins = Math.floor((seconds % 3600) / 60)
  const secs = Math.floor(seconds % 60)
  if (hours > 0) {
    return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
  }
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

const formatCount = (count) => {
  if (!count) return '0'
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

// 处理Header搜索
const handleHeaderSearch = (keyword) => {
  searchForm.title = keyword
  handleSearch()
}

onMounted(() => {
  if (route.query.keyword) {
    searchForm.title = route.query.keyword
  }
  loadCities()
  loadVideos()
})
</script>

<style scoped>
.video-list-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.video-list-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 20px;
  min-height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--bili-pink, #fb7299);
}

.page-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

/* 搜索筛选区域 */
.filters-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.filters {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  flex: 1;
  min-width: 280px;
}

.city-select {
  width: 180px;
}

.search-btn {
  padding: 12px 28px;
}

.result-info {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
  color: #666;
  font-size: 14px;
}

.result-info strong {
  color: var(--bili-pink, #fb7299);
  font-weight: 600;
}

/* 内容区域 */
.content-area {
  flex: 1;
  min-height: 400px;
}

/* 视频网格 */
.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.video-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.video-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
}

.video-cover {
  position: relative;
  width: 100%;
  padding-top: 56.25%;
  background: #f0f0f0;
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
  transform: scale(1.08);
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
}

.video-card:hover .video-overlay {
  opacity: 1;
}

.video-duration {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.85);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.video-info {
  padding: 16px;
}

.video-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.video-city, .video-views {
  display: flex;
  align-items: center;
  gap: 4px;
}

.video-description {
  font-size: 13px;
  color: #666;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 175px 20px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  color: #ddd;
  margin-bottom: 24px;
}

.empty-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.empty-description {
  font-size: 16px;
  color: #999;
  margin: 0 0 30px 0;
}

/* 分页区域 */
.pagination-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-top: auto;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-title {
    font-size: 28px;
  }

  .filters {
    flex-direction: column;
  }

  .search-input, .city-select {
    width: 100%;
    min-width: auto;
  }

  .search-btn {
    width: 100%;
  }

  .video-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 16px;
  }

  .empty-title {
    font-size: 22px;
  }
}
</style>