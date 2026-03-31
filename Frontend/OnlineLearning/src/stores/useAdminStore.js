import { computed, ref } from 'vue'

const dashboardMetrics = ref([
  { key: 'activeCourses', label: '进行中课程', value: 18, trend: '+2' },
  { key: 'students', label: '学习学生数', value: 1260, trend: '+9%' },
  { key: 'pendingSummaries', label: '待审核摘要', value: 18, trend: '-3' },
  { key: 'pendingQuestions', label: '待处理问答', value: 27, trend: '-5' },
  { key: 'noticeReadRate', label: '公告阅读率', value: '87%', trend: '+3%' },
])

const loading = ref(false)
const uiState = ref({
  currentSemester: '2026 春季学期',
  selectedCourseId: 'c-1001',
})

/**
 * 教师后台业务状态仓库。
 * @returns {{
 *  dashboardMetrics: import('vue').Ref<Array<{key: string, label: string, value: string|number, trend: string}>>,
 *  loading: import('vue').Ref<boolean>,
 *  uiState: import('vue').Ref<{currentSemester: string, selectedCourseId: string}>,
 *  completedRate: import('vue').ComputedRef<string>,
 *  setLoading: (value: boolean) => void,
 *  updateUIState: (payload: Partial<{currentSemester: string, selectedCourseId: string}>) => void
 * }}
 */
export function useAdminStore() {
  const completedRate = computed(() => {
    const pending = Number(
      dashboardMetrics.value.find((item) => item.key === 'pendingSummaries')?.value || 0,
    )
    const base = pending > 100 ? 100 : 100 - pending
    return `${base}%`
  })

  /**
   * 设置页面加载状态。
   * @param {boolean} value 载入标记
   * @returns {void}
   */
  const setLoading = (value) => {
    loading.value = value
  }

  /**
   * 更新全局筛选条件。
   * @param {Partial<{currentSemester: string, selectedCourseId: string}>} payload 状态变更
   * @returns {void}
   */
  const updateUIState = (payload) => {
    uiState.value = { ...uiState.value, ...payload }
  }

  return { dashboardMetrics, loading, uiState, completedRate, setLoading, updateUIState }
}
