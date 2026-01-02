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
              <el-button :icon="MagicStick" :loading="aiAnalyzing" @click="handleAiAnalyze">
                AI识别
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
                  :type="tag.tagType === 3 ? 'success' : ''"
                  @close="handleRemoveTag(tag.id)"
                >
                  <el-icon v-if="tag.tagType === 3" style="margin-right: 4px;"><MagicStick /></el-icon>
                  {{ tag.tagName }}
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
                <el-button
                  type="success"
                  link
                  :icon="MagicStick"
                  size="small"
                  :loading="aiAnalyzing"
                  @click="handleAiAnalyze"
                >
                  AI识别标签
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
              <div class="info-value">{{ image.fileType }}</div>
            </div>

            <div class="info-item">
              <label>上传时间</label>
              <div class="info-value">{{ formatDate(image.uploadTime) }}</div>
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
          :label="tag.tagName"
          :value="tag.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="showTagDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddTags">确定</el-button>
      </template>
    </el-dialog>

    <!-- AI识别结果对话框 -->
    <el-dialog
      v-model="showAiResultDialog"
      title="AI 识别结果"
      width="500px"
    >
      <div v-if="aiTags.length > 0" class="ai-result-container">
        <p class="ai-tip">以下是 AI 识别出的标签，勾选后点击"添加选中标签"可将其添加到图片：</p>
        <div class="ai-tags-list">
          <el-checkbox-group v-model="selectedAiTags">
            <div v-for="tag in aiTags" :key="tag.keyword" class="ai-tag-item">
              <el-checkbox :value="tag.keyword">
                <div class="ai-tag-info">
                  <span class="ai-tag-name">{{ tag.keyword }}</span>
                  <el-tag size="small" type="info" class="ai-tag-category">{{ tag.category || '未分类' }}</el-tag>
                  <el-progress
                    :percentage="Math.round(tag.score * 100)"
                    :stroke-width="6"
                    :show-text="true"
                    class="ai-tag-score"
                  />
                </div>
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </div>
      </div>
      <el-empty v-else description="未识别到任何标签" />
      <template #footer>
        <el-button @click="showAiResultDialog = false">关闭</el-button>
        <el-button
          type="primary"
          :disabled="selectedAiTags.length === 0"
          :loading="addingAiTags"
          @click="handleAddAiTags"
        >
          添加选中标签 ({{ selectedAiTags.length }})
        </el-button>
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
  Location,
  MagicStick
} from '@element-plus/icons-vue'
import { useImageStore } from '@/stores/image'
import { useTagStore } from '@/stores/tag'
import { addImageTag, removeImageTag, editImage } from '@/api/image'
import { analyzeExistingImage, analyzeAndAddTags } from '@/api/ai'
import { getImageUrl, formatFileSize, formatDate } from '@/utils/image'
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
const showAiResultDialog = ref(false)
const selectedNewTags = ref([])

// AI 相关状态
const aiAnalyzing = ref(false)
const aiTags = ref([])
const selectedAiTags = ref([])
const addingAiTags = ref(false)

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

// AI 分析图片
const handleAiAnalyze = async () => {
  if (!image.value) return
  
  aiAnalyzing.value = true
  aiTags.value = []
  selectedAiTags.value = []
  
  try {
    const res = await analyzeExistingImage(image.value.id)
    if (res.code === 200 && res.data) {
      aiTags.value = res.data
      // 默认选中置信度大于0.5的标签
      selectedAiTags.value = res.data
        .filter(tag => tag.score >= 0.5)
        .map(tag => tag.keyword)
      showAiResultDialog.value = true
    } else {
      ElMessage.error(res.message || 'AI分析失败')
    }
  } catch (error) {
    console.error('AI分析失败:', error)
    ElMessage.error('AI分析失败，请检查是否已配置百度智能云API')
  } finally {
    aiAnalyzing.value = false
  }
}

