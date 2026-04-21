import { request } from '@/utils/request'

export function bindResourceToChapter(resourceId, chapterId) {
  return request(`/resource/${resourceId}/bind/${chapterId}`, {
    method: 'POST',
  })
}

