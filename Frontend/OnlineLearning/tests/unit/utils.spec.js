import { formatDate, formatNumber } from '@/utils/format'
import { hasTeacherPermission } from '@/utils/permission'

describe('utils', () => {
  test('formatNumber 返回千分位字符串', () => {
    expect(formatNumber(12600)).toBe('12,600')
    expect(formatNumber('abc')).toBe('-')
  })

  test('formatDate 返回 yyyy-mm-dd', () => {
    expect(formatDate('2026-03-31T12:00:00')).toBe('2026-03-31')
    expect(formatDate('invalid')).toBe('')
  })

  test('hasTeacherPermission 判断路由权限', () => {
    expect(hasTeacherPermission('dashboard')).toBe(true)
    expect(hasTeacherPermission('admin-root')).toBe(false)
  })
})
