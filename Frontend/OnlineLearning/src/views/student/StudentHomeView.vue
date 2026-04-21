<template>
  <div class="student-home">
    <el-row :gutter="16">
      <el-col :xs="24" :md="8">
        <el-card class="student-home__metric" shadow="hover">
          <p class="student-home__metric-label">学习中课程</p>
          <p class="student-home__metric-value">{{ summary.learningCourseCount }}</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="student-home__metric" shadow="hover">
          <p class="student-home__metric-label">已完成课程</p>
          <p class="student-home__metric-value">{{ summary.completedCourseCount }}</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="student-home__metric" shadow="hover">
          <p class="student-home__metric-label">累计学习时长</p>
          <p class="student-home__metric-value">{{ formatSeconds(summary.totalWatchedS) }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card class="student-home__panel" shadow="hover">
          <template #header>
            <div class="student-home__panel-head">
              <strong>最近学习</strong>
              <el-button link type="primary" @click="router.push('/student/learning')">查看全部</el-button>
            </div>
          </template>
          <el-empty v-if="!recentCourses.length" description="暂无学习记录" />
          <el-table v-else :data="recentCourses" stripe>
            <el-table-column prop="title" label="课程" min-width="170" />
            <el-table-column label="进度" width="120">
              <template #default="{ row }">{{ row.progressPercent || 0 }}%</template>
            </el-table-column>
            <el-table-column label="最近学习" width="170">
              <template #default="{ row }">{{ formatDateTime(row.lastStudiedAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" @click="goLearn(row.courseId)">继续学习</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="student-home__panel" shadow="hover">
          <template #header>
            <div class="student-home__panel-head">
              <strong>推荐课程</strong>
              <el-button link type="primary" @click="router.push('/student/courses')">去选课</el-button>
            </div>
          </template>
          <el-empty v-if="!recommendedCourses.length" description="暂无推荐课程" />
          <el-table v-else :data="recommendedCourses" stripe>
            <el-table-column prop="title" label="课程" min-width="170" />
            <el-table-column label="难度" width="100">
              <template #default="{ row }">{{ formatDifficulty(row.difficulty) }}</template>
            </el-table-column>
            <el-table-column label="分类" min-width="120">
              <template #default="{ row }">{{ row.category || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" @click="goLearn(row.courseId)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="student-home__panel" shadow="hover">
      <template #header>
        <div class="student-home__panel-head">
          <strong>学习中课程</strong>
          <el-button link type="primary" @click="router.push('/student/learning')">进入学习中心</el-button>
        </div>
      </template>
      <el-empty v-if="!learningCourses.length" description="暂无学习中课程" />
      <el-table v-else :data="learningCourses" stripe>
        <el-table-column prop="title" label="课程" min-width="180" />
        <el-table-column label="分类" min-width="120">
          <template #default="{ row }">{{ row.category || '-' }}</template>
        </el-table-column>
        <el-table-column label="进度" width="120">
          <template #default="{ row }">{{ row.progressPercent || 0 }}%</template>
        </el-table-column>
        <el-table-column label="累计学习时长" width="170">
          <template #default="{ row }">{{ formatSeconds(row.totalWatchedS) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="goLearn(row.courseId)">继续学习</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getLearningHome } from '@/api/learning'
import { formatDateTime, formatDifficulty, formatSeconds } from '@/utils/format'

const router = useRouter()

const summary = reactive({
  learningCourseCount: 0,
  completedCourseCount: 0,
  totalWatchedS: 0,
})
const recentCourses = ref([])
const learningCourses = ref([])
const recommendedCourses = ref([])

/**
 * 加载首页聚合信息。
 */
async function loadHomeData() {
  try {
    const data = await getLearningHome()
    Object.assign(summary, data?.summary || {})
    recentCourses.value = data?.recentLearningCourses || []
    learningCourses.value = data?.learningCourses || []
    recommendedCourses.value = data?.recommendedCourses || []
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '首页数据加载失败')
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
  loadHomeData()
})
</script>

<style scoped>
.student-home {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.student-home__metric {
  border-radius: 16px;
}

.student-home__metric-label {
  margin: 0;
  color: #64748b;
}

.student-home__metric-value {
  margin: 8px 0 0;
  font-size: 28px;
  font-weight: 700;
  color: #1e40af;
}

.student-home__panel {
  border-radius: 16px;
}

.student-home__panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
