<template>
  <div class="resources-page">
    <PageSection title="课程资源建设">
      <template #actions>
        <button
          class="primary-btn"
          type="button"
          @click="onTriggerAISummary"
        >
          生成章节摘要（AI）
        </button>
      </template>

      <DataTable
        :columns="columns"
        :rows="rows"
        :loading="loading"
      />
    </PageSection>

    <div class="resources-page__grid">
      <PageSection title="视频上传">
        <div class="upload-block">
          <div class="upload-block__row">
            <label class="field-label" for="course-select">课程</label>
            <select
              id="course-select"
              v-model="selectedCourseId"
              class="field-select"
            >
              <option value="c-1001">Python机器学习实战</option>
              <option value="c-1002">数据结构高频题解</option>
            </select>
          </div>

          <div class="upload-block__row">
            <label class="field-label" for="chapter-select">章节</label>
            <select
              id="chapter-select"
              v-model="selectedChapterId"
              class="field-select"
            >
              <option value="ch-1">第 1 章：监督学习基础</option>
              <option value="ch-2">第 2 章：分类与评估</option>
            </select>
          </div>

          <div class="upload-block__row">
            <input
              id="video-file"
              class="field-file"
              type="file"
              accept="video/*"
            />
          </div>

          <div class="upload-block__row upload-block__actions">
            <button
              class="primary-btn"
              type="button"
              :disabled="isUploading"
              @click="onStartUpload"
            >
              开始上传
            </button>
            <button
              class="ghost-btn"
              type="button"
              :disabled="!canPause"
              @click="onPauseUpload"
            >
              中断
            </button>
            <button
              class="ghost-btn"
              type="button"
              :disabled="!canRetry"
              @click="onRetryUpload"
            >
              继续/重试
            </button>
          </div>

          <div v-if="isUploading || uploadProgress > 0" class="upload-block__progress">
            <div class="upload-block__progress-bar">
              <i :style="{ width: `${uploadProgress}%` }" />
            </div>
            <div class="upload-block__progress-meta">
              <span>进度：{{ uploadProgress }}%</span>
              <span v-if="uploadError" class="upload-block__error">{{ uploadError }}</span>
            </div>
          </div>

          <p v-if="aiAssistError" class="note note--error">AI：{{ aiAssistError }}</p>
          <p v-if="aiAssistDraft" class="note">AI 草稿已生成，可在“AI 摘要审核与发布”中继续审核发布。</p>
        </div>
      </PageSection>

      <PageSection title="AI 交互辅助（草稿生成）">
        <div class="ai-assist">
          <label class="field-checkbox">
            <input v-model="simulateAiTimeout" type="checkbox" />
            <span>模拟 AI 接口超时（用于验证降级/重试）</span>
          </label>

          <div class="ai-assist__actions">
            <button
              class="ai-btn"
              type="button"
              :disabled="aiGenerating"
              @click="onGenerateAISummaryDraft"
            >
              {{ aiGenerating ? '生成中...' : '生成章节摘要草稿' }}
            </button>
            <button
              class="ghost-btn"
              type="button"
              :disabled="!aiAssistDraft"
              @click="onGoToReview"
            >
              去审核发布
            </button>
          </div>

          <div v-if="aiGenerating" class="skeleton">
            <div class="skeleton-line" />
            <div class="skeleton-line skeleton-line--short" />
            <div class="skeleton-line" />
          </div>

          <div v-if="aiAssistDraft" class="ai-assist__draft">
            <h4>草稿预览</h4>
            <pre>{{ aiAssistDraft }}</pre>
          </div>
        </div>
      </PageSection>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import DataTable from '@/components/common/DataTable.vue'
import PageSection from '@/components/common/PageSection.vue'
import { fetchCourseChapterResourceRows } from '@/api/teacherApi'

const router = useRouter()

const loading = ref(false)
const rows = ref([])
const columns = [
  { key: 'courseTitle', label: '课程' },
  { key: 'chapterTitle', label: '章节' },
  { key: 'resourceType', label: '资源类型' },
  { key: 'resourceTitle', label: '资源名称' },
  { key: 'status', label: '状态' },
  { key: 'updatedAt', label: '更新时间' },
]

onMounted(async () => {
  loading.value = true
  try {
    rows.value = await fetchCourseChapterResourceRows()
  } finally {
    loading.value = false
  }
})

// -------------------------
// 上传：断点/中断/重试（前端侧演示与错误处理）
// -------------------------
const selectedCourseId = ref('c-1001')
const selectedChapterId = ref('ch-1')

const isUploading = ref(false)
const uploadProgress = ref(0)
const uploadTimerRef = ref(/** @type {ReturnType<typeof setInterval> | null} */ (null))
const uploadError = ref('')
const canPause = ref(false)
const canRetry = ref(false)

const clearUploadTimer = () => {
  if (uploadTimerRef.value) {
    clearInterval(uploadTimerRef.value)
  }
  uploadTimerRef.value = null
}

