import { request } from '@/utils/request'

/**
 * 查询教师端课程分页。
 * @param {Object} params 查询参数
 * @returns {Promise<any>}
 */
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

/**
 * 创建课程。
 * @param {Object} payload 课程保存参数
 * @returns {Promise<any>}
 */
export function createCourse(payload) {
  return request('/course', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/**
 * 更新课程。
 * @param {number|string} courseId 课程ID
 * @param {Object} payload 课程保存参数
 * @returns {Promise<any>}
 */
export function updateCourse(courseId, payload) {
  return request(`/course/${courseId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

/**
 * 发布课程。
 * @param {number|string} courseId 课程ID
 * @returns {Promise<any>}
 */
export function publishCourse(courseId) {
  return request(`/course/${courseId}/publish`, {
    method: 'POST',
  })
}

/**
 * 下架课程。
 * @param {number|string} courseId 课程ID
 * @returns {Promise<any>}
 */
export function unpublishCourse(courseId) {
  return request(`/course/${courseId}/unpublish`, {
    method: 'POST',
  })
}

/**
 * 删除课程。
 * @param {number|string} courseId 课程ID
 * @returns {Promise<any>}
 */
export function deleteCourse(courseId) {
  return request(`/course/${courseId}`, {
    method: 'DELETE',
  })
}

/**
 * 查询学生端公开课程分页。
 * @param {Object} params 查询参数
 * @returns {Promise<any>}
 */
export function getPublicCourseList(params = {}) {
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
  return request(`/course/public/list?${query.toString()}`)
}

/**
 * 查询学生端公开课程详情。
 * @param {number|string} courseId 课程ID
 * @returns {Promise<any>}
 */
export function getPublicCourseDetail(courseId) {
  return request(`/course/public/${courseId}`)
}

/**
 * 查询学生端课程目录。
 * @param {number|string} courseId 课程ID
 * @returns {Promise<any>}
 */
export function getPublicCourseCatalog(courseId) {
  return request(`/course/public/${courseId}/catalog`)
}
