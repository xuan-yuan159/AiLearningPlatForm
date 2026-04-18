<template>
  <div class="content-page">
    <PageSection title="课程内容建设">
      <template #actions>
        <button
class="ghost-btn"
type="button"
@click="goBackToCourseManage">返回课程管理</button>
      </template>

      <p
v-if="pageNotice.text"
class="notice"
:class="`notice--${pageNotice.type}`">
        {{ pageNotice.text }}
      </p>

      <div v-if="pageLoading" class="empty">课程加载中...</div>
      <div v-else-if="!course" class="empty">课程不存在或无权限访问</div>
      <div v-else class="course-summary">
        <img
v-if="course.coverUrl"
class="course-summary__cover"
:src="course.coverUrl"
alt="课程封面" />
        <div class="course-summary__content">
          <div class="course-summary__head">
            <h3>{{ course.title }}</h3>
            <StatusTag :text="mapCourseStatusText(course.status)" :type="mapCourseStatusType(course.status)" />
          </div>
          <p class="course-summary__desc">{{ course.description || '暂无课程描述' }}</p>
          <p class="course-summary__meta">课程ID：{{ course.id }}</p>
        </div>
      </div>
    </PageSection>

    <div class="content-page__grid">
      <PageSection title="章节管理">
        <p
v-if="chapterNotice.text"
class="notice"
:class="`notice--${chapterNotice.type}`">
          {{ chapterNotice.text }}
        </p>

        <form class="chapter-form" @submit.prevent="onSubmitChapter">
          <div class="chapter-form__inline">
            <label>
              章节标题
              <input
v-model.trim="chapterForm.title"
class="field-input"
required />
            </label>
            <label>
              排序
              <input
v-model.number="chapterForm.sortOrder"
class="field-input"
type="number"
min="1" />
            </label>
          </div>
          <label>
            章节简介
            <textarea
v-model.trim="chapterForm.summary"
class="field-textarea"
rows="2" />
          </label>
          <div class="form-actions">
            <button
class="primary-btn"
type="submit"
:disabled="chapterSubmitting">
              {{ chapterSubmitting ? '处理中...' : chapterForm.id ? '保存章节' : '新增章节' }}
            </button>
            <button
class="ghost-btn"
type="button"
:disabled="chapterSubmitting"
@click="resetChapterForm">
              {{ chapterForm.id ? '取消编辑' : '重置' }}
            </button>
          </div>
        </form>

        <div v-if="chapterLoading" class="empty">章节加载中...</div>
        <table v-else class="chapter-table">
          <thead>
            <tr>
              <th>排序</th>
              <th>标题</th>
              <th>简介</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="chapter in chapters" :key="chapter.id">
              <td>{{ chapter.sortOrder }}</td>
              <td>{{ chapter.title }}</td>
              <td class="chapter-table__summary">{{ chapter.summary || '-' }}</td>
              <td>
                <button
class="text-btn"
type="button"
@click="onEditChapter(chapter)">编辑</button>
                <button
class="text-btn text-btn--danger"
type="button"
@click="onDeleteChapter(chapter.id)">
                  删除
                </button>
              </td>
            </tr>
            <tr v-if="chapters.length === 0">
              <td colspan="4" class="empty">暂无章节，请先创建章节</td>
            </tr>
          </tbody>
        </table>
      </PageSection>

      <PageSection title="资源上传并绑定">
        <p
v-if="uploadNotice.text"
class="notice"
:class="`notice--${uploadNotice.type}`">
          {{ uploadNotice.text }}
        </p>

        <label>
          章节
          <select
v-model="selectedChapterId"
class="field-select"
:disabled="chapters.length === 0">
            <option disabled value="">请选择章节</option>
            <option
v-for="chapter in chapters"
:key="chapter.id"
:value="String(chapter.id)">
              {{ chapter.sortOrder }}. {{ chapter.title }}
            </option>
          </select>
        </label>

        <div class="upload-panel">
          <h4>上传视频</h4>
          <label>
            资源标题
            <input
