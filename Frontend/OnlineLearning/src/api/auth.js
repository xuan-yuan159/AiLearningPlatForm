import { request } from '@/utils/request'

export async function loginByPassword(payload) {
  if (!payload?.username || !payload?.password) {
    throw new Error('用户名与密码不能为空')
  }

  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function registerUser(payload) {
  if (!payload?.username || !payload?.password) {
    throw new Error('用户名与密码不能为空')
  }

  return request('/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function logoutUser() {
  return request('/auth/logout', {
    method: 'POST',
  })
}

