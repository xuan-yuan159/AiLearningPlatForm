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
    path: '/student/home',
    name: 'student-home',
    component: () => import('@/views/student/StudentHomeView.vue'),
    meta: { requiresAuth: true, title: '学生首页' },
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
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

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
