import { createRouter, createWebHistory } from 'vue-router'
import TeacherLayout from '@/layouts/TeacherLayout.vue'
import StudentLayout from '@/layouts/StudentLayout.vue'
import { useAuthStore } from '@/stores/useAuthStore'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true, title: '用户登录' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { public: true, title: '用户注册' },
  },
  {
    path: '/',
    component: StudentLayout,
    meta: { requiresAuth: true, role: 'STUDENT' },
    children: [
      {
        path: '',
        name: 'student-home',
        component: () => import('@/views/student-app/StudentHomeView.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'profile',
        name: 'student-profile',
        component: () => import('@/views/teacher/profile/ProfileCenterView.vue'),
        meta: { title: '个人中心' },
      },
    ],
  },
  {
    path: '/teacher',
    component: TeacherLayout,
    redirect: '/teacher/dashboard',
    meta: { requiresAuth: true, role: 'TEACHER' },
    children: [
      {
        path: 'dashboard',
        name: 'teacher-dashboard',
        component: () => import('@/views/teacher/dashboard/DashboardView.vue'),
        meta: { title: '仪表盘' },
      },
      {
        path: 'courses',
        name: 'teacher-courses',
        component: () => import('@/views/teacher/course/CourseManagementView.vue'),
        meta: { title: '课程管理' },
      },
      {
        path: 'courses/:courseId/content',
        name: 'teacher-course-content',
        component: () => import('@/views/teacher/course/CourseContentBuildView.vue'),
        meta: { title: '课程内容建设' },
      },
      {
        path: 'qa',
        name: 'teacher-qa',
        component: () => import('@/views/teacher/qa/QuestionManagementView.vue'),
        meta: { title: '在线问答管理' },
      },
      {
        path: 'ai-summaries',
        name: 'teacher-ai-summaries',
        component: () => import('@/views/teacher/ai/AIContentReviewView.vue'),
        meta: { title: 'AI 摘要审核与发布' },
      },
      {
        path: 'notices',
        name: 'teacher-notices',
        component: () => import('@/views/teacher/notice/NoticeAnnouncementView.vue'),
        meta: { title: '通知公告' },
      },
      {
        path: 'profile',
        name: 'teacher-profile',
        component: () => import('@/views/teacher/profile/ProfileCenterView.vue'),
        meta: { title: '个人中心' },
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

  if (to.meta.public) {
    return true
  }

  if (!authStore.isAuthenticated.value) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  const userRole = authStore.profile.value.role
  if (!userRole) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.role !== undefined && to.meta.role !== userRole) {
    if (userRole === 'TEACHER') {
      return { name: 'teacher-dashboard' }
    }
    return { name: 'student-home' }
  }

  return true
})

export default router
