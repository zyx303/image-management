import request from '@/utils/request'

/**
 * 检查AI服务状态
 */
export function checkAiStatus() {
  return request({
    url: '/ai/status',
    method: 'get'
  })
}

/**
 * 获取AI配置
 */
export function getAiConfig() {
  return request({
    url: '/ai/config',
    method: 'get'
  })
}

/**
 * 保存AI配置
 * @param {Object} config { apiKey, secretKey }
 */
export function saveAiConfig(config) {
  return request({
    url: '/ai/config',
    method: 'post',
    data: config
  })
}

/**
 * 测试AI配置连接
 * @param {Object} config { apiKey, secretKey }
 */
export function testAiConfig(config) {
  return request({
    url: '/ai/config/test',
    method: 'post',
    data: config,
    timeout: 30000
  })
}

/**
 * 分析上传的图片
 * @param {File} file 图片文件
 */
export function analyzeUploadedImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/ai/analyze',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 60000 // AI分析可能需要更长时间
  })
}

/**
 * 分析已存在的图片
 * @param {number} imageId 图片ID
 */
export function analyzeExistingImage(imageId) {
  return request({
    url: `/ai/analyze/${imageId}`,
    method: 'get',
    timeout: 60000
  })
}

/**
 * 分析图片并自动添加标签
 * @param {number} imageId 图片ID
 * @param {number} minScore 最低置信度阈值（0-1），默认0.5
 */
export function analyzeAndAddTags(imageId, minScore = 0.5) {
  return request({
    url: `/ai/analyze-and-tag/${imageId}`,
    method: 'post',
    params: { minScore },
    timeout: 60000
  })
}

/**
 * 通过URL分析图片
 * @param {string} url 图片URL
 */
export function analyzeImageByUrl(url) {
  return request({
    url: '/ai/analyze-url',
    method: 'post',
    data: { url },
    timeout: 60000
  })
}
