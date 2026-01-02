<template>
  <div class="upload-container">
    <div class="upload-card">
      <h2 class="upload-title">
        <el-icon><Upload /></el-icon>
        上传图片
      </h2>

      <!-- 上传区域 -->
      <el-upload
        ref="uploadRef"
        v-model:file-list="fileList"
        class="upload-area"
        drag
        :auto-upload="false"
        :multiple="true"
        accept="image/*"
        :on-change="handleFileChange"
        :on-remove="handleRemove"
        :limit="10"
        :on-exceed="handleExceed"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">
          <p class="main-text">点击或拖拽文件到此区域上传</p>
          <p class="sub-text">支持 JPG、PNG、GIF 等格式，单次最多上传 10 张图片</p>
        </div>
      </el-upload>

      <!-- 图片预览列表 -->
      <div v-if="previewList.length > 0" class="preview-section">
        <h3>待上传图片 ({{ previewList.length }})</h3>
        <div class="preview-list">
          <div
            v-for="(item, index) in previewList"
            :key="index"
            class="preview-item"
          >
            <img :src="item.url" :alt="item.file.name" />
            <div class="preview-info">
              <div class="file-name">{{ item.file.name }}</div>
              <div class="file-size">{{ formatFileSize(item.file.size) }}</div>
              <div v-if="item.exifInfo" class="exif-info">
                <el-tag v-if="item.exifInfo.DateTime" size="small">
                  {{ item.exifInfo.DateTime }}
                </el-tag>
                <el-tag v-if="item.exifInfo.width && item.exifInfo.height" size="small" type="info">
                  {{ item.exifInfo.width }} × {{ item.exifInfo.height }}
                </el-tag>
                <el-tag v-if="item.exifInfo.GPSLatitude" size="small" type="success">
                  <el-icon><Location /></el-icon>
                  GPS
                </el-tag>
              </div>
            </div>
            <el-button
              type="danger"
              :icon="Delete"
              circle
              size="small"
              class="remove-btn"
              @click="handleRemovePreview(index)"
            />
          </div>
        </div>

        <!-- 标签选择 -->
        <div class="tags-section">
          <h4>添加标签</h4>
          <div class="tags-container">
            <el-tag
              v-for="tag in tagStore.userTags"
              :key="tag.id"
              :type="selectedTags.includes(tag.id) ? 'primary' : 'info'"
              class="tag-item"
              @click="toggleTag(tag.id)"
            >
              {{ tag.tagName }}
            </el-tag>
            <el-button
              type="primary"
              link
              :icon="Plus"
              size="small"
              @click="showAddTagDialog = true"
            >
              新建标签
            </el-button>
          </div>
        </div>

        <!-- AI自动识别选项 -->
        <div class="ai-option-section">
          <el-checkbox v-model="enableAiTag" size="large">
            <div class="ai-option-label">
              <el-icon><MagicStick /></el-icon>
              <span>上传后自动 AI 识别标签</span>
              <el-tag size="small" type="success">智能</el-tag>
            </div>
          </el-checkbox>
          <p class="ai-option-tip">启用后，上传完成后会自动调用百度AI识别图片内容并添加标签</p>
        </div>

        <!-- 上传进度 -->
        <div v-if="uploading" class="progress-section">
          <el-progress
            :percentage="uploadProgress"
            :status="uploadProgress === 100 ? 'success' : undefined"
          />
          <p class="progress-text">
            正在上传... {{ uploadedCount }}/{{ previewList.length }}
          </p>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button size="large" @click="handleClear">清空</el-button>
          <el-button
            type="primary"
            size="large"
            :loading="uploading"
            :disabled="previewList.length === 0"
            @click="handleUpload"
          >
            <el-icon><Upload /></el-icon>
            开始上传
          </el-button>
        </div>
      </div>
    </div>

    <!-- 添加标签对话框 -->
    <el-dialog v-model="showAddTagDialog" title="新建标签" width="400px">
      <el-form :model="tagForm" label-width="80px">
        <el-form-item label="标签名称">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="标签颜色">
          <el-color-picker v-model="tagForm.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddTagDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddTag">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Upload,
  UploadFilled,
  Delete,
  Plus,
  Location,
  MagicStick
} from '@element-plus/icons-vue'
import { useImageStore } from '@/stores/image'
import { useTagStore } from '@/stores/tag'
import { analyzeAndAddTags } from '@/api/ai'
import ExifReader from 'exifreader'

const router = useRouter()
const imageStore = useImageStore()
const tagStore = useTagStore()

const uploadRef = ref(null)
const fileList = ref([])
const previewList = ref([])
const selectedTags = ref([])
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadedCount = ref(0)
const showAddTagDialog = ref(false)
const enableAiTag = ref(false)

const tagForm = reactive({
  name: '',
  color: '#409eff'
})

onMounted(() => {
  tagStore.fetchUserTags()
})