v-model.trim="videoTitle"
class="field-input"
placeholder="例如：第1讲-线性回归" />
          </label>
          <input
class="field-file"
type="file"
accept="video/*"
@change="onVideoFileChange" />
          <button
class="primary-btn"
type="button"
:disabled="videoUploading || chapters.length === 0"
@click="onUploadVideo">
            {{ videoUploading ? '上传中...' : '上传视频并绑定章节' }}
          </button>
          <button
class="danger-btn"
type="button"
:disabled="!videoUploading"
@click="onCancelVideoUpload">
            取消视频上传
          </button>
          <div v-if="videoUploading || videoUploadProgress > 0" class="upload-progress">
            <div class="upload-progress__track">
              <div class="upload-progress__value" :style="{ width: `${videoUploadProgress}%` }"></div>
            </div>
            <span>{{ videoUploadProgress }}%</span>
          </div>
        </div>

        <div class="upload-panel">
          <h4>上传图片</h4>
          <label>
            资源标题
            <input
v-model.trim="imageTitle"
class="field-input"
placeholder="例如：章节配图/课件截图" />
          </label>
          <input
class="field-file"
type="file"
accept="image/*"
@change="onImageFileChange" />
          <button
class="primary-btn"
type="button"
:disabled="imageUploading || chapters.length === 0"
@click="onUploadImage">
            {{ imageUploading ? '上传中...' : '上传图片并绑定章节' }}
          </button>
          <button
class="danger-btn"
type="button"
:disabled="!imageUploading"
@click="onCancelImageUpload">
            取消图片上传
          </button>
          <div v-if="imageUploading || imageUploadProgress > 0" class="upload-progress">
            <div class="upload-progress__track">
              <div class="upload-progress__value" :style="{ width: `${imageUploadProgress}%` }"></div>
            </div>
            <span>{{ imageUploadProgress }}%</span>
          </div>
        </div>

        <div class="divider"></div>
        <h4 class="result-title">最近上传结果</h4>
        <div v-if="uploadResults.length === 0" class="empty">暂无上传记录</div>
        <article
v-for="result in uploadResults"
:key="result.resourceId + result.typeLabel"
class="result-item">
          <div class="result-item__top">
            <strong>{{ result.typeLabel }}</strong>
            <span>resourceId: {{ result.resourceId }}</span>
          </div>
          <p class="result-item__line">objectKey: {{ result.objectKey }}</p>
          <p class="result-item__line">
            url:
            <a
:href="result.url"
target="_blank"
rel="noopener noreferrer">{{ result.url }}</a>
          </p>
        </article>
      </PageSection>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageSection from '@/components/common/PageSection.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import {
  bindResourceToChapter,
  createUploadImageTask,
  createUploadVideoTask,
  createChapter,
  deleteUploadedResource,
  deleteChapter,
  fetchChapterList,
  fetchCourseDetail,
  isCanceledUploadError,
  mapCourseStatusText,
  mapCourseStatusType,
  updateChapter,
} from '@/api/teacherApi'

const route = useRoute()
const router = useRouter()

const pageLoading = ref(false)
const chapterLoading = ref(false)
const chapterSubmitting = ref(false)
const videoUploading = ref(false)
const imageUploading = ref(false)
const videoUploadProgress = ref(0)
const imageUploadProgress = ref(0)
const videoUploadTask = ref(null)
const imageUploadTask = ref(null)
const videoCancelRequested = ref(false)
const imageCancelRequested = ref(false)

const course = ref(null)
const chapters = ref([])
const selectedChapterId = ref('')
const videoTitle = ref('')
const imageTitle = ref('')
const videoFile = ref(null)
const imageFile = ref(null)
const uploadResults = ref([])

const pageNotice = reactive({ type: 'default', text: '' })
const chapterNotice = reactive({ type: 'default', text: '' })
const uploadNotice = reactive({ type: 'default', text: '' })
const chapterForm = reactive({
  id: null,
  title: '',
  summary: '',
  sortOrder: 1,
})

onMounted(async () => {
  await loadPage()
})

