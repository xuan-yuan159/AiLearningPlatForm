import { ajax, createUploadTask, isUploadCanceledError } from '@/api/ajax'

/**
 * 课程状态文案映射。
 * @param {number|string} status
 * @returns {string}
 */
export function mapCourseStatusText(status) {
  const value = Number(status)
  if (value === 0) return '草稿'
  if (value === 1) return '已发布'
  if (value === 2) return '已下架'
  if (value === 3) return '已删除'
  return '未知'
}

/**
 * 课程状态标签类型映射。
 * @param {number|string} status
 * @returns {'default'|'success'|'warning'|'error'}
 */
export function mapCourseStatusType(status) {
  const value = Number(status)
  if (value === 1) return 'success'
  if (value === 2) return 'warning'
  if (value === 3) return 'error'
  return 'default'
}

/**
 * 获取仪表盘统计信息（当前保留 mock）。
 * @returns {Promise<Array<{key: string, label: string, value: string|number, trend: string}>>}
 */
export function fetchDashboardMetrics() {
  return ajax('/api/teacher/dashboard', {
    mockData: [
      { key: 'activeCourses', label: '进行中课程', value: 18, trend: '+2' },
      { key: 'students', label: '学习学生数', value: 1260, trend: '+9%' },
      { key: 'pendingSummaries', label: '待审核摘要', value: 18, trend: '-3' },
      { key: 'pendingQuestions', label: '待处理问答', value: 27, trend: '-5' },
      { key: 'noticeReadRate', label: '公告阅读率', value: '87%', trend: '+3%' },
    ],
  })
}

/**
 * 获取课程分页数据。
 * @param {{page?: number, size?: number, category?: string, keyword?: string}} params
 * @returns {Promise<{records: Array<any>, total: number, current: number, size: number}>}
 */
export async function fetchCoursePage(params = {}) {
  const page = params.page || 1
  const size = params.size || 50
  const query = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  if (params.category) {
    query.set('category', params.category)
  }
  if (params.keyword) {
    query.set('keyword', params.keyword)
  }

  const pageData = await ajax(`/api/course/list?${query.toString()}`)
  return {
    records: pageData?.records || [],
    total: pageData?.total || 0,
    current: pageData?.current || page,
    size: pageData?.size || size,
  }
}

/**
 * 获取课程列表（兼容旧调用）。
 * @param {{page?: number, size?: number, category?: string, keyword?: string}} params
 * @returns {Promise<Array<any>>}
 */
export async function fetchCourseList(params = {}) {
  const pageData = await fetchCoursePage(params)
  return pageData.records
}

/**
 * 获取课程详情。
 * @param {number|string} courseId
 * @returns {Promise<any>}
 */
export function fetchCourseDetail(courseId) {
  return ajax(`/api/course/${courseId}`)
}

/**
 * 创建课程。
 * @param {{title: string, description?: string, coverUrl?: string, category?: string, tags?: string, difficulty?: number}} payload
 * @returns {Promise<number>}
 */
