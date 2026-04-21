<template>
  <el-card class="placeholder-card" shadow="hover">
    <template #header>
      <strong>学生组件区（预留）</strong>
    </template>
    <p>此目录用于后续扩展学生端组件，例如：</p>
    <ul>
      <li>课程卡片</li>
      <li>学习进度条</li>
      <li>学习提醒面板</li>
    </ul>
    <el-alert
title="当前版本以答辩演示为主，保持轻量实现。"
type="info"
show-icon
:closable="false" />
  </el-card>
</template>

<style scoped>
.placeholder-card {
  border-radius: 16px;
}

p {
  margin-top: 0;
}

ul {
  margin-top: 0;
  padding-left: 18px;
  color: #606266;
}
</style>

