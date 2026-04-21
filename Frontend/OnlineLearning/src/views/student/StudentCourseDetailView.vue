<template>
  <div class="student-course-detail">
    <el-card class="student-course-detail__course" shadow="hover">
      <template #header>
        <div class="student-course-detail__head">
          <strong>{{ courseDetail.title || '课程学习' }}</strong>
          <el-space>
            <el-tag type="info">{{ formatDifficulty(courseDetail.difficulty) }}</el-tag>
            <el-button type="primary" plain @click="router.push('/student/courses')">返回广场</el-button>
          </el-space>
        </div>
      </template>
      <p class="student-course-detail__desc">{{ courseDetail.description || '暂无课程简介' }}</p>
      <el-alert
        v-if="needEnroll"
        type="warning"
        :closable="false"
        show-icon
        title="当前课程尚未选课，部分学习进度数据不可见"
        description="点击“立即选课”后，可查看完整进度与学习统计。" />
      <div v-if="needEnroll" class="student-course-detail__enroll-wrap">
        <el-button type="warning" :loading="enrolling" @click="onEnrollCourse">立即选课</el-button>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="9">
        <el-card class="student-course-detail__catalog" shadow="hover">
          <template #header>
            <strong>课程目录</strong>
          </template>
          <el-empty v-if="!catalogChapters.length" description="暂无目录资源" />
          <el-collapse v-else accordion>
            <el-collapse-item
              v-for="chapter in catalogChapters"
              :key="chapter.chapterId"
              :title="chapter.title">
              <el-table :data="chapter.resources || []" stripe>
                <el-table-column prop="title" label="资源" min-width="150" />
                <el-table-column label="类型" width="80">
                  <template #default="{ row }">{{ formatResourceType(row.type) }}</template>
                </el-table-column>
                <el-table-column label="进度" width="90">
                  <template #default="{ row }">{{ getResourceProgress(row.resourceId).progressSec || 0 }}秒</template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="onSelectResource(row, chapter.chapterId)">
                      学习
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="15">
        <el-card class="student-course-detail__viewer" shadow="hover">
          <template #header>
            <div class="student-course-detail__viewer-head">
              <strong>{{ selectedResource?.title || '请选择资源开始学习' }}</strong>
              <el-space>
                <el-tag v-if="selectedResource" type="success">
                  {{ formatResourceType(selectedResource.type) }}
                </el-tag>
                <el-button
                  v-if="selectedResource"
                  link
                  type="primary"
                  :loading="reporting"
                  @click="onManualSaveProgress">
                  手动保存进度
                </el-button>
                <el-button
                  v-if="selectedResource"
                  link
                  type="warning"
                  :loading="reporting"
                  @click="onMarkCompleted">
                  标记完成
                </el-button>
              </el-space>
            </div>
          </template>

          <el-empty v-if="!selectedResource" description="请选择左侧资源" />

          <div v-else class="student-course-detail__resource">
            <video
              v-if="Number(selectedResource.type) === 1"
              ref="videoRef"
              :key="selectedResource.resourceId"
              class="student-course-detail__video"
              :src="selectedResource.url"
              controls
              preload="metadata"
              @loadedmetadata="onVideoLoadedMetadata"
              @play="onVideoPlay"
              @pause="onVideoPause"
              @ended="onVideoEnded" />

            <div v-else class="student-course-detail__material">
              <el-result icon="success" title="资料资源">
                <template #sub-title>
                  当前资源为资料类型，请点击下方按钮查看，并手动保存学习进度。
                </template>
                <template #extra>
                  <el-button type="primary" :disabled="!selectedResource.url" @click="openMaterial">
                    打开资料
                  </el-button>
                </template>
              </el-result>
            </div>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="当前进度">
                {{ getResourceProgress(selectedResource.resourceId).progressSec || 0 }} 秒
              </el-descriptions-item>
              <el-descriptions-item label="累计学习时长">
                {{ formatSeconds(getResourceProgress(selectedResource.resourceId).totalWatchedS || 0) }}
              </el-descriptions-item>
              <el-descriptions-item label="完成状态">
                {{ Number(getResourceProgress(selectedResource.resourceId).completed) === 1 ? '已完成' : '未完成' }}
              </el-descriptions-item>
              <el-descriptions-item label="最近学习">
                {{ formatDateTime(getResourceProgress(selectedResource.resourceId).lastStudiedAt) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublicCourseCatalog, getPublicCourseDetail } from '@/api/course'
import { enrollCourse, getCourseProgress, reportProgress } from '@/api/learning'
import { formatDateTime, formatDifficulty, formatResourceType, formatSeconds } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const videoRef = ref(null)

const loading = reactive({
  course: false,
  catalog: false,
  progress: false,
})
const enrolling = ref(false)
const reporting = ref(false)
const needEnroll = ref(false)

const courseDetail = reactive({})
const catalogData = ref({ chapters: [] })
const progressMap = ref({})
const selectedResource = ref(null)
const selectedChapterId = ref(null)
const watchedDeltaSec = ref(0)

let watchTimer = null
let autoReportTimer = null

const courseId = computed(() => route.params.courseId)
const catalogChapters = computed(() => catalogData.value?.chapters || [])

/**
 * 加载课程详情。
 */
async function loadCourseDetail() {
  loading.course = true
  try {
    const data = await getPublicCourseDetail(courseId.value)
    Object.assign(courseDetail, data || {})
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程详情加载失败')
  } finally {
    loading.course = false
  }
}

/**
 * 加载课程目录。
 */
async function loadCourseCatalog() {
  loading.catalog = true
  try {
    const data = await getPublicCourseCatalog(courseId.value)
    catalogData.value = data || { chapters: [] }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程目录加载失败')
    catalogData.value = { chapters: [] }
  } finally {
    loading.catalog = false
  }
}

/**
 * 加载课程学习进度。
 */
async function loadCourseProgress() {
  loading.progress = true
  needEnroll.value = false
  try {
    const data = await getCourseProgress(courseId.value)
    progressMap.value = buildProgressMap(data?.chapters || [])
  } catch (error) {
    progressMap.value = {}
    const message = error instanceof Error ? error.message : '课程进度加载失败'
    if (message.includes('enroll')) {
      needEnroll.value = true // 未选课时提示先选课
      return
    }
    ElMessage.warning(message)
  } finally {
    loading.progress = false
  }
}

/**
 * 构建资源进度索引。
 * @param {Array} chapters 章节列表
 * @returns {Object}
 */
function buildProgressMap(chapters) {
  const map = {}
  ;(chapters || []).forEach((chapter) => {
    ;(chapter.resources || []).forEach((resource) => {
      map[resource.resourceId] = {
        progressSec: Number(resource.progressSec || 0),
        totalWatchedS: Number(resource.totalWatchedS || 0),
        completed: Number(resource.completed || 0),
        lastStudiedAt: resource.lastStudiedAt || '',
      }
    })
  })
  return map
}

/**
 * 获取资源进度。
 * @param {number|string} resourceId 资源ID
 * @returns {Object}
 */
function getResourceProgress(resourceId) {
  return (
    progressMap.value[resourceId] || {
      progressSec: 0,
      totalWatchedS: 0,
      completed: 0,
      lastStudiedAt: '',
    }
  )
}

/**
 * 选择学习资源。
 * @param {Object} resource 资源对象
 * @param {number|string} chapterId 章节ID
 */
async function onSelectResource(resource, chapterId) {
  if (selectedResource.value?.resourceId) {
    await flushProgressReport(false) // 切换资源前补报一次
  }
  stopWatchTimers()
  watchedDeltaSec.value = 0
  selectedResource.value = resource
  selectedChapterId.value = chapterId
}

/**
 * 播放器加载元数据后恢复上次进度。
 */
function onVideoLoadedMetadata() {
  const currentResource = selectedResource.value
  const videoElement = videoRef.value
  if (!currentResource || !videoElement) {
    return
  }
  const saved = getResourceProgress(currentResource.resourceId)
  const target = Math.floor(saved.progressSec || 0)
  if (target > 0 && target < videoElement.duration) {
    videoElement.currentTime = target // 恢复上次学习进度
  }
}

/**
 * 播放事件处理。
 */
function onVideoPlay() {
  startWatchTimers()
}

/**
 * 暂停事件处理。
 */
async function onVideoPause() {
  stopWatchTimers()
  await flushProgressReport(false)
}

/**
 * 播放结束事件处理。
 */
async function onVideoEnded() {
  stopWatchTimers()
  await flushProgressReport(true)
}

/**
 * 启动学习计时与自动上报计时器。
 */
function startWatchTimers() {
  stopWatchTimers()
  watchTimer = window.setInterval(() => {
    watchedDeltaSec.value += 1 // 按秒累计真实观看时长
  }, 1000)
  autoReportTimer = window.setInterval(() => {
    void flushProgressReport(false)
  }, 20000) // 每20秒自动上报一次
}

/**
 * 停止学习计时器。
 */
function stopWatchTimers() {
  if (watchTimer) {
    window.clearInterval(watchTimer)
    watchTimer = null
  }
  if (autoReportTimer) {
    window.clearInterval(autoReportTimer)
    autoReportTimer = null
  }
}

/**
 * 统一上报学习进度。
 * @param {boolean} forceCompleted 是否强制标记完成
 * @returns {Promise<boolean>}
 */
async function flushProgressReport(forceCompleted) {
  if (reporting.value) {
    return false
  }
  const currentResource = selectedResource.value
  if (!currentResource) {
    return false
  }

  const currentProgress = getCurrentProgressSec()
  const watchedDelta = Math.max(0, Math.floor(watchedDeltaSec.value))
  const duration = Number(currentResource.durationS || 0)
  const completed = forceCompleted || (duration > 0 && currentProgress >= duration)

  if (watchedDelta <= 0 && !completed) {
    return false
  }

  reporting.value = true
  try {
    await reportProgress({
      courseId: Number(courseId.value),
      chapterId: selectedChapterId.value ? Number(selectedChapterId.value) : null,
      resourceId: Number(currentResource.resourceId),
      progressSec: currentProgress,
      totalWatchedS: watchedDelta,
      completed,
    })
    patchLocalProgress(currentResource.resourceId, currentProgress, watchedDelta, completed)
    watchedDeltaSec.value = 0
    if (needEnroll.value) {
      needEnroll.value = false // 上报成功后说明已建立选课关系
    }
    return true
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '学习进度上报失败')
    return false
  } finally {
    reporting.value = false
  }
}

