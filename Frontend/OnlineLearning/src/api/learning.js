import { request } from '@/utils/request'

/**
 * 查询学生首页聚合数据。
 * @returns {Promise<any>}
 */
export function getLearningHome() {
  return request('/learning/home')
}

/**
 * 查询我的选课分页列表。
 * @param {Object} params 查询参数
 * @returns {Promise<any>}
 */
export function getMyEnrollments(params = {}) {
  const query = new URLSearchParams({
    page: String(params.page || 1),
    size: String(params.size || 10),
  })
  if (params.status) {
    query.set('status', String(params.status)) // 按状态筛选：1-学习中 2-已完成
  }
  return request(`/learning/enrollments/me?${query.toString()}`)
}

/**
 * 学生选课。
 * @param {number|string} courseId 课程ID
 * @returns {Promise<any>}
 */
export function enrollCourse(courseId) {
  return request(`/learning/enrollments/${courseId}`, {
    method: 'POST',
  })
}

/**
 * 查询课程学习进度。
 * @param {number|string} courseId 课程ID
 * @returns {Promise<any>}
 */
export function getCourseProgress(courseId) {
  return request(`/learning/progress/course/${courseId}`)
}

/**
 * 上报学习进度。
 * @param {Object} payload 上报参数
 * @returns {Promise<any>}
 */
export function reportProgress(payload) {
  return request('/learning/progress', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/**
 * 查询推荐课程列表。
 * @param {number} limit 返回数量
 * @returns {Promise<any>}
 */
export function getRecommendations(limit = 6) {
  const query = new URLSearchParams({
    limit: String(limit || 6),
  })
  return request(`/learning/recommendations/courses?${query.toString()}`)
}
