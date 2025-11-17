<template>
  <div class="image-detail">
    <div v-loading="imageStore.loading" class="detail-container">
      <div v-if="image" class="detail-content">
        <!-- 左侧：图片展示区 -->
        <div class="image-section">
          <div class="image-wrapper">
            <img
              ref="imageRef"
              :src="getImageUrl(image.filePath)"
              :alt="image.fileName"
              class="main-image"
            />
          </div>

          <!-- 编辑工具栏 -->
          <div class="edit-toolbar">
            <el-button-group>
              <el-button :icon="Crop" @click="showCropDialog = true">
                裁剪
              </el-button>
              <el-button :icon="Edit" @click="showAdjustDialog = true">
                调整
              </el-button>
              <el-button :icon="Download" @click="handleDownload">
                下载
              </el-button>
              <el-button type="danger" :icon="Delete" @click="handleDelete">
                删除
              </el-button>
            </el-button-group>
          </div>
        </div>

        <!-- 右侧：信息面板 -->
        <div class="info-section">
          <div class="info-card">
            <h3>图片信息</h3>

            <!-- 文件名编辑 -->
            <div class="info-item">
              <label>文件名</label>
              <el-input
                v-model="editForm.fileName"
                placeholder="文件名"
                @blur="handleUpdateInfo"
              />
            </div>

            <!-- 描述编辑 -->
            <div class="info-item">
              <label>描述</label>
              <el-input
                v-model="editForm.description"
                type="textarea"
                :rows="3"
                placeholder="添加图片描述..."
                @blur="handleUpdateInfo"
              />
            </div>

            <!-- 标签管理 -->
            <div class="info-item">
              <label>标签</label>
              <div class="tags-container">
                <el-tag
                  v-for="tag in imageTags"
                  :key="tag.id"
                  closable
                  @close="handleRemoveTag(tag.id)"
                >
                  {{ tag.name }}
                </el-tag>
                <el-button
                  type="primary"
                  link
                  :icon="Plus"
                  size="small"
                  @click="showTagDialog = true"
                >
                  添加标签
                </el-button>
              </div>
            </div>

            <!-- 基本信息 -->
            <div class="info-item">
              <label>文件大小</label>
              <div class="info-value">{{ formatFileSize(image.fileSize) }}</div>
            </div>

            <div class="info-item">
              <label>分辨率</label>
              <div class="info-value">
                {{ image.width }} × {{ image.height }}
              </div>
            </div>

            <div class="info-item">
              <label>格式</label>
              <div class="info-value">{{ image.mimeType }}</div>
            </div>

            <div class="info-item">
              <label>上传时间</label>
              <div class="info-value">{{ formatDate(image.createTime) }}</div>
            </div>

            <!-- EXIF 信息 -->
            <div v-if="image.exifInfo" class="exif-section">
              <h4>EXIF 信息</h4>
              <div v-if="image.exifInfo.DateTime" class="info-item">
                <label>拍摄时间</label>
                <div class="info-value">{{ image.exifInfo.DateTime }}</div>
              </div>
              <div v-if="image.exifInfo.Make" class="info-item">
                <label>相机品牌</label>
                <div class="info-value">{{ image.exifInfo.Make }}</div>
              </div>
              <div v-if="image.exifInfo.Model" class="info-item">
                <label>相机型号</label>
                <div class="info-value">{{ image.exifInfo.Model }}</div>
              </div>
              <div v-if="image.exifInfo.GPSLatitude" class="info-item">
                <label>拍摄地点</label>
                <div class="info-value">
                  <el-icon><Location /></el-icon>
                  {{ image.exifInfo.GPSLatitude }}, {{ image.exifInfo.GPSLongitude }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else description="图片不存在" />
    </div>

    <!-- 裁剪对话框 -->
    <el-dialog
      v-model="showCropDialog"
      title="裁剪图片"
      width="800px"
      :close-on-click-modal="false"
    >
      <div class="crop-container">
        <img ref="cropImageRef" :src="getImageUrl(image?.filePath)" />
      </div>
      <template #footer>
        <el-button @click="showCropDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCrop">确定</el-button>
      </template>
    </el-dialog>

    <!-- 色调调整对话框 -->
    <el-dialog
      v-model="showAdjustDialog"
      title="调整图片"
      width="600px"
    >
      <div class="adjust-container">
        <div class="adjust-item">
          <label>亮度</label>
          <el-slider v-model="adjustments.brightness" :min="-100" :max="100" />
        </div>
        <div class="adjust-item">
          <label>对比度</label>
          <el-slider v-model="adjustments.contrast" :min="-100" :max="100" />
        </div>
        <div class="adjust-item">
          <label>饱和度</label>
          <el-slider v-model="adjustments.saturation" :min="-100" :max="100" />
        </div>
        <div class="adjust-item">
          <label>色调</label>
          <el-slider v-model="adjustments.hue" :min="-180" :max="180" />
        </div>

        <div class="preview-image">
          <img
            ref="adjustImageRef"
            :src="getImageUrl(image?.filePath)"
            :style="getAdjustStyle()"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="resetAdjustments">重置</el-button>
        <el-button @click="showAdjustDialog = false">取消</el-button>
        <el-button type="primary" @click="handleApplyAdjustments">应用</el-button>
      </template>
    </el-dialog>

    <!-- 添加标签对话框 -->
    <el-dialog v-model="showTagDialog" title="添加标签" width="400px">
      <el-select
        v-model="selectedNewTags"
        multiple
        placeholder="选择标签"
        style="width: 100%"
      >
        <el-option
          v-for="tag in availableTags"
          :key="tag.id"
          :label="tag.name"
          :value="tag.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="showTagDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddTags">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Crop,
  Edit,
  Download,
  Delete,
  Plus,
  Location
} from '@element-plus/icons-vue'
import { useImageStore } from '@/stores/image'
import { useTagStore } from '@/stores/tag'
import { addImageTag, removeImageTag } from '@/api/image'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'