/**
 * 获取当前进度秒数。
 * @returns {number}
 */
function getCurrentProgressSec() {
  const currentResource = selectedResource.value
  if (!currentResource) {
    return 0
  }
  if (Number(currentResource.type) === 1 && videoRef.value) {
    return Math.max(0, Math.floor(videoRef.value.currentTime || 0))
  }
  return Math.max(0, Math.floor(getResourceProgress(currentResource.resourceId).progressSec || 0))
}

/**
 * 更新前端本地进度缓存。
 * @param {number|string} resourceId 资源ID
 * @param {number} progressSec 播放进度
 * @param {number} watchedDelta 本次新增学习时长
 * @param {boolean} completed 是否完成
 */
function patchLocalProgress(resourceId, progressSec, watchedDelta, completed) {
  const oldValue = getResourceProgress(resourceId)
  progressMap.value = {
    ...progressMap.value,
    [resourceId]: {
      progressSec: Math.max(Number(oldValue.progressSec || 0), Number(progressSec || 0)),
      totalWatchedS: Number(oldValue.totalWatchedS || 0) + Number(watchedDelta || 0),
      completed: completed ? 1 : Number(oldValue.completed || 0),
      lastStudiedAt: new Date().toISOString(),
    },
  }
}

/**
 * 手动保存进度。
 */
