/**
 * 图片相关工具函数
 */

import { useUserStore } from '@/stores/user'

/**
 * 获取图片URL
 * @param {string} path - 图片相对路径
 * @returns {string} 完整的图片URL（包含认证token）
 */
export function getImageUrl(path) {
  if (!path) return ''
  if (path.startsWith('http')) return path

  const userStore = useUserStore()

  // 使用相对路径，让请求通过Vite代理，避免ORB拦截
  // Vite会将/api/*代理到后端
  if (userStore.token) {
    return `/api/files/${path}?token=${userStore.token}`
  }

  return `/api/files/${path}`
}

/**
 * 获取缩略图URL
 * @param {string} path - 图片相对路径
 * @returns {string} 完整的缩略图URL（包含认证token）
 */
export function getThumbnailUrl(path) {
  if (!path) return ''
  if (path.startsWith('http')) return path

  const userStore = useUserStore()

  // 使用相对路径，让请求通过Vite代理，避免ORB拦截
  if (userStore.token) {
    return `/api/files/thumbnails/${path}?token=${userStore.token}`
  }

  return `/api/files/thumbnails/${path}`
}

/**
 * 格式化文件大小
 * @param {number} bytes - 文件大小（字节）
 * @returns {string} 格式化后的文件大小
 */
export function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

/**
 * 格式化日期
 * @param {string|Date} date - 日期
 * @returns {string} 格式化后的日期
 */
export function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

/**
 * 格式化日期（仅日期部分）
 * @param {string|Date} date - 日期
 * @returns {string} 格式化后的日期
 */
export function formatDateOnly(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

