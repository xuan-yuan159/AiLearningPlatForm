<template>
  <PageSection title="作业批改">
    <DataTable :columns="columns" :rows="homeworkRows" />
  </PageSection>
</template>

<script setup>
import DataTable from '@/components/common/DataTable.vue'
import PageSection from '@/components/common/PageSection.vue'

/**
 * 作业批改页面，展示待批改队列与评分信息。
 */
const columns = [
  { key: 'student', label: '学生' },
  { key: 'assignment', label: '作业名称' },
  { key: 'className', label: '班级' },
  { key: 'submittedAt', label: '提交时间' },
  { key: 'status', label: '状态' },
]

const homeworkRows = [
  {
    id: 'hw-1',
    student: '李晨',
    assignment: '线性回归实验',
    className: 'AI-1班',
    submittedAt: '2026-03-28 20:10',
    status: '待批改',
  },
  {
    id: 'hw-2',
    student: '赵琪',
    assignment: '决策树实验',
    className: 'AI-2班',
    submittedAt: '2026-03-28 20:19',
    status: '已批改',
  },
  {
    id: 'hw-3',
    student: '马旭',
    assignment: 'KNN分类实验',
    className: 'AI-2班',
    submittedAt: '2026-03-29 09:05',
    status: '待复核',
  },
]
</script>
