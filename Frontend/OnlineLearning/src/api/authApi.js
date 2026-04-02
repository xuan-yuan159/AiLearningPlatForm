import { ajax } from '@/api/ajax'

/**
 * 登录接口。
 * @param {{username: string, password: string}} payload 登录参数
 * @returns {Promise<{token: string, userId: string, username: string, identity: number}>}
 */
export async function loginByPassword(payload) {
  if (!payload.username || !payload.password) {
    throw new Error('用户名与密码不能为空')
  }

  return ajax('/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/**
 * 注册接口。
 * @param {{username: string, password: string, identity: number, nickname?: string, email?: string}} payload 注册参数
 * @returns {Promise<void>}
 */
export async function registerUser(payload) {
  if (!payload.username || !payload.password) {
    throw new Error('用户名与密码不能为空')
  }

  return ajax('/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/**
 * 登出接口。
 * @returns {Promise<void>}
 */
export async function logoutUser() {
  return ajax('/auth/logout', {
    method: 'POST',
  })
}
