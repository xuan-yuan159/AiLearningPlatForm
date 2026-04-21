<template>
  <div class="student-placeholder">
    <el-result icon="info" title="AI学习建议建设中">
      <template #sub-title>
        后续将基于学习记录与课程行为，提供个性化学习建议与复习计划。
      </template>
      <template #extra>
        <el-button type="primary" @click="router.push('/student/home')">返回学生首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.student-placeholder {
  min-height: 420px;
  display: grid;
  place-items: center;
}
</style>
