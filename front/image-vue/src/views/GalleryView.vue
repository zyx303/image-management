<template>
  <div class="gallery-view">
    <div class="gallery-header">
      <h1>图片画廊</h1>
      <p>浏览您的所有精彩瞬间</p>
    </div>

    <div class="gallery-container">
      <!-- 筛选和搜索 -->
      <div class="filters">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索图片..."
          :prefix-icon="Search"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>

        <el-select
          v-model="sortBy"
          placeholder="排序方式"
          class="sort-select"
          @change="handleSort"
        >
          <el-option label="最新上传" value="createTime_desc" />
          <el-option label="最早上传" value="createTime_asc" />
          <el-option label="文件名 A-Z" value="fileName_asc" />
          <el-option label="文件名 Z-A" value="fileName_desc" />
          <el-option label="文件大小从大到小" value="fileSize_desc" />
          <el-option label="文件大小从小到大" value="fileSize_asc" />
        </el-select>

        <el-button
          type="primary"
          :icon="VideoPlay"
          @click="showSlideshow = true"
        >
          幻灯片播放
        </el-button>
      </div>

      <!-- 图片网格 -->
      <div v-loading="imageStore.loading" class="gallery-grid">
        <div
          v-for="image in imageStore.images"
          :key="image.id"
          class="gallery-item"
          @click="handleImageClick(image)"
        >
          <div class="image-box">
            <img
              :src="getThumbnailUrl(image.thumbnailPath)"
              :alt="image.fileName"
              loading="lazy"
            />
            <div class="image-overlay">
              <div class="overlay-content">
                <h4>{{ image.fileName }}</h4>
                <p>{{ formatDateOnly(image.createTime) }}</p>
                <div class="overlay-actions">
                  <el-button
                    type="primary"
                    :icon="View"
                    circle
                    @click.stop="handleImageClick(image)"
                  />
                  <el-button
                    type="danger"
                    :icon="Delete"
                    circle
                    @click.stop="handleDelete(image.id)"
                  />
                </div>
              </div>
            </div>
          </div>
          <div class="image-caption">
            <span class="file-name">{{ image.fileName }}</span>
            <span class="file-size">{{ formatFileSize(image.fileSize) }}</span>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!imageStore.loading && imageStore.images.length === 0"
        description="暂无图片"
      >
        <el-button type="primary" @click="router.push('/upload')">
          上传图片
        </el-button>
      </el-empty>

      <!-- 分页 -->
      <div v-if="imageStore.total > pageSize" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="imageStore.total"
          :page-sizes="[12, 24, 48, 96]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 幻灯片播放对话框 -->
    <el-dialog
      v-model="showSlideshow"
      title="幻灯片播放"
      width="90%"
      fullscreen
      :show-close="true"
    >
      <ImageSlideshow :images="imageStore.images" :auto-play="true" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  VideoPlay,
  View,
  Delete
} from '@element-plus/icons-vue'
import { useImageStore } from '@/stores/image'
import { getImageUrl, getThumbnailUrl, formatFileSize, formatDateOnly } from '@/utils/image'
import ImageSlideshow from '@/components/ImageSlideshow.vue'

const router = useRouter()
const imageStore = useImageStore()

const searchKeyword = ref('')
const sortBy = ref('createTime_desc')
const currentPage = ref(1)
const pageSize = ref(24)
const showSlideshow = ref(false)

onMounted(() => {
  loadImages()
})

const loadImages = async () => {
  try {
    const [sortField, sortOrder] = sortBy.value.split('_')
    await imageStore.fetchUserImages({
      page: currentPage.value,
      pageSize: pageSize.value,
      sortField,
      sortOrder
    })
  } catch (error) {
    ElMessage.error('加载图片失败')
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadImages()
    return
  }

  try {
    await imageStore.search({
      keyword: searchKeyword.value,
      page: currentPage.value,
      pageSize: pageSize.value
    })
  } catch (error) {
    ElMessage.error('搜索失败')
  }
}

const handleSort = () => {
  currentPage.value = 1
  loadImages()
}

const handleImageClick = (image) => {
  router.push(`/image/${image.id}`)
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这张图片吗？', '提示', {
      type: 'warning'
    })
    await imageStore.remove(id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadImages()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadImages()
}
</script>

<style scoped>
.gallery-view {
  min-height: calc(100vh - 160px);
  background: #f5f7fa;
}

.gallery-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 20px;
  text-align: center;
}

.gallery-header h1 {
  font-size: 42px;
  margin: 0 0 10px 0;
  font-weight: bold;
}

.gallery-header p {
  font-size: 18px;
  margin: 0;
  opacity: 0.9;
}

.gallery-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
}

.filters {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 200px;
  max-width: 400px;
}

.sort-select {
  width: 200px;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
  margin-bottom: 40px;
}

.gallery-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;
}

.gallery-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.image-box {
  position: relative;
  width: 100%;
  height: 280px;
  overflow: hidden;
  background: #f5f7fa;
}

.image-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.gallery-item:hover .image-box img {
  transform: scale(1.1);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  opacity: 0;
  transition: opacity 0.3s;
  display: flex;
  align-items: flex-end;
}

.gallery-item:hover .image-overlay {
  opacity: 1;
}

.overlay-content {
  padding: 20px;
  color: white;
  width: 100%;
}

.overlay-content h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.overlay-content p {
  margin: 0 0 15px 0;
  font-size: 12px;
  opacity: 0.9;
}

.overlay-actions {
  display: flex;
  gap: 10px;
}

.image-caption {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.file-size {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .gallery-header {
    padding: 40px 20px;
  }

  .gallery-header h1 {
    font-size: 32px;
  }

  .gallery-header p {
    font-size: 16px;
  }

  .gallery-container {
    padding: 20px 10px;
  }

  .filters {
    flex-direction: column;
  }

  .search-input,
  .sort-select {
    width: 100%;
    max-width: 100%;
  }

  .gallery-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 15px;
  }

  .image-box {
    height: 180px;
  }

  .image-caption {
    padding: 10px;
  }

  .file-name {
    font-size: 12px;
  }
}
</style>

