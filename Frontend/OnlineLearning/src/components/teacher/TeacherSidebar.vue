<template>
  <el-menu
:default-active="activeMenu"
router
class="sidebar-menu">
    <el-menu-item index="/teacher/dashboard">
      <el-icon><DataBoard /></el-icon>
      <span>教学概览</span>
    </el-menu-item>
    <el-menu-item index="/teacher/course">
      <el-icon><Notebook /></el-icon>
      <span>课程管理</span>
    </el-menu-item>
    <el-menu-item index="/teacher/profile">
      <el-icon><User /></el-icon>
      <span>个人中心</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { DataBoard, Notebook, User } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar-menu {
  border-right: none;
  background: transparent;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.sidebar-menu :deep(.el-menu-item) {
  margin: 2px 4px;
  border-radius: 10px;
  color: #1e3a8a;
  transition: all 0.2s ease;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: #dbeafe;
  color: #1d4ed8;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #2563eb 0%, #3b82f6 100%);
  color: #ffffff;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.24);
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  color: inherit;
}
</style>