const onStartUpload = () => {
  if (isUploading.value) return
  uploadError.value = ''
  canPause.value = true
  canRetry.value = false
  isUploading.value = true
  const startValue = uploadProgress.value || 0
  let progress = startValue

  clearUploadTimer()
  uploadTimerRef.value = setInterval(() => {
    // 每次推进一点进度，用于模拟“上传中断/失败重试”
    progress = Math.min(100, progress + 5)
    uploadProgress.value = progress

    // 模拟一次上传中断（可用于验证 UI 的恢复能力）
    if (progress >= 55 && progress < 60 && Math.random() < 0.35) {
      clearUploadTimer()
      isUploading.value = false
      canPause.value = false
      canRetry.value = true
      uploadError.value = '上传中断：网络波动导致请求失败，可点击“继续/重试”。'
    }

    if (progress >= 100) {
      clearUploadTimer()
      isUploading.value = false
      canPause.value = false
      canRetry.value = false
      uploadError.value = ''
      uploadProgress.value = 100
    }
  }, 260)
}

const onPauseUpload = () => {
  if (!isUploading.value) return
  clearUploadTimer()
  isUploading.value = false
  canPause.value = false
  canRetry.value = true
  uploadError.value = '已中断上传，可点击“继续/重试”。'
}

const onRetryUpload = () => {
  // 继续上传：从当前进度值开始推进（模拟断点续传）
  uploadError.value = ''
  onStartUpload()
}

// -------------------------
// AI：草稿生成与降级/重试提示
// -------------------------
const simulateAiTimeout = ref(false)
const aiGenerating = ref(false)
const aiAssistError = ref('')
const aiAssistDraft = ref('')

const mockGenerateAISummary = () => {
  // 这里用 Promise + 随机失败模拟“AI 接口超时”
  return new Promise((resolve, reject) => {
    const delay = 900
    setTimeout(() => {
      if (simulateAiTimeout.value || Math.random() < 0.15) {
        reject(new Error('AI接口超时：请稍后重试，或使用人工审核继续流程。'))
        return
      }
      resolve(
        `【${selectedCourseId.value} / ${selectedChapterId.value}】章节摘要草稿：\n` +
          '1) 关键概念：... \n' +
          '2) 核心方法：... \n' +
          '3) 学习要点：... \n' +
          '4) 常见问题：...'
      )
    }, delay)
  })
}

const onGenerateAISummaryDraft = async () => {
  aiGenerating.value = true
  aiAssistError.value = ''
  try {
    aiAssistDraft.value = await mockGenerateAISummary()
  } catch (err) {
    aiAssistDraft.value = ''
    aiAssistError.value = err instanceof Error ? err.message : '生成失败'
  } finally {
    aiGenerating.value = false
  }
}

const onTriggerAISummary = () => {
  onGenerateAISummaryDraft()
}

const onGoToReview = () => {
  router.push('/ai-summaries')
}
</script>

<style lang="scss" scoped>
.resources-page {
  display: flex;
  flex-direction: column;
  gap: $space-5;
}

.resources-page__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-5;

  @include down(md) {
    grid-template-columns: 1fr;
  }
}

.primary-btn {
  @include button-primary;
}

.ai-btn {
  border: 0;
  border-radius: $radius-md;
  color: $color-white;
  background: linear-gradient(135deg, $color-ai-theme, $color-accent);
  padding: $space-2 $space-4;
  cursor: pointer;
  font-weight: $font-weight-semibold;
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

.upload-block__row {
  display: flex;
  align-items: center;
  gap: $space-3;
  margin-bottom: $space-3;
}

.field-label {
  min-width: 52px;
  color: $color-text-secondary;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
}

.field-select {
  flex: 1;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-2 $space-3;
}

.field-file {
  width: 100%;
}

.upload-block__actions {
  margin-top: $space-2;
}

.upload-block__progress {
  margin-top: $space-4;
}

.upload-block__progress-bar {
  height: 12px;
  border-radius: $radius-pill;
  background: rgba($color-primary, 0.12);
  overflow: hidden;
}

.upload-block__progress-bar i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, $color-primary, $color-accent);
}

.upload-block__progress-meta {
  margin-top: $space-2;
  display: flex;
  justify-content: space-between;
  color: $color-text-secondary;
  font-size: $font-size-sm;
}

.upload-block__error {
  color: $color-error;
}

.note {
  margin: 0;
  color: $color-text-secondary;
  font-size: $font-size-sm;
  line-height: 1.5;
}

.note--error {
  color: $color-error;
}

.upload-block__actions .ghost-btn:disabled,
.upload-block__actions .primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.field-checkbox {
  display: flex;
  align-items: center;
  gap: $space-2;
  color: $color-text-secondary;
  font-size: $font-size-sm;
}

.ai-assist__actions {
  margin-top: $space-4;
  display: flex;
  gap: $space-3;
}

.ai-assist__draft {
  margin-top: $space-4;
  padding: $space-3;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  background: rgba($color-ai-theme, 0.06);
}

.ai-assist__draft h4 {
  margin: 0 0 $space-2;
  color: $color-text-secondary;
}

.ai-assist__draft pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.skeleton {
  margin-top: $space-4;
}

.skeleton-line {
  height: 12px;
  border-radius: $radius-pill;
  background: rgba($color-primary, 0.12);
  margin-bottom: $space-2;
}

.skeleton-line--short {
  width: 60%;
}
</style>

