<template>
  <el-dialog
    v-model="dialogVisible"
    :title="activeTab === 'login' ? '登录' : '注册'"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
    :z-index="2000"
    append-to-body
  >
    <!-- 登录表单 -->
    <div v-if="activeTab === 'login'" class="form-container">
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
        class="form-content"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="handleLogin"
            :loading="loginLoading"
            class="submit-btn"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 切换到注册 -->
      <div class="switch-link">
        还没有账号？<a @click="activeTab = 'register'">去注册</a>
      </div>
    </div>

    <!-- 注册表单 -->
    <div v-else class="form-container">
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
        class="form-content"
      >
          <!-- 头像上传 -->
          <el-form-item label="头像" prop="avatar" required>
            <div class="avatar-uploader">
              <div 
                class="avatar-preview" 
                @click="triggerAvatarUpload"
                :class="{ 'has-avatar': avatarPreviewUrl }"
              >
                <img v-if="avatarPreviewUrl" :src="avatarPreviewUrl" alt="头像" />
                <div v-else class="avatar-placeholder">
                  <el-icon><Plus /></el-icon>
                  <span>上传头像</span>
                </div>
                <div class="avatar-overlay" v-if="avatarPreviewUrl">
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
              <div class="avatar-tip">点击上传头像（支持裁剪调整）</div>
            </div>
          </el-form-item>
          
          <!-- 图片裁剪弹窗 -->
          <el-dialog
            v-model="showCropper"
            title="裁剪头像"
            width="800px"
            :close-on-click-modal="false"
          >
            <div class="cropper-container">
              <vue-cropper
                ref="cropperRef"
                :img="cropperImage"
                :output-size="1"
                :output-type="cropperOutputType"
                :info="true"
                :full="false"
                :can-move="true"
                :can-move-box="true"
                :fixed="true"
                :fixed-number="[1, 1]"
                :auto-crop="true"
                :auto-crop-width="200"
                :auto-crop-height="200"
                :center-box="true"
                :high="true"
              />
            </div>
            <template #footer>
              <el-button @click="cancelCrop">取消</el-button>
              <el-button type="primary" @click="confirmCrop">确定裁剪</el-button>
            </template>
          </el-dialog>

          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="3-20个字符"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="至少6位"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="再次输入密码"
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
              placeholder="选填"
              :prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="选填"
              :prefix-icon="Phone"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              @click="handleRegister"
              :loading="registerLoading"
              class="submit-btn"
            >
              注册
            </el-button>
          </el-form-item>
      </el-form>
      
      <!-- 切换到登录 -->
      <div class="switch-link">
        已经有账号？<a @click="activeTab = 'login'">去登录</a>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import { userAPI } from '../api/video'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone, Plus, Camera } from '@element-plus/icons-vue'
import { getMediaUrl } from '../utils/mediaUrl'
import { VueCropper } from 'vue-cropper'
import 'vue-cropper/dist/index.css'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  defaultTab: {
    type: String,
    default: 'login' // 'login' or 'register'
  }
})

const emit = defineEmits(['update:modelValue', 'loginSuccess'])

const userStore = useUserStore()

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeTab = ref(props.defaultTab)

// 登录表单
const loginFormRef = ref()
const loginLoading = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 注册表单
const registerFormRef = ref()
const avatarInput = ref()
const registerLoading = ref(false)
const avatarFile = ref(null) // 存储选择的头像文件
const avatarPreviewUrl = ref('') // 头像预览URL

// 裁剪器相关状态
const showCropper = ref(false) // 显示裁剪弹窗
const cropperRef = ref(null) // 裁剪器实例
const cropperImage = ref('') // 待裁剪的图片
const cropperOutputType = ref('jpeg') // 输出类型
const originalFileName = ref('') // 原始文件名

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

// 处理头像选择（打开裁剪器）
const handleAvatarChange = (event) => {
  const file = event.target.files[0]
  if (!file) return

  console.log('Selected file:', file.name, file.type, file.size)

  // 验证文件类型（注意：JPG可能是image/jpeg或image/jpg）
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('请上传 jpg、png、gif 或 webp 格式的图片')
    event.target.value = ''
    return
  }

  // 验证文件大小（15MB）
  if (file.size > 15 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 15MB')
    event.target.value = ''
    return
  }

  // 保存原始文件名
  originalFileName.value = file.name
  
  // 确定输出类型
  if (file.type === 'image/png') {
    cropperOutputType.value = 'png'
  } else {
    cropperOutputType.value = 'jpeg'
  }

  // 读取文件并显示裁剪器
  const reader = new FileReader()
  reader.onload = async (e) => {
    const base64 = e.target.result
    console.log('FileReader loaded, result length:', base64?.length)
    console.log('Base64 starts with:', base64?.substring(0, 50))
    console.log('Is valid data URL:', base64?.startsWith('data:image/'))
    
    // 先打开弹窗
    showCropper.value = true
    
    // 等待 DOM 更新
    await nextTick()
    
    // 再等待一段时间确保 cropper 组件完全挂载
    setTimeout(() => {
      cropperImage.value = base64
      console.log('Cropper image set, length:', cropperImage.value?.length)
      console.log('Cropper should now display the image')
    }, 200)
  }
  reader.onerror = (error) => {
    console.error('FileReader error:', error)
    ElMessage.error('图片读取失败')
  }
  reader.readAsDataURL(file)

  event.target.value = ''
}

