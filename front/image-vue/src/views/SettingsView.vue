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
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

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

const handleSave = () => {
  // 保存设置到 localStorage 或服务器
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
</script>

<style scoped>
.settings-view {
  min-height: calc(100vh - 160px);
  background: #f5f7fa;
  padding: 40px 20px;
}

.settings-container {
  max-width: 800px;
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

@media (max-width: 768px) {
  .settings-view {
    padding: 20px 10px;
  }

  .settings-tabs {
    padding: 10px;
  }
}
</style>

