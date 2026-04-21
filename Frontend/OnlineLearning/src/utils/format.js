/**
 * 将数字格式化为千分位。
 * @param {number|string} value 数值
 * @returns {string}
 */
export function formatNumber(value) {
  const parsed = Number(value)
  if (Number.isNaN(parsed)) {
    return '-'
  }
  return parsed.toLocaleString('zh-CN')
}

/**
 * 将日期转为 yyyy-mm-dd。
 * @param {Date|string|number} dateValue 日期值
 * @returns {string}
 */
export function formatDate(dateValue) {
  const date = new Date(dateValue)
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  return date.toISOString().slice(0, 10)
}

/**
 * 将日期时间转为 yyyy-mm-dd hh:mm。
 * @param {Date|string|number} dateValue 日期值
 * @returns {string}
 */
export function formatDateTime(dateValue) {
  const date = new Date(dateValue)
  if (Number.isNaN(date.getTime())) {
    return '-'
  }
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

/**
 * 将秒数格式化为“xh ym zs”。
 * @param {number|string} seconds 秒
 * @returns {string}
 */
export function formatSeconds(seconds) {
  const total = Number(seconds || 0)
  if (Number.isNaN(total) || total <= 0) {
    return '0秒'
  }
  const hour = Math.floor(total / 3600)
  const minute = Math.floor((total % 3600) / 60)
  const second = total % 60
  if (hour > 0) {
    return `${hour}小时${minute}分${second}秒`
  }
  if (minute > 0) {
    return `${minute}分${second}秒`
  }
  return `${second}秒`
}

/**
 * 课程难度文案。
 * @param {number|string} difficulty 难度
 * @returns {string}
 */
export function formatDifficulty(difficulty) {
  const value = Number(difficulty)
  if (value === 1) return '入门'
  if (value === 2) return '进阶'
  if (value === 3) return '高级'
  return '-'
}

/**
 * 选课状态文案。
 * @param {number|string} status 状态
 * @returns {string}
 */
export function formatEnrollmentStatus(status) {
  const value = Number(status)
  if (value === 1) return '学习中'
  if (value === 2) return '已完成'
  return '未知'
}

/**
 * 资源类型文案。
 * @param {number|string} type 类型
 * @returns {string}
 */
export function formatResourceType(type) {
  const value = Number(type)
  if (value === 1) return '视频'
  if (value === 2) return '资料'
  return '未知'
}
