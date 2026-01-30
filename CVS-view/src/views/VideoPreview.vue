<template>
  <div class="video-preview-page">
    <!-- 顶部导航 -->
    <AppHeader />

    <div class="preview-container">
      <!-- 管理员提示横�?-->
      <div class="admin-banner">
        <el-icon><Warning /></el-icon>
        <span>管理员预览模�?- 此视频尚未通过审核</span>
        <el-tag :type="statusTagType" size="small">{{ statusText }}</el-tag>
      </div>

      <!-- 视频播放区域 -->
      <div class="video-player" v-if="video">
        <div class="video-container">
          <!-- 视频加载占位容器 -->
          <div 
            class="video-placeholder" 
            v-show="!videoLoaded"
            :style="{ backgroundImage: `url(${video.coverUrl})` }"
          >
            <div class="video-loading-overlay">
              <el-icon class="is-loading video-loading-icon">
                <Loading />
              </el-icon>
              <p>视频加载�?..</p>
            </div>
          </div>
          <video
            v-show="videoLoaded"
            :src="video.videoUrl"
            controls
            :poster="video.coverUrl"
            @loadeddata="onVideoLoaded"
            @canplay="onVideoLoaded"
          ></video>
        </div>

        <!-- 视频信息 -->
        <div class="video-info">
          <h1>{{ video.title }}</h1>
          <div class="video-meta">
            <span class="city">{{ video.city }}</span>
            <span class="date">{{ formatDate(video.concertDate) }}</span>
            <span class="uploader">上传�? {{ video.uploaderName || video.uploaderId }}</span>
            <span class="upload-time">上传时间: {{ formatTime(video.createTime) }}</span>
          </div>

          <!-- 视频描述 -->
          <div class="video-description" v-if="video.description">
            <h3>视频介绍</h3>
            <p>{{ video.description }}</p>
          </div>

          <!-- 审核操作 -->
          <div class="review-section">
            <h3>审核操作</h3>
            <el-input
              v-model="reviewComment"
              type="textarea"
              :rows="3"
              placeholder="请输入审核意见（可选）"
              maxlength="500"
              show-word-limit
            />
            <div class="review-actions">
              <el-button 
                type="success" 
                size="large"
                @click="handleReview(1)"
                :loading="reviewLoading"
              >
                <el-icon><Check /></el-icon>
                通过审核
              </el-button>
              <el-button 
                type="danger" 
                size="large"
                @click="handleReview(2)"
                :loading="reviewLoading"
              >
                <el-icon><Close /></el-icon>
                拒绝
              </el-button>
              <el-button 
                size="large"
                @click="goBack"
              >
                返回列表
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载状�?-->
      <div v-else-if="loading" class="loading">
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        <p>加载�?..</p>
      </div>

      <!-- 错误状�?-->
      <div v-else class="error-state">
        <el-icon class="error-icon"><Warning /></el-icon>
        <p>{{ errorMessage || '视频加载失败' }}</p>
        <el-button type="primary" @click="goBack">返回列表</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { videoAPI } from '../api/video'
import { ElMessage } from 'element-plus'
import {
  Loading,
  Warning,
  Check,
  Close
} from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const video = ref(null)
const loading = ref(true)
const videoLoaded = ref(false)
const reviewComment = ref('')
const reviewLoading = ref(false)
const errorMessage = ref('')

const videoId = computed(() => route.params.id)

const statusText = computed(() => {
  if (!video.value) return ''
  switch (video.value.status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知'
  }
})

const statusTagType = computed(() => {
  if (!video.value) return 'info'
  switch (video.value.status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
})

// 加载视频详情
const loadVideo = async () => {
  // 检查管理员权限
  if (!userStore.isAdmin) {
    ElMessage.error('权限不足，请以管理员身份登录')
    router.push('/login')
    return
  }

  try {
    const response = await videoAPI.previewVideo(videoId.value)
    if (response.data.success) {
      video.value = response.data.data
    } else {
      errorMessage.value = response.data.message || '视频不存在'
    }
  } catch (error) {
    console.error('加载视频失败:', error)
    if (error.response?.status === 403) {
      errorMessage.value = '权限不足'
    } else if (error.response?.status === 404) {
      errorMessage.value = '视频不存在'
    } else {
      errorMessage.value = '加载失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

// 视频加载完成
const onVideoLoaded = () => {
  videoLoaded.value = true
}

// 处理审核
const handleReview = async (status) => {
  reviewLoading.value = true
  try {
    const response = await videoAPI.reviewVideo(videoId.value, {
      status,
      reviewComment: reviewComment.value
    })
    if (response.data.success) {
      ElMessage.success(status === 1 ? '视频已通过审核' : '视频已拒绝')
      router.push('/admin/review')
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('审核失败:', error)
  } finally {
    reviewLoading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push('/admin/review')
}

// 格式化日�?
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 格式化时�?
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const time = new Date(timeStr)
  return time.toLocaleString('zh-CN')
}

onMounted(() => {
  loadVideo()
})
</script>

<style scoped>
.video-preview-page {
  min-height: 100vh;
  background-color: var(--fnds-bg, #f5f5f5);
}

.preview-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.admin-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  border-radius: 8px;
  margin-bottom: 20px;
  font-weight: 500;
}

.admin-banner .el-icon {
  font-size: 20px;
}

.video-player {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.video-container {
  position: relative;
  background: #000;
  min-height: 400px;
}

.video-placeholder {
  width: 100%;
  height: 450px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
}

.video-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.video-loading-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.video-loading-overlay p {
  margin: 0;
  font-size: 16px;
}

.video-container video {
  width: 100%;
  max-height: 500px;
  display: block;
}

.video-info {
  padding: 20px;
}

.video-info h1 {
  margin: 0 0 15px 0;
  font-size: 24px;
  color: #333;
}

.video-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
}

.video-description {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.video-description h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.video-description p {
  margin: 0;
  line-height: 1.6;
  color: #666;
}

.review-section {
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.review-section h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #333;
}

.review-actions {
  display: flex;
  gap: 15px;
  margin-top: 15px;
}

.loading,
.error-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.loading .el-icon,
.error-icon {
  font-size: 48px;
  color: #909399;
}

.error-icon {
  color: #f56c6c;
}

.loading p,
.error-state p {
  margin: 15px 0;
  color: #666;
  font-size: 16px;
}

@media (max-width: 768px) {
  .video-meta {
    flex-direction: column;
    gap: 8px;
  }

  .review-actions {
    flex-direction: column;
  }

  .admin-banner {
    flex-wrap: wrap;
  }
}
</style>
