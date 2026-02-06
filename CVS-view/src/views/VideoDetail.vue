<template>
  <div class="video-detail-page">
    <!-- 顶部导航 -->
    <AppHeader :showBanner="false" />

    <div class="video-detail">
      <!-- 视频播放区域 -->
      <div class="video-player" v-if="video">
      <div class="video-container">
        <!-- 视频加载占位容器 -->
        <div 
          class="video-placeholder" 
          v-show="!videoLoaded"
          :style="{ backgroundImage: `url(${getMediaUrl(video.coverUrl)})` }"
        >
          <div class="video-loading-overlay">
            <el-icon class="is-loading video-loading-icon">
              <Loading />
            </el-icon>
            <p>视频加载?..</p>
          </div>
        </div>
        <video
          v-show="videoLoaded"
          :src="getMediaUrl(video.videoUrl)"
          controls
          :poster="getMediaUrl(video.coverUrl)"
          @play="onVideoPlay"
          @loadeddata="onVideoLoaded"
          @canplay="onVideoLoaded"
          @error="onVideoError"
        ></video>
        <div v-if="videoError" class="video-error-message">
          <el-icon><Warning /></el-icon>
          <p>视频加载失败，请检查OSS配置(CORS和读写权限)</p>
        </div>
      </div>

      <!-- 视频信息 -->
      <div class="video-info">
        <div class="uploader-info" v-if="video.uploaderId" @click="$router.push(`/user/${video.uploaderId}`)">
          <el-avatar :size="40" :src="getMediaUrl(video.uploaderAvatar) || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          <span class="uploader-name">{{ video.uploaderName || '匿名用户' }}</span>
        </div>
        <h1>{{ video.title }}</h1>
        <div class="video-meta">
          <span class="city">{{ video.city }}</span>
          <span class="date">{{ formatDate(video.concertDate) }}</span>
          <span class="views">{{ video.viewCount }} 次观看</span>
        </div>

        <!-- 操作按钮 -->
        <div class="video-actions">
          <el-button
            :type="isLiked ? 'primary' : ''"
            :icon="isLiked ? StarFilled : Star"
            @click="toggleLike"
            :loading="likeLoading"
          >
            {{ isLiked ? '已点赞' : '点赞' }} ({{ video.likeCount }})
          </el-button>

          <el-button
            :type="isCollected ? 'primary' : ''"
            :icon="isCollected ? CollectionTag : Collection"
            @click="toggleCollect"
            :loading="collectLoading"
          >
            {{ isCollected ? '已收藏' : '收藏' }} ({{ video.collectCount }})
          </el-button>

          <el-button
            :icon="Share"
            @click="shareVideo"
          >
            分享
          </el-button>

          <el-button
            v-if="userStore.isLoggedIn && video.uploaderId !== userStore.user.id"
            :icon="Warning"
            @click="reportVideo"
          >
            举报
          </el-button>
        </div>

        <!-- 视频描述 -->
        <div class="video-description" v-if="video.description">
          <h3>视频介绍</h3>
          <p>{{ video.description }}</p>
        </div>
      </div>
    </div>

    <!-- 评论区域 -->
    <div class="comment-section" v-if="video">
      <div class="comment-header">
        <h3>评论 ({{ comments.length }})</h3>
      </div>

      <!-- 评论输入�?-->
      <div class="comment-input" v-if="userStore.isLoggedIn">
        <el-input
          v-model="newComment"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
          maxlength="500"
          show-word-limit
        />
        <div class="comment-actions">
          <el-button
            type="primary"
            @click="submitComment"
            :loading="commentLoading"
            :disabled="!newComment.trim()"
          >
            发表评论
          </el-button>
        </div>
      </div>
      <div v-else class="login-prompt">
        <p>请先 <el-button type="text" @click="$router.push('/login')">登录</el-button> 后再评论</p>
      </div>

      <!-- 评论列表 -->
      <div class="comment-list">
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="comment-item"
        >
          <div class="comment-avatar">
            <el-avatar :src="getMediaUrl(comment.user?.avatar)">{{ comment.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
          </div>
          <div class="comment-content">
            <div class="comment-info">
              <span class="username">{{ comment.user?.nickname || comment.user?.username || '匿名用户' }}</span>
              <span class="time">{{ formatTime(comment.createTime) }}</span>
            </div>
            <p class="comment-text">{{ comment.content }}</p>
            <div class="comment-actions">
              <el-button
                type="text"
                size="small"
                @click="showReplyInput(comment)"
                v-if="userStore.isLoggedIn"
              >
                回复
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="deleteComment(String(comment.id))"
                v-if="userStore.isLoggedIn && (comment.userId === userStore.user.id || userStore.isAdmin)"
              >
                删除
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="reportComment(comment)"
                v-if="userStore.isLoggedIn && comment.userId !== userStore.user.id"
              >
                举报
              </el-button>
            </div>

            <!-- 回复输入�?-->
            <div v-if="replyingTo === comment.id" class="reply-input">
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="2"
                placeholder="写下你的回复..."
                maxlength="300"
                show-word-limit
              />
              <div class="reply-actions">
                <el-button size="small" @click="cancelReply">取消</el-button>
                <el-button
                  type="primary"
                  size="small"
                  @click="submitReply(String(comment.id))"
                  :loading="replyLoading"
                  :disabled="!replyContent.trim()"
                >
                  回复
                </el-button>
              </div>
            </div>

            <!-- 回复列表 -->
            <div v-if="comment.replies && comment.replies.length > 0" class="replies">
              <div
                v-for="reply in comment.replies"
                :key="reply.id"
                class="reply-item"
              >
                <div class="reply-avatar">
                  <el-avatar size="small" :src="getMediaUrl(reply.user?.avatar)">{{ reply.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
                </div>
                <div class="reply-content">
                  <div class="reply-info">
                    <span class="username">{{ reply.user?.nickname || reply.user?.username || '匿名用户' }}</span>
                    <span class="time">{{ formatTime(reply.createTime) }}</span>
                  </div>
                  <p class="reply-text">{{ reply.content }}</p>
                  <div class="reply-actions">
                    <el-button
                      type="text"
                      size="small"
                      @click="showReplyToReply(comment, reply)"
                      v-if="userStore.isLoggedIn"
                    >
                      回复
                    </el-button>
                    <el-button
                      type="text"
                      size="small"
                      @click="deleteComment(String(reply.id))"
                      v-if="userStore.isLoggedIn && (reply.userId === userStore.user.id || userStore.isAdmin)"
                    >
                      删除
                    </el-button>
                  </div>
                  
                  <!-- 回复的回复输入框 -->
                  <div v-if="replyingToReply === reply.id" class="reply-input">
                    <el-input
                      v-model="replyToReplyContent"
                      type="textarea"
                      :rows="2"
                      :placeholder="'回复 @' + (reply.user?.nickname || '用户') + '...'"
                      maxlength="300"
                      show-word-limit
                    />
                    <div class="reply-actions">
                      <el-button size="small" @click="cancelReplyToReply">取消</el-button>
                      <el-button
                        type="primary"
                        size="small"
                        @click="submitReplyToReply(comment, reply)"
                        :loading="replyLoading"
                        :disabled="!replyToReplyContent.trim()"
                      >
                        回复
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

      <!-- 加载状�?-->
      <div v-else class="loading">
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        <p>加载�?..</p>
      </div>
    </div>

    <!-- 举报对话框 -->
    <ReportDialog
      v-model="reportDialogVisible"
      :target-id="reportTargetId"
      :target-type="reportTargetType"
      @success="onReportSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { videoAPI, commentAPI, likeAPI, collectAPI } from '../api/video'
import { ElMessage } from 'element-plus'
import {
  Star,
  StarFilled,
  Collection,
  CollectionTag,
  Share,
  Loading,
  Warning
} from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import ReportDialog from '../components/ReportDialog.vue'
import { getMediaUrl } from '../utils/mediaUrl'

const route = useRoute()
const userStore = useUserStore()

const video = ref(null)
const comments = ref([])
const newComment = ref('')
const replyContent = ref('')
const replyingTo = ref(null)
const replyingToReply = ref(null) // 正在回复的回复ID
const replyToReplyContent = ref('') // 回复回复的内�?
const replyToReplyParentComment = ref(null) // 被回复的父评�?
const isLiked = ref(false)
const isCollected = ref(false)
const loading = ref(true)
const videoLoaded = ref(false)
const videoError = ref(false)
const commentLoading = ref(false)
const replyLoading = ref(false)
const likeLoading = ref(false)
const collectLoading = ref(false)

const videoId = computed(() => route.params.id)

// 举报相关
const reportDialogVisible = ref(false)
const reportTargetId = ref(null)
const reportTargetType = ref(null) // 0: 视频, 1: 评论

// 获取视频详情
const loadVideoDetail = async () => {
  try {
    const response = await videoAPI.getVideoDetail(videoId.value)
    if (response.data.success) {
      video.value = response.data.data
      await loadComments()
      await checkLikeStatus()
      await checkCollectStatus()
    } else {
      ElMessage.error(response.data.message || '视频不存在')
    }
  } catch (error) {
    ElMessage.error('加载视频失败')
    console.error('加载视频失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载评论
const loadComments = async () => {
  try {
    const response = await commentAPI.getVideoComments(videoId.value)
    if (response.data.success) {
      comments.value = response.data.data
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

// 检查点赞状�?
const checkLikeStatus = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const response = await likeAPI.checkLikeStatus(videoId.value)
    if (response.data.success) {
      isLiked.value = response.data.liked
    }
  } catch (error) {
    // 401错误是正常的（未登录），不需要显�?
    if (error.response?.status !== 401) {
      console.error('检查点赞状态失�?', error)
    }
  }
}

// 检查收藏状�?
const checkCollectStatus = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const response = await collectAPI.checkCollectStatus(videoId.value)
    if (response.data.success) {
      isCollected.value = response.data.collected
    }
  } catch (error) {
    // 401错误是正常的（未登录），不需要显�?
    if (error.response?.status !== 401) {
      console.error('检查收藏状态失�?', error)
    }
  }
}

// 点赞/取消点赞
const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  likeLoading.value = true
  try {
    const response = await likeAPI.toggleLike(videoId.value)
    if (response.data.success) {
      isLiked.value = response.data.liked
      video.value.likeCount += response.data.liked ? 1 : -1
      ElMessage.success(response.data.message)
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    likeLoading.value = false
  }
}

// 收藏/取消收藏
const toggleCollect = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  collectLoading.value = true
  try {
    const response = await collectAPI.toggleCollect(videoId.value)
    if (response.data.success) {
      isCollected.value = response.data.collected
      video.value.collectCount += response.data.collected ? 1 : -1
      ElMessage.success(response.data.message)
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    collectLoading.value = false
  }
}

// 分享视频
const shareVideo = () => {
  const url = window.location.href
  if (navigator.share) {
    navigator.share({
      title: video.value.title,
      text: `来看这个精彩的演唱会视频{video.value.title}`,
      url: url
    })
  } else {
    navigator.clipboard.writeText(url).then(() => {
      ElMessage.success('链接已复制到剪贴板')
    })
  }
}

// 提交评论
const submitComment = async () => {
  if (!newComment.value.trim()) return

  commentLoading.value = true
  try {
    const response = await commentAPI.addComment({
      videoId: videoId.value,
      content: newComment.value.trim()
    })

    if (response.data.success) {
      ElMessage.success('评论成功')
      newComment.value = ''
      await loadComments()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    commentLoading.value = false
  }
}

// 显示回复输入�?
const showReplyInput = (comment) => {
  replyingTo.value = comment.id
  replyContent.value = ''
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

// 提交回复
const submitReply = async (commentId) => {
  if (!replyContent.value.trim()) return

  replyLoading.value = true
  try {
    const response = await commentAPI.addComment({
      videoId: videoId.value,
      content: replyContent.value.trim(),
      parentId: commentId
    })

    if (response.data.success) {
      ElMessage.success('回复成功')
      cancelReply()
      await loadComments()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    replyLoading.value = false
  }
}

// 显示回复回复的输入框
const showReplyToReply = (parentComment, reply) => {
  // 关闭其他回复输入�?
  replyingTo.value = null
  replyContent.value = ''
  
  replyingToReply.value = reply.id
  replyToReplyContent.value = ''
  replyToReplyParentComment.value = parentComment
}

// 取消回复回复
const cancelReplyToReply = () => {
  replyingToReply.value = null
  replyToReplyContent.value = ''
  replyToReplyParentComment.value = null
}

// 提交回复回复
const submitReplyToReply = async (parentComment, reply) => {
  if (!replyToReplyContent.value.trim()) return

  replyLoading.value = true
  try {
    // 在回复内容前加上 @用户�?
    const replyToUsername = reply.user?.nickname || reply.user?.username || '用户'
    const contentWithAt = `@${replyToUsername} ${replyToReplyContent.value.trim()}`
    
    const response = await commentAPI.addComment({
      videoId: videoId.value,
      content: contentWithAt,
      parentId: String(parentComment.id), // 回复归属于父评论
      replyToUserId: String(reply.userId) // 被回复用户的ID，用于消息通知
    })

    if (response.data.success) {
      ElMessage.success('回复成功')
      cancelReplyToReply()
      await loadComments()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    replyLoading.value = false
  }
}

// 删除评论
const deleteComment = async (commentId) => {
  try {
    const response = await commentAPI.deleteComment(commentId)
    if (response.data.success) {
      ElMessage.success('删除成功')
      await loadComments()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 初始化浏览量更新
const initViewCount = async () => {
  try {
    await videoAPI.recordView(videoId.value)
  } catch (error) {
    console.error('记录浏览失败:', error)
  }
}

// 视频加载完成事件
const onVideoLoaded = () => {
  videoLoaded.value = true
}

// 视频加载失败
const onVideoError = (e) => {
  console.error('视频加载失败:', e)
  videoError.value = true
  videoLoaded.value = true // 隐藏placeholder
}

// 格式化日?
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 格式化时�?
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now - time

  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`

  return time.toLocaleDateString('zh-CN')
}

// 举报视频
const reportVideo = () => {
  reportTargetId.value = videoId.value
  reportTargetType.value = 0 // 视频类型
  reportDialogVisible.value = true
}

// 举报评论
const reportComment = (comment) => {
  reportTargetId.value = comment.id
  reportTargetType.value = 1 // 评论类型
  reportDialogVisible.value = true
}

// 举报成功回调
const onReportSuccess = () => {
  ElMessage.success('举报成功，我们会尽快处理')
  reportDialogVisible.value = false
}

onMounted(() => {
  loadVideoDetail()
  initViewCount()  // 记录浏览
})
</script>

<style scoped>
.video-detail-page {
  min-height: 100vh;
  background-color: var(--fnds-bg, #f5f5f5);
}

.video-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.video-player {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
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

.video-error-message {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  z-index: 10;
}

.video-error-message .el-icon {
  font-size: 48px;
  color: #f56c6c;
  margin-bottom: 16px;
}

.video-info {
  padding: 20px;
}

.video-info h1 {
  margin: 0 0 15px 0;
  font-size: 24px;
  color: #333;
}

.uploader-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  cursor: pointer;
  padding: 10px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.uploader-info:hover {
  background-color: #f5f5f5;
}

.uploader-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.video-meta {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  font-size: 14px;
  color: #666;
}

.video-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.video-description h3 {
  margin: 20px 0 10px 0;
  font-size: 18px;
  color: #333;
}

.video-description p {
  line-height: 1.6;
  color: #666;
  margin: 0;
}

.comment-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.comment-header h3 {
  margin: 0 0 20px 0;
  color: #333;
}

.comment-input {
  margin-bottom: 20px;
}

.comment-actions {
  text-align: right;
  margin-top: 10px;
}

.login-prompt {
  text-align: center;
  padding: 20px;
  color: #666;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.username {
  font-weight: 500;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  margin: 8px 0;
  line-height: 1.5;
  color: #333;
}

.comment-actions {
  margin-top: 5px;
}

.reply-input {
  margin-top: 10px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
}

.reply-actions {
  text-align: right;
  margin-top: 8px;
}

.replies {
  margin-top: 15px;
  padding-left: 20px;
  border-left: 2px solid #f0f0f0;
}

.reply-item {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-avatar {
  flex-shrink: 0;
  margin-top: 2px;
}

.reply-content {
  flex: 1;
}

.reply-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
}

.reply-info .username {
  font-size: 14px;
}

.reply-info .time {
  font-size: 11px;
}

.reply-text {
  margin: 5px 0;
  font-size: 14px;
  line-height: 1.4;
  color: #555;
}

.loading {
  text-align: center;
  padding: 50px 20px;
}

.loading p {
  margin: 15px 0 0 0;
  color: #666;
}

@media (max-width: 768px) {
  .video-meta {
    flex-direction: column;
    gap: 5px;
  }

  .video-actions {
    flex-wrap: wrap;
  }

  .comment-item {
    flex-direction: column;
    gap: 8px;
  }

  .reply-item {
    flex-direction: column;
    gap: 6px;
  }
}
</style>
