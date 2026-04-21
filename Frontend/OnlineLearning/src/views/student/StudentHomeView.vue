<template>
  <div class="student-home">
    <el-card class="student-home__hero">
      <h1>学生首页</h1>
      <p>这是毕设演示版学生端：首页聚焦“看课程、看进度、看建议”。</p>
      <el-space wrap>
        <el-tag type="warning">当前用户：{{ auth.profile.value.nickname || auth.profile.value.username || '学生' }}</el-tag>
        <el-button
type="danger"
plain
@click="onLogout">退出登录</el-button>
      </el-space>
      <div class="student-home__gap"></div>
      <el-space wrap>
        <el-tag type="primary">演示友好</el-tag>
        <el-tag type="success">代码简洁</el-tag>
        <el-tag type="info">易于答辩讲解</el-tag>
      </el-space>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :md="16">
        <el-card class="student-home__card" shadow="hover">
          <template #header>
            <strong>推荐学习课程（静态演示数据）</strong>
          </template>
          <el-table :data="courses" stripe>
            <el-table-column prop="name" label="课程名称" />
            <el-table-column
prop="progress"
label="学习进度"
width="160" />
            <el-table-column
prop="level"
label="难度"
width="120" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <StudentPlaceholderCard />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logoutUser } from '@/api/auth'
import { useAuth } from '@/utils/auth'
import StudentPlaceholderCard from '@/components/student/StudentPlaceholderCard.vue'

const router = useRouter()
const auth = useAuth()

const courses = [
  { name: 'Vue 3 实战开发', progress: '60%', level: '入门' },
  { name: '机器学习基础', progress: '35%', level: '进阶' },
  { name: '大模型应用导论', progress: '20%', level: '进阶' },
]

async function onLogout() {
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
.student-home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.student-home__hero {
  border-radius: 16px;
}

.student-home__hero h1 {
  margin: 0 0 8px;
  font-size: 32px;
}

.student-home__hero p {
  margin: 0 0 14px;
  color: #606266;
}

.student-home__gap {
  height: 10px;
}

.student-home__card {
  border-radius: 16px;
}
</style>
