<template>
  <div class="home">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside v-if="!isMobile" width="250px" class="sidebar">
        <div class="sidebar-content">

          <div class="section-header">
            <h3 class="section-title">我的标签</h3>
            <el-button
              v-if="selectedTags.length > 0"
              type="primary"
              link
              size="small"
              @click="clearTagSelection"
            >
              清除选择
            </el-button>
          </div>
          <div class="tags-list">
            <el-tag
              v-for="tag in tagStore.userTags"
              :key="tag.id"
              class="tag-item"
              :type="selectedTags.includes(tag.id) ? 'primary' : 'info'"
              :effect="selectedTags.includes(tag.id) ? 'dark' : 'light'"
              closable
              @click="handleTagSelect(tag.id)"
              @close="handleDeleteTag(tag)"
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
              @focus="handleSearchFocus"
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

        <!-- 视图切换和排序 -->
        <div class="view-options">
          <div class="view-options-left">
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button label="grid">
                <el-icon><Grid /></el-icon>
                <span v-if="!isMobile">网格</span>
              </el-radio-button>
              <el-radio-button label="list">
                <el-icon><List /></el-icon>
                <span v-if="!isMobile">列表</span>
              </el-radio-button>
              <el-radio-button label="slideshow">
                <el-icon><VideoPlay /></el-icon>
                <span v-if="!isMobile">轮播</span>
              </el-radio-button>
            </el-radio-group>

            <!-- 移动端标签筛选按钮 -->
            <el-button
              v-if="isMobile"
              type="primary"
              :icon="PictureFilled"
              size="small"
              @click="showTagDrawer = true"
            >
              标签
              <el-badge v-if="selectedTags.length > 0" :value="selectedTags.length" class="tag-badge" />
            </el-button>
          </div>

          <el-select
            v-model="sortBy"
            placeholder="排序方式"
            class="sort-select"
            size="small"
            @change="handleSort"
          >
            <el-option label="最新上传" value="createTime_desc" />
            <el-option label="最早上传" value="createTime_asc" />
            <el-option label="文件名 A-Z" value="fileName_asc" />
            <el-option label="文件名 Z-A" value="fileName_desc" />
            <el-option label="文件大小从大到小" value="fileSize_desc" />
            <el-option label="文件大小从小到大" value="fileSize_asc" />
          </el-select>
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
                :value="image.id"
                class="image-checkbox"
                @click.stop
              />
              <img :src="getThumbnailUrl(image.thumbnailPath)" :alt="image.fileName" />
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
                  :src="getThumbnailUrl(row.thumbnailPath)"
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
        <div v-if="imageStore.images.length > 0" class="pagination">
          <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :total="imageStore.total"
            :page-sizes="[8, 12, 16, 24]"
            :layout="isMobile ? 'total, prev, pager, next, sizes' : 'total, sizes, prev, pager, next, jumper'"
            :small="isMobile"
            @update:current-page="handlePageChange"
            @update:page-size="handleSizeChange"
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
      </el-form>
      <template #footer>
        <el-button @click="showAddTagDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddTag">确定</el-button>
      </template>
    </el-dialog>

    <!-- 移动端标签抽屉 -->
    <el-drawer
      v-model="showTagDrawer"
      title="标签筛选"
      direction="rtl"
      size="80%"
    >
      <div class="mobile-tags-container">
        <div class="section-header">
          <h3 class="section-title">我的标签</h3>
          <el-button
            v-if="selectedTags.length > 0"
            type="primary"
            link
            size="small"
            @click="clearTagSelection"
          >
            清除选择
          </el-button>
        </div>
        <div class="tags-list">
          <el-tag
            v-for="tag in tagStore.userTags"
            :key="tag.id"
            class="tag-item"
            :type="selectedTags.includes(tag.id) ? 'primary' : 'info'"
            :effect="selectedTags.includes(tag.id) ? 'dark' : 'light'"
            closable
            @click="handleTagSelect(tag.id)"
            @close="handleDeleteTag(tag)"
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
            添加标签
          </el-button>
        </div>
      </div>
    </el-drawer>
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
import { getThumbnailUrl, formatFileSize, formatDate } from '@/utils/image'
import ImageSlideshow from '@/components/ImageSlideshow.vue'

const router = useRouter()
const imageStore = useImageStore()
const tagStore = useTagStore()

const searchKeyword = ref('')
const viewMode = ref('grid')
const activeCategory = ref('all')
const selectedTags = ref([])
const selectedImages = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const filteredImagesByTags = ref([]) // 缓存多标签筛选结果
const showAddTagDialog = ref(false)
const showTagDrawer = ref(false) // 移动端标签抽屉
const isMobile = ref(window.innerWidth < 768)
const sortBy = ref('createTime_desc')

const tagForm = reactive({
  name: ''
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
    const [sortField, sortOrder] = sortBy.value.split('_')
    await imageStore.fetchUserImages({
      current: currentPage.value,
      size: pageSize.value,
      sortField,
      sortOrder
    })
  } catch (error) {
    ElMessage.error('加载图片失败')
  }
}

