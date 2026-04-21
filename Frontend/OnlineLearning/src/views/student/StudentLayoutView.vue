<template>
  <div class="student-layout">
    <header class="student-layout__header">
      <div class="student-layout__brand">学生学习中心</div>
      <el-menu
        mode="horizontal"
        :default-active="activeMenu"
        class="student-layout__menu"
        @select="onMenuSelect">
        <el-menu-item index="/student/home">首页</el-menu-item>
        <el-menu-item index="/student/courses">课程广场</el-menu-item>
        <el-menu-item index="/student/learning">学习中心</el-menu-item>
        <el-menu-item index="/student/qa">问答中心</el-menu-item>
        <el-menu-item index="/student/ai-assistant">AI建议</el-menu-item>
      </el-menu>
      <div class="student-layout__user">
        <el-tag type="info">
          {{ auth.profile.value.nickname || auth.profile.value.username || '学生用户' }}
        </el-tag>
        <el-button type="danger" plain @click="onLogout">退出</el-button>
      </div>
    </header>

    <main class="student-layout__content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logoutUser } from '@/api/auth'
import { useAuth } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuth()

const activeMenu = computed(() => {
  if (route.path.startsWith('/student/course/')) {
    return '/student/learning'
  }
  return route.path
})

/**
 * 顶部菜单跳转。
 * @param {string} index 目标路由
 */
function onMenuSelect(index) {
  router.push(index)
}

/**
 * 退出登录。
 */
async function onLogout() {
  try {
    await logoutUser()
  } catch {
    ElMessage.warning('登出接口调用失败，已执行本地退出')
  } finally {
    auth.logout() // 清理本地登录态
    router.push('/login')
    ElMessage.success('已退出登录')
  }
}
</script>

<style scoped>
.student-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.student-layout__header {
  height: 72px;
  padding: 0 20px;
  border-bottom: 1px solid #dbeafe;
  background: linear-gradient(90deg, #ffffff 0%, #f5f9ff 100%);
  display: grid;
  grid-template-columns: 180px 1fr auto;
  align-items: center;
  gap: 14px;
}

.student-layout__brand {
  font-size: 20px;
  font-weight: 700;
  color: #1d4ed8;
}

.student-layout__menu {
  border-bottom: none;
}

.student-layout__user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.student-layout__content {
  flex: 1;
  padding: 18px 20px;
  background: linear-gradient(180deg, #f8fbff 0%, #eef4ff 100%);
}

@media (max-width: 960px) {
  .student-layout__header {
    height: auto;
    padding: 10px;
    grid-template-columns: 1fr;
  }

  .student-layout__menu {
    overflow-x: auto;
  }
}
</style>