// 添加AI识别的标签
const handleAddAiTags = async () => {
  if (selectedAiTags.value.length === 0) {
    ElMessage.warning('请选择要添加的标签')
    return
  }
  
  addingAiTags.value = true
  
  try {
    let addedCount = 0
    const addedTagNames = new Set() // 用于去重
    
    // 遍历用户选中的标签关键词
    for (const keyword of selectedAiTags.value) {
      // 找到对应的AI标签对象
      const aiTag = aiTags.value.find(t => t.keyword === keyword)
      if (!aiTag || !aiTag.category) continue
      
      // 从category中提取大类（破折号前的部分）
      const tagName = aiTag.category.split('-')[0].trim()
      if (!tagName || addedTagNames.has(tagName)) continue
      
      addedTagNames.add(tagName)
      
      // 查找该标签是否已存在于用户标签中
      let tag = tagStore.userTags.find((t) => t.tagName === tagName)
      
      // 如果不存在，创建新标签
      if (!tag) {
        try {
          await tagStore.create({ name: tagName, tagType: 3 }) // tagType: 3 表示AI标签
          // 重新获取标签列表
          await tagStore.fetchUserTags()
          tag = tagStore.userTags.find((t) => t.tagName === tagName)
        } catch (createError) {
          console.error(`创建标签 ${tagName} 失败:`, createError)
          continue
        }
      }
      
      if (tag) {
        // 检查图片是否已有该标签
        const hasTag = imageTags.value.some((t) => t.id === tag.id)
        if (!hasTag) {
          try {
            await addImageTag(image.value.id, tag.id)
            imageTags.value.push(tag)
            addedCount++
          } catch (addError) {
            console.error(`添加标签 ${tagName} 失败:`, addError)
          }
        }
      }
    }
    
    if (addedCount > 0) {
      ElMessage.success(`成功添加 ${addedCount} 个标签`)
      showAiResultDialog.value = false
      selectedAiTags.value = []
      // 刷新图片详情以确保数据同步
      await imageStore.fetchImageDetail(image.value.id)
      if (image.value) {
        imageTags.value = image.value.tags || []
      }
    } else {
      ElMessage.warning('所选标签已存在或添加失败')
    }
  } catch (error) {
    console.error('添加AI标签失败:', error)
    ElMessage.error('添加标签失败')
  } finally {
    addingAiTags.value = false
  }
}

const handleCrop = async () => {
  if (!cropper) return

  const canvas = cropper.getCroppedCanvas()
  const imageData = canvas.toDataURL('image/jpeg', 0.9)
  
  try {
    const res = await editImage(image.value.id, imageData)
    if (res.code === 200) {
      ElMessage.success('裁剪成功')
      showCropDialog.value = false
      // 刷新图片详情
      await imageStore.fetchImageDetail(image.value.id)
    } else {
      ElMessage.error(res.message || '裁剪失败')
    }
  } catch (error) {
    ElMessage.error('裁剪失败')
  }
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

const handleApplyAdjustments = async () => {
  if (!adjustImageRef.value || !image.value) return
  
  try {
    // 创建canvas应用滤镜
    const img = new Image()
    img.crossOrigin = 'anonymous'
    
    await new Promise((resolve, reject) => {
      img.onload = resolve
      img.onerror = reject
      img.src = getImageUrl(image.value.filePath)
    })
    
    const canvas = document.createElement('canvas')
    canvas.width = img.width
    canvas.height = img.height
    const ctx = canvas.getContext('2d')
    
    // 应用滤镜
    ctx.filter = `
      brightness(${100 + adjustments.brightness}%)
      contrast(${100 + adjustments.contrast}%)
      saturate(${100 + adjustments.saturation}%)
      hue-rotate(${adjustments.hue}deg)
    `
    ctx.drawImage(img, 0, 0)
    
    // 获取图片数据
    const imageData = canvas.toDataURL('image/jpeg', 0.9)
    
    const res = await editImage(image.value.id, imageData)
    if (res.code === 200) {
      ElMessage.success('应用成功')
      showAdjustDialog.value = false
      resetAdjustments()
      // 刷新图片详情
      await imageStore.fetchImageDetail(image.value.id)
    } else {
      ElMessage.error(res.message || '应用失败')
    }
  } catch (error) {
    console.error('应用调整失败:', error)
    ElMessage.error('应用失败')
  }
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
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    // 用户点击取消
    return
  }

  try {
    await imageStore.remove(image.value.id)
    ElMessage.success('删除成功')
    router.push('/')
  } catch (error) {
    console.error('删除图片失败:', error)
    ElMessage.error('删除失败')
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

/* AI识别结果样式 */
.ai-result-container {
  max-height: 400px;
  overflow-y: auto;
}

.ai-tip {
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
}

.ai-tags-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-tag-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.ai-tag-item :deep(.el-checkbox__label) {
  width: 100%;
}

.ai-tag-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.ai-tag-name {
  font-weight: 500;
  color: #333;
  min-width: 80px;
}

.ai-tag-category {
  flex-shrink: 0;
}

.ai-tag-score {
  flex: 1;
  min-width: 100px;
  max-width: 150px;
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