const handleSort = () => {
  currentPage.value = 1
  loadImages()
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadImages()
    return
  }
  
  try {
    await imageStore.search({
      keyword: searchKeyword.value,
      current: currentPage.value,
      size: pageSize.value
    })
  } catch (error) {
    ElMessage.error('搜索失败')
  }
}

const handleTagSelect = async (tagId) => {
  // 点击标签时清空搜索框
  searchKeyword.value = ''
  
  const index = selectedTags.value.indexOf(tagId)
  if (index > -1) {
    // 取消选择
    selectedTags.value.splice(index, 1)
  } else {
    // 添加选择
    selectedTags.value.push(tagId)
  }
  
  currentPage.value = 1
  await loadImagesByTags(true) // 强制刷新
}

const handleSearchFocus = () => {
  // 点击搜索框时清空标签选择
  if (selectedTags.value.length > 0) {
    selectedTags.value = []
    filteredImagesByTags.value = []
    tagFilterLoaded.value = false
    currentPage.value = 1
    loadImages()
  }
}

const clearTagSelection = () => {
  selectedTags.value = []
  filteredImagesByTags.value = []
  tagFilterLoaded.value = false
  currentPage.value = 1
  loadImages()
}

// 标记是否已加载过标签筛选数据
const tagFilterLoaded = ref(false)

const loadImagesByTags = async (forceRefresh = false) => {
  if (selectedTags.value.length === 0) {
    filteredImagesByTags.value = []
    tagFilterLoaded.value = false
    loadImages()
    return
  }
  
  try {
    // 如果需要刷新或未加载过数据，重新获取
    if (forceRefresh || !tagFilterLoaded.value) {
      // 获取所有选中标签的图片，然后取交集
      const promises = selectedTags.value.map(tagId => 
        tagStore.fetchImagesByTag(tagId, {
          page: 1,
          pageSize: 1000 // 获取足够多的数据用于交集计算
        })
      )
      
      const results = await Promise.all(promises)
      
      // 提取每个标签对应的图片ID集合
      const imageSets = results.map(res => {
        const images = res.data.records || res.data.list || res.data || []
        return new Set(images.map(img => img.id))
      })
      
      // 计算交集
      let intersectionIds = imageSets[0]
      for (let i = 1; i < imageSets.length; i++) {
        intersectionIds = new Set([...intersectionIds].filter(id => imageSets[i].has(id)))
      }
      
      // 获取第一个结果中符合交集的图片
      const firstImages = results[0].data.records || results[0].data.list || results[0].data || []
      filteredImagesByTags.value = firstImages.filter(img => intersectionIds.has(img.id))
      tagFilterLoaded.value = true
    }
    
    // 分页处理
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    
    imageStore.images = filteredImagesByTags.value.slice(start, end)
    imageStore.total = filteredImagesByTags.value.length
  } catch (error) {
    ElMessage.error('加载标签图片失败')
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
  } catch (error) {
    ElMessage.error('创建标签失败')
  }
}

const handleDeleteTag = async (tag) => {
  try {
    await ElMessageBox.confirm(`确定要删除标签「${tag.tagName}」吗？`, '提示', {
      type: 'warning'
    })
    await tagStore.remove(tag.id)
    // 如果删除的标签在选中列表中，移除它
    const index = selectedTags.value.indexOf(tag.id)
    if (index > -1) {
      selectedTags.value.splice(index, 1)
      if (selectedTags.value.length === 0) {
        loadImages()
      } else {
        loadImagesByTags(true)
      }
    }
    ElMessage.success('标签删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除标签失败')
    }
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
    // 刷新图片列表
    if (selectedTags.value.length > 0) {
      await loadImagesByTags(true)
    } else {
      await loadImages()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  if (selectedTags.value.length > 0) {
    // 分页切换时不需要重新获取数据，只需要重新切片
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    imageStore.images = filteredImagesByTags.value.slice(start, end)
  } else {
    loadImages()
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  if (selectedTags.value.length > 0) {
    // 每页数量变化时重新切片
    const start = 0
    const end = pageSize.value
    imageStore.images = filteredImagesByTags.value.slice(start, end)
  } else {
    loadImages()
  }
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

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-header .section-title {
  margin: 0;
}

.section-title {
  margin-top: 30px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item.el-tag--dark {
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.4);
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.view-options-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tag-badge {
  margin-left: 5px;
}

.sort-select {
  width: 180px;
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

.mobile-tags-container {
  padding: 10px;
}

.mobile-tags-container .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.mobile-tags-container .section-title {
  font-size: 16px;
  color: #333;
  margin: 0;
}

.mobile-tags-container .tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.pagination :deep(.el-pagination) {
  flex-wrap: wrap;
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

  .pagination {
    margin-top: 20px;
  }

  .pagination :deep(.el-pagination) {
    justify-content: center;
  }

  .pagination :deep(.el-pagination__total),
  .pagination :deep(.el-pagination__sizes) {
    margin-bottom: 10px;
  }

  .sort-select {
    width: 100%;
  }

  .view-options {
    flex-direction: column;
    align-items: stretch;
  }

  .view-options-left {
    justify-content: space-between;
    width: 100%;
  }
}
</style>

