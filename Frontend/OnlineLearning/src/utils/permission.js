const teacherWhitelist = [
  'dashboard',
  'courses',
  'resources',
  'qa',
  'ai-summaries',
  'students',
  'notices',
  'profile',
  'prototype-low',
  'prototype-high',
  'no-permission',
]

/**
 * 判断教师是否拥有路由权限。
 * @param {string} routeName 路由名称
 * @returns {boolean}
 */
export function hasTeacherPermission(routeName) {
  return teacherWhitelist.includes(routeName)
}