onBeforeUnmount(() => {
  cancelAllUploadTasks()
})

watch(
  () => route.params.courseId,
  async () => {
    await loadPage()
  },
)

async function loadPage() {
  const courseId = Number(route.params.courseId)
  if (!courseId) {
    setPageNotice('error', '课程ID无效')
    return
  }
  pageLoading.value = true
  clearNotices()
  uploadResults.value = []
  try {
    await Promise.all([loadCourse(courseId), loadChapters(courseId)])
  } catch (error) {
    setPageNotice('error', getErrorMessage(error))
  } finally {
    pageLoading.value = false
  }
}

async function loadCourse(courseId) {
  course.value = await fetchCourseDetail(courseId)
}

async function loadChapters(courseId) {
  chapterLoading.value = true
  try {
    chapters.value = await fetchChapterList(courseId)
    selectedChapterId.value = chapters.value[0] ? String(chapters.value[0].id) : ''
    if (!chapterForm.id) {
      chapterForm.sortOrder = (chapters.value[chapters.value.length - 1]?.sortOrder || 0) + 1
    }
  } finally {
    chapterLoading.value = false
  }
}

async function onSubmitChapter() {
  if (!course.value?.id) {
    setChapterNotice('warning', '课程上下文缺失，请返回重试')
    return
  }
  if (!chapterForm.title.trim()) {
    setChapterNotice('warning', '章节标题不能为空')
    return
  }
  chapterSubmitting.value = true
  try {
    const payload = {
      courseId: Number(course.value.id),
      title: chapterForm.title.trim(),
      summary: chapterForm.summary.trim(),
      sortOrder: Number(chapterForm.sortOrder) || 1,
    }
    if (chapterForm.id) {
      await updateChapter(chapterForm.id, payload)
      setChapterNotice('success', '章节更新成功')
    } else {
      await createChapter(payload)
      setChapterNotice('success', '章节创建成功')
    }
    await loadChapters(course.value.id)
    resetChapterForm()
  } catch (error) {
    setChapterNotice('error', getErrorMessage(error))
  } finally {
    chapterSubmitting.value = false
  }
}

function onEditChapter(chapter) {
  chapterForm.id = chapter.id
  chapterForm.title = chapter.title || ''
  chapterForm.summary = chapter.summary || ''
  chapterForm.sortOrder = Number(chapter.sortOrder) || 1
}

async function onDeleteChapter(chapterId) {
  if (!window.confirm('确认删除该章节吗？')) {
    return
  }
  chapterSubmitting.value = true
  try {
    await deleteChapter(chapterId)
    setChapterNotice('success', '章节删除成功')
    await loadChapters(course.value.id)
    resetChapterForm()
  } catch (error) {
    setChapterNotice('error', getErrorMessage(error))
  } finally {
    chapterSubmitting.value = false
  }
}

function resetChapterForm() {
  chapterForm.id = null
  chapterForm.title = ''
  chapterForm.summary = ''
  chapterForm.sortOrder = (chapters.value[chapters.value.length - 1]?.sortOrder || 0) + 1 || 1
}

function onVideoFileChange(event) {
  const files = event.target?.files
  videoFile.value = files && files[0] ? files[0] : null
}

function onImageFileChange(event) {
  const files = event.target?.files
  imageFile.value = files && files[0] ? files[0] : null
}

