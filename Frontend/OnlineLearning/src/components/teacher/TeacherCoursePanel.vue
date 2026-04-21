<template>
  <el-card class="panel-card" shadow="hover">
    <template #header>
      <div class="panel-head">
        <strong>课程管理（演示版）</strong>
        <el-button type="primary">新建课程</el-button>
      </div>
    </template>

    <el-table :data="courseRows" stripe>
      <el-table-column
prop="title"
label="课程名称"
min-width="220" />
      <el-table-column
prop="category"
label="分类"
width="160" />
      <el-table-column
prop="status"
label="状态"
width="120" />
      <el-table-column
prop="updatedAt"
label="最近更新"
width="180" />
      <el-table-column label="操作" width="180">
        <template #default>
          <el-button link type="primary">编辑</el-button>
          <el-button link>预览</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
const courseRows = [
  { title: 'Vue 3 项目开发', category: '前端开发', status: '已发布', updatedAt: '2026-04-18' },
  { title: '机器学习实践入门', category: '人工智能', status: '草稿', updatedAt: '2026-04-19' },
  { title: '软件工程导论', category: '专业基础', status: '已发布', updatedAt: '2026-04-20' },
]
</script>

<style scoped>
.panel-card {
  border-radius: 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>