// 确认裁剪
const confirmCrop = () => {
  cropperRef.value.getCropBlob((blob) => {
    // 保存文件对象
    const file = new File([blob], originalFileName.value, { type: blob.type })
    avatarFile.value = file
    
    // 创建本地预览URL
    if (avatarPreviewUrl.value) {
      URL.revokeObjectURL(avatarPreviewUrl.value)
    }
    avatarPreviewUrl.value = URL.createObjectURL(blob)
    
    console.log('Cropped image created, size:', blob.size)
    
    // 标记表单有头像
    registerForm.avatar = 'local-preview'
    
    // 关闭裁剪器
    showCropper.value = false
  })
}

// 取消裁剪
const cancelCrop = () => {
  showCropper.value = false
  cropperImage.value = ''
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

// 处理登录
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loginLoading.value = true

    const result = await userStore.login(loginForm)
    if (result.success) {
      ElMessage.success('登录成功')
      dialogVisible.value = false
      emit('loginSuccess')
      // 重置表单
      loginForm.username = ''
      loginForm.password = ''
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    console.error('登录验证失败:', error)
  } finally {
    loginLoading.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    
    if (!avatarFile.value) {
      ElMessage.error('请上传头像')
      return
    }
    
    registerLoading.value = true

    // 先注册账号（不包含头像）
    const registerData = {
      username: registerForm.username,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      nickname: registerForm.nickname,
      email: registerForm.email,
      phone: registerForm.phone,
      avatar: '' // 暂时为空
    }
    
    const result = await userStore.register(registerData)
    if (result.success) {
      // 注册成功后，上传头像
      try {
        ElMessage.info('正在上传头像...')
        const uploadResult = await userAPI.uploadAvatarToOSS(avatarFile.value)
        
        if (uploadResult && uploadResult.url) {
          // 将头像URL更新到注册数据中，等待登录后可以看到
          // 这里可以选择：
          // 1. 直接登录并更新头像
          // 2. 或者在后端注册接口中支持头像上传
          
          // 方案1：自动登录并更新头像
          const loginResult = await userStore.login({
            username: registerForm.username,
            password: registerForm.password
          })
          
          if (loginResult.success) {
            // 更新用户头像
            await userAPI.updateUserAvatar(uploadResult.url)
            ElMessage.success('注册成功！')
            dialogVisible.value = false
            emit('loginSuccess')
            
            // 重置表单
            resetRegisterForm()
          } else {
            ElMessage.success('注册成功，请登录')
            activeTab.value = 'login'
            resetRegisterForm()
          }
        } else {
          ElMessage.warning('注册成功，但头像上传失败，请登录后重新上传')
          activeTab.value = 'login'
          resetRegisterForm()
        }
      } catch (uploadError) {
        console.error('头像上传失败:', uploadError)
        ElMessage.warning('注册成功，但头像上传失败，请登录后重新上传')
        activeTab.value = 'login'
        resetRegisterForm()
      }
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    console.error('注册验证失败:', error)
  } finally {
    registerLoading.value = false
  }
}

// 重置注册表单
const resetRegisterForm = () => {
  Object.assign(registerForm, {
    username: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    email: '',
    phone: '',
    avatar: ''
  })
  avatarFile.value = null
  if (avatarPreviewUrl.value) {
    URL.revokeObjectURL(avatarPreviewUrl.value)
    avatarPreviewUrl.value = ''
  }
}

// 弹窗关闭时重置表单
const handleClose = () => {
  loginFormRef.value?.resetFields()
  registerFormRef.value?.resetFields()
}

// 监听 defaultTab 变化
watch(() => props.defaultTab, (newTab) => {
  activeTab.value = newTab
})
</script>

<style scoped>
.form-container {
  padding: 20px 0;
}

.form-content {
  margin-bottom: 20px;
}

.submit-btn {
  width: 100%;
}

.switch-link {
  text-align: center;
  padding: 16px 0;
  color: #606266;
  font-size: 14px;
  border-top: 1px solid #EBEEF5;
}

.switch-link a {
  color: #409EFF;
  cursor: pointer;
  text-decoration: none;
  margin-left: 4px;
}

.switch-link a:hover {
  color: #66B1FF;
  text-decoration: underline;
}

/* 头像上传样式 */
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

/* 裁剪器样式 */
.cropper-container {
  width: 100%;
  height: 500px;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 确保 vue-cropper 组件填充容器 */
.cropper-container :deep(.vue-cropper) {
  width: 100% !important;
  height: 100% !important;
}
</style>
