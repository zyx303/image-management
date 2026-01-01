<template>
  <header class="layout-header">
    <div class="header-content">
      <div class="logo" @click="router.push('/')">
        <el-icon :size="24"><PictureFilled /></el-icon>
        <span class="logo-text">图片管理系统</span>
      </div>

      <nav v-if="!isMobile" class="nav-menu">
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :ellipsis="false"
          background-color="transparent"
          text-color="#fff"
          active-text-color="#ffd04b"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/upload">
            <el-icon><Upload /></el-icon>
            <span>上传</span>
          </el-menu-item>
        </el-menu>
      </nav>

      <div class="user-actions">
        <el-dropdown v-if="userStore.isLoggedIn" @command="handleCommand">
          <div class="user-info">
            <el-avatar :size="32" :icon="UserFilled" />
            <span v-if="!isMobile" class="username">{{ userStore.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人资料
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                设置
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-button v-else type="primary" @click="router.push('/login')">
          登录
        </el-button>

        <!-- 移动端菜单按钮 -->
        <el-button
          v-if="isMobile"
          :icon="Menu"
          circle
          class="mobile-menu-btn"
          @click="showMobileMenu = true"
        />
      </div>
    </div>

    <!-- 移动端菜单抽屉 -->
    <el-drawer
      v-model="showMobileMenu"
      title="菜单"
      direction="rtl"
      size="70%"
    >
      <el-menu
        :default-active="activeMenu"
        @select="handleMobileMenuSelect"
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/upload">
          <el-icon><Upload /></el-icon>
          <span>上传</span>
        </el-menu-item>
      </el-menu>
    </el-drawer>
  </header>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  PictureFilled,
  HomeFilled,
  Upload,
  UserFilled,
  ArrowDown,
  User,
  Setting,
  SwitchButton,
  Menu
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const showMobileMenu = ref(false)
const isMobile = ref(window.innerWidth < 768)

const activeMenu = computed(() => route.path)

// 监听窗口大小变化
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth < 768
})

const handleMenuSelect = (index) => {
  router.push(index)
}

const handleMobileMenuSelect = (index) => {
  router.push(index)
  showMobileMenu.value = false
}

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          type: 'warning'
        })
        await userStore.userLogout()
        router.push('/login')
      } catch (error) {
        // 用户取消
      }
      break
  }
}
</script>

<style scoped>
.layout-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  font-size: 20px;
  font-weight: bold;
  cursor: pointer;
  transition: opacity 0.3s;
}

.logo:hover {
  opacity: 0.8;
}

.logo-text {
  font-size: 18px;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu :deep(.el-menu) {
  border-bottom: none;
}

.nav-menu :deep(.el-menu-item) {
  border-bottom: 2px solid transparent;
}

.nav-menu :deep(.el-menu-item.is-active) {
  border-bottom-color: #ffd04b;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 20px;
  transition: background 0.3s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.username {
  font-size: 14px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mobile-menu-btn {
  color: white;
  border-color: white;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .logo-text {
    font-size: 16px;
  }

  .header-content {
    padding: 0 15px;
  }
}
</style>

