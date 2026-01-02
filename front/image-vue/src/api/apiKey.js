import request from '@/utils/request'

/**
 * 获取用户的所有 API Key
 */
export function getApiKeys() {
  return request({
    url: '/api-keys',
    method: 'get'
  })
}

/**
 * 创建 API Key
 */
export function createApiKey(name) {
  return request({
    url: '/api-keys',
    method: 'post',
    data: { name }
  })
}

/**
 * 删除 API Key
 */
export function deleteApiKey(id) {
  return request({
    url: `/api-keys/${id}`,
    method: 'delete'
  })
}

/**
 * 禁用/启用 API Key
 */
export function toggleApiKey(id) {
  return request({
    url: `/api-keys/${id}/toggle`,
    method: 'put'
  })
}

/**
 * 重新生成 API Key
 */
export function regenerateApiKey(id) {
  return request({
    url: `/api-keys/${id}/regenerate`,
    method: 'put'
  })
}
