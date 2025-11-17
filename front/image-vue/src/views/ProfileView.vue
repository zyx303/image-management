<template>
  <div class="profile-view">
    <div class="profile-container">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <h2>个人资料</h2>
          </div>
        </template>

        <div class="profile-content">
          <div class="avatar-section">
            <el-avatar :size="100" :icon="UserFilled" />
            <el-button type="primary" link class="change-avatar">
              更换头像
            </el-button>
          </div>

          <el-form :model="form" label-width="100px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>

            <el-form-item label="邮箱">
              <el-input v-model="form.email" disabled />
            </el-form-item>

            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>

            <el-form-item label="个人简介">
              <el-input
                v-model="form.bio"
                type="textarea"
                :rows="4"
                placeholder="介绍一下你自己..."
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSave">
                保存修改
              </el-button>
              <el-button @click="showPasswordDialog = true">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <div class="stats-section">
            <h3>统计信息</h3>
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ stats.imageCount }}</div>
                <div class="stat-label">图片总数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ stats.totalSize }}</div>
                <div class="stat-label">存储空间</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ stats.tagCount }}</div>
                <div class="stat-label">标签数量</div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="当前密码">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const showPasswordDialog = ref(false)

const form = reactive({
  username: '',
  email: '',
  nickname: '',
  bio: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const stats = reactive({
  imageCount: 0,
  totalSize: '0 MB',
  tagCount: 0
})

onMounted(() => {
  if (userStore.userInfo) {
    form.username = userStore.userInfo.username
    form.email = userStore.userInfo.email
    form.nickname = userStore.userInfo.nickname || ''
    form.bio = userStore.userInfo.bio || ''
  }
  
  // 加载统计信息
  loadStats()
})

const loadStats = () => {
  // 这里应该从 API 获取统计数据
  stats.imageCount = 0
  stats.totalSize = '0 MB'
  stats.tagCount = 0
}

const handleSave = () => {
  ElMessage.success('保存成功')
}

const handleChangePassword = () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写所有字段')
    return
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }

  if (passwordForm.newPassword.length < 6) {
    ElMessage.error('密码至少6个字符')
    return
  }

  ElMessage.success('密码修改成功')
  showPasswordDialog.value = false
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}
</script>

<style scoped>
.profile-view {
  min-height: calc(100vh - 160px);
  background: #f5f7fa;
  padding: 40px 20px;
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.profile-content {
  padding: 20px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.change-avatar {
  margin-top: 10px;
}

.profile-form {
  max-width: 600px;
  margin: 0 auto;
}

.stats-section h3 {
  font-size: 18px;
  color: #333;
  margin: 20px 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

@media (max-width: 768px) {
  .profile-view {
    padding: 20px 10px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>

