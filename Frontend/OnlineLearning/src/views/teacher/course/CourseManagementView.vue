<template>
  <div class="course-page">
    <div class="course-page__grid">
      <PageSection title="课程列表">
        <template #actions>
          <div class="course-list-actions">
            <button
class="ghost-btn"
type="button"
:disabled="courseLoading"
@click="loadCourses()">
              刷新
            </button>
            <button
class="primary-btn"
type="button"
@click="onCreateCourse">
              新建课程
            </button>
          </div>
        </template>

        <div class="search-bar">
          <input
            v-model.trim="keyword"
            class="field-input"
            type="text"
            placeholder="按课程标题搜索"
            @keyup.enter="loadCourses()"
          />
          <button
class="ghost-btn"
type="button"
:disabled="courseLoading"
@click="loadCourses()">
            搜索
          </button>
        </div>

        <div v-if="courseLoading" class="course-list-empty">课程加载中...</div>
        <div v-else-if="courses.length === 0" class="course-list-empty">暂无课程</div>
        <ul v-else class="course-list">
          <li
            v-for="course in courses"
            :key="course.id"
            class="course-list-item"
            :class="{ 'course-list-item--active': Number(course.id) === Number(activeCourseId) && !isCreatingCourse }"
            @click="onSelectCourse(course.id)"
          >
            <div class="course-list-item__top">
              <div class="course-list-item__title">{{ course.title }}</div>
              <StatusTag :text="mapCourseStatusText(course.status)" :type="mapCourseStatusType(course.status)" />
            </div>
            <div class="course-list-item__meta">
              <span>{{ course.category || '未分类' }}</span>
              <span>{{ formatDate(course.updatedAt || course.createdAt) }}</span>
            </div>
            <div class="course-list-item__actions">
              <button
class="text-btn"
type="button"
@click.stop="goToCourseContent(course.id)">
                进入内容建设
              </button>
            </div>
          </li>
        </ul>
      </PageSection>

      <PageSection :title="isCreatingCourse ? '新建课程' : '课程详情与章节管理'">
        <p
v-if="courseNotice.text"
class="notice"
:class="`notice--${courseNotice.type}`">
          {{ courseNotice.text }}
        </p>

        <form class="course-form" @submit.prevent="onSubmitCourse">
          <label>
            课程标题
            <input
v-model.trim="courseForm.title"
class="field-input"
required
maxlength="128" />
          </label>
          <label>
            课程描述
            <textarea
v-model.trim="courseForm.description"
class="field-textarea"
rows="3" />
          </label>
          <label>
            课程封面
            <div class="cover-upload">
              <input
ref="coverInputRef"
class="field-file"
type="file"
accept="image/*"
@change="onCourseCoverChange" />
              <div class="cover-upload__actions">
                <button
class="ghost-btn"
type="button"
:disabled="courseSubmitting"
@click="clearSelectedCover">
                  清空已选文件
                </button>
                <button
class="ghost-btn"
type="button"
:disabled="courseSubmitting"
@click="removeCurrentCover">
                  清空封面
                </button>
                <button
class="danger-btn"
type="button"
:disabled="!coverUploading"
@click="onCancelCoverUpload">
                  取消封面上传
                </button>
              </div>
              <div v-if="coverUploading || coverUploadProgress > 0" class="cover-upload__progress">
                <div class="cover-upload__progress-track">
                  <div class="cover-upload__progress-value" :style="{ width: `${coverUploadProgress}%` }"></div>
                </div>
                <span>{{ coverUploadProgress }}%</span>
              </div>
              <p class="cover-upload__hint">新建时会自动执行：建课 → 上传封面 → 回写课程封面地址</p>
              <img
v-if="coverPreviewUrl"
:src="coverPreviewUrl"
class="cover-upload__preview"
alt="课程封面预览" />
            </div>
          </label>
          <div class="course-form__inline">
            <label>
              分类
              <input v-model.trim="courseForm.category" class="field-input" />
            </label>
            <label>
              难度
              <select v-model.number="courseForm.difficulty" class="field-select">
                <option :value="1">入门</option>
                <option :value="2">进阶</option>
                <option :value="3">高级</option>
              </select>
            </label>
          </div>
          <label>
            标签（逗号分隔）
            <input v-model.trim="courseForm.tags" class="field-input" />
          </label>
          <div class="form-actions">
            <button
class="primary-btn"
type="submit"
:disabled="courseSubmitting">
              {{ courseSubmitting ? '提交中...' : isCreatingCourse ? '创建课程' : '保存修改' }}
            </button>
            <button
