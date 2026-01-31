<template>
  <div class="admin-page">
    <!-- 顶部导航 -->
    <AppHeader :showSearch="false" :showBanner="false" />

    <div class="admin">
      <el-card class="admin-card">
        <template #header>
          <div class="card-header">
            <h2>城市管理员后台</h2>
          </div>
        </template>

      <!-- 统计信息 -->
      <div class="stats">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-number">{{ stats.pendingVideos }}</div>
              <div class="stat-label">待审核视频</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-number">{{ stats.totalVideos }}</div>
              <div class="stat-label">管理城市总视频数</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-number">{{ stats.todayUploads }}</div>
              <div class="stat-label">今日上传</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 管理的城市 -->
      <div class="managed-cities" v-if="managedCities.length > 0">
        <el-tag v-for="city in managedCities" :key="city" type="success" style="margin-right: 8px;">
          {{ city }}
        </el-tag>
      </div>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab" style="margin-top: 30px;" @tab-change="handleTabChange">
        <!-- 视频审核 -->
        <el-tab-pane label="视频审核" name="review">
          <div class="review-section">
            <div class="filter-bar">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索视频标题或用户名"
                style="width: 300px"
                @keyup.enter="loadPendingVideos"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="loadPendingVideos">搜索</el-button>
            </div>

            <div class="video-list">
              <div v-if="pendingVideos.length === 0" class="empty-state">
                <el-empty description="暂无待审核视频">
                  <el-button type="primary" @click="loadPendingVideos">刷新</el-button>
                </el-empty>
              </div>

              <el-card
                v-for="video in pendingVideos"
                :key="video.id"
                class="video-card"
              >
                <div class="video-header">
                  <div class="video-basic-info">
                    <h4>{{ video.title }}</h4>
                    <p class="video-meta">
                      <span>上传者: {{ video.uploaderName || video.uploaderId }}</span>
                      <span>城市: {{ video.city }}</span>
                      <span>上传时间: {{ formatDate(video.createTime) }}</span>
                    </p>
                  </div>
                  <div class="video-actions">
                    <el-button type="info" @click="previewVideo(video)">预览</el-button>
                    <el-button type="success" @click="reviewVideo(video, 1)">通过</el-button>
                    <el-button type="danger" @click="showRejectDialog(video)">拒绝</el-button>
                  </div>
                </div>

                <div class="video-content">
                  <div class="video-preview">
                    <img
                      :src="getMediaUrl(video.thumbnailUrl) || '/placeholder.jpg'"
                      :alt="video.title"
                      class="video-thumbnail"
                      @click="previewVideo(video)"
                    />
                    <div class="play-overlay" @click="previewVideo(video)">
                      <el-icon :size="48"><VideoPlay /></el-icon>
                    </div>
                  </div>
                  <div class="video-details">
                    <div class="detail-item">
                      <label>演唱会日期:</label>
                      <span>{{ formatDate(video.concertDate) }}</span>
                    </div>
                    <div class="detail-item">
                      <label>视频时长:</label>
                      <span>{{ formatDuration(video.duration) }}</span>
                    </div>
                    <div class="detail-item">
                      <label>描述:</label>
                      <span>{{ video.description }}</span>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>

            <!-- 分页 -->
            <div class="pagination" v-if="pendingVideos.length > 0">
              <el-pagination
                v-model:current-page="pagination.current"
                v-model:page-size="pagination.size"
                :total="pagination.total"
                layout="total, prev, pager, next"
                @current-change="handlePageChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 视频管理 -->
        <el-tab-pane label="视频管理" name="manage">
          <div class="manage-section">
            <div class="filter-bar">
              <el-select v-model="manageFilter.status" placeholder="选择状态" style="width: 120px">
                <el-option label="全部" value="" />
                <el-option label="审核通过" :value="1" />
                <el-option label="审核拒绝" :value="2" />
              </el-select>
              <el-input
                v-model="manageFilter.keyword"
                placeholder="搜索视频标题"
                style="width: 300px"
                @keyup.enter="loadAllVideos"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="loadAllVideos">搜索</el-button>
            </div>

            <el-table
              :data="allVideos"
              style="width: 100%"
              v-loading="tableLoading"
            >
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip />
              <el-table-column prop="city" label="城市" width="100" />
              <el-table-column label="上传者" width="120">
                <template #default="scope">
                  {{ scope.row.uploaderName || scope.row.uploaderId }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusTagType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="viewCount" label="观看次数" width="100" />
              <el-table-column label="上传时间" width="150">
                <template #default="scope">
                  {{ formatDate(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button
                    type="primary"
                    size="small"
                    @click="viewVideoDetail(scope.row)"
                  >
                    查看
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="deleteVideo(scope.row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination" v-if="allVideos.length > 0">
              <el-pagination
                v-model:current-page="managePagination.current"
                v-model:page-size="managePagination.size"
                :total="managePagination.total"
                layout="total, prev, pager, next"
                @current-change="handleManagePageChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 拒绝审核对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝审核"
      width="500px"
    >
      <div class="reject-form">
        <p>请填写拒绝原因：</p>
        <el-input
          v-model="rejectReason"
          type="textarea"
          :rows="4"
          placeholder="请输入拒绝审核的原因..."
          maxlength="200"
          show-word-limit
        />
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="reviewing" @click="confirmReject">
            确认拒绝
          </el-button>
        </span>
      </template>
    </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { videoAPI, adminAPI } from '../api/video'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import { useUserStore } from '../stores/user'
import { getMediaUrl } from '../utils/mediaUrl'

const router = useRouter()
const userStore = useUserStore()

// 处理401错误
const handle401Error = (error) => {
  if (error.response?.status === 401) {
    ElMessage.error('登录已过期，请重新登录')
    userStore.logout()
    router.push('/login')
    return true
  }
  return false
}

const activeTab = ref('review')

// 统计数据
const stats = reactive({
  pendingVideos: 0,
  totalVideos: 0,
  todayUploads: 0
})

// 管理的城市列表
const managedCities = ref([])

// 审核相关
const pendingVideos = ref([])
const searchKeyword = ref('')
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 管理相关
const allVideos = ref([])
const manageFilter = reactive({
  status: '',
  keyword: ''
})
const managePagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 拒绝审核相关
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentRejectVideo = ref(null)
const reviewing = ref(false)
const tableLoading = ref(false)

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await adminAPI.getStats()
    if (response.data.success) {
      const data = response.data.data
      stats.pendingVideos = data.pendingVideos || 0
      stats.totalVideos = data.totalVideos || 0
      stats.todayUploads = data.todayUploads || 0
      managedCities.value = data.managedCities || []
    }
  } catch (error) {
    if (!handle401Error(error)) {
      console.error('加载统计数据失败:', error)
    }
  }
}

// 加载待审核视频
const loadPendingVideos = async () => {
  try {
    tableLoading.value = true
    const response = await videoAPI.getPendingVideos()
    if (response.data.success) {
      pendingVideos.value = response.data.data
      pagination.total = pendingVideos.value.length
    }
  } catch (error) {
    if (!handle401Error(error)) {
      ElMessage.error('加载待审核视频失败')
      console.error('加载待审核视频失败:', error)
    }
  } finally {
    tableLoading.value = false
  }
}

// 审核视频
const reviewVideo = async (video, status) => {
  try {
    reviewing.value = true
    const reviewData = {
      status: status,
      reviewComment: status === 1 ? '审核通过' : rejectReason.value
    }

    const response = await videoAPI.reviewVideo(video.id, reviewData)
    if (response.data.success) {
      ElMessage.success(status === 1 ? '审核通过' : '审核拒绝')
      loadPendingVideos()
      loadStats()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('审核失败')
    console.error('审核失败:', error)
  } finally {
    reviewing.value = false
  }
}

// 预览视频
const previewVideo = (video) => {
  router.push(`/admin/preview/${video.id}`)
}

// 查看视频详情
const viewVideoDetail = (video) => {
  if (video.status === 1) {
    router.push(`/videos/${video.id}`)
  } else {
    router.push(`/admin/preview/${video.id}`)
  }
}

// 显示拒绝对话框
const showRejectDialog = (video) => {
  currentRejectVideo.value = video
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  reviewVideo(currentRejectVideo.value, 2)
  rejectDialogVisible.value = false
}

// 加载所有视频
const loadAllVideos = async () => {
  try {
    tableLoading.value = true
    const params = {
      current: managePagination.current,
      size: managePagination.size
    }
    if (manageFilter.status !== '') {
      params.status = manageFilter.status
    }
    if (manageFilter.keyword) {
      params.keyword = manageFilter.keyword
    }

    const response = await adminAPI.getAllVideos(params)
    if (response.data.success) {
      allVideos.value = response.data.data
      managePagination.total = response.data.total
    }
  } catch (error) {
    if (!handle401Error(error)) {
      ElMessage.error('加载视频列表失败')
      console.error('加载视频列表失败:', error)
    }
  } finally {
    tableLoading.value = false
  }
}

// 删除视频
const deleteVideo = async (video) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除视频"${video.title}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await adminAPI.deleteVideo(video.id)
    if (response.data.success) {
      ElMessage.success('删除成功')
      loadAllVideos()
      loadStats()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 分页处理
const handlePageChange = (page) => {
  pagination.current = page
  loadPendingVideos()
}

const handleManagePageChange = (page) => {
  managePagination.current = page
  loadAllVideos()
}

// 工具函数
const getStatusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知'
  }
}

const getStatusTagType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return ''
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const formatDuration = (seconds) => {
  if (!seconds) return '未知'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'review' && pendingVideos.value.length === 0) {
    loadPendingVideos()
  } else if (tabName === 'manage' && allVideos.value.length === 0) {
    loadAllVideos()
  }
}

onMounted(() => {
  loadStats()
  loadPendingVideos()
})
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background-color: var(--fnds-bg, #f5f5f5);
}

.admin {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.admin-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #67c23a;
}

.stats {
  margin-top: 20px;
}

.stat-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #67c23a;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.managed-cities {
  margin-top: 15px;
  padding: 10px;
  background: #f0f9eb;
  border-radius: 4px;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}

.video-list {
  margin-top: 20px;
}

.video-card {
  margin-bottom: 20px;
}

.video-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.video-basic-info h4 {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.video-meta {
  display: flex;
  gap: 15px;
  font-size: 14px;
  color: #666;
  margin: 0;
}

.video-actions {
  display: flex;
  gap: 10px;
}

.video-content {
  display: flex;
  gap: 20px;
}

.video-preview {
  flex: 0 0 300px;
  position: relative;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.video-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.4);
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.video-preview:hover .play-overlay {
  opacity: 1;
}

.video-details {
  flex: 1;
  padding-left: 20px;
}

.detail-item {
  margin-bottom: 10px;
}

.detail-item label {
  font-weight: 500;
  color: #606266;
  margin-right: 10px;
  display: inline-block;
  min-width: 80px;
}

.detail-item span {
  color: #909399;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.reject-form p {
  margin-bottom: 10px;
  color: #606266;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 768px) {
  .video-content {
    flex-direction: column;
  }

  .stats .el-col {
    margin-bottom: 15px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .video-header {
    flex-direction: column;
    gap: 10px;
  }

  .video-actions {
    justify-content: flex-end;
  }
}
</style>
