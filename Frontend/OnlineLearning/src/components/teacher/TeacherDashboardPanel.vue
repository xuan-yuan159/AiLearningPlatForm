<template>
  <div class="panel">
    <el-row :gutter="16">
      <el-col
v-for="item in metrics"
:key="item.label"
:xs="24"
:sm="12"
:lg="6">
        <el-card class="metric-card" shadow="hover">
          <p class="metric-card__label">{{ item.label }}</p>
          <p class="metric-card__value">{{ item.value }}</p>
          <p class="metric-card__trend">{{ item.trend }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card" shadow="hover">
      <template #header>
        <strong>本周教学提醒</strong>
      </template>
      <el-timeline>
        <el-timeline-item timestamp="周一">更新课程封面与简介，提升学生点击率。</el-timeline-item>
        <el-timeline-item timestamp="周三">集中回复问答，保证学习反馈及时。</el-timeline-item>
        <el-timeline-item timestamp="周五">检查课程进度，准备下周授课内容。</el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
const metrics = [
  { label: '课程总数', value: 6, trend: '较上周 +1' },
  { label: '学习学生', value: 128, trend: '较上周 +8%' },
  { label: '待处理问题', value: 12, trend: '较上周 -3' },
  { label: '本周完成率', value: '74%', trend: '较上周 +4%' },
]
</script>

<style scoped>
.panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.metric-card__label {
  margin: 0;
  color: #909399;
}

.metric-card__value {
  margin: 8px 0 4px;
  font-size: 30px;
  font-weight: 700;
}

.metric-card__trend {
  margin: 0;
  color: #67c23a;
}

.panel-card {
  border-radius: 16px;
}
</style>

