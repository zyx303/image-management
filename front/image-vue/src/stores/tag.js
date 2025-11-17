import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getAllTags,
  getUserTags,
  createTag,
  updateTag,
  deleteTag,
  getImagesByTag,
  searchTags
} from '@/api/tag'

export const useTagStore = defineStore('tag', () => {
  // 状态
  const tags = ref([])
  const userTags = ref([])
  const loading = ref(false)

  // 获取所有标签
  async function fetchAllTags() {
    loading.value = true
    try {
      const res = await getAllTags()
      tags.value = res.data || []
      return res
    } catch (error) {
      console.error('Fetch all tags failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取用户标签
  async function fetchUserTags() {
    loading.value = true
    try {
      const res = await getUserTags()
      userTags.value = res.data || []
      // console.log('userTags.value', userTags.value[0])
      return res
    } catch (error) {
      console.error('Fetch user tags failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 创建标签
  async function create(data) {
    try {
      const res = await createTag(data)
      // 添加到本地列表
      if (res.data) {
        userTags.value.push(res.data)
        tags.value.push(res.data)
      }
      return res
    } catch (error) {
      console.error('Create tag failed:', error)
      throw error
    }
  }

  // 更新标签
  async function update(id, data) {
    try {
      const res = await updateTag(id, data)
      // 更新本地数据
      const updateLocal = (list) => {
        const index = list.findIndex((tag) => tag.id === id)
        if (index !== -1) {
          list[index] = { ...list[index], ...data }
        }
      }
      updateLocal(tags.value)
      updateLocal(userTags.value)
      return res
    } catch (error) {
      console.error('Update tag failed:', error)
      throw error
    }
  }

  // 删除标签
  async function remove(id) {
    try {
      const res = await deleteTag(id)
      // 从本地列表中移除
      tags.value = tags.value.filter((tag) => tag.id !== id)
      userTags.value = userTags.value.filter((tag) => tag.id !== id)
      return res
    } catch (error) {
      console.error('Delete tag failed:', error)
      throw error
    }
  }

  // 根据标签获取图片
  async function fetchImagesByTag(tagId, params = {}) {
    loading.value = true
    try {
      const res = await getImagesByTag(tagId, params)
      return res
    } catch (error) {
      console.error('Fetch images by tag failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 搜索标签
  async function search(keyword) {
    loading.value = true
    try {
      const res = await searchTags(keyword)
      return res
    } catch (error) {
      console.error('Search tags failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  return {
    tags,
    userTags,
    loading,
    fetchAllTags,
    fetchUserTags,
    create,
    update,
    remove,
    fetchImagesByTag,
    search
  }
})