class="ghost-btn"
type="button"
:disabled="courseSubmitting"
@click="onResetCourseForm">
              重置
            </button>
          </div>
        </form>

        <div v-if="!isCreatingCourse && activeCourseId" class="course-status-actions">
          <button
class="primary-btn"
type="button"
:disabled="courseSubmitting"
@click="goToCourseContent(activeCourseId)">
            进入内容建设
          </button>
          <button
class="ghost-btn"
type="button"
:disabled="courseSubmitting || !canPublish"
@click="onPublishCourse">
            发布课程
          </button>
          <button
class="ghost-btn"
type="button"
:disabled="courseSubmitting || !canUnpublish"
@click="onUnpublishCourse">
            下架课程
          </button>
          <button
class="danger-btn"
type="button"
:disabled="courseSubmitting"
@click="onDeleteCourse">
            删除课程
          </button>
        </div>

        <div class="divider"></div>

        <div class="chapter-section">
          <h4>章节管理</h4>
          <p v-if="!activeCourseId || isCreatingCourse" class="chapter-empty">请先保存或选择课程，再管理章节。</p>
          <template v-else>
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

            <div v-if="chapterLoading" class="chapter-empty">章节加载中...</div>
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
@click="onDeleteChapter(chapter.id)">删除</button>
                  </td>
                </tr>
                <tr v-if="chapters.length === 0">
                  <td colspan="4" class="chapter-empty">暂无章节</td>
                </tr>
              </tbody>
            </table>
          </template>
        </div>
      </PageSection>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageSection from '@/components/common/PageSection.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import {
  createUploadImageTask,
  createChapter,
  createCourse,
  deleteUploadedResource,
  deleteChapter,
  deleteCourse,
  fetchChapterList,
  fetchCourseDetail,
  fetchCourseList,
  isCanceledUploadError,
  mapCourseStatusText,
  mapCourseStatusType,
  publishCourse,
  unpublishCourse,
  updateChapter,
  updateCourse,
} from '@/api/teacherApi'

const router = useRouter()

const courseLoading = ref(false)
const courseSubmitting = ref(false)
const chapterLoading = ref(false)
const chapterSubmitting = ref(false)

const courses = ref([])
const chapters = ref([])
const keyword = ref('')

const activeCourseId = ref(null)
const isCreatingCourse = ref(false)

const courseNotice = reactive({ type: 'default', text: '' })
const chapterNotice = reactive({ type: 'default', text: '' })
const coverInputRef = ref(null)
const coverFile = ref(null)
const coverPreviewFromFile = ref('')
const coverUploading = ref(false)
const coverUploadProgress = ref(0)
const coverUploadTask = ref(null)
const coverCancelRequested = ref(false)

const courseForm = reactive({
  title: '',
  description: '',
  coverUrl: '',
  category: '',
  tags: '',
  difficulty: 1,
})

const chapterForm = reactive({
  id: null,
  title: '',
  summary: '',
  sortOrder: 1,
})

const activeCourse = computed(() =>
  courses.value.find((item) => Number(item.id) === Number(activeCourseId.value)) || null,
)
const coverPreviewUrl = computed(() => coverPreviewFromFile.value || courseForm.coverUrl || '')

const canPublish = computed(() => {
  if (!activeCourse.value) return false
  const status = Number(activeCourse.value.status)
  return status === 0 || status === 2
})

const canUnpublish = computed(() => {
  if (!activeCourse.value) return false
  return Number(activeCourse.value.status) === 1
})

onMounted(async () => {
  await loadCourses()
})

onBeforeUnmount(() => {
  cancelCoverUploadTask()
  revokeCoverPreview()
})

async function loadCourses(selectedId = null) {
  courseLoading.value = true
  try {
    const records = await fetchCourseList({
      page: 1,
      size: 100,
      keyword: keyword.value || '',
    })
    courses.value = records || []

    if (courses.value.length === 0) {
      if (!isCreatingCourse.value) {
        onCreateCourse()
      }
      return
    }

    const targetId = selectedId || activeCourseId.value || courses.value[0].id
    if (courses.value.some((course) => Number(course.id) === Number(targetId))) {
      await onSelectCourse(targetId)
    } else {
      await onSelectCourse(courses.value[0].id)
    }
  } catch (error) {
    setCourseNotice('error', getErrorMessage(error))
  } finally {
    courseLoading.value = false
  }
}

