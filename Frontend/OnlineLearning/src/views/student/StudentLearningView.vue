<template>
  <div class="student-learning">
    <el-card class="student-learning__panel" shadow="hover">
      <template #header>
        <div class="student-learning__head">
          <strong>学习中心</strong>
          <el-button type="primary" plain @click="loadEnrollments">刷新</el-button>
        </div>
      </template>

      <div class="student-learning__query">
        <el-radio-group v-model="query.status" @change="onStatusChange">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button :label="1">学习中</el-radio-button>
          <el-radio-button :label="2">已完成</el-radio-button>
        </el-radio-group>
      </div>

      <el-table v-loading="loading" :data="enrollmentPage.records" stripe>
        <el-table-column prop="title" label="课程标题" min-width="220" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">{{ formatEnrollmentStatus(row.status) }}</template>
        </el-table-column>
        <el-table-column label="进度" width="120">
          <template #default="{ row }">{{ row.progressPercent || 0 }}%</template>
        </el-table-column>
        <el-table-column label="已完成/总资源" width="130">
          <template #default="{ row }">{{ row.completedResources || 0 }}/{{ row.totalResources || 0 }}</template>
        </el-table-column>
        <el-table-column label="累计学习时长" width="160">
          <template #default="{ row }">{{ formatSeconds(row.totalWatchedS) }}</template>
        </el-table-column>
        <el-table-column label="最近学习" width="170">
          <template #default="{ row }">{{ formatDateTime(row.lastStudiedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="180">
          <template #default="{ row }">
            <el-space>
              <el-button link type="primary" @click="goLearn(row.courseId)">继续学习</el-button>
              <el-button link type="success" @click="openProgress(row.courseId)">进度详情</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="student-learning__pagination">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :current-page="query.page"
          :page-size="query.size"
          :page-sizes="[5, 10, 20]"
          :total="enrollmentPage.total"
          @current-change="onPageChange"
          @size-change="onSizeChange" />
      </div>
    </el-card>

    <el-drawer v-model="progressDrawer.visible" title="课程进度详情" size="55%">
      <el-skeleton v-if="progressDrawer.loading" :rows="8" animated />
      <el-empty v-else-if="!progressDrawer.data" description="暂无进度详情" />
      <div v-else class="student-learning__drawer">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课程标题">
            {{ progressDrawer.data.courseTitle }}
          </el-descriptions-item>
          <el-descriptions-item label="课程进度">
            {{ progressDrawer.data.progressPercent || 0 }}%
          </el-descriptions-item>
          <el-descriptions-item label="资源完成">
            {{ progressDrawer.data.completedResources || 0 }}/{{ progressDrawer.data.totalResources || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="累计学习时长">
            {{ formatSeconds(progressDrawer.data.totalWatchedS) }}
          </el-descriptions-item>
        </el-descriptions>

        <el-collapse class="student-learning__chapters">
          <el-collapse-item
            v-for="chapter in progressDrawer.data.chapters || []"
            :key="chapter.chapterId"
            :title="chapter.title">
            <el-table :data="chapter.resources || []" stripe>
              <el-table-column prop="title" label="资源标题" min-width="180" />
              <el-table-column label="类型" width="90">
                <template #default="{ row }">{{ formatResourceType(row.type) }}</template>
              </el-table-column>
              <el-table-column label="学习进度" width="120">
                <template #default="{ row }">{{ row.progressSec || 0 }}秒</template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">{{ Number(row.completed) === 1 ? '已完成' : '未完成' }}</template>
              </el-table-column>
            </el-table>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCourseProgress, getMyEnrollments } from '@/api/learning'
import { formatDateTime, formatEnrollmentStatus, formatResourceType, formatSeconds } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const query = reactive({
  status: '',
  page: 1,
  size: 10,
})
const enrollmentPage = reactive({
  records: [],
  total: 0,
})
const progressDrawer = reactive({
  visible: false,
  loading: false,
  data: null,
})

/**
 * 查询我的课程分页。
 */
async function loadEnrollments() {
  loading.value = true
  try {
    const data = await getMyEnrollments({
      status: query.status,
      page: query.page,
      size: query.size,
    })
    enrollmentPage.records = data?.records || []
    enrollmentPage.total = Number(data?.total || 0)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '学习中心数据加载失败')
  } finally {
    loading.value = false
  }
}

/**
 * 状态筛选切换。
 */
function onStatusChange() {
  query.page = 1
  loadEnrollments()
}

/**
 * 页码变更。
 * @param {number} page 页码
 */
function onPageChange(page) {
  query.page = page
  loadEnrollments()
}

/**
 * 每页数量变更。
 * @param {number} size 每页条数
 */
function onSizeChange(size) {
  query.size = size
  query.page = 1
  loadEnrollments()
}

/**
 * 打开课程进度详情。
 * @param {number|string} courseId 课程ID
 */
async function openProgress(courseId) {
  progressDrawer.visible = true
  progressDrawer.loading = true
  progressDrawer.data = null
  try {
    progressDrawer.data = await getCourseProgress(courseId)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程进度加载失败')
  } finally {
    progressDrawer.loading = false
  }
}

/**
 * 跳转课程学习页。
 * @param {number|string} courseId 课程ID
 */
function goLearn(courseId) {
  router.push(`/student/course/${courseId}`)
}

onMounted(() => {
  loadEnrollments()
})
</script>

<style scoped>
.student-learning__panel {
  border-radius: 16px;
}

.student-learning__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.student-learning__query {
  margin-bottom: 12px;
}

.student-learning__pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.student-learning__drawer {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.student-learning__chapters {
  margin-top: 8px;
}
</style>
