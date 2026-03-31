import { shallowMount } from '@vue/test-utils'
import LoginView from '@/views/auth/LoginView.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

const mockPush = jest.fn()

jest.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
  useRoute: () => ({ query: {}, meta: { title: '仪表盘' } }),
  RouterLink: { template: '<a><slot /></a>' },
  RouterView: { template: '<div />' },
}))

describe('auth and layout', () => {
  beforeEach(() => {
    localStorage.clear()
    mockPush.mockClear()
  })

  test('LoginView 登录成功后跳转仪表盘', async () => {
    const wrapper = shallowMount(LoginView)
    await wrapper.find('form').trigger('submit.prevent')
    await new Promise((resolve) => setTimeout(resolve, 220))
    expect(mockPush).toHaveBeenCalledWith('/dashboard')
  })

  test('AdminLayout 正常展示菜单并支持退出', async () => {
    const wrapper = shallowMount(AdminLayout)
    expect(wrapper.text()).toContain('AI助教后台')
    await wrapper.find('.admin-layout__logout').trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/login')
  })
})
