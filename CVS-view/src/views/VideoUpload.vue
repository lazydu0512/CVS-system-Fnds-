<template>
  <div class="upload-page">
    <!-- 顶部导航 -->
    <AppHeader :showSearch="false" />

    <div class="video-upload">
      <el-card class="upload-card">
        <template #header>
          <div class="card-header">
            <h2>上传演唱会视频</h2>
          </div>
        </template>

      <el-form
        ref="uploadFormRef"
        :model="uploadForm"
        :rules="uploadRules"
        label-width="100px"
      >
        <!-- 视频标题 -->
        <el-form-item label="视频标题" prop="title">
          <el-input
            v-model="uploadForm.title"
            placeholder="请输入视频标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <!-- 演唱会城�?-->
        <el-form-item label="演唱会城市" prop="city">
          <el-select
            v-model="uploadForm.city"
            placeholder="请选择演唱会城市"
            style="width: 100%"
          >
            <el-option
              v-for="city in cities"
              :key="city.id"
              :label="city.name"
              :value="city.name"
            />
          </el-select>
        </el-form-item>

        <!-- 演唱会日�?-->
        <el-form-item label="演唱会日期" prop="concertDate">
          <el-date-picker
            v-model="uploadForm.concertDate"
            type="date"
            placeholder="选择演唱会日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 视频描述 -->
        <el-form-item label="视频描述" prop="description">
          <el-input
            v-model="uploadForm.description"
            type="textarea"
            :rows="4"
            placeholder="请描述视频内容、演唱会亮点等信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 上传视频文件 -->
        <el-form-item label="上传视频" required>
          <el-upload
            ref="videoUploadRef"
            class="video-uploader"
            :auto-upload="false"
            :on-change="handleVideoChange"
            :on-remove="handleVideoRemove"
            :limit="1"
            accept="video/mp4,video/avi,video/mov,video/mkv"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将视频文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 MP4、AVI、MOV、MKV 格式，建议大小不超过500MB
              </div>
            </template>
          </el-upload>
          <div v-if="videoFile" class="file-info">
            <el-icon><VideoPlay /></el-icon>
            <span>{{ videoFile.name }}</span>
            <span class="file-size">{{ formatFileSize(videoFile.size) }}</span>
          </div>
          <el-progress
            v-if="videoUploadProgress > 0 && videoUploadProgress < 100"
            :percentage="videoUploadProgress"
            :status="videoUploadProgress === 100 ? 'success' : undefined"
          />
        </el-form-item>

        <!-- 上传封面图片 -->
        <el-form-item label="上传封面" required>
          <el-upload
            ref="coverUploadRef"
            class="cover-uploader"
            :auto-upload="false"
            :on-change="handleCoverChange"
            :on-remove="handleCoverRemove"
            :limit="1"
            accept="image/jpeg,image/jpg,image/png,image/gif"
            list-type="picture-card"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                支持 JPG、PNG、GIF 格式，建议尺�?1280x720，大小不超过5MB
              </div>
            </template>
          </el-upload>
          <el-progress
            v-if="coverUploadProgress > 0 && coverUploadProgress < 100"
            :percentage="coverUploadProgress"
            :status="coverUploadProgress === 100 ? 'success' : undefined"
          />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            :loading="uploading"
            @click="handleUpload"
            style="width: 100%"
            size="large"
          >
            {{ uploading ? '上传中...' : '上传视频' }}
          </el-button>
        </el-form-item>
      </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { videoAPI, cityAPI } from '../api/video'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Plus, VideoPlay } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'

const router = useRouter()

const uploadFormRef = ref()
const videoUploadRef = ref()
const coverUploadRef = ref()
const uploading = ref(false)

// 城市列表
const cities = ref([])

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

onMounted(() => {
  loadCities()
})

// 文件对象
const videoFile = ref(null)
const coverFile = ref(null)

// 上传进度
const videoUploadProgress = ref(0)
const coverUploadProgress = ref(0)

const uploadForm = reactive({
  title: '',
  city: '',
  concertDate: '',
  description: '',
  videoUrl: '',
  thumbnailUrl: '',
  duration: 0
})

// 表单验证规则
const uploadRules = {
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度应为2-100个字', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请选择演唱会城市', trigger: 'change' }
  ],
  concertDate: [
    { required: true, message: '请选择演唱会日期', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入视频描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度应为10-500个字', trigger: 'blur' }
  ]
}

// 禁用未来的日�?
const disabledDate = (time) => {
  return time.getTime() > Date.now()
}

// 处理视频文件选择
const handleVideoChange = (file) => {
  videoFile.value = file.raw
  // 自动获取视频时长
  getVideoDuration(file.raw)
}

// 处理视频文件移除
const handleVideoRemove = () => {
  videoFile.value = null
  uploadForm.duration = 0
  videoUploadProgress.value = 0
}

// 处理封面文件选择
const handleCoverChange = (file) => {
  coverFile.value = file.raw
}

// 处理封面文件移除
const handleCoverRemove = () => {
  coverFile.value = null
  coverUploadProgress.value = 0
}

// 获取视频时长
const getVideoDuration = (file) => {
  const video = document.createElement('video')
  video.preload = 'metadata'

  video.onloadedmetadata = () => {
    uploadForm.duration = Math.floor(video.duration)
    URL.revokeObjectURL(video.src)
    console.log('视频时长:', uploadForm.duration, '秒')
  }

  video.src = URL.createObjectURL(file)
}

