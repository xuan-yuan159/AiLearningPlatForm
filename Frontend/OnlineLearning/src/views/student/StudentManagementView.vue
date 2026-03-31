<template>
  <PageSection title="学生管理">
    <DataTable :columns="columns" :rows="studentRows" />
  </PageSection>
</template>

<script setup>
import DataTable from '@/components/common/DataTable.vue'
import PageSection from '@/components/common/PageSection.vue'

/**
 * 学生管理页面，展示学生学习进度和风险状态。
 */
const columns = [
  { key: 'name', label: '姓名' },
  { key: 'className', label: '班级' },
  { key: 'progress', label: '课程进度' },
  { key: 'lastStudyAt', label: '最近学习' },
  { key: 'risk', label: '预警' },
]

const studentRows = [
  {
    id: 's-1',
    name: '李晨',
    className: 'AI-1班',
    progress: '82%',
    lastStudyAt: '2026-03-30 21:40',
    risk: '正常',
  },
  {
    id: 's-2',
    name: '赵琪',
    className: 'AI-2班',
    progress: '67%',
    lastStudyAt: '2026-03-27 19:15',
    risk: '需关注',
  },
  {
    id: 's-3',
    name: '马旭',
    className: '算法冲刺班',
    progress: '44%',
    lastStudyAt: '2026-03-24 23:06',
    risk: '高风险',
  },
]
</script>
