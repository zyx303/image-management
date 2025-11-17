import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo, logout } from '@/api/auth'

export const useUserStore = defineStore(
  'user',
  () => {
    // 状态
    const token = ref('')
    const userInfo = ref(null)

    // 计算属性
    const isLoggedIn = computed(() => !!token.value)
    const username = computed(() => userInfo.value?.username || '')
    const email = computed(() => userInfo.value?.email || '')

    // 登录
    async function userLogin(loginForm) {
      try {
        const res = await login(loginForm)
        token.value = res.data.token
        userInfo.value = res.data.user
        return res
      } catch (error) {
        console.error('Login failed:', error)
        throw error
      }
    }

    // 注册
    async function userRegister(registerForm) {
      try {
        const res = await register(registerForm)
        return res
      } catch (error) {
        console.error('Register failed:', error)
        throw error
      }
    }

    // 获取用户信息
    async function fetchUserInfo() {
      try {
        const res = await getUserInfo()
        userInfo.value = res.data
        return res
      } catch (error) {
        console.error('Fetch user info failed:', error)
        throw error
      }
    }

    // 退出登录
    async function userLogout() {
      try {
        await logout()
      } catch (error) {
        console.error('Logout failed:', error)
      } finally {
        token.value = ''
        userInfo.value = null
      }
    }

    // 更新用户信息
    function updateUserInfo(info) {
      userInfo.value = { ...userInfo.value, ...info }
    }

    return {
      token,
      userInfo,
      isLoggedIn,
      username,
      email,
      userLogin,
      userRegister,
      fetchUserInfo,
      userLogout,
      updateUserInfo
    }
  },
  {
    persist: {
      key: 'image-user',
      storage: localStorage,
      paths: ['token', 'userInfo']
    }
  }
)

