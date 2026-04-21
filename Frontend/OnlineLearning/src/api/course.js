import { request } from '@/utils/request'

export function fetchCoursePage(params = {}) {
  const query = new URLSearchParams({
    page: String(params.page || 1),
    size: String(params.size || 10),
  })

  if (params.category) {
    query.set('category', params.category)
  }
  if (params.keyword) {
    query.set('keyword', params.keyword)
  }

  return request(`/course/list?${query.toString()}`)
}

export function createCourse(payload) {
  return request('/course', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateCourse(courseId, payload) {
  return request(`/course/${courseId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function publishCourse(courseId) {
  return request(`/course/${courseId}/publish`, {
    method: 'POST',
  })
}

export function unpublishCourse(courseId) {
  return request(`/course/${courseId}/unpublish`, {
    method: 'POST',
  })
}

export function deleteCourse(courseId) {
  return request(`/course/${courseId}`, {
    method: 'DELETE',
  })
}

