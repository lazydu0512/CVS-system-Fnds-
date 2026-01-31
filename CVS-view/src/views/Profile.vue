<template>
  <div class="profile-page">
    <!-- 顶部导航 -->
    <AppHeader :showSearch="false" :showBanner="false" />

    <div class="profile">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <h2>个人中心</h2>
          </div>
        </template>

        <!-- 用户信息 -->
        <div class="user-info">
          <div class="avatar-section">
            <div class="avatar-wrapper" @click="triggerAvatarUpload">
              <el-avatar :size="80" :src="avatarUrl">
                {{ userStore.user?.nickname?.charAt(0)?.toUpperCase() || 'U' }}
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon><Camera /></el-icon>
              </div>
            </div>
            <input
              ref="avatarInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleAvatarChange"
            />
          </div>
        <div class="info-section">
          <h3>{{ userStore.user.nickname }}</h3>
          <p class="username">@{{ userStore.user.username }}</p>
          <p class="role">{{ userStore.user.role === 1 ? '城市管理者' : '普通用户' }}</p>
          <p class="email" v-if="userStore.user.email">{{ userStore.user.email }}</p>
          <p class="join-date">加入时间：{{ formatDate(userStore.user.createTime) }}</p>
        </div>
        <div class="actions-section">
          <el-button type="primary" @click="showEditDialog = true">编辑资料</el-button>
        </div>
      </div>
    </el-card>

    <!-- 标签页 -->
    <el-card class="content-card" style="margin-top: 20px;">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <!-- 我的视频 -->
        <el-tab-pane label="我的视频" name="videos">
          <div class="video-list">
            <div v-if="myVideos.length === 0" class="empty-state">
              <el-empty description="你还没有上传视频">
                <el-button type="primary" @click="$router.push('/upload')">上传第一个视频</el-button>
              </el-empty>
            </div>
            <div v-else class="video-grid">
              <el-card
                v-for="video in myVideos"
                :key="video.id"
                class="video-card"
                @click="$router.push(`/videos/${video.id}`)"
              >
                <div class="video-thumbnail">
                  <img :src="getMediaUrl(video.thumbnailUrl) || '/placeholder.jpg'" :alt="video.title" />
                  <div class="video-status" :class="getStatusClass(video)">
                    {{ getStatusText(video) }}
                  </div>
                </div>
                <div class="video-info">
                  <h4>{{ video.title }}</h4>
                  <p class="video-meta">
                    <span>{{ video.city }}</span>
                    <span>{{ video.viewCount }} 次观看</span>
                  </p>
                  <p class="video-description">{{ video.description }}</p>
                  <div class="video-actions">
                    <el-button
                      type="text"
                      size="small"
                      @click.stop="editVideo(video)"
                    >
                      编辑
                    </el-button>
                    <el-button
                      v-if="video.offlineStatus === 0 || !video.offlineStatus"
                      type="text"
                      size="small"
                      @click.stop="handleOfflineVideo(video)"
                    >
                      下架
                    </el-button>
                    <el-button
                      v-else
                      type="text"
                      size="small"
                      @click.stop="handleOnlineVideo(video)"
                    >
                      重新上架
                    </el-button>
                  </div>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <!-- 我的收藏 -->
        <el-tab-pane label="我的收藏" name="favorites">
          <div class="favorite-list">
            <div v-if="favoriteVideos.length === 0" class="empty-state">
              <el-empty description="你还没有收藏视频">
                <el-button type="primary" @click="$router.push('/videos')">去发现视频</el-button>
              </el-empty>
            </div>
            <div v-else class="video-grid">
              <el-card
                v-for="collect in favoriteVideos"
                :key="collect.id"
                class="video-card"
                @click="$router.push(`/videos/${collect.videoId}`)"
              >
                <div class="video-thumbnail">
                  <img :src="getMediaUrl(collect.video?.thumbnailUrl) || '/placeholder.jpg'" :alt="collect.video?.title" />
                </div>
                <div class="video-info">
                  <h4>{{ collect.video?.title }}</h4>
                  <p class="video-meta">
                    <span>{{ collect.video?.city }}</span>
                    <span>{{ collect.video?.viewCount }} 次观看</span>
                  </p>
                  <p class="video-description">{{ collect.video?.description }}</p>
                  <p class="collect-time">收藏于{{ formatDate(collect.createTime) }}</p>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <!-- 我的评论 -->
        <el-tab-pane label="我的评论" name="comments">
          <div class="comment-list">
            <div v-if="myComments.length === 0" class="empty-state">
              <el-empty description="你还没有发表评论">
                <el-button type="primary" @click="$router.push('/videos')">去发现视频</el-button>
              </el-empty>
            </div>
            <div v-else class="comments-container">
              <el-card
                v-for="comment in myComments"
                :key="comment.id"
                class="comment-card"
              >
                <div class="comment-header">
                  <div class="video-info" @click="$router.push(`/videos/${comment.videoId}`)">
                    <img 
                      :src="getMediaUrl(comment.video?.thumbnailUrl) || '/placeholder.jpg'" 
                      :alt="comment.video?.title" 
                      class="video-thumb"
                    />
                    <div class="video-text">
                      <h4>{{ comment.video?.title || '视频已删除' }}</h4>
                      <span class="video-city">{{ comment.video?.city }}</span>
                    </div>
                  </div>
                </div>
                <div class="comment-body">
                  <p class="comment-content">{{ comment.content }}</p>
                  <div class="comment-footer">
                    <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                    <el-button 
                      type="text" 
                      size="small" 
                      @click="deleteMyComment(comment.id)"
                      class="delete-btn"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <!-- 消息通知 -->
        <el-tab-pane name="messages">
          <template #label>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="message-badge">
              消息通知
            </el-badge>
          </template>
            <div class="message-list">
            <div class="pending-review-alert" v-if="userStore.isAdmin && pendingVideoCount > 0">
              <el-alert
                :title="`有${pendingVideoCount} 个视频等待审核`"
                type="warning"
                show-icon
                :closable="false"
              >
                <template #default>
                  <el-button type="primary" link @click="$router.push('/admin/review')">前往审核</el-button>
                </template>
              </el-alert>
            </div>
            <div class="message-header">
              <div class="message-tabs">
                <span 
                  class="message-tab" 
                  :class="{ active: messageFilter === 'all' }"
                  @click="messageFilter = 'all'"
                >全部消息</span>
                <span class="divider">|</span>
                <span 
                  class="message-tab" 
                  :class="{ active: messageFilter === 'unread' }"
                  @click="messageFilter = 'unread'"
                >未读消息</span>
              </div>
              <el-button 
                type="text" 
                @click="markAllMessagesAsRead"
                v-if="unreadCount > 0"
              >
                全部标记为已读
              </el-button>
            </div>
            <div v-if="filteredMessages.length === 0" class="empty-state">
              <el-empty :description="messageFilter === 'unread' ? '暂无未读消息' : '暂无消息'" />
            </div>
            <div v-else class="messages-container">
              <el-card
                v-for="msg in filteredMessages"
                :key="msg.id"
                class="message-card"
                :class="{ 'message-unread': msg.isRead === 0 }"
                @click="handleMessageClick(msg)"
              >
                <div class="message-content">
                  <div class="message-avatar">
                    <el-avatar :size="40" :src="getMediaUrl(msg.fromUser?.avatar)">
                      {{ msg.fromUser?.nickname?.charAt(0) || 'U' }}
                    </el-avatar>
                  </div>
                  <div class="message-body">
                    <div class="message-title">
                      <span class="from-user">{{ msg.fromUser?.nickname || '用户' }}</span>
                      <span class="message-action">{{ getMessageAction(msg.type) }}</span>
                    </div>
                    <p class="message-text" v-if="msg.content">{{ msg.content }}</p>
                    <div class="message-meta">
                      <span class="video-title" v-if="msg.video">{{ msg.video.title }}</span>
                      <span class="message-time">{{ formatDate(msg.createTime) }}</span>
                    </div>
                  </div>
                  <div class="message-status">
                    <el-tag v-if="msg.isRead === 0" size="small" type="danger">未读</el-tag>
                  </div>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <!-- 账号设置 -->
        <el-tab-pane label="账号设置" name="settings">
          <div class="settings-form">
            <el-form
              ref="settingsFormRef"
              :model="settingsForm"
              :rules="settingsRules"
              label-width="100px"
            >
              <el-form-item label="用户名">
                <el-input v-model="settingsForm.username" disabled />
                <div class="form-tip">用户名不可修改</div>
              </el-form-item>

              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="settingsForm.nickname" placeholder="请输入昵称" />
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input v-model="settingsForm.email" placeholder="请输入邮箱" />
              </el-form-item>

              <el-form-item label="手机" prop="phone">
                <el-input v-model="settingsForm.phone" placeholder="请输入手机号" />
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  :loading="updating"
                  @click="updateSettings"
                >
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>

            <!-- 修改密码 -->
            <el-divider content-position="left">修改密码</el-divider>
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input 
                  v-model="passwordForm.oldPassword" 
                  type="password" 
                  placeholder="请输入原密码"
                  show-password
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input 
                  v-model="passwordForm.newPassword" 
                  type="password" 
                  placeholder="请输入新密码（至少6位）"
                  show-password
                />
              </el-form-item>

              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input 
                  v-model="passwordForm.confirmPassword" 
                  type="password" 
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>

              <el-form-item>
                <el-button
                  type="warning"
                  :loading="changingPassword"
                  @click="changePassword"
                >
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 编辑资料对话框-->
    <el-dialog
      v-model="showEditDialog"
      title="编辑资料"
      width="500px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="手机" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" :loading="updating" @click="updateProfile">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑视频对话框 -->
    <el-dialog
      v-model="showEditVideoDialog"
      title="编辑视频"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="editVideoFormRef"
        :model="editVideoForm"
        :rules="uploadRules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="editVideoForm.title" placeholder="请输入视频标题" />
        </el-form-item>

        <el-form-item label="简介" prop="description">
          <el-input
            v-model="editVideoForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入视频简介"
          />
        </el-form-item>

        <el-form-item label="城市" prop="city">
          <el-select v-model="editVideoForm.city" placeholder="请选择城市" style="width: 100%">
            <el-option
              v-for="city in cities"
              :key="city.id"
              :label="city.name"
              :value="city.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="视频封面">
          <div class="upload-area">
            <div 
              class="cover-uploader" 
              @click="triggerEditCoverUpload"
            >
              <img 
                v-if="editVideoForm.thumbnailUrl" 
                :src="getFullUrl(editVideoForm.thumbnailUrl)" 
                class="video-cover-image"
              />
              
              <div v-if="!editVideoForm.thumbnailUrl" class="upload-placeholder">
                <el-icon><Plus /></el-icon>
                <span>点击更换封面</span>
              </div>
              
              <div v-else class="upload-mask">
                <el-icon><Camera /></el-icon>
                <span>更换封面</span>
              </div>
            </div>
            <input
              ref="editCoverInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleEditCoverChange"
            />
            <div class="upload-tip">若更换封面需重新审核</div>
          </div>
        </el-form-item>

        <el-form-item label="视频文件">
          <div class="upload-area">
            <!-- 上传区无视频状态 -->
            <div 
              class="video-uploader" 
              @click="triggerEditVideoUpload"
              v-if="!editVideoForm.videoUrl && uploadProgress === 0"
            >
              <div class="upload-placeholder">
                <el-icon><VideoPlay /></el-icon>
                <span>点击上传视频</span>
              </div>
            </div>

            <!-- 视频预览状态 -->
            <div class="video-preview-container" v-if="editVideoForm.videoUrl && uploadProgress === 0">
              <video 
                :src="getFullUrl(editVideoForm.videoUrl)" 
                controls 
                class="video-preview-player"
              ></video>
              <div class="video-edit-mask">
                <el-button type="primary" size="small" @click="triggerEditVideoUpload" icon="Upload">
                  更换视频
                </el-button>
                <div class="file-tip">更换后需重新审核</div>
              </div>
            </div>

            <!-- 上传中状态 -->
            <div class="video-uploader uploading" v-if="uploadProgress > 0">
               <el-progress type="circle" :percentage="uploadProgress" />
               <div class="upload-status-text">正在上传新视频?..</div>
            </div>

            <input
              ref="editVideoInput"
              type="file"
              accept="video/*"
              style="display: none"
              @change="handleEditVideoChange"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditVideoDialog = false">取消</el-button>
          <el-button type="danger" @click="handleDeleteVideo">删除视频</el-button>
          <el-button type="primary" :loading="updatingVideo" @click="submitEditVideo">
            保存修改
          </el-button>
        </span>
      </template>
    </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { videoAPI, collectAPI, userAPI, commentAPI, messageAPI, cityAPI } from '../api/video'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera, Plus, VideoPlay } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import { getMediaUrl } from '../utils/mediaUrl'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('videos')