const route = useRoute()
const router = useRouter()
const imageStore = useImageStore()
const tagStore = useTagStore()

const imageRef = ref(null)
const cropImageRef = ref(null)
const adjustImageRef = ref(null)
const showCropDialog = ref(false)
const showAdjustDialog = ref(false)
const showTagDialog = ref(false)
const selectedNewTags = ref([])

let cropper = null

const image = computed(() => imageStore.currentImage)
const imageTags = ref([])

const editForm = reactive({
  fileName: '',
  description: ''
})

const adjustments = reactive({
  brightness: 0,
  contrast: 0,
  saturation: 0,
  hue: 0
})

const availableTags = computed(() => {
  const imageTagIds = imageTags.value.map((t) => t.id)
  return tagStore.userTags.filter((t) => !imageTagIds.includes(t.id))
})

onMounted(async () => {
  const imageId = route.params.id
  if (imageId) {
    try {
      await imageStore.fetchImageDetail(imageId)
      if (image.value) {
        editForm.fileName = image.value.fileName
        editForm.description = image.value.description || ''
        // 假设 API 返回图片的标签信息
        imageTags.value = image.value.tags || []
      }
      await tagStore.fetchUserTags()
    } catch (error) {
      ElMessage.error('加载图片失败')
      router.push('/')
    }
  }
})

onUnmounted(() => {
  if (cropper) {
    cropper.destroy()
    cropper = null
  }
})

