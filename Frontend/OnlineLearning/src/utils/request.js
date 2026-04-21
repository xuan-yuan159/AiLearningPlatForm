import { ElMessage } from 'element-plus'
import { getToken, logout } from '@/utils/auth'

let redirectingToLogin = false

function normalizeHeaders(rawHeaders) {
  if (rawHeaders instanceof Headers) {
    return new Headers(rawHeaders)
  }
  return new Headers(rawHeaders || {})
}

function withAuthHeader(headers) {
  if (headers.has('Authorization') || headers.has('authorization')) {
    return
  }
  const token = getToken()
  if (!token) {
    return
  }
  headers.set('Authorization', token.startsWith('Bearer ') ? token : `Bearer ${token}`)
}

function withJsonContentType(headers, body) {
  if (body === undefined || body === null || body instanceof FormData) {
    return
  }
  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
}

async function parseResponseBody(response) {
  if (response.status === 204) {
    return null
  }
  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    return response.json()
  }
  return response.text()
}

function resolveErrorMessage(data, fallback) {
  if (data && typeof data === 'object' && 'message' in data) {
    return data.message || fallback
  }
  if (typeof data === 'string' && data.trim()) {
    return data
  }
  return fallback
}

function handleUnauthorized() {
  if (redirectingToLogin) {
    return
  }
  redirectingToLogin = true
  logout()
  if (typeof window === 'undefined') {
    redirectingToLogin = false
    return
  }
  if (window.location.pathname.startsWith('/login')) {
    redirectingToLogin = false
    return
  }
  ElMessage.closeAll()
  ElMessage({
    type: 'error',
    message: '登录已过期，请重新登录',
    grouping: true,
    duration: 2000,
  })
  window.location.href = '/login'
}

function unwrapBusinessResponse(data) {
  if (data && typeof data === 'object' && 'code' in data) {
    if (data.code === 401) {
      handleUnauthorized()
      throw new Error('登录已过期，请重新登录')
    }
    if (data.code !== 200) {
      throw new Error(data.message || '业务处理失败')
    }
    return data.data
  }
  return data
}

export async function request(url, options = {}) {
  const headers = normalizeHeaders(options.headers)
  withAuthHeader(headers)
  withJsonContentType(headers, options.body)

  const response = await fetch(url, {
    ...options,
    headers,
  })

  const data = await parseResponseBody(response)
  if (!response.ok) {
    if (response.status === 401) {
      handleUnauthorized()
    }
    throw new Error(resolveErrorMessage(data, `请求失败：${response.status}`))
  }

  return unwrapBusinessResponse(data)
}
