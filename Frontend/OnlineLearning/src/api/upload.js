import { getToken } from '@/utils/auth'
import { request } from '@/utils/request'

export function createUploadTask(payload) {
  return request('/upload/tasks', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function uploadResource(payload) {
  const { file, courseId, title, resourceType, uploadTaskId } = payload || {}
  if (!file) {
    throw new Error('请选择上传文件')
  }
  if (!courseId) {
    throw new Error('请选择课程')
  }
  if (!title) {
    throw new Error('请输入资源标题')
  }
  if (!resourceType) {
    throw new Error('请选择资源类型')
  }
  if (!uploadTaskId) {
    throw new Error('缺少上传任务ID')
  }

  const formData = new FormData()
  formData.append('file', file)
  formData.append('courseId', String(courseId))
  formData.append('title', String(title).trim())
  formData.append('resourceType', resourceType)
  formData.append('uploadTaskId', uploadTaskId)

  const query = new URLSearchParams({ uploadTaskId })
  return request(`/upload/resources?${query.toString()}`, {
    method: 'POST',
    body: formData,
  })
}

export function cancelUploadTask(taskId) {
  return request(`/upload/tasks/${taskId}/cancel`, {
    method: 'POST',
  })
}

export function getUploadTask(taskId) {
  return request(`/upload/tasks/${taskId}`)
}

export function listRecentUploadTasks() {
  return request('/upload/tasks/recent')
}

function buildWsUrl(taskId) {
  const token = getToken()
  if (!token) {
    throw new Error('未获取到登录凭证，请重新登录')
  }

  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const params = new URLSearchParams({
    uploadTaskId: taskId,
    token: token.startsWith('Bearer ') ? token : `Bearer ${token}`,
  })
  return `${wsProtocol}//${window.location.host}/upload/ws/progress?${params.toString()}`
}

export function connectUploadProgressWs(taskId, handlers = {}) {
  const { onMessage, onOpen, onError, onClose } = handlers
  const socket = new WebSocket(buildWsUrl(taskId))

  socket.onopen = () => {
    if (onOpen) {
      onOpen()
    }
  }

  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (onMessage) {
        onMessage(data)
      }
    } catch {
      if (onError) {
        onError(new Error('上传进度消息解析失败'))
      }
    }
  }

  socket.onerror = () => {
    if (onError) {
      onError(new Error('上传进度连接异常'))
    }
  }

  socket.onclose = () => {
    if (onClose) {
      onClose()
    }
  }

  return {
    socket,
    close: () => {
      if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) {
        socket.close()
      }
    },
  }
}