async function onSelectCourse(courseId) {
  isCreatingCourse.value = false
  activeCourseId.value = Number(courseId)
  resetChapterForm()
  await Promise.all([loadCourseDetail(activeCourseId.value), loadChapters(activeCourseId.value)])
}

async function loadCourseDetail(courseId) {
  const detail = await fetchCourseDetail(courseId)
  fillCourseForm(detail)
}

async function loadChapters(courseId) {
  chapterLoading.value = true
  try {
    chapters.value = await fetchChapterList(courseId)
    if (!chapterForm.id) {
      chapterForm.sortOrder = (chapters.value[chapters.value.length - 1]?.sortOrder || 0) + 1
    }
  } catch (error) {
    setChapterNotice('error', getErrorMessage(error))
  } finally {
    chapterLoading.value = false
  }
}

function onCreateCourse() {
  isCreatingCourse.value = true
  activeCourseId.value = null
  chapters.value = []
  clearCourseNotice()
  clearChapterNotice()
  fillCourseForm()
  resetChapterForm()
}

function onResetCourseForm() {
  if (isCreatingCourse.value || !activeCourseId.value) {
    fillCourseForm()
    return
  }
  loadCourseDetail(activeCourseId.value)
}

async function onSubmitCourse() {
  if (!courseForm.title.trim()) {
    setCourseNotice('warning', '课程标题不能为空')
    return
  }
  courseSubmitting.value = true
  try {
    const payload = buildCoursePayload()
    if (isCreatingCourse.value) {
      const newCourseId = await createCourse({
        ...payload,
        coverUrl: '',
      })
      let noticeText = '课程创建成功'
      if (coverFile.value) {
        try {
          const coverUrl = await uploadCourseCover(newCourseId, payload.title)
          await updateCourse(newCourseId, {
            ...payload,
            coverUrl,
          })
          noticeText = '课程创建成功，封面上传成功'
        } catch (error) {
          if (isCanceledUploadError(error)) {
            setCourseNotice('warning', '课程已创建，封面上传已取消。请在课程详情中重新上传封面。')
          } else {
            setCourseNotice(
              'warning',
              `课程已创建，但封面上传失败：${getErrorMessage(error)}。请在课程详情中重新上传封面。`,
            )
          }
          isCreatingCourse.value = false
          await loadCourses(newCourseId)
          return
        }
      }
      clearSelectedCover()
      setCourseNotice('success', noticeText)
      isCreatingCourse.value = false
      await loadCourses(newCourseId)
      return
    }

    const hasNewCover = !!coverFile.value
    const submitPayload = { ...payload }
    if (hasNewCover) {
      try {
        submitPayload.coverUrl = await uploadCourseCover(activeCourseId.value, payload.title)
      } catch (error) {
        if (isCanceledUploadError(error)) {
          setCourseNotice('warning', '封面上传已取消，已保留原封面')
          return
        }
        throw error
      }
    }
    await updateCourse(activeCourseId.value, submitPayload)
    clearSelectedCover()
    setCourseNotice('success', hasNewCover ? '课程保存成功，封面已更新' : '课程保存成功')
    await loadCourses(activeCourseId.value)
  } catch (error) {
    setCourseNotice('error', getErrorMessage(error))
  } finally {
    courseSubmitting.value = false
  }
}

async function onPublishCourse() {
  if (!activeCourseId.value) return
  courseSubmitting.value = true
  try {
    await publishCourse(activeCourseId.value)
    setCourseNotice('success', '课程发布成功')
    await loadCourses(activeCourseId.value)
  } catch (error) {
    setCourseNotice('error', getErrorMessage(error))
  } finally {
    courseSubmitting.value = false
  }
}

async function onUnpublishCourse() {
  if (!activeCourseId.value) return
  courseSubmitting.value = true
  try {
    await unpublishCourse(activeCourseId.value)
    setCourseNotice('success', '课程下架成功')
    await loadCourses(activeCourseId.value)
  } catch (error) {
    setCourseNotice('error', getErrorMessage(error))
  } finally {
    courseSubmitting.value = false
  }
}

async function onDeleteCourse() {
  if (!activeCourseId.value) return
  if (!window.confirm('确认删除当前课程吗？')) {
    return
  }
  courseSubmitting.value = true
  try {
    await deleteCourse(activeCourseId.value)
    setCourseNotice('success', '课程删除成功')
    isCreatingCourse.value = false
    activeCourseId.value = null
    await loadCourses()
  } catch (error) {
    setCourseNotice('error', getErrorMessage(error))
  } finally {
    courseSubmitting.value = false
  }
}

