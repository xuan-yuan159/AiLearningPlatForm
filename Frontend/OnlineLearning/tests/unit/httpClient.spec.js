import { request } from '@/api/httpClient'
import {
  fetchAISummaryReviewTasks,
  fetchCourseChapterResourceRows,
  fetchCourseList,
  fetchDashboardMetrics,
  fetchTeacherQuestionRows,
} from '@/api/teacherApi'

describe('http client', () => {
  test('mockData 模式直接返回数据', async () => {
    const result = await request('/mock', { mockData: { ok: true }, mockDelay: 0 })
    expect(result.ok).toBe(true)
  })

  test('真实 fetch 请求失败会抛错', async () => {
    global.fetch = jest.fn().mockResolvedValue({
      ok: false,
      status: 500,
      headers: { get: () => 'application/json' },
    })
    await expect(request('/error')).rejects.toThrow('请求失败：500')
  })

  test('真实 fetch 请求返回 json', async () => {
    global.fetch = jest.fn().mockResolvedValue({
      ok: true,
      headers: { get: () => 'application/json' },
      json: () => Promise.resolve({ success: true }),
    })
    const result = await request('/json')
    expect(result.success).toBe(true)
  })

  test('teacherApi 返回课程与仪表盘数据', async () => {
    const courseList = await fetchCourseList()
    const metrics = await fetchDashboardMetrics()
    expect(courseList.length).toBeGreaterThan(0)
    expect(metrics.length).toBeGreaterThan(0)
  })

  test('teacherApi 返回资源/问答/AI摘要任务数据', async () => {
    const resourceRows = await fetchCourseChapterResourceRows()
    const questionRows = await fetchTeacherQuestionRows()
    const tasks = await fetchAISummaryReviewTasks()

    expect(resourceRows.length).toBeGreaterThan(0)
    expect(questionRows.length).toBeGreaterThan(0)
    expect(tasks.length).toBeGreaterThan(0)
  })
})
