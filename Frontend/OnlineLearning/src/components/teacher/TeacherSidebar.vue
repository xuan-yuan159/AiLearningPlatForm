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
}
</style>

