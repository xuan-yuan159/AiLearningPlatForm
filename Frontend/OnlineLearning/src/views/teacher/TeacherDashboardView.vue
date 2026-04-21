<template>
  <div class="teacher-page">
    <aside class="teacher-page__aside">
      <div class="teacher-page__brand">教师后台</div>
      <TeacherSidebar />
    </aside>

    <section class="teacher-page__main">
      <header class="teacher-page__header">
        <div>
          <p class="teacher-page__sub">毕业设计演示版</p>
          <h2>{{ route.meta.title || '教师后台' }}</h2>
        </div>
        <el-dropdown>
          <span class="teacher-page__user">
            <el-avatar :src="auth.profile.value.avatar" :size="36" />
            <span>{{ auth.profile.value.nickname || auth.profile.value.username || '教师用户' }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/teacher/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="onLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="teacher-page__content">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import TeacherSidebar from '@/components/teacher/TeacherSidebar.vue'
import { logoutUser } from '@/api/auth'
import { useAuth } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuth()

async function onLogout() {
  try {
    await ElMessageBox.confirm('确认退出当前账号吗？', '退出登录', {
      type: 'warning',
    })
  } catch {
    return
  }

  try {
    await logoutUser()
  } catch {
    ElMessage.warning('登出接口调用失败，已执行本地退出')
  } finally {
    auth.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  }
}
</script>

<style scoped>
.teacher-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 230px 1fr;
}

.teacher-page__aside {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  color: #fff;
  padding: 16px 12px;
}

.teacher-page__brand {
  font-size: 20px;
  font-weight: 700;
  padding: 12px;
}

.teacher-page__main {
  display: flex;
  flex-direction: column;
}

.teacher-page__header {
  height: 72px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.teacher-page__sub {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.teacher-page__header h2 {
  margin: 2px 0 0;
}

.teacher-page__user {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.teacher-page__content {
  padding: 20px;
}

@media (max-width: 900px) {
  .teacher-page {
    grid-template-columns: 1fr;
  }

  .teacher-page__aside {
    padding-bottom: 8px;
  }
}
</style>

