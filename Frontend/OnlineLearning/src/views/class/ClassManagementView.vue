<template>
  <PageSection title="班级管理">
    <DataTable :columns="columns" :rows="classRows" />
  </PageSection>
</template>

<script setup>
import DataTable from '@/components/common/DataTable.vue'
import PageSection from '@/components/common/PageSection.vue'

/**
 * 班级管理页面，展示班级与授课安排。
 */
const columns = [
  { key: 'name', label: '班级名称' },
  { key: 'course', label: '关联课程' },
  { key: 'teacher', label: '授课教师' },
  { key: 'students', label: '学生人数' },
  { key: 'schedule', label: '上课时间' },
]

const classRows = [
  {
    id: 'cl-1',
    name: 'AI-1班',
    course: 'Python机器学习实战',
    teacher: '王老师',
    students: 58,
    schedule: '周二 19:00',
  },
  {
    id: 'cl-2',
    name: 'AI-2班',
    course: 'Python机器学习实战',
    teacher: '王老师',
    students: 60,
    schedule: '周四 19:00',
  },
  {
    id: 'cl-3',
    name: '算法冲刺班',
    course: '数据结构高频题解',
    teacher: '张老师',
    students: 47,
    schedule: '周六 10:00',
  },
]
</script>
