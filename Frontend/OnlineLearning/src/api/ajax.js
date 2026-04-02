/**
 * 统一请求客户端，封装基础错误处理。
 * @param {string} url 请求地址
 * @param {RequestInit & {mockData?: unknown, mockDelay?: number}} options 请求选项
 * @returns {Promise<unknown>}
 * @throws {Error} 请求失败时抛出描述信息
 */
export async function ajax(url, options = {}) {
  const { mockData, mockDelay = 180, ...fetchOptions } = options
  if (mockData !== undefined) {
    return new Promise((resolve) => {
      setTimeout(() => resolve(mockData), mockDelay)
    })
  }

  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...fetchOptions.headers,
      Authorization: localStorage.getItem('teacher_token') || '',
    },
    ...fetchOptions,
  })

  const contentType = response.headers.get('content-type') || ''
  let data
  if (contentType.includes('application/json')) {
    data = await response.json()
  } else {
    data = await response.text()
  }

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('未授权，请重新登录')
    }
    const errorMsg = (data && data.message) || `请求失败：${response.status}`
    throw new Error(errorMsg)
  }

  // 处理业务 code
  if (data && typeof data === 'object' && 'code' in data) {
    if (data.code !== 200) {
      throw new Error(data.message || '业务处理失败')
    }
    return data.data
  }

  return data
}
