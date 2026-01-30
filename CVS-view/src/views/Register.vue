<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>注册</h2>
        </div>
      </template>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
      >
        <!-- 头像上传 -->
        <el-form-item label="头像" prop="avatar" required>
          <div class="avatar-uploader">
            <div 
              class="avatar-preview" 
              @click="triggerAvatarUpload"
              :class="{ 'has-avatar': registerForm.avatar }"
            >
              <img v-if="registerForm.avatar" :src="getMediaUrl(registerForm.avatar)" alt="头像" />
              <div v-else class="avatar-placeholder">
                <el-icon><Plus /></el-icon>
                <span>上传头像</span>
              </div>
              <div class="avatar-overlay" v-if="registerForm.avatar">
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
            <div class="avatar-tip">必须上传头像才能注册</div>
          </div>
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="请输入昵称"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            :prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            :prefix-icon="Phone"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleRegister"
            style="width: 100%"
          >
            注册
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button
            type="text"
            @click="$router.push('/login')"
            style="width: 100%"
          >
            已有账号？去登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { userAPI } from '../api/video'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone, Plus, Camera } from '@element-plus/icons-vue'
import { getMediaUrl } from '../utils/mediaUrl'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref()
const avatarInput = ref()
const loading = ref(false)
const uploadingAvatar = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
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
    ElMessage.error('请上传 jpg、png、gif 或 webp 格式的图片')
    return
  }

  // 验证文件大小（15MB）
  if (file.size > 15 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 15MB')
    return
  }

  try {
    uploadingAvatar.value = true
    const result = await userAPI.uploadAvatarToOSS(file)
    if (result && result.url) {
      registerForm.avatar = result.url
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    uploadingAvatar.value = false
    event.target.value = ''
  }
}

const validatePassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validateAvatar = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请上传头像'))
  } else {
    callback()
  }
}

const registerRules = {
  avatar: [
    { required: true, validator: validateAvatar, trigger: 'change' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
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

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    
    if (!registerForm.avatar) {
      ElMessage.error('请上传头像')
      return
    }
    
    loading.value = true

    const result = await userStore.register(registerForm)
    if (result.success) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    console.error('注册验证失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  width: 500px;
  max-width: 90vw;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #333;
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 2px dashed #d9d9d9;
  cursor: pointer;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  transition: border-color 0.3s;
}

.avatar-preview:hover {
  border-color: #409eff;
}

.avatar-preview.has-avatar {
  border-style: solid;
  border-color: #409eff;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #8c939d;
  font-size: 12px;
}

.avatar-placeholder .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
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

.avatar-preview:hover .avatar-overlay {
  opacity: 1;
}

.avatar-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #f56c6c;
}
</style>
