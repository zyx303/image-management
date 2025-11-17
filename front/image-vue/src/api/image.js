import request from '@/utils/request'

/**
 * 上传图片
 */
export function uploadImage(file, tagIds = [], onProgress) {
  const formData = new FormData()
  formData.append('file', file)

  // 添加标签ID数组
  if (tagIds && tagIds.length > 0) {
    tagIds.forEach(tagId => {
      formData.append('tagIds', tagId)
    })
  }
  
  return request({
    url: '/images/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percentCompleted)
      }
    }
  })
}

/**
 * 批量上传图片
 */
export function uploadImages(files, onProgress) {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('files', file)
  })
  
  return request({
    url: '/images/batch-upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percentCompleted)
      }
    }
  })
}

/**
 * 获取图片列表
 */
export function getImageList(params) {
  return request({
    url: '/images',
    method: 'get',
    params
  })
}

/**
 * 获取图片详情
 */
export function getImageDetail(id) {
  return request({
    url: `/images/${id}`,
    method: 'get'
  })
}

/**
 * 更新图片信息
 */
export function updateImage(id, data) {
  return request({
    url: `/images/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除图片
 */
export function deleteImage(id) {
  return request({
    url: `/images/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除图片
 */
export function batchDeleteImages(ids) {
  return request({
    url: '/images/batch-delete',
    method: 'delete',
    data: { ids }
  })
}

/**
 * 添加图片标签
 */
export function addImageTag(imageId, tagId) {
  return request({
    url: `/images/${imageId}/tags`,
    method: 'post',
    data: { tagId }
  })
}

/**
 * 移除图片标签
 */
export function removeImageTag(imageId, tagId) {
  return request({
    url: `/images/${imageId}/tags/${tagId}`,
    method: 'delete'
  })
}

/**
 * 搜索图片
 */
export function searchImages(params) {
  return request({
    url: '/images/search',
    method: 'get',
    params
  })
}

/**
 * 获取用户的所有图片
 */
export function getUserImages(params) {
  return request({
    url: '/images/user',
    method: 'get',
    params
  })
}

/**
 * 下载图片
 */
export function downloadImage(id) {
  return request({
    url: `/images/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

