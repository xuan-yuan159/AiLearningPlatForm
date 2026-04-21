import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { public: true, title: '注册' },
  },
  {
    path: '/student',
    component: () => import('@/views/student/StudentLayoutView.vue'),
    redirect: '/student/home',
    meta: { requiresAuth: true, role: 'STUDENT', title: '学生端' },
    children: [
      {
        path: 'home',
        name: 'student-home',
        component: () => import('@/views/student/StudentHomeView.vue'),
        meta: { title: '学生首页' },
      },
      {
        path: 'courses',
        name: 'student-courses',
        component: () => import('@/views/student/StudentCoursesView.vue'),
        meta: { title: '课程广场' },
      },
      {
        path: 'learning',
        name: 'student-learning',
        component: () => import('@/views/student/StudentLearningView.vue'),
        meta: { title: '学习中心' },
      },
      {
        path: 'course/:courseId',
        name: 'student-course-detail',
        component: () => import('@/views/student/StudentCourseDetailView.vue'),
        meta: { title: '课程学习' },
      },
      {
        path: 'qa',
        name: 'student-qa',
        component: () => import('@/views/student/StudentQaPlaceholderView.vue'),
        meta: { title: '问答中心' },
      },
      {
        path: 'ai-assistant',
        name: 'student-ai-assistant',
        component: () => import('@/views/student/StudentAiAssistantPlaceholderView.vue'),
        meta: { title: 'AI学习建议' },
      },
    ],
  },
  {
    path: '/teacher',
    component: () => import('@/views/teacher/TeacherDashboardView.vue'),
    redirect: '/teacher/dashboard',
    meta: { requiresAuth: true, role: 'TEACHER', title: '教师后台' },
    children: [
      {
        path: 'dashboard',
        name: 'teacher-dashboard',
        component: () => import('@/components/teacher/TeacherDashboardPanel.vue'),
        meta: { title: '教学概览' },
      },
      {
        path: 'course',
        name: 'teacher-course',
        component: () => import('@/components/teacher/TeacherCoursePanel.vue'),
        meta: { title: '课程管理' },
      },
      {
        path: 'profile',
        name: 'teacher-profile',
        component: () => import('@/components/teacher/TeacherProfilePanel.vue'),
        meta: { title: '个人中心' },
      },
    ],
  },
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL), // 使用 HTML5 history 模式
  routes,
})

/**
 * 全局路由守卫：处理鉴权、角色与标题。
 */
router.beforeEach((to) => {
  const auth = useAuth()

  if (to.meta.title) {
    document.title = `${to.meta.title} - AI助教在线学习平台`
  }

  if (to.meta.public) {
    if (auth.isAuthenticated.value && (to.name === 'login' || to.name === 'register')) {
      return auth.profile.value.role === 'TEACHER' ? { name: 'teacher-dashboard' } : { name: 'student-home' }
    }
    return true
  }

  if (!auth.isAuthenticated.value) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  const userRole = auth.profile.value.role
  if (!userRole) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.role && to.meta.role !== userRole) {
    return userRole === 'TEACHER' ? { name: 'teacher-dashboard' } : { name: 'student-home' }
  }

  return true
})

export default router
