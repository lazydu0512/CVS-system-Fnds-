<template>
  <div class="user-space">
    <AppHeader />

    <div class="user-header">
      <div class="header-banner"></div>
      <div class="user-profile-container">
        <div class="user-profile-card">
          <div class="user-avatar">
            <el-avatar :size="100" :src="userInfo?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          </div>
          <div class="user-info">
            <h1 class="nickname">{{ userInfo?.nickname || '加载中...' }}</h1>
            <p class="user-bio">{{ userInfo?.signature || '这个人很懒，什么都没留下' }}</p>
            <div class="user-stats">
              <div class="stat-item">
                <span class="count">{{ videoList.length }}</span>
                <span class="label">投稿</span>
              </div>
              <!-- 可以添加更多统计，如获赞数 -->
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="container">
        <el-tabs v-model="activeTab" class="user-tabs">
          <el-tab-pane label="投稿视频" name="videos">
            <div class="video-grid" v-if="videoList.length > 0">
              <div
                v-for="video in videoList"
                :key="video.id"
                class="video-card"
                @click="$router.push(`/videos/${video.id}`)"
              >
                <div class="video-cover">
                  <img :src="video.thumbnailUrl || 'https://via.placeholder.com/300x188'" :alt="video.title" />
                  <div class="video-duration">{{ formatDuration(video.duration) }}</div>
                  <div class="video-play-count">
                    <el-icon><VideoPlay /></el-icon>
                    {{ formatCount(video.viewCount) }}
                  </div>
                </div>
                <div class="video-info">
                  <h3 class="video-title">{{ video.title }}</h3>
                  <div class="video-meta">
                    <span class="upload-time">{{ formatDate(video.createTime) }}</span>
                    <span class="video-stats">
                      <el-icon><View /></el-icon> {{ formatCount(video.viewCount) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <el-empty description="暂无投稿视频" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { userAPI, videoAPI } from '../api/video'
import { VideoPlay, View } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userId = computed(() => route.params.userId)
const userInfo = ref(null)
const videoList = ref([])
const activeTab = ref('videos')

const loadUserInfo = async () => {
  try {
    const response = await userAPI.getUser(userId.value)
    userInfo.value = response.data
  } catch (error) {
    console.error('获取用户信息失败', error)
    ElMessage.error('获取用户信息失败')
  }
}

const loadUserVideos = async () => {
  try {
    const response = await videoAPI.getUserVideos(userId.value)
    if (response.data.success) {
      videoList.value = response.data.data
    }
  } catch (error) {
    console.error('获取用户视频失败', error)
  }
}

const formatCount = (count) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count
}

const formatDuration = (seconds) => {
  if (!seconds) return '00:00'
  const min = Math.floor(seconds / 60)
  const sec = seconds % 60
  return `${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadUserInfo()
  loadUserVideos()
})
</script>

<style scoped>
.user-space {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header-banner {
  height: 200px;
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
}

.user-profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  margin-top: -50px;
}

.user-profile-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: flex-end;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.user-avatar {
  margin-right: 24px;
  border: 4px solid white;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.user-info {
  flex: 1;
  padding-bottom: 10px;
}

.nickname {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.user-bio {
  color: #666;
  font-size: 14px;
  margin: 0 0 16px 0;
}

.user-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-item .count {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.stat-item .label {
  font-size: 12px;
  color: #999;
}

.main-content {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.user-tabs {
  background: white;
  padding: 20px;
  border-radius: 8px;
  min-height: 400px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.video-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.video-card:hover {
  transform: translateY(-5px);
}

.video-cover {
  position: relative;
  height: 180px;
}

.video-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.video-play-count {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.video-info {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.video-title {
  margin: 0 0 8px 0;
  font-size: 14px;
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.video-meta {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.upload-time {
  flex: 1;
}

.empty-state {
  padding: 40px 0;
}
</style>