// 格式化文件大�?
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 上传视频
const handleUpload = async () => {
  try {
    // 验证文件
    if (!videoFile.value) {
      ElMessage.error('请选择要上传的视频文件')
      return
    }
    if (!coverFile.value) {
      ElMessage.error('请选择视频封面图片')
      return
    }

    // 验证表单
    await uploadFormRef.value.validate()

    await ElMessageBox.confirm(
      '确认上传此视频？上传后需要等待管理员审核通过才能发布',
      '确认上传',
      {
        confirmButtonText: '确定上传',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    uploading.value = true

    // 1. 上传视频文件到OSS
    ElMessage.info('正在上传视频文件到云端...')
    try {
      const videoResult = await videoAPI.uploadVideoToOSS(videoFile.value, (percent) => {
        videoUploadProgress.value = percent
      })
      uploadForm.videoUrl = videoResult.url
      console.log('视频上传成功:', videoResult)
    } catch (error) {
      console.error('视频OSS上传失败:', error)
      throw new Error('视频上传失败: ' + (error.message || '未知错误'))
    }

    // 2. 上传封面图片到OSS
    ElMessage.info('正在上传封面图片到云端...')
    try {
      const coverResult = await videoAPI.uploadCoverToOSS(coverFile.value, (percent) => {
        coverUploadProgress.value = percent
      })
      uploadForm.thumbnailUrl = coverResult.url
      console.log('封面上传成功:', coverResult)
    } catch (error) {
      console.error('封面OSS上传失败:', error)
      throw new Error('封面上传失败: ' + (error.message || '未知错误'))
    }

    // 3. 提交视频信息
    ElMessage.info('正在保存视频信息...')
    const uploadData = {
      title: uploadForm.title,
      city: uploadForm.city,
      concertDate: uploadForm.concertDate,
      description: uploadForm.description,
      videoUrl: uploadForm.videoUrl,
      thumbnailUrl: uploadForm.thumbnailUrl,
      duration: uploadForm.duration
    }

    const response = await videoAPI.uploadVideo(uploadData)

    if (response.data.success) {
      ElMessage.success('视频上传成功！请等待管理员审核')

      // 重置表单和文�?
      uploadFormRef.value.resetFields()
      videoFile.value = null
      coverFile.value = null
      videoUploadProgress.value = 0
      coverUploadProgress.value = 0
      uploadForm.videoUrl = ''
      uploadForm.thumbnailUrl = ''
      uploadForm.duration = 0

      // 清空上传组件
      if (videoUploadRef.value) {
        videoUploadRef.value.clearFiles()
      }
      if (coverUploadRef.value) {
        coverUploadRef.value.clearFiles()
      }

      // 跳转到个人中心查看上传记�?
      setTimeout(() => {
        router.push('/profile')
      }, 2000)
    } else {
      ElMessage.error(response.data.message || '上传失败')
    }

  } catch (error) {
    if (error !== 'cancel') {
      // 详细的错误信�?
      let errorMessage = '上传失败，请重试'

      // 检查是否是表单验证错误
      if (error && typeof error === 'object' && !error.response && !error.request && !error.message) {
        // 这是表单验证错误
        errorMessage = '请检查表单填写是否完整'
        console.error('表单验证错误:', error)
      } else if (error.response) {
        // 服务器返回了错误响应
        errorMessage = error.response.data?.message || `服务器错�? ${error.response.status}`
        console.error('服务器错误详�?', {
          status: error.response.status,
          statusText: error.response.statusText,
          data: error.response.data,
          url: error.config?.url
        })
      } else if (error.request) {
        // 请求已发送但没有收到响应
        errorMessage = '网络错误，请检查网络连接或后端服务是否启动'
        console.error('网络错误详情:', {
          request: error.request,
          url: error.config?.url
        })
      } else if (error.message) {
        // 其他错误
        errorMessage = error.message
        console.error('错误详情:', error.message)
      }

      ElMessage.error(errorMessage)
      console.error('完整错误对象:', error)
    }
  } finally {
    uploading.value = false
    // 重置进度�?
    videoUploadProgress.value = 0
    coverUploadProgress.value = 0
  }
}
</script>

<style scoped>
.upload-page {
  min-height: 100vh;
  background-color: var(--fnds-bg, #f5f5f5);
}

.video-upload {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.upload-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: var(--fnds-pink);
}

/* 视频上传区域 */
.video-uploader {
  width: 100%;
}

.video-uploader :deep(.el-upload-dragger) {
  width: 100%;
  padding: 40px;
}

.file-info {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.file-info .el-icon {
  font-size: 20px;
  color: var(--fnds-pink);
}

.file-size {
  margin-left: auto;
  color: #909399;
  font-size: 12px;
}

/* 封面上传区域 */
.cover-uploader :deep(.el-upload--picture-card) {
  width: 180px;
  height: 120px;
}

.cover-uploader :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 180px;
  height: 120px;
}

/* 上传提示 */
.el-upload__tip {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}

/* 进度�?*/
:deep(.el-progress) {
  margin-top: 10px;
}

/* 响应�?*/
@media (max-width: 768px) {
  .video-upload {
    padding: 10px;
  }

  .upload-card {
    margin: 0;
  }

  .video-uploader :deep(.el-upload-dragger) {
    padding: 20px;
  }

  .cover-uploader :deep(.el-upload--picture-card) {
    width: 120px;
    height: 80px;
  }

  .cover-uploader :deep(.el-upload-list--picture-card .el-upload-list__item) {
    width: 120px;
    height: 80px;
  }
}
</style>
