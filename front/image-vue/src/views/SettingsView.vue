<template>
  <div class="settings-view">
    <div class="settings-container">
      <el-card class="settings-card">
        <template #header>
          <div class="card-header">
            <h2>系统设置</h2>
          </div>
        </template>

        <el-tabs v-model="activeTab" class="settings-tabs">
          <el-tab-pane label="AI 识图" name="ai">
            <div class="ai-section">
              <el-alert
                title="百度智能云 AI 识图配置"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 20px"
              >
                <p>配置百度智能云的 API Key 和 Secret Key，即可使用 AI 自动识别图片内容并生成标签。</p>
                <p style="margin-top: 8px;">
                  <a href="https://console.bce.baidu.com/ai/#/ai/imagerecognition/overview/index" target="_blank">前往百度智能云控制台获取 →</a>
                </p>
              </el-alert>

              <el-form :model="aiConfig" label-width="120px">
                <el-form-item label="API Key">
                  <el-input 
                    v-model="aiConfig.apiKey" 
                    placeholder="请输入百度智能云 API Key"
                    show-password
                  />
                </el-form-item>

                <el-form-item label="Secret Key">
                  <el-input 
                    v-model="aiConfig.secretKey" 
                    placeholder="请输入百度智能云 Secret Key"
                    show-password
                  />
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="handleSaveAiConfig" :loading="savingAiConfig">
                    保存配置
                  </el-button>
                  <el-button @click="handleTestAiConfig" :loading="testingAiConfig">
                    测试连接
                  </el-button>
                </el-form-item>
              </el-form>

              <el-divider />

              <div class="ai-status">
                <h4>服务状态</h4>
                <el-tag :type="aiServiceAvailable ? 'success' : 'danger'" size="large">
                  {{ aiServiceAvailable ? 'AI 服务已配置' : 'AI 服务未配置' }}
                </el-tag>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="MCP 接口" name="mcp">
            <div class="mcp-section">
              <el-alert
                title="MCP (Model Context Protocol) 接口"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 20px"
              >
                <p>通过 MCP 接口，您可以让 AI 助手（如 Claude、ChatGPT 等）检索您的图片库。</p>
              </el-alert>

              <div class="api-keys-section">
                <div class="section-header">
                  <h3>API Keys</h3>
                  <el-button type="primary" size="small" @click="showCreateKeyDialog">
                    <el-icon><Plus /></el-icon>
                    创建 API Key
                  </el-button>
                </div>

                <el-table :data="apiKeys" v-loading="loadingKeys" style="width: 100%">
                  <el-table-column prop="name" label="名称" width="150" />
                  <el-table-column prop="apiKey" label="API Key" min-width="200">
                    <template #default="{ row }">
                      <code class="api-key-code">{{ row.apiKey }}</code>
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="80">
                    <template #default="{ row }">
                      <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                        {{ row.status === 1 ? '启用' : '禁用' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="lastUsedTime" label="最后使用" width="160">
                    <template #default="{ row }">
                      {{ row.lastUsedTime ? formatDate(row.lastUsedTime) : '从未使用' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="200">
                    <template #default="{ row }">
                      <el-button-group size="small">
                        <el-button @click="handleToggleKey(row)">
                          {{ row.status === 1 ? '禁用' : '启用' }}
                        </el-button>
                        <el-button @click="handleRegenerateKey(row)">重新生成</el-button>
                        <el-button type="danger" @click="handleDeleteKey(row)">删除</el-button>
                      </el-button-group>
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <el-divider />

              <div class="mcp-config-section">
                <h3>MCP 配置说明</h3>
                <p>在您的 AI 工具配置文件中添加以下 MCP 服务器配置：</p>
                
                <div class="config-block">
                  <div class="config-header">
                    <span>Claude Desktop / Cursor 配置</span>
                    <el-button size="small" @click="copyConfig">复制配置</el-button>
                  </div>
                  <pre class="config-code">{{ mcpConfig }}</pre>
                </div>

                <el-alert
                  type="warning"
                  :closable="false"
                  style="margin-top: 15px"
                >
                  <template #title>
                    <strong>注意事项</strong>
                  </template>
                  <ul style="margin: 5px 0 0 0; padding-left: 20px;">
                    <li>请将 <code>YOUR_API_KEY</code> 替换为上方生成的 API Key</li>
                    <li>API Key 仅能访问您自己的图片</li>
                    <li>请妥善保管 API Key，不要泄露给他人</li>
                  </ul>
                </el-alert>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="关于" name="about">
            <div class="about-section">
              <h3>图片管理系统</h3>
              <p>版本: 1.0.0</p>
              <p>
                这是一个功能强大的图片管理系统，支持图片上传、编辑、分类和展示。
              </p>
              <el-divider />
              <p>© 2025 图片管理系统.</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <!-- 创建 API Key 对话框 -->
    <el-dialog v-model="createKeyDialogVisible" title="创建 API Key" width="400px">
      <el-form :model="newKeyForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="newKeyForm.name" placeholder="例如：Claude Desktop" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createKeyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateKey" :loading="creatingKey">创建</el-button>
      </template>
    </el-dialog>

    <!-- 显示新创建的 API Key 对话框 -->
    <el-dialog v-model="showNewKeyDialogVisible" title="API Key 已创建" width="500px">
      <el-alert
        type="warning"
        :closable="false"
        style="margin-bottom: 15px"
      >
        请立即复制此 API Key，关闭后将无法再次查看完整内容。
      </el-alert>
      <div class="new-key-display">
        <code>{{ newCreatedKey }}</code>
        <el-button type="primary" size="small" @click="copyNewKey">复制</el-button>
      </div>
      <template #footer>
        <el-button type="primary" @click="showNewKeyDialogVisible = false">我已保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getApiKeys, createApiKey, deleteApiKey, toggleApiKey, regenerateApiKey } from '@/api/apiKey'
import { checkAiStatus, getAiConfig, saveAiConfig, testAiConfig } from '@/api/ai'

const activeTab = ref('ai')

// AI 配置相关
const aiConfig = reactive({
  apiKey: '',
  secretKey: ''
})
const aiServiceAvailable = ref(false)
const savingAiConfig = ref(false)
const testingAiConfig = ref(false)

// API Keys 相关
const apiKeys = ref([])
const loadingKeys = ref(false)
const createKeyDialogVisible = ref(false)
const showNewKeyDialogVisible = ref(false)
const newCreatedKey = ref('')
const creatingKey = ref(false)
const newKeyForm = reactive({
  name: ''
})

// MCP 配置
const mcpConfig = computed(() => {
  return `{
  "mcpServers": {
    "image-search": {
      "command": "node",
      "args": ["/home/zyx/work/bs/mcp-server/src/index.js"],
      "env": {
        "IMAGE_API_URL": "http://localhost:8080/api",
        "IMAGE_API_KEY": "YOUR_API_KEY"
      }
    }
  }
}`
})

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 加载 API Keys
const loadApiKeys = async () => {
  loadingKeys.value = true
  try {
    const res = await getApiKeys()
    if (res.code === 200) {
      apiKeys.value = res.data || []
    }
  } catch (error) {
    console.error('加载 API Keys 失败:', error)
  } finally {
    loadingKeys.value = false
  }
}

// 显示创建对话框
const showCreateKeyDialog = () => {
  newKeyForm.name = ''
  createKeyDialogVisible.value = true
}

// 创建 API Key
const handleCreateKey = async () => {
  creatingKey.value = true
  try {
    const res = await createApiKey(newKeyForm.name || '默认 API Key')
    if (res.code === 200) {
      ElMessage.success('创建成功')
      newCreatedKey.value = res.data.apiKey
      createKeyDialogVisible.value = false
      showNewKeyDialogVisible.value = true
      loadApiKeys()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    ElMessage.error('创建失败:',error)
  } finally {
    creatingKey.value = false
  }
}

// 复制新创建的 Key
const copyNewKey = () => {
  navigator.clipboard.writeText(newCreatedKey.value)
  ElMessage.success('已复制到剪贴板')
}

// 切换 Key 状态
const handleToggleKey = async (row) => {
  try {
    const res = await toggleApiKey(row.id)
    if (res.code === 200) {
      ElMessage.success('操作成功')
      loadApiKeys()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 重新生成 Key
const handleRegenerateKey = async (row) => {
  try {
    await ElMessageBox.confirm(
      '重新生成后，旧的 API Key 将失效，确定继续？',
      '确认',
      { type: 'warning' }
    )
    const res = await regenerateApiKey(row.id)
    if (res.code === 200) {
      newCreatedKey.value = res.data.apiKey
      showNewKeyDialogVisible.value = true
      loadApiKeys()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 删除 Key
const handleDeleteKey = async (row) => {
  try {
    await ElMessageBox.confirm(
      '删除后无法恢复，确定删除此 API Key？',
      '确认删除',
      { type: 'warning' }
    )
    const res = await deleteApiKey(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadApiKeys()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 复制 MCP 配置
const copyConfig = () => {
  navigator.clipboard.writeText(mcpConfig.value)
  ElMessage.success('配置已复制到剪贴板')
}

// 加载 AI 配置
const loadAiConfig = async () => {
  try {
    const res = await getAiConfig()
    if (res.code === 200 && res.data) {
      aiConfig.apiKey = res.data.apiKey || ''
      aiConfig.secretKey = res.data.secretKey || ''
    }
  } catch (error) {
    console.error('加载 AI 配置失败:', error)
  }
}

// 加载 AI 服务状态
const loadAiStatus = async () => {
  try {
    const res = await checkAiStatus()
    if (res.code === 200) {
      aiServiceAvailable.value = res.data.available
    }
  } catch (error) {
    console.error('检查 AI 服务状态失败:', error)
  }
}

// 保存 AI 配置
const handleSaveAiConfig = async () => {
  savingAiConfig.value = true
  try {
    const res = await saveAiConfig(aiConfig)
    if (res.code === 200) {
      ElMessage.success('AI 配置保存成功')
      loadAiStatus()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    savingAiConfig.value = false
  }
}

// 测试 AI 配置
const handleTestAiConfig = async () => {
  if (!aiConfig.apiKey || !aiConfig.secretKey) {
    ElMessage.warning('请先填写 API Key 和 Secret Key')
    return
  }
  testingAiConfig.value = true
  try {
    const res = await testAiConfig(aiConfig)
    if (res.code === 200 && res.data.success) {
      ElMessage.success('连接测试成功！')
    } else {
      ElMessage.error(res.data?.message || res.message || '连接测试失败')
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
  } finally {
    testingAiConfig.value = false
  }
}

onMounted(() => {
  loadApiKeys()
  loadAiConfig()
  loadAiStatus()
})
</script>

<style scoped>
.settings-view {
  min-height: calc(100vh - 160px);
  background: #f5f7fa;
  padding: 40px 20px;
}

.settings-container {
  max-width: 900px;
  margin: 0 auto;
}

.settings-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.settings-tabs {
  padding: 20px;
  min-height: 400px;
}

.about-section {
  padding: 20px;
}

.about-section h3 {
  font-size: 20px;
  color: #333;
  margin: 0 0 15px 0;
}

.about-section p {
  color: #666;
  line-height: 1.8;
}

/* AI 识图设置样式 */
.ai-section {
  padding: 10px;
}

.ai-section h4 {
  font-size: 14px;
  color: #333;
  margin: 0 0 10px 0;
}

.ai-status {
  margin-top: 10px;
}

/* MCP 设置样式 */
.mcp-section {
  padding: 10px;
}

.mcp-section h3 {
  font-size: 16px;
  color: #333;
  margin: 0 0 15px 0;
}

.mcp-section p {
  color: #666;
  line-height: 1.6;
  margin: 10px 0;
}

.api-keys-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0;
}

.api-key-code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.mcp-config-section {
  margin-top: 20px;
}

.config-block {
  background: #1e1e1e;
  border-radius: 8px;
  overflow: hidden;
  margin-top: 10px;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #2d2d2d;
  color: #ccc;
  font-size: 13px;
}

.config-code {
  margin: 0;
  padding: 15px;
  color: #d4d4d4;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  overflow-x: auto;
}

.new-key-display {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 6px;
}

.new-key-display code {
  flex: 1;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  word-break: break-all;
}

@media (max-width: 768px) {
  .settings-view {
    padding: 20px 10px;
  }

  .settings-tabs {
    padding: 10px;
  }
  
  .section-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}
</style>