export function createCourse(payload) {
  return ajax('/api/course', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/**
 * 更新课程。
 * @param {number|string} courseId
 * @param {{title: string, description?: string, coverUrl?: string, category?: string, tags?: string, difficulty?: number}} payload
 * @returns {Promise<void>}
 */
export function updateCourse(courseId, payload) {
  return ajax(`/api/course/${courseId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

/**
 * 发布课程。
 * @param {number|string} courseId
 * @returns {Promise<void>}
 */
export function publishCourse(courseId) {
  return ajax(`/api/course/${courseId}/publish`, {
    method: 'POST',
  })
}

/**
 * 下架课程。
 * @param {number|string} courseId
 * @returns {Promise<void>}
 */
export function unpublishCourse(courseId) {
  return ajax(`/api/course/${courseId}/unpublish`, {
    method: 'POST',
  })
}

/**
 * 删除课程。
 * @param {number|string} courseId
 * @returns {Promise<void>}
 */
export function deleteCourse(courseId) {
  return ajax(`/api/course/${courseId}`, {
    method: 'DELETE',
  })
}

/**
 * 获取课程章节列表。
 * @param {number|string} courseId
 * @returns {Promise<Array<any>>}
 */
export function fetchChapterList(courseId) {
  return ajax(`/api/chapter/list/${courseId}`)
}

/**
 * 创建章节。
 * @param {{courseId: number, title: string, summary?: string, sortOrder: number}} payload
 * @returns {Promise<number>}
 */
export function createChapter(payload) {
  return ajax('/api/chapter', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/**
 * 更新章节。
 * @param {number|string} chapterId
 * @param {{courseId: number, title: string, summary?: string, sortOrder: number}} payload
 * @returns {Promise<void>}
 */
export function updateChapter(chapterId, payload) {
  return ajax(`/api/chapter/${chapterId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

/**
 * 删除章节。
 * @param {number|string} chapterId
 * @returns {Promise<void>}
 */
export function deleteChapter(chapterId) {
  return ajax(`/api/chapter/${chapterId}`, {
    method: 'DELETE',
  })
}

/**
 * 上传视频资源到 OSS 并落库。
 * @param {{file: File, courseId: number|string, title: string}} payload
 * @returns {Promise<{resourceId: number, url: string, objectKey: string}>}
 */
export function uploadVideoResource(payload) {
  return createUploadVideoTask(payload).promise
}

/**
 * 上传图片资源到 OSS 并落库。
 * @param {{file: File, courseId: number|string, title: string}} payload
 * @returns {Promise<{resourceId: number, url: string, objectKey: string}>}
 */
export function uploadImageResource(payload) {
  return createUploadImageTask(payload).promise
}

/**
 * 创建视频上传任务（支持进度与取消）。
 * @param {{file: File, courseId: number|string, title: string}} payload
 * @param {{onProgress?: (percent: number) => void}} options
 * @returns {{promise: Promise<{resourceId: number, url: string, objectKey: string}>, cancel: () => void}}
 */
export function createUploadVideoTask(payload, options = {}) {
  const formData = buildUploadFormData(payload)
  return createUploadTask('/api/upload/video', {
    formData,
    onProgress: options.onProgress,
  })
}

/**
 * 创建图片上传任务（支持进度与取消）。
 * @param {{file: File, courseId: number|string, title: string}} payload
 * @param {{onProgress?: (percent: number) => void}} options
 * @returns {{promise: Promise<{resourceId: number, url: string, objectKey: string}>, cancel: () => void}}
 */
export function createUploadImageTask(payload, options = {}) {
  const formData = buildUploadFormData(payload)
  return createUploadTask('/api/upload/image', {
    formData,
    onProgress: options.onProgress,
  })
}

/**
 * 删除已上传资源（OSS 对象 + resource 记录）。
 * @param {number|string} resourceId
 * @returns {Promise<void>}
 */
export function deleteUploadedResource(resourceId) {
  return ajax(`/api/upload/resource/${resourceId}`, {
    method: 'DELETE',
  })
}

/**
 * 判断是否为上传取消错误。
 * @param {unknown} error
 * @returns {boolean}
 */
export function isCanceledUploadError(error) {
  return isUploadCanceledError(error)
}

/**
 * 绑定资源到章节。
 * @param {number|string} resourceId
 * @param {number|string} chapterId
 * @returns {Promise<void>}
 */
export function bindResourceToChapter(resourceId, chapterId) {
  return ajax(`/api/resource/${resourceId}/bind/${chapterId}`, {
    method: 'POST',
  })
}

/**
 * 获取教师侧问答列表（当前保留 mock）。
 * @returns {Promise<Array<{id: string, courseTitle: string, chapterTitle: string, questioner: string, questionText: string, status: string, createdAt: string}>>}
 */
export function fetchTeacherQuestionRows() {
  return ajax('/api/teacher/questions', {
    mockData: [
      {
        id: 'q-10001',
        courseTitle: 'Python机器学习实战',
        chapterTitle: '第2章：分类与评估',
        questioner: '学生-小王',
        questionText: '逻辑回归的损失函数为什么通常用交叉熵？',
        status: '待回复',
        createdAt: '2026-03-29 21:12',
        teacherAnswer: '',
      },
      {
        id: 'q-10002',
        courseTitle: '数据结构高频题解',
        chapterTitle: '第1章：复杂度与栈队列',
        questioner: '学生-小李',
        questionText: '栈和队列在时间复杂度上有什么直观差别？',
        status: '已回复',
        createdAt: '2026-03-30 09:41',
        teacherAnswer:
          '栈与队列核心操作通常都可做到 O(1)，差异主要在先进后出与先进先出的约束语义。',
      },
      {
        id: 'q-10003',
        courseTitle: 'Python机器学习实战',
        chapterTitle: '第1章：监督学习基础',
        questioner: '学生-小张',
        questionText: '线性回归训练时怎么判断模型是否过拟合？',
        status: '待回复',
        createdAt: '2026-03-30 16:08',
        teacherAnswer: '',
      },
    ],
  })
}

/**
 * 获取 AI 摘要审核任务列表（当前保留 mock）。
 * @returns {Promise<Array<{id: string, targetType: string, targetTitle: string, status: string, updatedAt: string, publishedSummary: string, versions: Array<{versionNo: number, summary: string, updatedAt: string}>}>>}
 */
export function fetchAISummaryReviewTasks() {
  return ajax('/api/teacher/ai/summaries/tasks', {
    mockData: [
      {
        id: 's-20001',
        targetType: '章节',
        targetTitle: '第2章：分类与评估',
        status: '待审核',
        updatedAt: '2026-03-30 12:20',
        publishedSummary: '介绍了逻辑回归的基本思想与交叉熵损失的直观含义。',
        versions: [
          {
            versionNo: 1,
            summary: '（旧版本）逻辑回归与评估指标的概要。',
            updatedAt: '2026-03-28',
          },
        ],
      },
      {
        id: 's-20002',
        targetType: '课程',
        targetTitle: 'Python机器学习实战',
        status: '已发布',
        updatedAt: '2026-03-29 10:05',
        publishedSummary: '课程围绕监督学习与分类评估展开，包含训练、评估与常见实践要点。',
        versions: [
          {
            versionNo: 1,
            summary: '课程摘要初稿。',
            updatedAt: '2026-03-27',
          },
          {
            versionNo: 2,
            summary: '（当前版本）课程要点与学习路径。',
            updatedAt: '2026-03-29',
          },
        ],
      },
    ],
  })
}

function buildUploadFormData(payload) {
  const { file, courseId, title } = payload || {}
  if (!file) {
    throw new Error('请选择要上传的文件')
  }
  if (!courseId) {
    throw new Error('请选择课程')
  }
  if (!title || !String(title).trim()) {
    throw new Error('请输入资源标题')
  }
  const formData = new FormData()
  formData.append('file', file)
  formData.append('courseId', String(courseId))
  formData.append('title', String(title).trim())
  return formData
}
