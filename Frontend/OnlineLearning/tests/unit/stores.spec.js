import { useAdminStore } from '@/stores/useAdminStore'
import { useAuthStore } from '@/stores/useAuthStore'

describe('stores', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  test('authStore 登录与退出', () => {
    const store = useAuthStore()
    store.login({
      token: 'token-test',
      profile: {
        id: 't-1',
        name: '测试老师',
        title: '讲师',
        email: 'test@ailearning.com',
        phone: '13000000000',
        avatar: 'https://example.com/avatar.png',
      },
    })
    expect(store.isAuthenticated.value).toBe(true)
    expect(store.profile.value.name).toBe('测试老师')
    store.logout()
    expect(store.isAuthenticated.value).toBe(false)
  })

  test('adminStore 计算完成率', () => {
    const store = useAdminStore()
    expect(store.completedRate.value).toBe('82%')
    store.setLoading(true)
    expect(store.loading.value).toBe(true)
  })
})
