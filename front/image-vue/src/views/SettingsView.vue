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
          <el-tab-pane label="显示设置" name="display">
            <el-form label-width="120px">
              <el-form-item label="每页显示数量">
                <el-select v-model="settings.pageSize" placeholder="选择显示数量">
                  <el-option label="12 张" :value="12" />
                  <el-option label="24 张" :value="24" />
                  <el-option label="48 张" :value="48" />
                  <el-option label="96 张" :value="96" />
                </el-select>
              </el-form-item>

              <el-form-item label="默认排序">
                <el-select v-model="settings.defaultSort" placeholder="选择排序方式">
                  <el-option label="最新上传" value="createTime_desc" />
                  <el-option label="最早上传" value="createTime_asc" />
                  <el-option label="文件名 A-Z" value="fileName_asc" />
                  <el-option label="文件名 Z-A" value="fileName_desc" />
                </el-select>
              </el-form-item>

              <el-form-item label="默认视图">
                <el-radio-group v-model="settings.defaultView">
                  <el-radio label="grid">网格视图</el-radio>
                  <el-radio label="list">列表视图</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="上传设置" name="upload">
            <el-form label-width="120px">
              <el-form-item label="自动添加标签">
                <el-switch v-model="settings.autoAddTags" />
              </el-form-item>

              <el-form-item label="自动提取EXIF">
                <el-switch v-model="settings.autoExtractExif" />
              </el-form-item>

              <el-form-item label="上传后跳转">
                <el-switch v-model="settings.redirectAfterUpload" />
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="隐私设置" name="privacy">
            <el-form label-width="120px">
              <el-form-item label="图片默认公开">
                <el-switch v-model="settings.defaultPublic" />
              </el-form-item>

              <el-form-item label="允许下载">
                <el-switch v-model="settings.allowDownload" />
              </el-form-item>
            </el-form>
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

        <div class="settings-actions">
          <el-button type="primary" @click="handleSave">保存设置</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
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

const activeTab = ref('display')

const settings = reactive({
  pageSize: 24,
  defaultSort: 'createTime_desc',
  defaultView: 'grid',
  autoAddTags: true,
  autoExtractExif: true,
  redirectAfterUpload: false,
  defaultPublic: false,
  allowDownload: true
})

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

const handleSave = () => {
  localStorage.setItem('userSettings', JSON.stringify(settings))
  ElMessage.success('设置已保存')
}

const handleReset = () => {
  settings.pageSize = 24
  settings.defaultSort = 'createTime_desc'
  settings.defaultView = 'grid'
  settings.autoAddTags = true
  settings.autoExtractExif = true
  settings.redirectAfterUpload = false
  settings.defaultPublic = false
  settings.allowDownload = true
  ElMessage.info('设置已重置')
}

onMounted(() => {
  // 当切换到 MCP 标签时加载 API Keys
  loadApiKeys()
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

.settings-actions {
  padding: 20px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: center;
  gap: 15px;
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

