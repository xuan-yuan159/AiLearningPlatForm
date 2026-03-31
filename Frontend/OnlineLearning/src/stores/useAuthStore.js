import { computed, ref } from 'vue'

const token = ref(localStorage.getItem('teacher_token') || '')
const profile = ref({
  id: 't-1001',
  name: '王老师',
  title: '人工智能课程负责人',
  email: 'teacher@ailearning.com',
  phone: '13800000000',
  avatar:
    'https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=crop&w=120&q=80',
})

/**
 * 教师登录状态仓库。
 * @returns {{
 *  token: import('vue').Ref<string>,
 *  profile: import('vue').Ref<{id: string, name: string, title: string, email: string, phone: string, avatar: string}>,
 *  isAuthenticated: import('vue').ComputedRef<boolean>,
 *  login: (payload: {token: string, profile: {id: string, name: string, title: string, email: string, phone: string, avatar: string}}) => void,
 *  logout: () => void,
 *  updateProfile: (payload: Partial<{name: string, title: string, email: string, phone: string, avatar: string}>) => void
 * }}
 */
export function useAuthStore() {
  const isAuthenticated = computed(() => Boolean(token.value))

  /**
   * 持久化登录态。
   * @param {{token: string, profile: {id: string, name: string, title: string, email: string, phone: string, avatar: string}}} payload 登录响应数据
   * @returns {void}
   */
  const login = (payload) => {
    token.value = payload.token
    profile.value = payload.profile
    localStorage.setItem('teacher_token', payload.token)
    localStorage.setItem('teacher_profile', JSON.stringify(payload.profile))
  }

  /**
   * 清理登录态。
   * @returns {void}
   */
  const logout = () => {
    token.value = ''
    localStorage.removeItem('teacher_token')
  }

  /**
   * 更新教师个人资料。
   * @param {Partial<{name: string, title: string, email: string, phone: string, avatar: string}>} payload 个人信息变更
   * @returns {void}
   */
  const updateProfile = (payload) => {
    profile.value = { ...profile.value, ...payload }
    localStorage.setItem('teacher_profile', JSON.stringify(profile.value))
  }

  const persistedProfile = localStorage.getItem('teacher_profile')
  if (persistedProfile) {
    profile.value = { ...profile.value, ...JSON.parse(persistedProfile) }
  }

  return { token, profile, isAuthenticated, login, logout, updateProfile }
}