const showEditDialog = ref(false)
const updating = ref(false)
const avatarInput = ref(null)

const myVideos = ref([])
const favoriteVideos = ref([])
const myComments = ref([])
const myMessages = ref([])
const unreadCount = ref(0)
const pendingVideoCount = ref(0)
const messageFilter = ref('all')

const showEditVideoDialog = ref(false)
const updatingVideo = ref(false)
const cities = ref([])
const editVideoFormRef = ref()
const editCoverInput = ref()
const editVideoInput = ref()
const uploadProgress = ref(0)
const editVideoForm = reactive({
  id: '',
  title: '',
  description: '',
  city: '',
  videoUrl: '',
  thumbnailUrl: ''
})

const uploadRules = {
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在2到100个字', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入视频简介', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请选择城市', trigger: 'change' }
  ]
}

const filteredMessages = computed(() => {
  if (messageFilter.value === 'unread') {
    return myMessages.value.filter(msg => msg.isRead === 0)
  }
  return myMessages.value
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

// 加载待审核视频数量（管理员）
const loadPendingCount = async () => {
  if (!userStore.isAdmin) return
  try {
    const response = await videoAPI.getPendingVideos()
    if (response.data.success) {
      pendingVideoCount.value = response.data.data.length
    }
  } catch (error) {
    console.error('获取待审核视频失败:', error)
  }
}
// 计算头像URL
const avatarUrl = computed(() => {
  if (userStore.user?.avatar) {
    return getMediaUrl(userStore.user.avatar)
  }
  return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
})

// 触发头像上传
const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

// 处理头像上传
const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('请上传jpg、png、gif、webp格式的图片')
    return
  }

  // 验证文件大小
  if (file.size > 15 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 15MB')
    return
  }

  try {
    const result = await userAPI.uploadAvatarToOSS(file)
    if (result && result.url) {
      const avatarUrl = result.url
      // 更新用户头像
      const updateResponse = await userAPI.updateUser(userStore.user.id, { avatar: avatarUrl })
      if (updateResponse.data.success) {
        userStore.setUser(updateResponse.data.user)
        ElMessage.success('头像更新成功')
      } else {
        ElMessage.error(updateResponse.data.message || '更新失败')
      }
    } else {
      ElMessage.error(response.data.message || '上传失败')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('上传失败，请重试')
    }
  }

  // 清空input
  event.target.value = ''
}

const settingsForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: ''
})

const editForm = reactive({
  nickname: '',
  email: '',
  phone: ''
})

const settingsRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const editRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordFormRef = ref()
const changingPassword = ref(false)

// 密码验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 修改密码
const changePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    
    // 前端校验：新旧密码不能相等
    if (passwordForm.oldPassword === passwordForm.newPassword) {
      ElMessage.error('新密码不能与原密码相等')
      return
    }

    changingPassword.value = true

    const response = await userAPI.changePassword(
      passwordForm.oldPassword,
      passwordForm.newPassword
    )

    if (response.data.success) {
      ElMessage.success('密码修改成功，请重新登录')
      // 清空表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      
      // 登出并跳转
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error(response.data.message || '密码修改失败')
    }
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error !== 'cancel') {
      console.error('修改密码失败:', error)
    }
  } finally {
    changingPassword.value = false
  }
}

// 计算属性
const settingsFormRef = ref()
const editFormRef = ref()

// 加载用户视频
const loadMyVideos = async () => {
  try {
    const response = await videoAPI.getUserVideos(userStore.user.id)
    if (response.data.success) {
      myVideos.value = response.data.data
    }
  } catch (error) {
    console.error('加载我的视频失败:', error)
  }
}

// 加载收藏视频
const loadFavoriteVideos = async () => {
  try {
    const response = await collectAPI.getUserCollects()
    if (response.data.success) {
      favoriteVideos.value = response.data.data
      // 后端已经返回完整的视频信息，无需前端再处�?
    }
  } catch (error) {
    console.error('加载收藏视频失败:', error)
  }
}

