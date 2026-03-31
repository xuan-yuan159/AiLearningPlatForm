import { request } from '@/api/httpClient'

/**
 * 教师登录接口。
 * @param {{account: string, password: string}} payload 登录参数
 * @returns {Promise<{token: string, profile: {id: string, name: string, title: string, email: string, phone: string, avatar: string}}>}
 * @throws {Error} 当账号或密码为空时抛出异常
 */
export async function loginByPassword(payload) {
  if (!payload.account || !payload.password) {
    throw new Error('账号与密码不能为空')
  }

  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
    mockData: {
      token: `token_${Date.now()}`,
      profile: {
        id: 't-1001',
        name: '王老师',
        title: '人工智能课程负责人',
        email: 'teacher@ailearning.com',
        phone: '13800000000',
        avatar:
          'https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=crop&w=120&q=80',
      },
    },
  })
}
