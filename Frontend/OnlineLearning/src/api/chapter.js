import { request } from '@/utils/request'

export function fetchChapterList(courseId) {
  return request(`/chapter/list/${courseId}`)
}

export function createChapter(payload) {
  return request('/chapter', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateChapter(chapterId, payload) {
  return request(`/chapter/${chapterId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteChapter(chapterId) {
  return request(`/chapter/${chapterId}`, {
    method: 'DELETE',
  })
}

