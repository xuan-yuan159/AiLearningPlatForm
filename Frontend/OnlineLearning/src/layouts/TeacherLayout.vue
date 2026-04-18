<template>
  <div class="admin-layout">
    <aside class="admin-layout__sidebar">
      <h1 class="admin-layout__brand">AI助教后台</h1>
      <nav class="admin-layout__menu">
        <RouterLink
          v-for="item in menuList"
          :key="item.route"
          :to="item.route"
          class="admin-layout__menu-item"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>
    <main class="admin-layout__main">
      <header class="admin-layout__header">
        <div>
          <p class="admin-layout__subtitle">{{ currentSemester }}</p>
          <h2 class="admin-layout__title">{{ route.meta.title }}</h2>
        </div>
        <button
          class="admin-layout__logout"
          type="button"
          @click="onLogout"
        >
          退出登录
        </button>
      </header>
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/useAdminStore'
import { useAuthStore } from '@/stores/useAuthStore'
import { logoutUser } from '@/api/authApi'

/**
 * 教师后台布局组件，负责菜单导航与页头展示。
 */
const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()
const authStore = useAuthStore()

const menuList = [
  { label: '仪表盘', route: '/teacher/dashboard' },
  { label: '课程管理', route: '/teacher/courses' },
  { label: '在线问答管理', route: '/teacher/qa' },
  { label: 'AI 摘要审核与发布', route: '/teacher/ai-summaries' },
  { label: '通知公告', route: '/teacher/notices' },
  { label: '个人中心', route: '/teacher/profile' },
]

const currentSemester = computed(() => adminStore.uiState.value.currentSemester)

/**
 * 退出登录并跳转登录页。
 * @returns {void}
 */
const onLogout = async () => {
  try {
    await logoutUser()
  } catch (e) {
    console.error('登出接口调用失败', e)
  } finally {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.admin-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  min-height: 100vh;
  background: $color-bg-page;

  @include down(md) {
    grid-template-columns: 1fr;
  }
}

.admin-layout__sidebar {
  background: $color-bg-dark;
  color: $color-white;
  padding: $space-6 $space-5;
}

.admin-layout__brand {
  margin: 0 0 $space-6;
  font-size: $font-size-xl;
}

.admin-layout__menu {
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

.admin-layout__menu-item {
  color: rgba($color-white, 0.85);
  padding: $space-3;
  border-radius: $radius-md;
  transition: background $duration-fast ease;

  &.router-link-active {
    background: rgba($color-primary, 0.2);
    color: $color-white;
  }
}

.admin-layout__main {
  padding: $space-6;

  @include down(md) {
    padding: $space-4;
  }
}

.admin-layout__header {
  @include flex-between;
  margin-bottom: $space-5;
}

.admin-layout__subtitle {
  margin: 0;
  color: $color-text-secondary;
  font-size: $font-size-sm;
}

.admin-layout__title {
  margin: $space-1 0 0;
  font-size: $font-size-xxl;
}

.admin-layout__logout {
  @include button-primary;
}
</style>
