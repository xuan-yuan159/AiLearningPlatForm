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
