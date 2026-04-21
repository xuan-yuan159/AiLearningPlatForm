<template>
  <div class="course-workbench">
    <el-card class="panel-card" shadow="hover">
      <template #header>
        <div class="panel-head">
          <strong>课程管理</strong>
          <el-button type="primary" @click="openCourseDialog()">新建课程</el-button>
        </div>
      </template>

      <div class="query-bar">
        <el-input
v-model.trim="query.keyword"
placeholder="按课程标题搜索"
clearable />
        <el-select
v-model="query.category"
clearable
placeholder="选择分类">
          <el-option
v-for="item in categoryOptions"
:key="item"
:label="item"
:value="item" />
        </el-select>
        <el-button
type="primary"
:loading="operationLoading.loadingCourses"
@click="searchCourses">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <el-table
v-loading="operationLoading.loadingCourses"
:data="coursePage.records"
stripe>
        <el-table-column label="课程名称" min-width="220">
          <template #default="{ row }">
            <div class="course-title-cell">
              <span>{{ row.title }}</span>
              <el-tag size="small" :type="isOwnCourse(row) ? 'success' : 'warning'">
                {{ isOwnCourse(row) ? '本人课程' : '非本人课程' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column
prop="category"
label="分类"
width="140" />
        <el-table-column label="难度" width="110">
          <template #default="{ row }">
            {{ difficultyText(row.difficulty) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="courseStatusType(row.status)">
              {{ courseStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
prop="updatedAt"
label="更新时间"
width="180" />
        <el-table-column label="操作" min-width="330">
          <template #default="{ row }">
            <el-space wrap>
              <el-button size="small" @click="selectCourse(row)">内容区</el-button>
              <el-button
size="small"
type="primary"
:disabled="!isOwnCourse(row)"
@click="openCourseDialog(row)">
                编辑
              </el-button>
              <el-button
                size="small"
                type="success"
                :disabled="!isOwnCourse(row) || Number(row.status) === 1"
                @click="handlePublish(row)">
                发布
              </el-button>
              <el-button
                size="small"
                type="warning"
                :disabled="!isOwnCourse(row) || Number(row.status) === 2"
                @click="handleUnpublish(row)">
                下架
              </el-button>
              <el-button
size="small"
type="danger"
:disabled="!isOwnCourse(row)"
@click="handleDeleteCourse(row)">
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :current-page="query.page"
          :page-size="query.size"
          :page-sizes="[5, 10, 20]"
          :total="coursePage.total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange" />
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card class="panel-card" shadow="hover">
          <template #header>
            <div class="panel-head">
              <strong>章节管理</strong>
              <el-button
type="primary"
size="small"
:disabled="!canOperateSelectedCourse"
@click="openChapterDialog()">
                新建章节
              </el-button>
            </div>
          </template>

          <div class="selected-tip">
            当前课程：
            <strong>{{ selectedCourse ? selectedCourse.title : '请先在上方选择课程' }}</strong>
          </div>

          <el-table
v-loading="operationLoading.loadingChapters"
:data="chapters"
stripe>
            <el-table-column
prop="sortOrder"
label="排序"
width="90" />
            <el-table-column
prop="title"
label="章节标题"
min-width="160" />
            <el-table-column
prop="summary"
label="章节简介"
min-width="200"
show-overflow-tooltip />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-space>
                  <el-button
size="small"
type="primary"
:disabled="!canOperateSelectedCourse"
@click="openChapterDialog(row)">
                    编辑
                  </el-button>
                  <el-button
size="small"
type="danger"
:disabled="!canOperateSelectedCourse"
@click="handleDeleteChapter(row)">
                    删除
                  </el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="panel-card" shadow="hover">
          <template #header>
            <strong>上传与自动绑定</strong>
          </template>

          <el-form label-width="92px" @submit.prevent>
            <el-form-item label="目标章节">
              <el-select
v-model="uploadForm.chapterId"
placeholder="选择章节"
:disabled="!canOperateSelectedCourse">
                <el-option
v-for="item in chapters"
:key="item.id"
:label="item.title"
:value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="资源类型">
              <el-radio-group v-model="uploadForm.resourceType" :disabled="!canOperateSelectedCourse">
                <el-radio-button label="video">视频</el-radio-button>
                <el-radio-button label="image">图片</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="资源标题">
              <el-input v-model.trim="uploadForm.title" :disabled="!canOperateSelectedCourse" />
            </el-form-item>
            <el-form-item label="选择文件">
              <input
class="upload-input"
type="file"
:disabled="!canOperateSelectedCourse"
@change="onFileChange" />
            </el-form-item>
            <el-form-item>
              <el-button
type="primary"
:loading="operationLoading.uploading || operationLoading.binding"
:disabled="!canOperateSelectedCourse"
@click="handleUpload">
                开始上传并自动绑定
              </el-button>
            </el-form-item>
          </el-form>

          <el-table :data="uploadTaskList" stripe>
            <el-table-column
prop="title"
label="资源标题"
min-width="140" />
            <el-table-column
prop="resourceType"
label="类型"
width="80" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="uploadStatusType(row.status)">{{ uploadStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="进度" width="160">
              <template #default="{ row }">
                <el-progress :percentage="Number(row.percent || 0)" :status="uploadProgressStatus(row.status)" />
              </template>
            </el-table-column>
            <el-table-column
prop="stage"
label="阶段"
min-width="130" />
            <el-table-column label="绑定" width="110">
              <template #default="{ row }">
                <el-tag v-if="bindingTaskMap[row.taskId]" type="warning">绑定中</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="danger"
                  :disabled="isTaskFinished(row.status)"
                  @click="handleCancelTask(row.taskId)">
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
v-model="courseDialog.visible"
:title="courseDialog.mode === 'create' ? '新建课程' : '编辑课程'"
width="560px">
      <el-form label-width="90px" @submit.prevent>
        <el-form-item label="课程标题" required>
          <el-input v-model.trim="courseDialog.form.title" />
        </el-form-item>
        <el-form-item label="课程分类">
          <el-input v-model.trim="courseDialog.form.category" />
        </el-form-item>
        <el-form-item label="课程简介">
          <el-input
v-model.trim="courseDialog.form.description"
type="textarea"
:rows="3" />
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model.trim="courseDialog.form.coverUrl" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model.trim="courseDialog.form.tags" placeholder="例如：Vue3,毕设,AI助教" />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="courseDialog.form.difficulty">
            <el-option :value="1" label="入门" />
            <el-option :value="2" label="进阶" />
            <el-option :value="3" label="高级" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDialog.visible = false">取消</el-button>
        <el-button
type="primary"
:loading="operationLoading.submittingCourse"
@click="submitCourse">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
v-model="chapterDialog.visible"
:title="chapterDialog.mode === 'create' ? '新建章节' : '编辑章节'"
width="480px">
      <el-form label-width="90px" @submit.prevent>
        <el-form-item label="章节标题" required>
          <el-input v-model.trim="chapterDialog.form.title" />
        </el-form-item>
        <el-form-item label="章节简介">
          <el-input
v-model.trim="chapterDialog.form.summary"
type="textarea"
:rows="3" />
        </el-form-item>
        <el-form-item label="排序值" required>
          <el-input-number v-model="chapterDialog.form.sortOrder" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="chapterDialog.visible = false">取消</el-button>
        <el-button
type="primary"
:loading="operationLoading.submittingChapter"
@click="submitChapter">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createCourse,
  deleteCourse,
  fetchCoursePage,
  publishCourse,
  unpublishCourse,
  updateCourse,
} from '@/api/course'
import { createChapter, deleteChapter, fetchChapterList, updateChapter } from '@/api/chapter'
import { bindResourceToChapter } from '@/api/resource'
import {
  cancelUploadTask,
  connectUploadProgressWs,
  createUploadTask,
  listRecentUploadTasks,
  uploadResource,
} from '@/api/upload'
import { useAuth } from '@/utils/auth'

const auth = useAuth()

const categoryOptions = ['前端开发', '人工智能', '专业基础', '后端开发']

const query = reactive({
  page: 1,
  size: 10,
  category: '',
  keyword: '',
})

const coursePage = reactive({
  records: [],
  total: 0,
  current: 1,
  size: 10,
})

const selectedCourse = ref(null)
const chapters = ref([])

const operationLoading = reactive({
  loadingCourses: false,
  submittingCourse: false,
  loadingChapters: false,
  submittingChapter: false,
  uploading: false,
  binding: false,
})

const courseDialog = reactive({
  visible: false,
  mode: 'create',
  currentId: null,
  form: {
    title: '',
    category: '',
    description: '',
    coverUrl: '',
    tags: '',
    difficulty: 1,
  },
})

const chapterDialog = reactive({
  visible: false,
  mode: 'create',
  currentId: null,
  form: {
    title: '',
    summary: '',
    sortOrder: 1,
  },
})

const uploadForm = reactive({
  chapterId: null,
  resourceType: 'video',
  title: '',
  file: null,
})

const uploadTaskMap = reactive({})
const wsSessionMap = reactive({})
const wsErrorTaskMap = reactive({})
const bindingTaskMap = reactive({})

const currentUserId = computed(() => String(auth.profile.value.userId || ''))
const canOperateSelectedCourse = computed(() => !!selectedCourse.value && isOwnCourse(selectedCourse.value))
const uploadTaskList = computed(() =>
  Object.values(uploadTaskMap).sort((a, b) => {
    const left = new Date(a.updatedAt || a.createdAt || 0).getTime()
    const right = new Date(b.updatedAt || b.createdAt || 0).getTime()
    return right - left
  }),
)

function isOwnCourse(course) {
  if (!course) return false
  return String(course.teacherId || '') === currentUserId.value
}

function courseStatusText(status) {
  const value = Number(status)
  if (value === 0) return '草稿'
  if (value === 1) return '已发布'
  if (value === 2) return '已下架'
  if (value === 3) return '已删除'
  return '未知'
}

function courseStatusType(status) {
  const value = Number(status)
  if (value === 1) return 'success'
  if (value === 2) return 'warning'
  if (value === 3) return 'danger'
  return 'info'
}

function difficultyText(value) {
  const level = Number(value)
  if (level === 1) return '入门'
  if (level === 2) return '进阶'
  if (level === 3) return '高级'
  return '-'
}

function uploadStatusText(status) {
  const map = {
    created: '待上传',
    uploading: '上传中',
    received: '已接收',
    oss_uploading: '传OSS中',
    persisting: '保存中',
    success: '成功',
    error: '失败',
    canceled: '已取消',
  }
  return map[status] || status || '未知'
}

function uploadStatusType(status) {
  if (status === 'success') return 'success'
  if (status === 'error') return 'danger'
  if (status === 'canceled') return 'warning'
  return 'info'
}

function uploadProgressStatus(status) {
  if (status === 'success') return 'success'
  if (status === 'error') return 'exception'
  return ''
}

function isTaskFinished(status) {
  return status === 'success' || status === 'error' || status === 'canceled'
}

function normalizeCoursePage(pageData) {
  return {
    records: pageData?.records || [],
    total: Number(pageData?.total || 0),
    current: Number(pageData?.current || query.page),
    size: Number(pageData?.size || query.size),
  }
}

async function loadCoursePage() {
  operationLoading.loadingCourses = true
  try {
    const data = await fetchCoursePage(query)
    Object.assign(coursePage, normalizeCoursePage(data))
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程列表加载失败')
  } finally {
    operationLoading.loadingCourses = false
  }
}

async function loadChapters(courseId) {
  if (!courseId) {
    chapters.value = []
    return
  }
  operationLoading.loadingChapters = true
  try {
    chapters.value = await fetchChapterList(courseId)
  } catch (error) {
    chapters.value = []
    ElMessage.error(error instanceof Error ? error.message : '章节列表加载失败')
  } finally {
    operationLoading.loadingChapters = false
  }
}

async function loadRecentTasks() {
  try {
    const list = await listRecentUploadTasks()
    ;(list || []).forEach((item) => {
      if (item?.taskId) {
        uploadTaskMap[item.taskId] = item
      }
    })
  } catch {
    ElMessage.warning('最近上传任务加载失败')
  }
}

function selectCourse(row) {
  selectedCourse.value = row
  uploadForm.chapterId = null
  loadChapters(row.id)
}

function searchCourses() {
  query.page = 1
  loadCoursePage()
}

function resetQuery() {
  query.keyword = ''
  query.category = ''
  query.page = 1
  loadCoursePage()
}

function handlePageChange(page) {
  query.page = page
  loadCoursePage()
}

function handleSizeChange(size) {
  query.size = size
  query.page = 1
  loadCoursePage()
}

function openCourseDialog(row = null) {
  if (!row) {
    courseDialog.mode = 'create'
    courseDialog.currentId = null
    courseDialog.form = {
      title: '',
      category: '',
      description: '',
      coverUrl: '',
      tags: '',
      difficulty: 1,
    }
    courseDialog.visible = true
    return
  }

  courseDialog.mode = 'edit'
  courseDialog.currentId = row.id
  courseDialog.form = {
    title: row.title || '',
    category: row.category || '',
    description: row.description || '',
    coverUrl: row.coverUrl || '',
    tags: row.tags || '',
    difficulty: row.difficulty || 1,
  }
  courseDialog.visible = true
}

async function submitCourse() {
  if (!courseDialog.form.title) {
    ElMessage.warning('请输入课程标题')
    return
  }

  operationLoading.submittingCourse = true
  try {
    if (courseDialog.mode === 'create') {
      const newId = await createCourse(courseDialog.form)
      ElMessage.success('课程创建成功')
      courseDialog.visible = false
      await loadCoursePage()
      const target = coursePage.records.find((item) => String(item.id) === String(newId))
      if (target) {
        selectCourse(target)
      }
      return
    }

    await updateCourse(courseDialog.currentId, courseDialog.form)
    ElMessage.success('课程更新成功')
    courseDialog.visible = false
    await loadCoursePage()
    if (selectedCourse.value && String(selectedCourse.value.id) === String(courseDialog.currentId)) {
      const latest = coursePage.records.find((item) => String(item.id) === String(courseDialog.currentId))
      if (latest) {
        selectedCourse.value = latest
      }
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程保存失败')
  } finally {
    operationLoading.submittingCourse = false
  }
}

async function handlePublish(row) {
  try {
    await publishCourse(row.id)
    ElMessage.success('课程已发布')
    await loadCoursePage()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程发布失败')
  }
}

async function handleUnpublish(row) {
  try {
    await unpublishCourse(row.id)
    ElMessage.success('课程已下架')
    await loadCoursePage()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程下架失败')
  }
}

async function handleDeleteCourse(row) {
  try {
    await ElMessageBox.confirm(`确认删除课程「${row.title}」吗？`, '删除课程', { type: 'warning' })
  } catch {
    return
  }

  try {
    await deleteCourse(row.id)
    ElMessage.success('课程已删除')
    if (selectedCourse.value && String(selectedCourse.value.id) === String(row.id)) {
      selectedCourse.value = null
      chapters.value = []
      uploadForm.chapterId = null
    }
    await loadCoursePage()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程删除失败')
  }
}

function openChapterDialog(row = null) {
  if (!selectedCourse.value) {
    ElMessage.warning('请先选择课程')
    return
  }
  if (!canOperateSelectedCourse.value) {
    ElMessage.warning('非本人课程不可编辑章节')
    return
  }

  if (!row) {
    chapterDialog.mode = 'create'
    chapterDialog.currentId = null
    chapterDialog.form = {
      title: '',
      summary: '',
      sortOrder: chapters.value.length + 1,
    }
    chapterDialog.visible = true
    return
  }

  chapterDialog.mode = 'edit'
  chapterDialog.currentId = row.id
  chapterDialog.form = {
    title: row.title || '',
    summary: row.summary || '',
    sortOrder: row.sortOrder || 1,
  }
  chapterDialog.visible = true
}

async function submitChapter() {
  if (!selectedCourse.value) {
    ElMessage.warning('请先选择课程')
    return
  }
  if (!chapterDialog.form.title) {
    ElMessage.warning('请输入章节标题')
    return
  }
  if (!chapterDialog.form.sortOrder) {
    ElMessage.warning('请输入章节排序')
    return
  }

  const payload = {
    courseId: selectedCourse.value.id,
    title: chapterDialog.form.title,
    summary: chapterDialog.form.summary,
    sortOrder: Number(chapterDialog.form.sortOrder),
  }

  operationLoading.submittingChapter = true
  try {
    if (chapterDialog.mode === 'create') {
      await createChapter(payload)
      ElMessage.success('章节创建成功')
    } else {
      await updateChapter(chapterDialog.currentId, payload)
      ElMessage.success('章节更新成功')
    }
    chapterDialog.visible = false
    await loadChapters(selectedCourse.value.id)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '章节保存失败')
  } finally {
    operationLoading.submittingChapter = false
  }
}

async function handleDeleteChapter(row) {
  try {
    await ElMessageBox.confirm(`确认删除章节「${row.title}」吗？`, '删除章节', { type: 'warning' })
  } catch {
    return
  }

  try {
    await deleteChapter(row.id)
    ElMessage.success('章节已删除')
    await loadChapters(selectedCourse.value?.id)
    if (uploadForm.chapterId === row.id) {
      uploadForm.chapterId = null
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '章节删除失败')
  }
}

function onFileChange(event) {
  const [file] = event.target.files || []
  uploadForm.file = file || null
  if (!uploadForm.title && file?.name) {
    uploadForm.title = file.name.replace(/\.[^.]+$/, '')
  }
}

function closeTaskWs(taskId) {
  const wsHolder = wsSessionMap[taskId]
  if (!wsHolder) return
  wsHolder.close()
  delete wsSessionMap[taskId]
  delete wsErrorTaskMap[taskId]
}

function patchTask(task) {
  if (!task?.taskId) return
  uploadTaskMap[task.taskId] = {
    ...(uploadTaskMap[task.taskId] || {}),
    ...task,
  }
  if (isTaskFinished(task.status)) {
    delete bindingTaskMap[task.taskId]
    closeTaskWs(task.taskId)
  }
}

function connectTaskWs(taskId) {
  if (!taskId) return null
  if (wsSessionMap[taskId]) return wsSessionMap[taskId]

  wsSessionMap[taskId] = connectUploadProgressWs(taskId, {
    onMessage: (task) => {
      patchTask(task)
    },
    onError: (error) => {
      if (wsErrorTaskMap[taskId]) {
        return
      }
      wsErrorTaskMap[taskId] = true
      ElMessage({
        type: 'warning',
        message: error instanceof Error ? error.message : '上传进度连接失败',
        grouping: true,
        duration: 1800,
      })
    },
    onClose: () => {
      delete wsSessionMap[taskId]
      delete wsErrorTaskMap[taskId]
    },
  })

  return wsSessionMap[taskId]
}

async function handleUpload() {
  if (!selectedCourse.value) {
    ElMessage.warning('请先选择课程')
    return
  }
  if (!canOperateSelectedCourse.value) {
    ElMessage.warning('非本人课程不可上传')
    return
  }
  if (!uploadForm.chapterId) {
    ElMessage.warning('请选择要自动绑定的章节')
    return
  }
  if (!uploadForm.resourceType) {
    ElMessage.warning('请选择资源类型')
    return
  }
  if (!uploadForm.file) {
    ElMessage.warning('请选择上传文件')
    return
  }
  if (!uploadForm.title) {
    ElMessage.warning('请输入资源标题')
    return
  }

  operationLoading.uploading = true
  let currentTaskId = ''

  try {
    const task = await createUploadTask({
      courseId: selectedCourse.value.id,
      title: uploadForm.title,
      resourceType: uploadForm.resourceType,
      fileName: uploadForm.file.name,
      fileSize: uploadForm.file.size,
    })

    const taskId = task.taskId
    if (!taskId) {
      throw new Error('上传任务创建失败')
    }
    currentTaskId = taskId

    uploadTaskMap[taskId] = {
      taskId,
      title: uploadForm.title,
      resourceType: uploadForm.resourceType,
      status: 'created',
      stage: '等待上传',
      percent: 0,
    }

    const wsConnection = connectTaskWs(taskId)
    if (!wsConnection) {
      throw new Error('上传进度连接初始化失败')
    }
    await wsConnection.waitOpen(8000)

    const result = await uploadResource({
      file: uploadForm.file,
      courseId: selectedCourse.value.id,
      title: uploadForm.title,
      resourceType: uploadForm.resourceType,
      uploadTaskId: taskId,
    })

    patchTask(result)

    if (!result?.resourceId) {
      throw new Error('上传成功但未返回资源ID')
    }

    operationLoading.binding = true
    bindingTaskMap[taskId] = true
    await bindResourceToChapter(result.resourceId, uploadForm.chapterId)
    delete bindingTaskMap[taskId]
    operationLoading.binding = false
    ElMessage.success('上传成功，已自动绑定章节')

    uploadForm.file = null
    uploadForm.title = ''
  } catch (error) {
    operationLoading.binding = false
    if (currentTaskId) {
      delete bindingTaskMap[currentTaskId]
      const task = uploadTaskMap[currentTaskId]
      if (task && task.status === 'created') {
        uploadTaskMap[currentTaskId] = {
          ...task,
          status: 'error',
          stage: '连接上传进度失败',
        }
      }
      closeTaskWs(currentTaskId)
    }
    ElMessage.error(error instanceof Error ? error.message : '上传失败')
  } finally {
    operationLoading.uploading = false
  }
}

async function handleCancelTask(taskId) {
  try {
    await cancelUploadTask(taskId)
    patchTask({
      ...(uploadTaskMap[taskId] || { taskId }),
      status: 'canceled',
      stage: '已取消',
    })
    ElMessage.success('上传任务已取消')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '取消上传失败')
  }
}

onMounted(async () => {
  await loadCoursePage()
  await loadRecentTasks()
})

onBeforeUnmount(() => {
  Object.keys(wsSessionMap).forEach((taskId) => {
    closeTaskWs(taskId)
  })
})
</script>

<style scoped>
.course-workbench {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-card {
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid #dbeafe;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.query-bar {
  margin-bottom: 14px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px auto auto;
  gap: 10px;
}

.course-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.selected-tip {
  margin-bottom: 10px;
  color: #334155;
  background: #eff6ff;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  padding: 8px 10px;
}

.upload-input {
  width: 100%;
}

@media (max-width: 960px) {
  .query-bar {
    grid-template-columns: 1fr;
  }
}
</style>