// 处理文件变化
const handleFileChange = async (file) => {
  if (!file.raw) return

  // 验证文件类型
  if (!file.raw.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }

  // 创建预览
  const url = URL.createObjectURL(file.raw)
  
  // 读取 EXIF 信息
  let exifInfo = null
  try {
    const tags = await ExifReader.load(file.raw)
    exifInfo = {
      DateTime: tags.DateTime?.description,
      Make: tags.Make?.description,
      Model: tags.Model?.description,
      width: tags['Image Width']?.value || tags.PixelXDimension?.value,
      height: tags['Image Height']?.value || tags.PixelYDimension?.value,
      GPSLatitude: tags.GPSLatitude?.description,
      GPSLongitude: tags.GPSLongitude?.description,
      Orientation: tags.Orientation?.value
    }
  } catch (error) {
    console.log('读取 EXIF 信息失败:', error)
  }

  previewList.value.push({
    file: file.raw,
    url,
    exifInfo
  })
}

// 移除文件
const handleRemove = (file) => {
  const index = previewList.value.findIndex((item) => item.file.name === file.name)
  if (index !== -1) {
    URL.revokeObjectURL(previewList.value[index].url)
    previewList.value.splice(index, 1)
  }
}

// 移除预览
const handleRemovePreview = (index) => {
  URL.revokeObjectURL(previewList.value[index].url)
  previewList.value.splice(index, 1)
  fileList.value.splice(index, 1)
}

// 超出限制
const handleExceed = () => {
  ElMessage.warning('单次最多上传 10 张图片')
}

// 清空
const handleClear = () => {
  previewList.value.forEach((item) => URL.revokeObjectURL(item.url))
  previewList.value = []
  fileList.value = []
  selectedTags.value = []
}

// 切换标签选择
const toggleTag = (tagId) => {
  const index = selectedTags.value.indexOf(tagId)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tagId)
  }
  // console.log('selectedTags.value', selectedTags.value)
}

// 添加标签
const handleAddTag = async () => {
  if (!tagForm.name.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }

  try {
    await tagStore.create(tagForm)
    ElMessage.success('标签创建成功')
    showAddTagDialog.value = false
    tagForm.name = ''
    tagForm.color = '#409eff'
  } catch (error) {
    ElMessage.error('创建标签失败',error)
  }
}

// 上传
const handleUpload = async () => {
  if (previewList.value.length === 0) {
    ElMessage.warning('请选择要上传的图片')
    return
  }

  uploading.value = true
  uploadProgress.value = 0
  uploadedCount.value = 0

  try {
    const total = previewList.value.length

    for (let i = 0; i < previewList.value.length; i++) {
      const item = previewList.value[i]
      
      try {
        // 上传单张图片，传递选中的标签
        const uploadResult = await imageStore.upload(item.file, selectedTags.value)
        
        // 如果启用了AI识别，自动添加AI标签
        if (enableAiTag.value && uploadResult?.data?.id) {
          try {
            await analyzeAndAddTags(uploadResult.data.id, 0.5)
          } catch (aiError) {
            console.error(`AI识别 ${item.file.name} 失败:`, aiError)
            // AI识别失败不影响上传成功
          }
        }

        uploadedCount.value++
        uploadProgress.value = Math.round((uploadedCount.value / total) * 100)
      } catch (error) {
        console.error(`上传 ${item.file.name} 失败:`, error)
        ElMessage.error(`上传 ${item.file.name} 失败`)
      }
    }

    if (uploadedCount.value === total) {
      ElMessage.success('全部上传成功')
      handleClear()
      setTimeout(() => {
        router.push('/')
      }, 1000)
    } else {
      ElMessage.warning(`上传完成，成功 ${uploadedCount.value}/${total}`)
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}
</script>

<style scoped>
.upload-container {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
}

.upload-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.upload-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  color: #333;
  margin: 0 0 30px 0;
}

.upload-area {
  margin-bottom: 30px;
}

.upload-area :deep(.el-upload-dragger) {
  padding: 60px 20px;
}

.upload-icon {
  font-size: 80px;
  color: #409eff;
  margin-bottom: 20px;
}

.upload-text {
  text-align: center;
}

.main-text {
  font-size: 16px;
  color: #333;
  margin: 0 0 10px 0;
}

.sub-text {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.preview-section {
  margin-top: 40px;
}

.preview-section h3 {
  font-size: 18px;
  color: #333;
  margin: 0 0 20px 0;
}

.preview-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.preview-item {
  position: relative;
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s;
}

.preview-item:hover {
  transform: translateY(-4px);
}

.preview-item img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.preview-info {
  padding: 12px;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.exif-info {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.remove-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.9);
}

.tags-section {
  margin-bottom: 30px;
}

.tags-section h4 {
  font-size: 16px;
  color: #333;
  margin: 0 0 15px 0;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  cursor: pointer;
  transition: all 0.3s;
}

.tag-item:hover {
  transform: scale(1.05);
}

.ai-option-section {
  margin-bottom: 30px;
  padding: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 8px;
  border: 1px solid #bae6fd;
}

.ai-option-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #0369a1;
}

.ai-option-tip {
  margin: 8px 0 0 24px;
  font-size: 13px;
  color: #666;
}

.progress-section {
  margin-bottom: 30px;
}

.progress-text {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-top: 10px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .upload-card {
    padding: 20px;
  }

  .upload-title {
    font-size: 20px;
  }

  .upload-area :deep(.el-upload-dragger) {
    padding: 40px 20px;
  }

  .upload-icon {
    font-size: 60px;
  }

  .preview-list {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 10px;
  }

  .preview-item img {
    height: 150px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}
</style>

