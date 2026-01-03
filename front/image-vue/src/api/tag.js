import request from '@/utils/request'

/**
 * 获取所有标签（用户可见的：用户自己的 + 系统默认）
 */
export function getAllTags() {
  return request({
    url: '/tags',
    method: 'get'
  })
}

/**
 * 获取系统默认标签
 */
export function getDefaultTags() {
  return request({
    url: '/tags/default',
    method: 'get'
  })
}

/**
 * 获取用户的标签
 */
export function getUserTags() {
  return request({
    url: '/tags/user',
    method: 'get'
  })
}

/**
 * 创建标签
 */
export function createTag(data) {
  return request({
    url: '/tags',
    method: 'post',
    data
  })
}

/**
 * 更新标签
 */
export function updateTag(id, data) {
  return request({
    url: `/tags/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除标签
 */
export function deleteTag(id) {
  return request({
    url: `/tags/${id}`,
    method: 'delete'
  })
}

/**
 * 根据标签ID获取图片
 */
export function getImagesByTag(tagId, params) {
  return request({
    url: `/tags/${tagId}/images`,
    method: 'get',
    params
  })
}

/**
 * 搜索标签
 */
export function searchTags(keyword) {
  return request({
    url: '/tags/search',
    method: 'get',
    params: { keyword }
  })
}