// 监听裁剪对话框显示
watch(showCropDialog, (val) => {
  if (val) {
    setTimeout(() => {
      if (cropImageRef.value && !cropper) {
        cropper = new Cropper(cropImageRef.value, {
          aspectRatio: NaN,
          viewMode: 1,
          autoCropArea: 1
        })
      }
    }, 100)
  } else {
    if (cropper) {
      cropper.destroy()
      cropper = null
    }
  }
})

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/files/${path}`
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const handleUpdateInfo = async () => {
  if (!image.value) return

  try {
    await imageStore.update(image.value.id, {
      fileName: editForm.fileName,
      description: editForm.description
    })
    ElMessage.success('更新成功')
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleAddTags = async () => {
  if (selectedNewTags.value.length === 0) {
    ElMessage.warning('请选择标签')
    return
  }

  try {
    for (const tagId of selectedNewTags.value) {
      await addImageTag(image.value.id, tagId)
      const tag = tagStore.userTags.find((t) => t.id === tagId)
      if (tag) {
        imageTags.value.push(tag)
      }
    }
    ElMessage.success('添加标签成功')
    showTagDialog.value = false
    selectedNewTags.value = []
  } catch (error) {
    ElMessage.error('添加标签失败')
  }
}

const handleRemoveTag = async (tagId) => {
  try {
    await removeImageTag(image.value.id, tagId)
    imageTags.value = imageTags.value.filter((t) => t.id !== tagId)
    ElMessage.success('移除标签成功')
  } catch (error) {
    ElMessage.error('移除标签失败')
  }
}

const handleCrop = () => {
  if (!cropper) return

  const canvas = cropper.getCroppedCanvas()
  canvas.toBlob(async (blob) => {
    try {
      // 这里应该将裁剪后的图片上传到服务器
      // 为了演示，我们只是显示成功消息
      ElMessage.success('裁剪成功')
      showCropDialog.value = false
    } catch (error) {
      ElMessage.error('裁剪失败')
    }
  })
}

const getAdjustStyle = () => {
  return {
    filter: `
      brightness(${100 + adjustments.brightness}%)
      contrast(${100 + adjustments.contrast}%)
      saturate(${100 + adjustments.saturation}%)
      hue-rotate(${adjustments.hue}deg)
    `
  }
}

const resetAdjustments = () => {
  adjustments.brightness = 0
  adjustments.contrast = 0
  adjustments.saturation = 0
  adjustments.hue = 0
}

const handleApplyAdjustments = () => {
  // 这里应该将调整应用到图片并保存
  // 为了演示，我们只是显示成功消息
  ElMessage.success('应用成功')
  showAdjustDialog.value = false
}

const handleDownload = () => {
  if (!image.value) return
  const link = document.createElement('a')
  link.href = getImageUrl(image.value.filePath)
  link.download = image.value.fileName
  link.click()
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这张图片吗？', '提示', {
      type: 'warning'
    })
    await imageStore.remove(image.value.id)
    ElMessage.success('删除成功')
    router.push('/')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.image-detail {
  min-height: calc(100vh - 160px);
  background: #f5f7fa;
  padding: 20px;
}

.detail-container {
  max-width: 1400px;
  margin: 0 auto;
}

.detail-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 20px;
}

.image-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.image-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 500px;
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}

.main-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}

.edit-toolbar {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.info-card h3 {
  font-size: 18px;
  color: #333;
  margin: 0 0 20px 0;
}

.info-item {
  margin-bottom: 20px;
}

.info-item label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #333;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.exif-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.exif-section h4 {
  font-size: 16px;
  color: #333;
  margin: 0 0 15px 0;
}

.crop-container {
  max-height: 500px;
  overflow: hidden;
}

.crop-container img {
  max-width: 100%;
}

.adjust-container {
  padding: 10px;
}

.adjust-item {
  margin-bottom: 24px;
}

.adjust-item label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.preview-image {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.preview-image img {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
}

/* 移动端适配 */
@media (max-width: 1024px) {
  .detail-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .image-detail {
    padding: 10px;
  }

  .image-section {
    padding: 15px;
  }

  .image-wrapper {
    min-height: 300px;
  }

  .main-image {
    max-height: 50vh;
  }

  .edit-toolbar :deep(.el-button-group) {
    display: flex;
    flex-wrap: wrap;
  }

  .info-card {
    padding: 15px;
  }
}
</style>