async function onManualSaveProgress() {
  const reported = await flushProgressReport(false)
  if (reported) {
    ElMessage.success('学习进度已保存')
    return
  }
  ElMessage.info('当前无新增学习进度')
}

/**
 * 手动标记完成。
 */
async function onMarkCompleted() {
  const reported = await flushProgressReport(true)
  if (reported) {
    ElMessage.success('已标记完成')
    return
  }
  ElMessage.info('当前资源已是完成状态')
}

/**
 * 打开资料资源。
 */
function openMaterial() {
  if (!selectedResource.value?.url) {
    ElMessage.warning('当前资源暂无可访问链接')
    return
  }
  window.open(selectedResource.value.url, '_blank') // 新窗口打开资料链接
}

/**
 * 立即选课。
 */
async function onEnrollCourse() {
  enrolling.value = true
  try {
    await enrollCourse(courseId.value)
    needEnroll.value = false
    ElMessage.success('选课成功')
    await loadCourseProgress()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '选课失败')
  } finally {
    enrolling.value = false
  }
}

/**
 * 初始化页面数据并默认选中第一个资源。
 */
async function initPage() {
  await Promise.all([loadCourseDetail(), loadCourseCatalog(), loadCourseProgress()])
  const firstChapter = catalogChapters.value.find((item) => (item.resources || []).length > 0)
  const firstResource = firstChapter?.resources?.[0]
  if (firstResource) {
    await onSelectResource(firstResource, firstChapter.chapterId)
  }
}

onMounted(() => {
  initPage()
})

onBeforeUnmount(() => {
  stopWatchTimers()
  void flushProgressReport(false) // 页面卸载前补报
})

onBeforeRouteLeave(() => {
  stopWatchTimers()
  void flushProgressReport(false) // 路由离开前补报
})
</script>

<style scoped>
.student-course-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.student-course-detail__course,
.student-course-detail__catalog,
.student-course-detail__viewer {
  border-radius: 16px;
}

.student-course-detail__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.student-course-detail__desc {
  margin: 0;
  color: #475569;
}

.student-course-detail__enroll-wrap {
  margin-top: 8px;
}

.student-course-detail__viewer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.student-course-detail__resource {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.student-course-detail__video {
  width: 100%;
  max-height: 460px;
  border-radius: 12px;
  background: #000;
}
</style>