async function onUploadVideo() {
  if (videoUploading.value) return
  if (!selectedChapterId.value) {
    setUploadNotice('warning', '请先选择章节（无章节请先创建）')
    return
  }
  videoUploadProgress.value = 0
  videoCancelRequested.value = false
  videoUploading.value = true
  try {
    videoUploadTask.value = createUploadVideoTask(
      {
        file: videoFile.value,
        courseId: String(course.value.id),
        title: videoTitle.value,
      },
      {
        onProgress: (percent) => {
          videoUploadProgress.value = percent
        },
      },
    )
    const uploadResult = await videoUploadTask.value.promise
    if (videoCancelRequested.value) {
      await cleanupCanceledUpload(uploadResult.resourceId, '视频')
      return
    }
    await bindResourceToChapter(uploadResult.resourceId, selectedChapterId.value)
    appendUploadResult(uploadResult, '视频')
    setUploadNotice('success', `视频上传并绑定成功，resourceId=${uploadResult.resourceId}`)
    videoTitle.value = ''
    videoFile.value = null
    videoUploadProgress.value = 100
  } catch (error) {
    if (isCanceledUploadError(error)) {
      setUploadNotice('warning', '视频上传已取消')
      return
    }
    setUploadNotice('error', getErrorMessage(error))
  } finally {
    videoUploadTask.value = null
    videoCancelRequested.value = false
    videoUploading.value = false
  }
}

async function onUploadImage() {
  if (imageUploading.value) return
  if (!selectedChapterId.value) {
    setUploadNotice('warning', '请先选择章节（无章节请先创建）')
    return
  }
  imageUploadProgress.value = 0
  imageCancelRequested.value = false
  imageUploading.value = true
  try {
    imageUploadTask.value = createUploadImageTask(
      {
        file: imageFile.value,
        courseId: String(course.value.id),
        title: imageTitle.value,
      },
      {
        onProgress: (percent) => {
          imageUploadProgress.value = percent
        },
      },
    )
    const uploadResult = await imageUploadTask.value.promise
    if (imageCancelRequested.value) {
      await cleanupCanceledUpload(uploadResult.resourceId, '图片')
      return
    }
    await bindResourceToChapter(uploadResult.resourceId, selectedChapterId.value)
    appendUploadResult(uploadResult, '图片')
    setUploadNotice('success', `图片上传并绑定成功，resourceId=${uploadResult.resourceId}`)
    imageTitle.value = ''
    imageFile.value = null
    imageUploadProgress.value = 100
  } catch (error) {
    if (isCanceledUploadError(error)) {
      setUploadNotice('warning', '图片上传已取消')
      return
    }
    setUploadNotice('error', getErrorMessage(error))
  } finally {
    imageUploadTask.value = null
    imageCancelRequested.value = false
    imageUploading.value = false
  }
}

function onCancelVideoUpload() {
  if (!videoUploading.value || !videoUploadTask.value) {
    return
  }
  videoCancelRequested.value = true
  videoUploadTask.value.cancel()
  setUploadNotice('warning', '正在取消视频上传...')
}

function onCancelImageUpload() {
  if (!imageUploading.value || !imageUploadTask.value) {
    return
  }
  imageCancelRequested.value = true
  imageUploadTask.value.cancel()
  setUploadNotice('warning', '正在取消图片上传...')
}

async function cleanupCanceledUpload(resourceId, typeLabel) {
  try {
    await deleteUploadedResource(resourceId)
    setUploadNotice('warning', `${typeLabel}上传已取消，已清理资源（resourceId=${resourceId}）`)
  } catch (error) {
    setUploadNotice('error', `${typeLabel}取消成功，但清理失败：${getErrorMessage(error)}`)
  }
}

function cancelAllUploadTasks() {
  if (videoUploadTask.value) {
    videoCancelRequested.value = true
    videoUploadTask.value.cancel()
    videoUploadTask.value = null
  }
  if (imageUploadTask.value) {
    imageCancelRequested.value = true
    imageUploadTask.value.cancel()
    imageUploadTask.value = null
  }
}

function appendUploadResult(result, typeLabel) {
  uploadResults.value.unshift({
    ...result,
    typeLabel,
  })
}

function clearNotices() {
  pageNotice.type = 'default'
  pageNotice.text = ''
  chapterNotice.type = 'default'
  chapterNotice.text = ''
  uploadNotice.type = 'default'
  uploadNotice.text = ''
}

function setPageNotice(type, text) {
  pageNotice.type = type
  pageNotice.text = text
}

function setChapterNotice(type, text) {
  chapterNotice.type = type
  chapterNotice.text = text
}

function setUploadNotice(type, text) {
  uploadNotice.type = type
  uploadNotice.text = text
}

