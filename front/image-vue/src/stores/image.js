import { defineStore } from 'pinia'
import { ref } from 'vue'
import { 
  getImageList, 
  getImageDetail, 
  uploadImage,
  uploadImages,
  updateImage,
  deleteImage,
  batchDeleteImages,
  searchImages,
  getUserImages
} from '@/api/image'

export const useImageStore = defineStore('image', () => {
  // 状态
  const images = ref([])
  const currentImage = ref(null)
  const total = ref(0)
  const loading = ref(false)
  const uploadProgress = ref(0)

  // 获取图片列表
  async function fetchImages(params = {}) {
    loading.value = true
    try {
      const res = await getImageList(params)
      images.value = res.data.records || res.data.list || res.data
      total.value = res.data.total || images.value.length
      return res
    } catch (error) {
      console.error('Fetch images failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取用户图片列表
  async function fetchUserImages(params = {}) {
    loading.value = true
    try {
      const res = await getUserImages(params)
      images.value = res.data.records || res.data.list || res.data
      total.value = res.data.total || images.value.length
      return res
    } catch (error) {
      console.error('Fetch user images failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 搜索图片
  async function search(params) {
    loading.value = true
    try {
      const res = await searchImages(params)
      images.value = res.data.records || res.data.list || res.data
      total.value = res.data.total || images.value.length
      return res
    } catch (error) {
      console.error('Search images failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取图片详情
  async function fetchImageDetail(id) {
    loading.value = true
    try {
      const res = await getImageDetail(id)
      currentImage.value = res.data
      return res
    } catch (error) {
      console.error('Fetch image detail failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 上传单张图片
  async function upload(file, tagIds = []) {
    uploadProgress.value = 0
    try {
      const res = await uploadImage(file, tagIds, (progress) => {
        uploadProgress.value = progress
      })
      return res
    } catch (error) {
      console.error('Upload image failed:', error)
      throw error
    } finally {
      uploadProgress.value = 0
    }
  }

  // 批量上传图片
  async function batchUpload(files) {
    uploadProgress.value = 0
    try {
      const res = await uploadImages(files, (progress) => {
        uploadProgress.value = progress
      })
      return res
    } catch (error) {
      console.error('Batch upload images failed:', error)
      throw error
    } finally {
      uploadProgress.value = 0
    }
  }

  // 更新图片信息
  async function update(id, data) {
    try {
      const res = await updateImage(id, data)
      // 更新本地数据
      if (currentImage.value && currentImage.value.id === id) {
        currentImage.value = { ...currentImage.value, ...data }
      }
      const index = images.value.findIndex((img) => img.id === id)
      if (index !== -1) {
        images.value[index] = { ...images.value[index], ...data }
      }
      return res
    } catch (error) {
      console.error('Update image failed:', error)
      throw error
    }
  }

  // 删除图片
  async function remove(id) {
    try {
      const res = await deleteImage(id)
      // 从列表中移除
      images.value = images.value.filter((img) => img.id !== id)
      total.value = Math.max(0, total.value - 1)
      if (currentImage.value && currentImage.value.id === id) {
        currentImage.value = null
      }
      return res
    } catch (error) {
      console.error('Delete image failed:', error)
      throw error
    }
  }

  // 批量删除图片
  async function batchRemove(ids) {
    try {
      const res = await batchDeleteImages(ids)
      // 从列表中移除
      images.value = images.value.filter((img) => !ids.includes(img.id))
      total.value = Math.max(0, total.value - ids.length)
      return res
    } catch (error) {
      console.error('Batch delete images failed:', error)
      throw error
    }
  }

  // 清空当前图片
  function clearCurrentImage() {
    currentImage.value = null
  }

  // 设置当前图片
  function setCurrentImage(image) {
    currentImage.value = image
  }

  return {
    images,
    currentImage,
    total,
    loading,
    uploadProgress,
    fetchImages,
    fetchUserImages,
    search,
    fetchImageDetail,
    upload,
    batchUpload,
    update,
    remove,
    batchRemove,
    clearCurrentImage,
    setCurrentImage
  }
})

