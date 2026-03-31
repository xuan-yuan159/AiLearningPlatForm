import { shallowMount } from '@vue/test-utils'
import ClassManagementView from '@/views/class/ClassManagementView.vue'
import CourseManagementView from '@/views/course/CourseManagementView.vue'
import DashboardView from '@/views/dashboard/DashboardView.vue'
import HomeworkReviewView from '@/views/homework/HomeworkReviewView.vue'
import NoticeAnnouncementView from '@/views/notice/NoticeAnnouncementView.vue'
import ProfileCenterView from '@/views/profile/ProfileCenterView.vue'
import ScoreStatisticsView from '@/views/score/ScoreStatisticsView.vue'
import StudentManagementView from '@/views/student/StudentManagementView.vue'

jest.mock('@/api/teacherApi', () => ({
  fetchCourseList: jest.fn().mockResolvedValue([]),
  fetchDashboardMetrics: jest.fn().mockResolvedValue([]),
}))

describe('views render', () => {
  test('DashboardView 可正常挂载', () => {
    const wrapper = shallowMount(DashboardView, {
      global: { stubs: { PageSection: true } },
    })
    expect(wrapper.exists()).toBe(true)
  })

  test('CourseManagementView 可正常挂载', () => {
    const wrapper = shallowMount(CourseManagementView, {
      global: { stubs: { PageSection: true, DataTable: true } },
    })
    expect(wrapper.exists()).toBe(true)
  })

  test('其余核心页面可正常挂载', () => {
    const pages = [
      ClassManagementView,
      HomeworkReviewView,
      ScoreStatisticsView,
      StudentManagementView,
      NoticeAnnouncementView,
      ProfileCenterView,
    ]
    pages.forEach((Page) => {
      const wrapper = shallowMount(Page, {
        global: { stubs: { PageSection: true, DataTable: true, StatusTag: true } },
      })
      expect(wrapper.exists()).toBe(true)
    })
  })
})
