<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`举报${targetTypeName}`"
    width="500px"
    @close="handleClose"
  >
    <el-form :model="reportForm" :rules="rules" ref="reportFormRef" label-width="100px">
      <el-form-item label="举报分类" prop="reasonCategory">
        <el-select v-model="reportForm.reasonCategory" placeholder="请选择举报分类" style="width: 100%">
          <el-option label="色情低俗" value="色情低俗" />
          <el-option label="涉政敏感" value="涉政敏感" />
          <el-option label="人身攻击" value="人身攻击" />
          <el-option label="虚假信息" value="虚假信息" />
          <el-option label="垃圾广告" value="垃圾广告" />
          <el-option label="其他违规" value="其他违规" />
        </el-select>
      </el-form-item>

      <el-form-item label="详细描述" prop="description">
        <el-input
          v-model="reportForm.description"
          type="textarea"
          :rows="4"
          placeholder="请详细描述违规情况（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="danger" @click="handleSubmit" :loading="submitting">提交举报</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { reportAPI } from '../api/report'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  targetType: {
    type: Number,
    required: true // 0: 视频, 1: 评论
  },
  targetId: {
    type: [Number, String],
    required: true
  }
})

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = ref(false)
const submitting = ref(false)
const reportFormRef = ref(null)

const reportForm = ref({
  reasonCategory: '',
  description: ''
})

const rules = {
  reasonCategory: [
    { required: true, message: '请选择举报分类', trigger: 'change' }
  ]
}

const targetTypeName = computed(() => {
  return props.targetType === 0 ? '视频' : '评论'
})

// 监听 visible prop 变化
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
})

// 监听 dialogVisible 变化
watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

const handleClose = () => {
  dialogVisible.value = false
  resetForm()
}

const resetForm = () => {
  if (reportFormRef.value) {
    reportFormRef.value.resetFields()
  }
  reportForm.value = {
    reasonCategory: '',
    description: ''
  }
}

const handleSubmit = async () => {
  if (!reportFormRef.value) return

  await reportFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const data = {
        targetId: props.targetId,
        targetType: props.targetType,
        reasonCategory: reportForm.value.reasonCategory,
        description: reportForm.value.description || null
      }

      const response = await reportAPI.submitReport(data)
      if (response.data.success) {
        ElMessage.success(response.data.message || '举报提交成功')
        emit('success')
        handleClose()
      } else {
        ElMessage.error(response.data.message || '举报提交失败')
      }
    } catch (error) {
      console.error('提交举报失败:', error)
      ElMessage.error(error.response?.data?.message || '举报提交失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
