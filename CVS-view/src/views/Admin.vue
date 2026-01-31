<template>
  <div class="admin-page">
    <!-- 顶部导航 -->
    <AppHeader :showSearch="false" :showBanner="false" />

    <div class="admin">
      <el-card class="admin-card">
        <template #header>
          <div class="card-header">
            <h2>管理员后台</h2>
          </div>
        </template>

      <!-- 统计信息 -->
      <div class="stats">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ stats.pendingVideos }}</div>
              <div class="stat-label">待审核视频</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ stats.totalVideos }}</div>
              <div class="stat-label">总视频数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ stats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ stats.todayUploads }}</div>
              <div class="stat-label">今日上传</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 标签�?-->
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

        <!-- 用户管理 (仅系统管理员) -->
        <el-tab-pane label="用户管理" name="users" v-if="userStore.isSystemAdmin">
          <div class="user-management">
            <div class="filter-bar">
              <el-input
                v-model="userSearchKeyword"
                placeholder="搜索用户名或昵称"
                style="width: 300px"
                @keyup.enter="loadUsers"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="loadUsers">搜索</el-button>
            </div>

            <el-table
              :data="users"
              style="width: 100%"
              v-loading="tableLoading"
            >
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column prop="nickname" label="昵称" width="120" />
              <el-table-column prop="email" label="邮箱" width="180" />
              <el-table-column label="角色" width="120">
                <template #default="scope">
                  <el-tag :type="scope.row.role === 1 ? 'danger' : (scope.row.role === 2 ? 'warning' : 'info')">
                    {{ scope.row.role === 1 ? '系统管理员' : (scope.row.role === 2 ? '城市管理员' : '普通用户') }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
                    {{ scope.row.status === 0 ? '正常' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="注册时间" width="150">
                <template #default="scope">
                  {{ formatDate(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button
                    type="warning"
                    size="small"
                    @click="toggleUserStatus(scope.row)"
                  >
                    {{ scope.row.status === 0 ? '禁用' : '启用' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination" v-if="users.length > 0">
              <el-pagination
                v-model:current-page="userPagination.current"
                v-model:page-size="userPagination.size"
                :total="userPagination.total"
                layout="total, prev, pager, next"
                @current-change="handleUserPageChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 城市管理 (仅系统管理员) -->
        <el-tab-pane label="城市管理" name="cities" v-if="userStore.isSystemAdmin">
          <div class="city-management">
            <div class="filter-bar">
              <el-button type="primary" @click="showAddCityDialog">
                <el-icon><Plus /></el-icon> 添加城市
              </el-button>
            </div>

            <el-table
              :data="cities"
              style="width: 100%"
              v-loading="tableLoading"
            >
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="城市名称" width="150" />
              <el-table-column label="创建时间" width="200">
                <template #default="scope">
                  {{ formatDate(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button
                    type="primary"
                    size="small"
                    @click="showEditCityDialog(scope.row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="deleteCity(scope.row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 城市管理员管理 (仅系统管理员) -->
        <el-tab-pane label="城市管理员" name="cityManagers" v-if="userStore.isSystemAdmin">
          <div class="city-manager-management">
            <div class="filter-bar">
              <el-button type="primary" @click="showAssignCityDialog">
                <el-icon><Plus /></el-icon> 分配城市
              </el-button>
            </div>

            <el-table
              :data="cityManagers"
              style="width: 100%"
              v-loading="tableLoading"
            >
              <el-table-column prop="managerName" label="管理员昵称" width="150" />
              <el-table-column prop="cityName" label="管理的城市" width="150" />
              <el-table-column label="分配时间" width="200">
                <template #default="scope">
                  {{ formatDate(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button
                    type="danger"
                    size="small"
                    @click="removeCityFromManager(scope.row)"
                  >
                    移除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <h4 style="margin-top: 30px;">城市管理员列表</h4>
            <el-table
              :data="cityManagerUsers"
              style="width: 100%"
              v-loading="tableLoading"
            >
              <el-table-column prop="id" label="用户ID" width="100" />
              <el-table-column prop="nickname" label="昵称" width="150" />
              <el-table-column prop="username" label="用户名" width="150" />
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button
                    type="danger"
                    size="small"
                    @click="removeCityManagerRole(scope.row)"
                  >
                    取消角色
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 城市编辑对话框-->
    <el-dialog
      v-model="cityDialogVisible"
      :title="cityForm.id ? '编辑城市' : '添加城市'"
      width="400px"
    >
      <el-form :model="cityForm" label-width="80px">
        <el-form-item label="城市名称">
          <el-input v-model="cityForm.name" placeholder="请输入城市名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cityDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="citySubmitting" @click="submitCity">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 拒绝审核对话�?-->
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

    <!-- 分配城市对话框 -->
    <el-dialog
      v-model="assignCityDialogVisible"
      title="分配城市给管理员"
      width="500px"
    >
      <el-form :model="assignCityForm" label-width="100px">
        <el-form-item label="选择用户">
          <el-select v-model="assignCityForm.userId" placeholder="选择要分配的用户" style="width: 100%">
            <el-option
              v-for="user in availableUsersForCityManager"
              :key="user.id"
              :label="user.nickname || user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择城市">
          <el-select v-model="assignCityForm.cityId" placeholder="选择要分配的城市" style="width: 100%">
            <el-option
              v-for="city in availableCitiesForAssign"
              :key="city.id"
              :label="city.name"
              :value="city.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="assignCityForm.setAsManager">同时将该用户设为城市管理员</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="assignCityDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="assignCitySubmitting" @click="submitAssignCity">
            确定
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
import { videoAPI, userAPI, adminAPI, cityAPI, cityManagerAPI } from '../api/video'
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
  totalUsers: 0,
  todayUploads: 0
})

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

// 用户管理相关
const users = ref([])
const userSearchKeyword = ref('')
const userPagination = reactive({
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

// 城市管理相关
const cities = ref([])
const cityDialogVisible = ref(false)
const citySubmitting = ref(false)
const cityForm = reactive({
  id: null,
  name: ''
})

// 城市管理员管理相关
const cityManagers = ref([])
const cityManagerUsers = ref([])
const assignCityDialogVisible = ref(false)
const assignCitySubmitting = ref(false)
const assignCityForm = reactive({
  userId: null,
  cityId: null,
  setAsManager: true
})
const availableUsersForCityManager = ref([])
const availableCitiesForAssign = ref([])

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await adminAPI.getStats()
    if (response.data.success) {
      const data = response.data.data
      stats.pendingVideos = data.pendingVideos || 0
      stats.totalVideos = data.totalVideos || 0
      stats.totalUsers = data.totalUsers || 0
      stats.todayUploads = data.todayUploads || 0
    }
  } catch (error) {
    if (!handle401Error(error)) {
      console.error('加载统计数据失败:', error)
    }
  }
}

// 加载待审核视�?
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

// 查看视频详情（根据状态跳转不同页面）
const viewVideoDetail = (video) => {
  // 状�?1 表示已通过审核，跳转到正常的视频详情页
  if (video.status === 1) {
    router.push(`/videos/${video.id}`)
  } else {
    // 未通过审核的视频跳转到管理员预览页
    router.push(`/admin/preview/${video.id}`)
  }
}

// 显示拒绝对话�?
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

// 加载所有视�?
const loadAllVideos = async () => {
  try {
    tableLoading.value = true
    const response = await adminAPI.getAllVideos({
      current: managePagination.current,
      size: managePagination.size,
      status: manageFilter.status || undefined,
      keyword: manageFilter.keyword || undefined
    })
    if (response.data.success) {
      allVideos.value = response.data.data
      managePagination.total = response.data.total
    }
  } catch (error) {
    if (!handle401Error(error)) {
      console.error('加载视频列表失败:', error)
      ElMessage.error('加载视频列表失败')
    }
  } finally {
    tableLoading.value = false
  }
}

// 加载用户列表
const loadUsers = async () => {
  try {
    tableLoading.value = true
    const response = await userAPI.getAllUsers({
      current: userPagination.current,
      size: userPagination.size,
      keyword: userSearchKeyword.value || undefined
    })
    if (response.data.success) {
      users.value = response.data.data
      userPagination.total = response.data.total
    }
  } catch (error) {
    if (!handle401Error(error)) {
      console.error('加载用户列表失败:', error)
      ElMessage.error('加载用户列表失败')
    }
  } finally {
    tableLoading.value = false
  }
}

// 删除视频
const deleteVideo = async (video) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除视频${video.title}"吗？此操作不可恢复。`,
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

// 切换用户状�?
const toggleUserStatus = async (user) => {
  try {
    const action = user.status === 0 ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确定�?{action}用户"${user.nickname}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await userAPI.toggleUserStatus(user.id)
    if (response.data.success) {
      ElMessage.success(response.data.message || `${action}成功`)
      loadUsers()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
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

const handleUserPageChange = (page) => {
  userPagination.current = page
  loadUsers()
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

// 城市管理相关方法
const loadCities = async () => {
  try {
    tableLoading.value = true
    const response = await cityAPI.getAllCities()
    if (response.data.success) {
      cities.value = response.data.data
    }
  } catch (error) {
    if (!handle401Error(error)) {
      ElMessage.error('加载城市列表失败')
      console.error('加载城市列表失败:', error)
    }
  } finally {
    tableLoading.value = false
  }
}

const showAddCityDialog = () => {
  cityForm.id = null
  cityForm.name = ''
  cityDialogVisible.value = true
}

const showEditCityDialog = (city) => {
  cityForm.id = city.id
  cityForm.name = city.name
  cityDialogVisible.value = true
}

const submitCity = async () => {
  if (!cityForm.name.trim()) {
    ElMessage.warning('请输入城市名')
    return
  }

  try {
    citySubmitting.value = true
    let response
    if (cityForm.id) {
      response = await cityAPI.updateCity(cityForm.id, cityForm)
    } else {
      response = await cityAPI.addCity(cityForm)
    }

    if (response.data.success) {
      ElMessage.success(cityForm.id ? '更新成功' : '添加成功')
      cityDialogVisible.value = false
      loadCities()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('操作失败:', error)
  } finally {
    citySubmitting.value = false
  }
}

const deleteCity = async (city) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除城�?${city.name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await cityAPI.deleteCity(city.id)
    if (response.data.success) {
      ElMessage.success('删除成功')
      loadCities()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 城市管理员管理相关方法
const loadCityManagers = async () => {
  try {
    tableLoading.value = true
    const response = await cityManagerAPI.getAllCityManagers()
    if (response.data.success) {
      cityManagers.value = response.data.cityManagers || []
      cityManagerUsers.value = response.data.managerUsers || []
    }
  } catch (error) {
    if (!handle401Error(error)) {
      console.error('加载城市管理员失败:', error)
    }
  } finally {
    tableLoading.value = false
  }
}

const showAssignCityDialog = async () => {
  try {
    const userResponse = await userAPI.getAllUsers({ size: 100 })
    if (userResponse.data.success) {
      availableUsersForCityManager.value = userResponse.data.data.filter(u => u.role === 0 || u.role === 2)
    }
    
    const cityResponse = await cityAPI.getAllCities()
    if (cityResponse.data.success) {
      const assignedCityIds = cityManagers.value.map(cm => cm.cityId)
      availableCitiesForAssign.value = cityResponse.data.data.filter(c => !assignedCityIds.includes(c.id))
    }
    
    assignCityForm.userId = null
    assignCityForm.cityId = null
    assignCityForm.setAsManager = true
    assignCityDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const submitAssignCity = async () => {
  if (!assignCityForm.userId || !assignCityForm.cityId) {
    ElMessage.warning('请选择用户和城市')
    return
  }

  try {
    assignCitySubmitting.value = true
    
    const selectedUser = availableUsersForCityManager.value.find(u => u.id === assignCityForm.userId)
    if (assignCityForm.setAsManager && selectedUser && selectedUser.role === 0) {
      await cityManagerAPI.setUserAsCityManager(assignCityForm.userId)
    }
    
    const response = await cityManagerAPI.assignCity(assignCityForm.userId, assignCityForm.cityId)
    if (response.data.success) {
      ElMessage.success('城市分配成功')
      assignCityDialogVisible.value = false
      loadCityManagers()
    } else {
      ElMessage.error(response.data.message || '分配失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    assignCitySubmitting.value = false
  }
}

const removeCityFromManager = async (cityManager) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除"${cityManager.managerName}"对城市"${cityManager.cityName}"的管理权限吗？`,
      '确认移除',
      { confirmButtonText: '确定移除', cancelButtonText: '取消', type: 'warning' }
    )

    const response = await cityManagerAPI.removeCity(cityManager.userId, cityManager.cityId)
    if (response.data.success) {
      ElMessage.success('移除成功')
      loadCityManagers()
    } else {
      ElMessage.error(response.data.message || '移除失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('移除失败')
  }
}

const removeCityManagerRole = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消"${user.nickname}"的城市管理员角色吗？这将同时移除其所有管理的城市。`,
      '确认取消',
      { confirmButtonText: '确定取消', cancelButtonText: '取消', type: 'warning' }
    )

    const response = await cityManagerAPI.removeCityManagerRole(user.id)
    if (response.data.success) {
      ElMessage.success('取消成功')
      loadCityManagers()
    } else {
      ElMessage.error(response.data.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('取消失败')
  }
}

// 标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'review' && pendingVideos.value.length === 0) {
    loadPendingVideos()
  } else if (tabName === 'manage' && allVideos.value.length === 0) {
    loadAllVideos()
  } else if (tabName === 'users' && users.value.length === 0) {
    loadUsers()
  } else if (tabName === 'cities' && cities.value.length === 0) {
    loadCities()
  } else if (tabName === 'cityManagers' && cityManagers.value.length === 0) {
    loadCityManagers()
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
  color: #409eff;
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
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
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
