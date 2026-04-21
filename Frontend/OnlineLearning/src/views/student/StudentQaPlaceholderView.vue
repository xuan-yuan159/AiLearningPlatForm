<template>
  <div class="student-placeholder">
    <el-result icon="warning" title="问答中心建设中">
      <template #sub-title>
        当前版本优先交付学习闭环（选课、学习、进度）。<br />
        问答功能将在后续对接接口后开放。
      </template>
      <template #extra>
        <el-button type="primary" @click="router.push('/student/learning')">返回学习中心</el-button>
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