async function onSubmitChapter() {
  if (!activeCourseId.value) {
    setChapterNotice('warning', '请先保存课程后再管理章节')
    return
  }
  if (!chapterForm.title.trim()) {
    setChapterNotice('warning', '章节标题不能为空')
    return
  }
  chapterSubmitting.value = true
  try {
    const payload = {
      courseId: Number(activeCourseId.value),
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
    await loadChapters(activeCourseId.value)
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
    await loadChapters(activeCourseId.value)
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

function fillCourseForm(detail = null) {
  clearSelectedCover()
  courseForm.title = detail?.title || ''
  courseForm.description = detail?.description || ''
  courseForm.coverUrl = detail?.coverUrl || ''
  courseForm.category = detail?.category || ''
  courseForm.tags = detail?.tags || ''
  courseForm.difficulty = Number(detail?.difficulty) || 1
}

function buildCoursePayload() {
  return {
    title: courseForm.title.trim(),
    description: courseForm.description.trim(),
    coverUrl: courseForm.coverUrl.trim(),
    category: courseForm.category.trim(),
    tags: courseForm.tags.trim(),
    difficulty: Number(courseForm.difficulty) || 1,
  }
}

function onCourseCoverChange(event) {
  const files = event.target?.files
  const file = files && files[0] ? files[0] : null
  if (!file) {
    return
  }
  if (!file.type || !file.type.startsWith('image/')) {
    setCourseNotice('warning', '课程封面仅支持图片文件')
    resetCoverInput()
    return
  }
  setCoverFile(file)
}

function setCoverFile(file) {
  coverFile.value = file
  revokeCoverPreview()
  coverPreviewFromFile.value = URL.createObjectURL(file)
}

function clearSelectedCover() {
  coverFile.value = null
  coverUploadProgress.value = 0
  revokeCoverPreview()
  resetCoverInput()
}

function removeCurrentCover() {
  clearSelectedCover()
  courseForm.coverUrl = ''
}

function resetCoverInput() {
  if (coverInputRef.value) {
    coverInputRef.value.value = ''
  }
}

function revokeCoverPreview() {
  if (!coverPreviewFromFile.value) {
    return
  }
  URL.revokeObjectURL(coverPreviewFromFile.value)
  coverPreviewFromFile.value = ''
}

async function uploadCourseCover(courseId, courseTitle) {
  coverUploadProgress.value = 0
  coverCancelRequested.value = false
  coverUploading.value = true

  try {
    coverUploadTask.value = createUploadImageTask(
      {
        file: coverFile.value,
        courseId,
        title: `${courseTitle}-课程封面`,
      },
      {
        onProgress: (percent) => {
          coverUploadProgress.value = percent
        },
      },
    )
    const uploadResult = await coverUploadTask.value.promise
    if (coverCancelRequested.value) {
      await cleanupCanceledCoverResource(uploadResult.resourceId)
      throw new Error('上传已取消')
    }
    coverUploadProgress.value = 100
    return uploadResult.url
  } catch (error) {
    if (isCanceledUploadError(error)) {
      throw error
    }
    if (coverCancelRequested.value) {
      const canceledError = new Error('上传已取消')
      canceledError.name = 'UploadCanceledError'
      canceledError.isCanceled = true
      throw canceledError
    }
    throw error
  } finally {
    coverUploadTask.value = null
    coverCancelRequested.value = false
    coverUploading.value = false
  }
}

function onCancelCoverUpload() {
  if (!coverUploading.value || !coverUploadTask.value) {
    return
  }
  coverCancelRequested.value = true
  coverUploadTask.value.cancel()
  setCourseNotice('warning', '正在取消封面上传...')
}

function cancelCoverUploadTask() {
  if (!coverUploadTask.value) {
    return
  }
  coverCancelRequested.value = true
  coverUploadTask.value.cancel()
  coverUploadTask.value = null
}

async function cleanupCanceledCoverResource(resourceId) {
  try {
    await deleteUploadedResource(resourceId)
    setCourseNotice('warning', `封面上传已取消，已清理资源（resourceId=${resourceId}）`)
  } catch (error) {
    setCourseNotice('error', `封面取消成功，但清理失败：${getErrorMessage(error)}`)
  }
}

function goToCourseContent(courseId) {
  if (!courseId) {
    return
  }
  router.push(`/teacher/courses/${courseId}/content`)
}

function setCourseNotice(type, text) {
  courseNotice.type = type
  courseNotice.text = text
}

function clearCourseNotice() {
  courseNotice.type = 'default'
  courseNotice.text = ''
}

function setChapterNotice(type, text) {
  chapterNotice.type = type
  chapterNotice.text = text
}

function clearChapterNotice() {
  chapterNotice.type = 'default'
  chapterNotice.text = ''
}

function getErrorMessage(error) {
  return error instanceof Error ? error.message : '请求失败，请稍后重试'
}

function formatDate(dateText) {
  if (!dateText) return '-'
  return String(dateText).slice(0, 19).replace('T', ' ')
}
</script>

<style lang="scss" scoped>
.course-page {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.course-page__grid {
  display: grid;
  grid-template-columns: minmax(280px, 360px) 1fr;
  gap: $space-4;

  @include down(xl) {
    grid-template-columns: 1fr;
  }
}

.course-list-actions {
  display: flex;
  gap: $space-2;
}

.search-bar {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: $space-2;
}

.course-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

.course-list-item {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-3;
  cursor: pointer;
  transition: all $duration-fast ease;
}

.course-list-item:hover {
  border-color: rgba($color-primary, 0.4);
  background: rgba($color-primary, 0.04);
}

.course-list-item--active {
  border-color: $color-primary;
  background: rgba($color-primary, 0.08);
}

.course-list-item__top {
  @include flex-between;
  gap: $space-2;
}

.course-list-item__title {
  font-weight: $font-weight-semibold;
  color: $color-text-main;
}

.course-list-item__meta {
  margin-top: $space-2;
  display: flex;
  justify-content: space-between;
  color: $color-text-secondary;
  font-size: $font-size-xs;
}

.course-list-item__actions {
  margin-top: $space-2;
  display: flex;
  justify-content: flex-end;
}

.course-list-empty {
  padding: $space-4;
  text-align: center;
  color: $color-text-secondary;
}

.course-form {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.course-form label,
.chapter-form label {
  display: flex;
  flex-direction: column;
  gap: $space-1;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.course-form__inline,
.chapter-form__inline {
  display: grid;
  grid-template-columns: 1fr 160px;
  gap: $space-3;

  @include down(sm) {
    grid-template-columns: 1fr;
  }
}

.field-input,
.field-select,
.field-textarea {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-2 $space-3;
  font-size: $font-size-sm;
  color: $color-text-main;
}

.field-textarea {
  resize: vertical;
  min-height: 72px;
}

.field-file {
  border: 1px dashed $color-border;
  border-radius: $radius-md;
  padding: $space-2 $space-3;
  font-size: $font-size-sm;
}

.cover-upload {
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

.cover-upload__actions {
  display: flex;
  gap: $space-2;
  flex-wrap: wrap;
}

.cover-upload__hint {
  margin: 0;
  color: $color-text-secondary;
  font-size: $font-size-xs;
}

.cover-upload__progress {
  display: flex;
  align-items: center;
  gap: $space-2;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.cover-upload__progress-track {
  flex: 1;
  height: 8px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba($color-primary, 0.12);
}

.cover-upload__progress-value {
  height: 100%;
  background: $color-primary;
  transition: width $duration-fast ease;
}

.cover-upload__preview {
  width: 100%;
  max-width: 320px;
  height: 180px;
  border-radius: $radius-md;
  object-fit: cover;
  border: 1px solid $color-border;
}

.form-actions {
  display: flex;
  gap: $space-2;
  flex-wrap: wrap;
}

.course-status-actions {
  margin-top: $space-3;
  display: flex;
  gap: $space-2;
  flex-wrap: wrap;
}

.chapter-section {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.chapter-section h4 {
  margin: 0;
  font-size: $font-size-md;
}

.chapter-form {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.chapter-table {
  width: 100%;
  border-collapse: collapse;
}

.chapter-table th,
.chapter-table td {
  padding: $space-2 $space-3;
  border-bottom: 1px solid $color-border;
  font-size: $font-size-sm;
  text-align: left;
}

.chapter-table th {
  color: $color-text-secondary;
}

.chapter-table__summary {
  max-width: 420px;
  color: $color-text-secondary;
}

.chapter-empty {
  text-align: center;
  color: $color-text-secondary;
}

.notice {
  margin: 0;
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

.divider {
  height: 1px;
  background: $color-border;
  margin: $space-2 0;
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
