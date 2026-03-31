import { request } from '@/api/httpClient'

/**
 * 获取仪表盘统计信息。
 * @returns {Promise<Array<{key: string, label: string, value: string|number, trend: string}>>}
 */
export function fetchDashboardMetrics() {
  return request('/api/teacher/dashboard', {
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
 * 获取课程列表。
 * @returns {Promise<Array<{id: string, title: string, category: string, status: string, students: number, updatedAt: string}>>}
 */
export function fetchCourseList() {
  return request('/api/teacher/courses', {
    mockData: [
      {
        id: 'c-1001',
        title: 'Python机器学习实战',
        category: '人工智能',
        status: '已发布',
        students: 420,
        updatedAt: '2026-03-30',
      },
      {
        id: 'c-1002',
        title: '数据结构高频题解',
        category: '算法',
        status: '草稿',
        students: 188,
        updatedAt: '2026-03-29',
      },
    ],
  })
}

/**
 * 获取课程章节与资源概览，用于“课程资源建设”页面。
 * @returns {Promise<Array<{courseTitle: string, chapterTitle: string, resourceType: string, resourceTitle: string, status: string, updatedAt: string}>>}
 */
export function fetchCourseChapterResourceRows() {
  return request('/api/teacher/resources/overview', {
    mockData: [
      {
        courseTitle: 'Python机器学习实战',
        chapterTitle: '第 1 章：监督学习基础',
        resourceType: '视频',
        resourceTitle: '01-线性回归讲解',
        status: '已上传',
        updatedAt: '2026-03-30',
      },
      {
        courseTitle: 'Python机器学习实战',
        chapterTitle: '第 2 章：分类与评估',
        resourceType: '视频',
        resourceTitle: '02-逻辑回归与评估',
        status: '待完善',
        updatedAt: '2026-03-29',
      },
      {
        courseTitle: '数据结构高频题解',
        chapterTitle: '第 1 章：复杂度与栈队列',
        resourceType: '资料',
        resourceTitle: '讲义-PDF',
        status: '已上传',
        updatedAt: '2026-03-28',
      },
    ],
  })
}

/**
 * 获取教师侧问答列表，用于“在线问答管理”页面。
 * @returns {Promise<Array<{id: string, courseTitle: string, chapterTitle: string, questioner: string, questionText: string, status: string, createdAt: string}>>}
 */
export function fetchTeacherQuestionRows() {
  return request('/api/teacher/questions', {
    mockData: [
      {
        id: 'q-10001',
        courseTitle: 'Python机器学习实战',
        chapterTitle: '第 2 章：分类与评估',
        questioner: '学生-小王',
        questionText: '逻辑回归的损失函数为什么通常用交叉熵？',
        status: '待回复',
        createdAt: '2026-03-29 21:12',
        teacherAnswer: '',
      },
      {
        id: 'q-10002',
        courseTitle: '数据结构高频题解',
        chapterTitle: '第 1 章：复杂度与栈队列',
        questioner: '学生-小李',
        questionText: '栈和队列在时间复杂度上有什么直观差别？',
        status: '已回复',
        createdAt: '2026-03-30 09:41',
        teacherAnswer: '栈与队列的时间复杂度通常体现在“入栈/出栈”和“入队/出队”。在顺序存储结构下，两者对基本操作的复杂度相近（通常为 O(1)），差异主要来自约束条件与边界处理。',
      },
      {
        id: 'q-10003',
        courseTitle: 'Python机器学习实战',
        chapterTitle: '第 1 章：监督学习基础',
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
 * 获取 AI 摘要审核任务列表，用于“AI 摘要审核与发布”页面。
 * @returns {Promise<Array<{id: string, targetType: string, targetTitle: string, status: string, updatedAt: string, publishedSummary: string, versions: Array<{versionNo: number, summary: string, updatedAt: string}>}>>}
 */
export function fetchAISummaryReviewTasks() {
  return request('/api/teacher/ai/summaries/tasks', {
    mockData: [
      {
        id: 's-20001',
        targetType: '章节',
        targetTitle: '第 2 章：分类与评估',
        status: '待审核',
        updatedAt: '2026-03-30 12:20',
        publishedSummary: '介绍了逻辑回归的基本思想与交叉熵损失的直观含义...',
        versions: [
          {
            versionNo: 1,
            summary: '（旧版本）逻辑回归与评估指标的概要...',
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
        publishedSummary: '课程围绕监督学习与分类评估展开，包含训练、评估与常见实践要点...',
        versions: [
          {
            versionNo: 1,
            summary: '课程摘要初稿...',
            updatedAt: '2026-03-27',
          },
          {
            versionNo: 2,
            summary: '（当前版本）课程要点与学习路径...',
            updatedAt: '2026-03-29',
          },
        ],
      },
    ],
  })
}
