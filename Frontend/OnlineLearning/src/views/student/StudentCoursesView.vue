<template>
  <div class="student-courses">
    <el-card class="student-courses__panel" shadow="hover">
      <template #header>
        <div class="student-courses__head">
          <strong>课程广场</strong>
          <el-button type="primary" plain @click="loadCourseList">刷新</el-button>
        </div>
      </template>

      <div class="student-courses__query">
        <el-input v-model.trim="query.keyword" placeholder="按课程标题搜索" clearable />
        <el-input v-model.trim="query.category" placeholder="按课程分类筛选" clearable />
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="onReset">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="coursePage.records" stripe>
        <el-table-column prop="title" label="课程标题" min-width="220" />
        <el-table-column label="分类" min-width="120">
          <template #default="{ row }">{{ row.category || '-' }}</template>
        </el-table-column>
        <el-table-column label="难度" width="100">
          <template #default="{ row }">{{ formatDifficulty(row.difficulty) }}</template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="210">
          <template #default="{ row }">
            <el-space>
              <el-button link type="primary" @click="goCourse(row.id)">查看课程</el-button>
              <el-button
                link
                type="success"
                :disabled="enrolledCourseSet.has(String(row.id))"
                @click="onEnroll(row.id)">
                {{ enrolledCourseSet.has(String(row.id)) ? '已选课' : '立即选课' }}
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="student-courses__pagination">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :current-page="query.page"
          :page-size="query.size"
          :page-sizes="[5, 10, 20]"
          :total="coursePage.total"
          @current-change="onPageChange"
          @size-change="onSizeChange" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublicCourseList } from '@/api/course'
import { enrollCourse } from '@/api/learning'
import { formatDifficulty } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const enrolledCourseSet = ref(new Set())
const query = reactive({
  page: 1,
  size: 10,
  category: '',
  keyword: '',
})
const coursePage = reactive({
  records: [],
  total: 0,
})

/**
 * 加载公开课程分页。
 */
async function loadCourseList() {
  loading.value = true
  try {
    const data = await getPublicCourseList(query)
    coursePage.records = data?.records || []
    coursePage.total = Number(data?.total || 0)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程列表加载失败')
  } finally {
    loading.value = false
  }
}

/**
 * 查询按钮处理。
 */
function onSearch() {
  query.page = 1
  loadCourseList()
}

/**
 * 重置筛选项。
 */
function onReset() {
  query.keyword = ''
  query.category = ''
  query.page = 1
  loadCourseList()
}

/**
 * 页码变更。
 * @param {number} page 页码
 */
function onPageChange(page) {
  query.page = page
  loadCourseList()
}

/**
 * 每页数量变更。
 * @param {number} size 每页条数
 */
function onSizeChange(size) {
  query.size = size
  query.page = 1
  loadCourseList()
}

/**
 * 选课操作。
 * @param {number|string} courseId 课程ID
 */
async function onEnroll(courseId) {
  try {
    await enrollCourse(courseId)
    enrolledCourseSet.value.add(String(courseId)) // 本地标记已选课
    ElMessage.success('选课成功')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '选课失败')
  }
}

/**
 * 跳转课程学习页。
 * @param {number|string} courseId 课程ID
 */
function goCourse(courseId) {
  router.push(`/student/course/${courseId}`)
}

onMounted(() => {
  loadCourseList()
})
</script>

<style scoped>
.student-courses__panel {
  border-radius: 16px;
}

.student-courses__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.student-courses__query {
  margin-bottom: 12px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) auto auto;
  gap: 10px;
}

.student-courses__pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .student-courses__query {
    grid-template-columns: 1fr;
  }
}
</style>
