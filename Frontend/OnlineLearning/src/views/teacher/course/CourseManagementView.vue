<template>
  <PageSection title="课程管理">
    <template #actions>
      <button class="primary-btn" type="button">新建课程</button>
    </template>
    <DataTable
      :columns="columns"
      :rows="rows"
      :loading="loading"
    />
  </PageSection>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import DataTable from '@/components/common/DataTable.vue'
import PageSection from '@/components/common/PageSection.vue'
import { fetchCourseList } from '@/api/teacherApi'

/**
 * 课程管理页面，负责课程新增、编辑与状态展示。
 */
const loading = ref(false)
const rows = ref([])
const columns = [
  { key: 'title', label: '课程名称' },
  { key: 'category', label: '分类' },
  { key: 'status', label: '状态' },
  { key: 'students', label: '学习人数' },
  { key: 'updatedAt', label: '更新时间' },
]

/**
 * 拉取课程列表。
 * @returns {Promise<void>}
 */
const loadCourses = async () => {
  loading.value = true
  rows.value = await fetchCourseList()
  loading.value = false
}

onMounted(loadCourses)
</script>

<style lang="scss" scoped>
.primary-btn {
  @include button-primary;
}
</style>
