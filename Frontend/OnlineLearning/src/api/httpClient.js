/**
 * 统一请求客户端，封装基础错误处理。
 * @param {string} url 请求地址
 * @param {RequestInit & {mockData?: unknown, mockDelay?: number}} options 请求选项
 * @returns {Promise<unknown>}
 * @throws {Error} 请求失败时抛出描述信息
 */
export async function request(url, options = {}) {
  const { mockData, mockDelay = 180, ...fetchOptions } = options
  if (mockData !== undefined) {
    return new Promise((resolve) => {
      setTimeout(() => resolve(mockData), mockDelay)
    })
  }

  const response = await fetch(url, {
    headers: { 'Content-Type': 'application/json', ...fetchOptions.headers },
    ...fetchOptions,
  })

  if (!response.ok) {
    throw new Error(`请求失败：${response.status}`)
  }

  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    return response.json()
  }
  return response.text()
}
