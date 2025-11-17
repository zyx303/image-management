<template>
  <div class="home">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside v-if="!isMobile" width="250px" class="sidebar">
        <div class="sidebar-content">
          <h3>分类筛选</h3>
          
          <el-menu :default-active="activeCategory" @select="handleCategorySelect">
            <el-menu-item index="all">
              <el-icon><PictureFilled /></el-icon>
              <span>全部图片</span>
            </el-menu-item>
            <el-menu-item index="recent">
              <el-icon><Clock /></el-icon>
              <span>最近上传</span>
            </el-menu-item>
          </el-menu>

          <h3 class="section-title">我的标签</h3>
          <div class="tags-list">
            <el-tag
              v-for="tag in tagStore.userTags"
              :key="tag.id"
              class="tag-item"
              :type="selectedTag === tag.id ? 'primary' : 'info'"
              @click="handleTagSelect(tag.id)"
            >
              {{ tag.name }}
            </el-tag>
            <el-button
              type="primary"
              link
              :icon="Plus"
              size="small"
              @click="showAddTagDialog = true"
            >
              添加标签
            </el-button>
          </div>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <!-- 搜索和操作栏 -->
        <div class="toolbar">
          <div class="search-bar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索图片..."
              :prefix-icon="Search"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button :icon="Search" @click="handleSearch" />
              </template>
            </el-input>
          </div>

          <div class="action-buttons">
            <el-button
              type="primary"
              :icon="Upload"
              @click="router.push('/upload')"
            >
              上传图片
            </el-button>
            <el-button
              v-if="selectedImages.length > 0"
              type="danger"
              :icon="Delete"
              @click="handleBatchDelete"
            >
              删除选中 ({{ selectedImages.length }})
            </el-button>
          </div>
        </div>

        <!-- 视图切换 -->
        <div class="view-options">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="grid">
              <el-icon><Grid /></el-icon>
              网格
            </el-radio-button>
            <el-radio-button label="list">
              <el-icon><List /></el-icon>
              列表
            </el-radio-button>
            <el-radio-button label="slideshow">
              <el-icon><VideoPlay /></el-icon>
              轮播
            </el-radio-button>
          </el-radio-group>
        </div>

        <!-- 图片展示区 -->
        <div v-loading="imageStore.loading" class="images-container">
          <!-- 网格视图 -->
          <div v-if="viewMode === 'grid'" class="grid-view">
            <div
              v-for="image in imageStore.images"
              :key="image.id"
              class="image-card"
              @click="handleImageClick(image)"
            >
              <el-checkbox
                v-model="selectedImages"
                :label="image.id"
                class="image-checkbox"
                @click.stop
              />
              <img :src="getImageUrl(image.thumbnailPath)" :alt="image.fileName" />
              <div class="image-info">
                <div class="image-name">{{ image.fileName }}</div>
                <div class="image-meta">
                  <span>{{ formatFileSize(image.fileSize) }}</span>
                  <span>{{ formatDate(image.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 列表视图 -->
          <el-table
            v-else-if="viewMode === 'list'"
            :data="imageStore.images"
            @row-click="handleImageClick"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column label="预览" width="80">
              <template #default="{ row }">
                <img
                  :src="getImageUrl(row.thumbnailPath)"
                  class="table-thumbnail"
                  :alt="row.fileName"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fileName" label="文件名" min-width="200" />
            <el-table-column label="大小" width="100">
              <template #default="{ row }">
                {{ formatFileSize(row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column label="分辨率" width="120">
              <template #default="{ row }">
                {{ row.width }} × {{ row.height }}
              </template>
            </el-table-column>
            <el-table-column label="上传时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click.stop="handleImageClick(row)">
                  查看
                </el-button>
                <el-button type="danger" link size="small" @click.stop="handleDelete(row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 轮播视图 -->
          <div v-else-if="viewMode === 'slideshow'" class="slideshow-view">
            <ImageSlideshow :images="imageStore.images" />
          </div>

          <!-- 空状态 -->
          <el-empty
            v-if="!imageStore.loading && imageStore.images.length === 0"
            description="暂无图片"
          >
            <el-button type="primary" @click="router.push('/upload')">
              上传第一张图片
            </el-button>
          </el-empty>
        </div>

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
      </el-main>
    </el-container>

    <!-- 添加标签对话框 -->
    <el-dialog v-model="showAddTagDialog" title="添加标签" width="400px">
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Upload,
  Delete,
  Plus,
  Grid,
  List,
  VideoPlay,
  PictureFilled,
  Clock
} from '@element-plus/icons-vue'
import { useImageStore } from '@/stores/image'
import { useTagStore } from '@/stores/tag'
import ImageSlideshow from '@/components/ImageSlideshow.vue'

const router = useRouter()
const imageStore = useImageStore()
const tagStore = useTagStore()

const searchKeyword = ref('')
const viewMode = ref('grid')
const activeCategory = ref('all')
const selectedTag = ref(null)
const selectedImages = ref([])
const currentPage = ref(1)
const pageSize = ref(24)
const showAddTagDialog = ref(false)
const isMobile = ref(window.innerWidth < 768)

const tagForm = reactive({
  name: '',
  color: '#409eff'
})

// 监听窗口大小变化
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth < 768
})

onMounted(() => {
  loadImages()
  tagStore.fetchUserTags()
})

const loadImages = async () => {
  try {
    await imageStore.fetchUserImages({
      page: currentPage.value,
      pageSize: pageSize.value
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

const handleCategorySelect = (index) => {
  activeCategory.value = index
  selectedTag.value = null
  loadImages()
}

const handleTagSelect = async (tagId) => {
  if (selectedTag.value === tagId) {
    selectedTag.value = null
    loadImages()
  } else {
    selectedTag.value = tagId
    try {
      const res = await tagStore.fetchImagesByTag(tagId, {
        page: currentPage.value,
        pageSize: pageSize.value
      })
      imageStore.images = res.data.records || res.data.list || res.data
      imageStore.total = res.data.total || imageStore.images.length
    } catch (error) {
      ElMessage.error('加载标签图片失败')
    }
  }
}

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
    ElMessage.error('创建标签失败')
  }
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

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedImages.value.length} 张图片吗？`, '提示', {
      type: 'warning'
    })
    await imageStore.batchRemove(selectedImages.value)
    ElMessage.success('批量删除成功')
    selectedImages.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadImages()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadImages()
}

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
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  background: white;
  border-right: 1px solid #e4e7ed;
  padding: 20px;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.sidebar-content h3 {
  font-size: 16px;
  color: #333;
  margin: 0 0 15px 0;
}

.section-title {
  margin-top: 30px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  cursor: pointer;
  transition: all 0.3s;
}

.tag-item:hover {
  transform: translateY(-2px);
}

.main-content {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 20px;
  flex-wrap: wrap;
}

.search-bar {
  flex: 1;
  max-width: 500px;
  min-width: 200px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.view-options {
  margin-bottom: 20px;
}

.images-container {
  min-height: 400px;
}

.grid-view {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.image-card {
  position: relative;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.image-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.image-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.image-checkbox {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 10;
}

.image-info {
  padding: 12px;
}

.image-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.table-thumbnail {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.slideshow-view {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-bar {
    max-width: 100%;
  }

  .action-buttons {
    justify-content: center;
  }

  .grid-view {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 10px;
  }

  .image-card img {
    height: 150px;
  }
}
</style>

