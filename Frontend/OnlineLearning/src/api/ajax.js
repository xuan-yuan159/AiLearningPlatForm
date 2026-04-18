/**
 * 统一请求客户端，封装鉴权与错误处理。
 * @param {string} url 请求地址
 * @param {RequestInit & {mockData?: unknown, mockDelay?: number}} options 请求选项
 * @returns {Promise<unknown>}
 */
export async function ajax(url, options = {}) {
  const { mockData, mockDelay = 180, ...fetchOptions } = options
  if (mockData !== undefined) {
    return new Promise((resolve) => {
      setTimeout(() => resolve(mockData), mockDelay)
    })
  }

  const headers = normalizeHeaders(fetchOptions.headers)
  applyAuthorization(headers)
  applyJsonContentType(headers, fetchOptions.body)

  const response = await fetch(url, {
    ...fetchOptions,
    headers,
  })

  const data = await parseResponseData(response)

  if (!response.ok) {
    if (response.status === 401) {
      handleUnauthorized()
    }
    throw new Error(resolveErrorMessage(data, `请求失败：${response.status}`))
  }

  if (isBusinessResponse(data)) {
    if (data.code === 401) {
      handleUnauthorized()
    }
    if (data.code !== 200) {
      throw new Error(data.message || '业务处理失败')
    }
    return data.data
  }

  return data
}

/**
 * 创建一个支持进度与取消的上传任务。
 * @param {string} url 请求地址
 * @param {{formData: FormData, method?: string, headers?: HeadersInit, onProgress?: (percent: number) => void}} options 请求选项
 * @returns {{promise: Promise<unknown>, cancel: () => void}}
 */
export function createUploadTask(url, options = {}) {
  const { formData, method = 'POST', headers: rawHeaders, onProgress } = options
  if (!(typeof FormData !== 'undefined' && formData instanceof FormData)) {
    throw new Error('上传任务缺少有效 FormData')
  }

  const headers = normalizeHeaders(rawHeaders)
  applyAuthorization(headers)

  const xhr = new XMLHttpRequest()
  let canceled = false
  let settled = false

  const promise = new Promise((resolve, reject) => {
    xhr.open(method, url, true)
    headers.forEach((value, key) => {
      xhr.setRequestHeader(key, value)
    })

    xhr.upload.onprogress = (event) => {
      if (!onProgress || !event.lengthComputable) {
        return
      }
      const percent = Math.min(100, Math.max(0, Math.round((event.loaded / event.total) * 100)))
      onProgress(percent)
    }

    xhr.onload = () => {
      if (settled) {
        return
      }
      settled = true
      try {
        const data = parseXhrResponseData(xhr)
        if (xhr.status < 200 || xhr.status >= 300) {
          if (xhr.status === 401) {
            handleUnauthorized()
          }
          reject(new Error(resolveErrorMessage(data, `请求失败：${xhr.status}`)))
          return
        }
        if (isBusinessResponse(data)) {
          if (data.code === 401) {
            handleUnauthorized()
          }
          if (data.code !== 200) {
            reject(new Error(data.message || '业务处理失败'))
            return
          }
          resolve(data.data)
          return
        }
        resolve(data)
      } catch (error) {
        reject(error instanceof Error ? error : new Error('上传响应解析失败'))
      }
    }

    xhr.onerror = () => {
      if (settled) {
        return
      }
      settled = true
      reject(new Error('上传请求失败，请检查网络连接'))
    }

    xhr.onabort = () => {
      if (settled) {
        return
      }
      settled = true
      reject(createUploadCanceledError(canceled))
    }

    xhr.send(formData)
  })

  const cancel = () => {
    if (settled) {
      return
    }
    canceled = true
    xhr.abort()
  }

  return { promise, cancel }
}

/**
 * 判断是否为“上传取消”错误。
 * @param {unknown} error
 * @returns {boolean}
 */
export function isUploadCanceledError(error) {
  return !!(error && typeof error === 'object' && (error.isCanceled === true || error.name === 'UploadCanceledError'))
}

function normalizeHeaders(rawHeaders) {
  if (rawHeaders instanceof Headers) {
    return new Headers(rawHeaders)
  }
  return new Headers(rawHeaders || {})
}

function applyAuthorization(headers) {
  if (headers.has('Authorization') || headers.has('authorization')) {
    return
  }
  const token = localStorage.getItem('teacher_token') || ''
  if (!token) {
    return
  }
  const normalizedToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`
  headers.set('Authorization', normalizedToken)
}

function applyJsonContentType(headers, body) {
  if (body === undefined || body === null) {
    return
  }
  if (typeof FormData !== 'undefined' && body instanceof FormData) {
    return
  }
  if (!headers.has('Content-Type') && !headers.has('content-type')) {
    headers.set('Content-Type', 'application/json')
  }
}

async function parseResponseData(response) {
  if (response.status === 204) {
    return null
  }
  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    return response.json()
  }
  return response.text()
}

function parseXhrResponseData(xhr) {
  if (xhr.status === 204) {
    return null
  }
  const contentType = xhr.getResponseHeader('content-type') || ''
  const responseText = xhr.responseText || ''
  if (contentType.includes('application/json')) {
    if (!responseText.trim()) {
      return null
    }
    return JSON.parse(responseText)
  }
  return responseText
}

function isBusinessResponse(data) {
  return data && typeof data === 'object' && 'code' in data
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

function createUploadCanceledError(canceledByClient) {
  const error = new Error('上传已取消')
  error.name = 'UploadCanceledError'
  error.isCanceled = !!canceledByClient
  return error
}

function handleUnauthorized() {
  localStorage.removeItem('teacher_token')
  localStorage.removeItem('teacher_profile')

  if (typeof window === 'undefined') {
    throw new Error('未授权，请重新登录')
  }

  const currentPath = `${window.location.pathname}${window.location.search || ''}`
  if (!window.location.pathname.startsWith('/login')) {
    window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`
  }
  throw new Error('未授权，请重新登录')
}