function goBackToCourseManage() {
  router.push('/teacher/courses')
}

function getErrorMessage(error) {
  return error instanceof Error ? error.message : '请求失败，请稍后重试'
}
</script>

<style lang="scss" scoped>
.content-page {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.content-page__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-4;

  @include down(xl) {
    grid-template-columns: 1fr;
  }
}

.course-summary {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: $space-3;

  @include down(md) {
    grid-template-columns: 1fr;
  }
}

.course-summary__cover {
  width: 100%;
  height: 140px;
  object-fit: cover;
  border: 1px solid $color-border;
  border-radius: $radius-md;
}

.course-summary__head {
  @include flex-between;
  gap: $space-2;
}

.course-summary__head h3 {
  margin: 0;
  font-size: $font-size-lg;
}

.course-summary__desc,
.course-summary__meta {
  margin: $space-2 0 0;
  color: $color-text-secondary;
  font-size: $font-size-sm;
}

.chapter-form {
  display: flex;
  flex-direction: column;
  gap: $space-3;
  margin-bottom: $space-3;
}

.chapter-form__inline {
  display: grid;
  grid-template-columns: 1fr 140px;
  gap: $space-3;
}

.field-input,
.field-select,
.field-file,
.field-textarea {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-2 $space-3;
  font-size: $font-size-sm;
}

.field-textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: $space-2;
  flex-wrap: wrap;
}

.chapter-table {
  width: 100%;
  border-collapse: collapse;
}

.chapter-table th,
.chapter-table td {
  border-bottom: 1px solid $color-border;
  padding: $space-2 $space-3;
  font-size: $font-size-sm;
  text-align: left;
}

.chapter-table th {
  color: $color-text-secondary;
}

.chapter-table__summary {
  max-width: 320px;
  color: $color-text-secondary;
}

.upload-panel {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-3;
  margin-top: $space-3;
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

.upload-progress {
  display: flex;
  align-items: center;
  gap: $space-2;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.upload-progress__track {
  flex: 1;
  height: 8px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba($color-primary, 0.12);
}

.upload-progress__value {
  height: 100%;
  background: $color-primary;
  transition: width $duration-fast ease;
}

.upload-panel h4,
.result-title {
  margin: 0;
  font-size: $font-size-md;
}

.result-item {
  margin-top: $space-2;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-3;
}

.result-item__top {
  @include flex-between;
  gap: $space-2;
  font-size: $font-size-sm;
}

.result-item__line {
  margin: $space-2 0 0;
  color: $color-text-secondary;
  font-size: $font-size-sm;
  word-break: break-all;
}

.result-item__line a {
  color: $color-primary;
}

.empty {
  color: $color-text-secondary;
  text-align: center;
  padding: $space-3;
}

.divider {
  height: 1px;
  background: $color-border;
  margin: $space-3 0;
}

.notice {
  margin: 0 0 $space-2;
  padding: $space-2 $space-3;
  border-radius: $radius-md;
  font-size: $font-size-sm;
}

.notice--success {
  color: $color-success;
  background: rgba($color-success, 0.12);
}

.notice--warning {
  color: $color-warning;
  background: rgba($color-warning, 0.12);
}

.notice--error {
  color: $color-error;
  background: rgba($color-error, 0.12);
}

.primary-btn {
  @include button-primary;
}

.ghost-btn {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-2 $space-4;
  background: transparent;
  cursor: pointer;
  color: $color-text-main;
  font-weight: $font-weight-semibold;
}

.danger-btn {
  border: 1px solid rgba($color-error, 0.35);
  border-radius: $radius-md;
  padding: $space-2 $space-4;
  background: rgba($color-error, 0.08);
  cursor: pointer;
  color: $color-error;
  font-weight: $font-weight-semibold;
}

.text-btn {
  border: 0;
  background: transparent;
  color: $color-primary;
  cursor: pointer;
  padding: 0;
  margin-right: $space-3;
}

.text-btn--danger {
  color: $color-error;
}
</style>
