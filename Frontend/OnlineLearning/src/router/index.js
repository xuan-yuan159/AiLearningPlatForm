import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { hasTeacherPermission } from '@/utils/permission'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true, title: '教师登录' },
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '仪表盘' },
      },
      {
        path: 'courses',
        name: 'courses',
        component: () => import('@/views/course/CourseManagementView.vue'),
        meta: { title: '课程管理' },
      },
      {
        path: 'resources',
        name: 'resources',
        component: () => import('@/views/resources/CourseResourceManagementView.vue'),
        meta: { title: '课程资源建设' },
      },
      {
        path: 'qa',
        name: 'qa',
        component: () => import('@/views/qa/QuestionManagementView.vue'),
        meta: { title: '在线问答管理' },
      },
      {
        path: 'ai-summaries',
        name: 'ai-summaries',
        component: () => import('@/views/ai/AIContentReviewView.vue'),
        meta: { title: 'AI 摘要审核与发布' },
      },
      {
        path: 'students',
        name: 'students',
        component: () => import('@/views/student/StudentManagementView.vue'),
        meta: { title: '学生管理' },
      },
      {
        path: 'notices',
        name: 'notices',
        component: () => import('@/views/notice/NoticeAnnouncementView.vue'),
        meta: { title: '通知公告' },
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/profile/ProfileCenterView.vue'),
        meta: { title: '个人中心' },
      },
      {
        path: 'prototype/low',
        name: 'prototype-low',
        component: () => import('@/views/prototype/LowFidelityPrototypeView.vue'),
        meta: { title: '低保真原型' },
      },
      {
        path: 'prototype/high',
        name: 'prototype-high',
        component: () => import('@/views/prototype/HighFidelityPrototypeView.vue'),
        meta: { title: '高保真视觉稿' },
      },
      {
        path: 'no-permission',
        name: 'no-permission',
        component: () => import('@/views/system/NoPermissionView.vue'),
        meta: { title: '无权限' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/system/NotFoundView.vue'),
    meta: { public: true, title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (!to.meta.public && !authStore.isAuthenticated.value) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && authStore.isAuthenticated.value) {
    return { name: 'dashboard' }
  }
  if (to.name && !to.meta.public && !hasTeacherPermission(String(to.name))) {
    return { name: 'no-permission' }
  }
  document.title = `AI助教教师后台 - ${to.meta.title || '管理端'}`
  return true
})

export default router