// 加载我的评论
const loadMyComments = async () => {
  try {
    const response = await commentAPI.getUserComments()
    if (response.data.success) {
      myComments.value = response.data.data
    }
  } catch (error) {
    console.error('加载我的评论失败:', error)
  }
}

// 删除我的评论
const deleteMyComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await commentAPI.deleteComment(commentId)
    if (response.data.success) {
      ElMessage.success('删除成功')
      // 从列表中移除
      myComments.value = myComments.value.filter(c => c.id !== commentId)
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 加载消息列表
const loadMyMessages = async () => {
  try {
    const response = await messageAPI.getMessages()
    if (response.data.success) {
      myMessages.value = response.data.data
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

// 加载未读消息数量
const loadUnreadCount = async () => {
  try {
    const response = await messageAPI.getUnreadCount()
    if (response.data.success) {
      unreadCount.value = response.data.count
    }
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

// 标记所有消息为已读
const markAllMessagesAsRead = async () => {
  try {
    const response = await messageAPI.markAllAsRead()
    if (response.data.success) {
      myMessages.value.forEach(msg => msg.isRead = 1)
      unreadCount.value = 0
      ElMessage.success('已全部标记为已读')
    }
  } catch (error) {
    console.error('标记失败:', error)
  }
}

// 处理消息点击
const handleMessageClick = async (msg) => {
  // 标记为已�?
  if (msg.isRead === 0) {
    try {
      await messageAPI.markAsRead(msg.id)
      msg.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  
  // 跳转到相关视�?
  if (msg.videoId) {
    router.push(`/videos/${msg.videoId}`)
  }
}

// 获取消息类型描述
const getMessageAction = (type) => {
  switch (type) {
    case 1: return '赞了你的视频'
    case 2: return '评论了你的视频'
    case 3: return '回复了你的评论'
    default: return '发来了消息'
  }
}

// 标签页切�?
const handleTabClick = (tab) => {
  if (tab.props.name === 'videos' && myVideos.value.length === 0) {
    loadMyVideos()
  } else if (tab.props.name === 'favorites' && favoriteVideos.value.length === 0) {
    loadFavoriteVideos()
  } else if (tab.props.name === 'comments' && myComments.value.length === 0) {
    loadMyComments()
  } else if (tab.props.name === 'messages' && myMessages.value.length === 0) {
    loadMyMessages()
  }
}

// 打开编辑视频弹窗
const editVideo = (video) => {
  editVideoForm.id = video.id
  editVideoForm.title = video.title
  editVideoForm.description = video.description
  editVideoForm.city = video.city
  editVideoForm.videoUrl = video.videoUrl
  editVideoForm.thumbnailUrl = video.thumbnailUrl
  showEditVideoDialog.value = true
}

// 获取完整图片地址 - 使用统一的mediaUrl工具
const getFullUrl = (url) => {
  return getMediaUrl(url)
}

// 触发编辑封面上传
const triggerEditCoverUpload = () => {
  editCoverInput.value?.click()
}

// 处理编辑封面更改
const handleEditCoverChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('请上传图片格式文件')
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  // 本地预览
  const previewUrl = URL.createObjectURL(file)
  editVideoForm.thumbnailUrl = previewUrl

  try {
    const result = await videoAPI.uploadCoverToOSS(file)
    if (result && result.url) {
      editVideoForm.thumbnailUrl = result.url
      ElMessage.success('封面上传成功')
    }
  } catch (error) {
    ElMessage.error('封面上传失败')
  }
  event.target.value = ''
}

// 触发编辑视频上传
const triggerEditVideoUpload = () => {
  editVideoInput.value?.click()
}

// 处理编辑视频更改
const handleEditVideoChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (file.size > 500 * 1024 * 1024) {
    ElMessage.error('视频大小不能超过500MB')
    return
  }

  try {
    uploadProgress.value = 0
    const result = await videoAPI.uploadVideoToOSS(file, (progress) => {
      uploadProgress.value = progress
    })
    
    if (result && result.url) {
      editVideoForm.videoUrl = result.url
      ElMessage.success('视频上传成功')
    }
  } catch (error) {
    ElMessage.error('视频上传失败')
  } finally {
    uploadProgress.value = 0
  }
  event.target.value = ''
}

// 提交视频修改
const submitEditVideo = async () => {
  try {
    await editVideoFormRef.value.validate()
    updatingVideo.value = true

    const response = await videoAPI.updateVideo(editVideoForm)
    if (response.data.success) {
      ElMessage.success('更新成功')
      showEditVideoDialog.value = false
      loadMyVideos() // 刷新列表
    } else {
      ElMessage.error(response.data.message || '更新失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新视频失败:', error)
      ElMessage.error('更新失败')
    }
  } finally {
    updatingVideo.value = false
  }
}

// 删除视频
const handleDeleteVideo = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个视频吗？删除后无法恢复',
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await videoAPI.deleteVideo(editVideoForm.id)
    if (response.data.success) {
      ElMessage.success('删除成功')
      showEditVideoDialog.value = false
      loadMyVideos()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除视频失败:', error)
    }
  }
}

// 下架视频
const handleOfflineVideo = async (video) => {
  try {
    await ElMessageBox.confirm(
      '确定要下架这个视频吗？下架后其他用户将无法观看',
      '确认下架',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await videoAPI.offlineVideo(video.id)
    if (response.data.success) {
      ElMessage.success('下架成功')
      loadMyVideos()
    } else {
      ElMessage.error(response.data.message || '下架失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架视频失败:', error)
    }
  }
}

// 重新上架视频
const handleOnlineVideo = async (video) => {
  try {
    const response = await videoAPI.onlineVideo(video.id)
    if (response.data.success) {
      ElMessage.success('上架成功')
      loadMyVideos()
    } else {
      ElMessage.error(response.data.message || '上架失败')
    }
  } catch (error) {
    console.error('上架视频失败:', error)
    ElMessage.error('上架失败')
  }
}

// 获取状态文�?
const getStatusText = (video) => {
  // 优先检查下架状�?
  if (video.offlineStatus && video.offlineStatus !== 0) {
    return '已下架'
  }
  // 然后检查审核状�?
  switch (video.status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知'
  }
}

// 获取状态样式类
const getStatusClass = (video) => {
  // 优先检查下架状�?
  if (video.offlineStatus && video.offlineStatus !== 0) {
    return 'status-offline'
  }
  // 然后检查审核状�?
  switch (video.status) {
    case 0: return 'status-pending'
    case 1: return 'status-approved'
    case 2: return 'status-rejected'
    default: return 'status-unknown'
  }
}

// 更新账号设置
const updateSettings = async () => {
  try {
    await settingsFormRef.value.validate()
    updating.value = true

    const result = await userStore.updateUser(settingsForm)
    if (result.success) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    updating.value = false
  }
}

// 更新个人资料
const updateProfile = async () => {
  try {
    await editFormRef.value.validate()
    updating.value = true

    const result = await userStore.updateUser(editForm)
    if (result.success) {
      ElMessage.success('保存成功')
      showEditDialog.value = false
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    updating.value = false
  }
}

// 格式化日�?
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 初始化表单数�?
const initForms = () => {
  const user = userStore.user
  Object.assign(settingsForm, {
    username: user.username,
    nickname: user.nickname,
    email: user.email || '',
    phone: user.phone || ''
  })
  Object.assign(editForm, {
    nickname: user.nickname,
    email: user.email || '',
    phone: user.phone || ''
  })
}

onMounted(() => {
  initForms()
  loadMyVideos()
  loadUnreadCount()
  loadPendingCount()
  loadCities()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background-color: var(--fnds-bg, #f5f5f5);
}

.profile {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card, .content-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: var(--fnds-pink, #2b86c3);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-section {
  flex-shrink: 0;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
  font-size: 24px;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.info-section {
  flex: 1;
}

.info-section h3 {
  margin: 0 0 5px 0;
  font-size: 24px;
  color: #333;
}

.username {
  color: #666;
  margin: 0 0 5px 0;
}

.role {
  color: #409eff;
  font-weight: 500;
  margin: 0 0 5px 0;
}

.email, .join-date {
  color: #999;
  margin: 2px 0;
  font-size: 14px;
}

.actions-section {
  flex-shrink: 0;
}

.video-list, .favorite-list {
  margin-top: 20px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.video-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.video-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.video-thumbnail {
  position: relative;
  margin-bottom: 10px;
}

.video-thumbnail img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 4px;
}

.video-status {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.status-pending {
  background: #e6a23c;
}

.status-approved {
  background: #67c23a;
}

.status-rejected {
  background: #f56c6c;
}

.status-offline {
  background: #909399;
}

.video-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.video-description {
  font-size: 14px;
  color: #666;
  margin: 0 0 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.collect-time {
  font-size: 12px;
  color: #999;
  margin: 4px 0 0 0;
}

.video-actions {
  text-align: right;
}

.settings-form {
  max-width: 600px;
  margin: 0 auto;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.dialog-footer {
  text-align: right;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
}

/* 我的评论样式 */
.comments-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-card {
  cursor: default;
}

.comment-header {
  margin-bottom: 12px;
}

.comment-header .video-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.comment-header .video-info:hover {
  background-color: #f5f5f5;
}

.video-thumb {
  width: 80px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.video-text h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #333;
}

.video-city {
  font-size: 12px;
  color: #999;
}

.comment-body {
  padding: 0 8px;
}

.comment-content {
  margin: 0 0 8px 0;
  color: #666;
  line-height: 1.6;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.delete-btn {
  color: #f56c6c;
}

.delete-btn:hover {
  color: #ff7875;
}

/* 消息通知样式 */
.message-badge {
  display: inline-block;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 8px;
}

.message-tabs {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.message-tab {
  cursor: pointer;
  color: #666;
  transition: color 0.3s;
}

.message-tab:hover, .message-tab.active {
  color: var(--fnds-pink, #2b86c3);
  font-weight: 500;
}

.divider {
  color: #ddd;
}


.messages-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-card {
  cursor: pointer;
  transition: all 0.3s;
}

.message-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.message-unread {
  background-color: #f0f7ff;
  border-left: 3px solid #409eff;
}

.message-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.message-avatar {
  flex-shrink: 0;
}

.message-body {
  flex: 1;
  min-width: 0;
}

.message-title {
  margin-bottom: 4px;
}

.from-user {
  font-weight: 600;
  color: #333;
  margin-right: 8px;
}

.message-action {
  color: #666;
  font-size: 14px;
}

.message-text {
  color: #666;
  font-size: 14px;
  margin: 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

.video-title {
  color: #409eff;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-time {
  flex-shrink: 0;
}

.message-status {
  flex-shrink: 0;
}

.cover-uploader {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  border: 1px dashed #d9d9d9;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.video-cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #8c939d;
}

.upload-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-uploader:hover .upload-mask {
  opacity: 1;
}

.video-file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f0f9eb;
  border-radius: 4px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.upload-progress {
  margin-top: 10px;
}

.upload-status-text {
  font-size: 12px;
  color: #409eff;
  margin-top: 4px;
  text-align: center;
}

.video-uploader {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  border: 1px dashed #d9d9d9;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  cursor: pointer;
  transition: border-color 0.3s;
}

.video-uploader:hover {
  border-color: #409eff;
}

.video-preview-container {
  width: 100%;
  border-radius: 8px;
  background: #000;
  overflow: hidden;
  position: relative;
}

.video-preview-player {
  width: 100%;
  max-height: 300px;
  display: block;
}

.video-edit-mask {
  padding: 10px;
  background: #f5f7fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #eee;
}

.file-tip {
  font-size: 12px;
  color: #f56c6c;
}

.video-uploader.uploading {
  cursor: default;
  background: #fff;
  border: none;
}

@media (max-width: 768px) {
  .user-info {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }

  .video-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}
</style>
