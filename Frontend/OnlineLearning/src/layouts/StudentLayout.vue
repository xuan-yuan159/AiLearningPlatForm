<template>
  <div class="student-layout">
    <header class="student-layout__header">
      <div class="container">
        <div class="brand" @click="router.push('/')">
          <div class="logo">AI</div>
          <span class="name">AI 助教学习平台</span>
        </div>
        <nav class="nav-menu">
          <RouterLink to="/" class="nav-item">首页</RouterLink>
          <RouterLink to="/my-courses" class="nav-item">我的课程</RouterLink>
          <RouterLink to="/ai-tutor" class="nav-item">AI 助教</RouterLink>
        </nav>
        <div class="user-info">
          <div class="user-profile" @click="toggleUserMenu">
            <img :src="authStore.profile.value.avatar" alt="avatar" class="avatar" />
            <span class="username">{{ authStore.profile.value.nickname || authStore.profile.value.username }}</span>
          </div>
          <div v-if="showUserMenu" class="user-dropdown">
            <div class="dropdown-item" @click="router.push('/profile')">个人中心</div>
            <div class="dropdown-item" @click="onLogout">退出登录</div>
          </div>
        </div>
      </div>
    </header>
    <main class="student-layout__main">
      <div class="container">
        <RouterView />
      </div>
    </main>
    <footer class="student-layout__footer">
      <div class="container">
        <p>&copy; 2026 AI Learning Platform. 基于微服务架构的毕业设计项目.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'
import { logoutUser } from '@/api/authApi'

const router = useRouter()
const authStore = useAuthStore()
const showUserMenu = ref(false)

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const onLogout = async () => {
  try {
    await logoutUser()
  } catch (e) {
    console.error('Logout failed', e)
  } finally {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.student-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.student-layout__header {
  height: 64px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;

  .container {
    height: 100%;
    display: flex;
    align-items: center;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 $space-4;
  }

  .brand {
    display: flex;
    align-items: center;
    gap: $space-2;
    cursor: pointer;
    margin-right: $space-10;

    .logo {
      width: 32px;
      height: 32px;
      background: $color-primary;
      color: #fff;
      border-radius: $radius-sm;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
    }

    .name {
      font-size: $font-size-lg;
      font-weight: 600;
      color: $color-text-primary;
    }
  }

  .nav-menu {
    display: flex;
    gap: $space-6;
    flex: 1;

    .nav-item {
      text-decoration: none;
      color: $color-text-secondary;
      font-weight: 500;
      transition: color 0.3s;

      &:hover, &.router-link-active {
        color: $color-primary;
      }
    }
  }

  .user-info {
    position: relative;
    
    .user-profile {
      display: flex;
      align-items: center;
      gap: $space-2;
      cursor: pointer;
      padding: $space-1 $space-2;
      border-radius: $radius-full;
      transition: background 0.3s;

      &:hover {
        background: #f0f2f5;
      }

      .avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        object-fit: cover;
      }

      .username {
        font-size: $font-size-sm;
        color: $color-text-primary;
      }
    }

    .user-dropdown {
      position: absolute;
      top: calc(100% + 8px);
      right: 0;
      width: 120px;
      background: #fff;
      border-radius: $radius-md;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      padding: $space-2;
      border: 1px solid $color-border;

      .dropdown-item {
        padding: $space-2 $space-3;
        font-size: $font-size-sm;
        cursor: pointer;
        border-radius: $radius-sm;
        transition: background 0.3s;

        &:hover {
          background: #f5f7fa;
          color: $color-primary;
        }
      }
    }
  }
}

.student-layout__main {
  flex: 1;
  padding: $space-6 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 $space-4;
  }
}

.student-layout__footer {
  padding: $space-6 0;
  background: #fff;
  border-top: 1px solid $color-border;
  text-align: center;
  color: $color-text-secondary;
  font-size: $font-size-sm;
}
</style>
