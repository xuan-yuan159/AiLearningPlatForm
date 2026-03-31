<template>
  <div class="ai-review-page">
    <PageSection title="AI 摘要审核与发布">
      <div class="ai-review-layout">
        <div class="ai-review-layout__list">
          <div class="ai-review-table-wrap">
            <table class="ai-review-table">
              <thead>
                <tr>
                  <th>对象</th>
                  <th>标题</th>
                  <th>状态</th>
                  <th>更新时间</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="task in tasks"
                  :key="task.id"
                  class="ai-review-row"
                  :class="{ 'ai-review-row--active': task.id === activeId }"
                  @click="onSelect(task.id)"
                >
                  <td>{{ task.targetType }}</td>
                  <td class="ai-review-table__cell-title">{{ task.targetTitle }}</td>
                  <td>
                    <StatusTag :text="task.status" :type="task.status === '已发布' ? 'success' : 'warning'" />
                  </td>
                  <td class="ai-review-table__cell-time">{{ task.updatedAt }}</td>
                </tr>

                <tr v-if="tasks.length === 0">
                  <td colspan="4" class="ai-review-table__empty">暂无数据</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="ai-review-layout__detail">
          <template v-if="activeTask">
            <h3 class="ai-review-detail__title">
              {{ activeTask.targetType }}：{{ activeTask.targetTitle }}
            </h3>

            <div class="ai-review-detail__grid">
              <div class="ai-review-card">
                <div class="ai-review-card__label">版本/结果切换</div>
                <div class="ai-review-card__field">
                  <label class="field-label" for="version-select">版本</label>
                  <select
                    id="version-select"
                    v-model="selectedVersionNo"
                    class="field-select"
                  >
                    <option
                      v-for="v in versionOptions"
                      :key="v.versionNo"
                      :value="v.versionNo"
                    >
                      v{{ v.versionNo }} - {{ v.updatedAt }}
                    </option>
                  </select>
                </div>

                <div class="ai-review-card__summary">
                  <div class="ai-review-card__summary-title">当前对比内容</div>
                  <pre class="ai-review-pre">{{ selectedPublishedSummary }}</pre>
                </div>
              </div>

              <div class="ai-review-card">
                <div class="ai-review-card__label">AI 生成 - 人工审核 - 发布</div>
                <div class="ai-review-card__actions">
                  <label class="field-checkbox">
                    <input v-model="simulateAiTimeout" type="checkbox" />
                    <span>模拟 AI 接口超时（验证降级与可重试）</span>
                  </label>

                  <div class="ai-review-card__actions-row">
                    <button
                      class="ai-btn"
                      type="button"
                      :disabled="aiStatus === 'loading'"
                      @click="onGenerateDraft"
                    >
                      {{ aiStatus === 'loading' ? '生成中...' : '生成摘要草稿' }}
                    </button>
                    <button
                      class="ghost-btn"
                      type="button"
                      :disabled="!aiDraft || aiStatus === 'loading'"
                      @click="onPublish"
                    >
                      教师确认并发布
                    </button>
                  </div>
                </div>

                <p v-if="aiError" class="note note--error">{{ aiError }}</p>
                <p v-else-if="aiDraft" class="note">已生成草稿，可直接发布（或先对比确认）。</p>

                <div v-if="aiDraft" class="ai-review-compare">
                  <div class="ai-review-compare__label">差异对比（发布前）</div>
                  <div class="ai-review-compare__grid">
                    <div class="ai-review-compare__col">
                      <div class="ai-review-compare__col-title">对比内容</div>
                      <pre class="ai-review-pre ai-review-pre--diff">{{ selectedPublishedSummary }}</pre>
                    </div>
                    <div class="ai-review-compare__col">
                      <div class="ai-review-compare__col-title">AI 草稿</div>
                      <pre class="ai-review-pre ai-review-pre--diff">{{ aiDraft }}</pre>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <template v-else>
            <div class="ai-review-empty">
              <p>请选择一条 AI 摘要审核任务。</p>
            </div>
          </template>
        </div>
      </div>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import PageSection from '@/components/common/PageSection.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { fetchAISummaryReviewTasks } from '@/api/teacherApi'

const tasks = ref([])
const activeId = ref('')

const aiStatus = ref('idle') // idle | loading | error | ready
const aiDraft = ref('')
const aiError = ref('')
const simulateAiTimeout = ref(false)

const selectedVersionNo = ref(1)

onMounted(async () => {
  tasks.value = await fetchAISummaryReviewTasks()
  if (tasks.value.length > 0) {
    activeId.value = tasks.value[0].id
    // 默认选中第一个版本
    selectedVersionNo.value = tasks.value[0].versions[0]?.versionNo || 1
  }
})

const activeTask = computed(() => tasks.value.find((t) => t.id === activeId.value) || null)

const versionOptions = computed(() => {
  if (!activeTask.value) return []
  return activeTask.value.versions.map((v) => ({ ...v }))
})

const selectedPublishedSummary = computed(() => {
  if (!activeTask.value) return ''
  const found = activeTask.value.versions.find((v) => v.versionNo === selectedVersionNo.value)
  return found?.summary || activeTask.value.publishedSummary || ''
})

const resetAIState = () => {
  aiStatus.value = 'idle'
  aiDraft.value = ''
  aiError.value = ''
}

const onSelect = (id) => {
  activeId.value = id
  resetAIState()
  const t = tasks.value.find((x) => x.id === id)
  selectedVersionNo.value = t?.versions[0]?.versionNo || 1
}

const mockGenerateDraft = async () => {
  await new Promise((r) => setTimeout(r, 1000))
  if (simulateAiTimeout.value || Math.random() < 0.2) {
    throw new Error('AI接口超时：请稍后重试，或继续人工审核流程。')
  }

  if (!activeTask.value) return ''
  return `AI摘要草稿：\n` +
    `- 对象：${activeTask.value.targetType}：${activeTask.value.targetTitle}\n` +
    `- 覆盖要点：... \n- 学习路径：... \n- 常见问题：... \n` +
    `（生成于 ${new Date().toLocaleString()}）`
}

const onGenerateDraft = async () => {
  if (!activeTask.value) return
  aiStatus.value = 'loading'
  aiError.value = ''
  aiDraft.value = ''
  try {
    aiDraft.value = await mockGenerateDraft()
    aiStatus.value = 'ready'
  } catch (err) {
    aiStatus.value = 'error'
    aiDraft.value = ''
    aiError.value = err instanceof Error ? err.message : '生成失败'
  }
}

const onPublish = () => {
  if (!activeTask.value || !aiDraft.value.trim()) return
  const idx = tasks.value.findIndex((t) => t.id === activeTask.value.id)
  if (idx === -1) return

  const nextVersionNo =
    (tasks.value[idx].versions[tasks.value[idx].versions.length - 1]?.versionNo || 0) + 1

  const nextVersion = {
    versionNo: nextVersionNo,
    summary: aiDraft.value,
    updatedAt: new Date().toLocaleDateString(),
  }

  tasks.value.splice(idx, 1, {
    ...tasks.value[idx],
    publishedSummary: aiDraft.value,
    versions: [...tasks.value[idx].versions, nextVersion],
    status: '已发布',
    updatedAt: new Date().toLocaleString(),
  })

  aiDraft.value = ''
  resetAIState()
  selectedVersionNo.value = nextVersionNo
}
</script>

<style lang="scss" scoped>
.ai-review-page {
  display: flex;
  flex-direction: column;
  gap: $space-5;
}

.ai-review-layout {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: $space-5;

  @include down(md) {
    grid-template-columns: 1fr;
  }
}

.ai-review-table-wrap {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  overflow: hidden;
}

.ai-review-table {
  width: 100%;
  border-collapse: collapse;
}

.ai-review-table th,
.ai-review-table td {
  padding: $space-3;
  border-bottom: 1px solid $color-border;
  text-align: left;
  font-size: $font-size-sm;
}

.ai-review-table th {
  color: $color-text-secondary;
  background: rgba($color-primary, 0.04);
}

.ai-review-row {
  cursor: pointer;
}

.ai-review-row:hover {
  background: rgba($color-primary, 0.05);
}

.ai-review-row--active {
  background: rgba($color-primary, 0.10);
}

.ai-review-table__cell-title {
  max-width: 320px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ai-review-table__cell-time {
  white-space: nowrap;
}

.ai-review-table__empty {
  text-align: center;
  color: $color-text-secondary;
  padding: $space-5;
}

.ai-review-layout__detail {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.ai-review-detail__title {
  margin: 0;
  font-size: $font-size-lg;
}

.ai-review-detail__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-4;

  @include down(sm) {
    grid-template-columns: 1fr;
  }
}

.ai-review-card {
  @include card;
}

.ai-review-card__label {
  margin-bottom: $space-3;
  color: $color-text-secondary;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
}

.ai-review-card__field {
  display: flex;
  align-items: center;
  gap: $space-3;
  margin-bottom: $space-4;
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

.ai-review-card__summary-title {
  margin-bottom: $space-2;
  color: $color-text-secondary;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
}

.ai-review-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.55;
}

.ai-review-pre--diff {
  max-height: 210px;
  overflow: auto;
}

.ai-review-card__actions {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.field-checkbox {
  display: flex;
  align-items: center;
  gap: $space-2;
  color: $color-text-secondary;
  font-size: $font-size-sm;
}

.ai-review-card__actions-row {
  display: flex;
  gap: $space-3;
  flex-wrap: wrap;
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

.note {
  margin: 0;
  color: $color-text-secondary;
  font-size: $font-size-sm;
  line-height: 1.5;
}

.note--error {
  color: $color-error;
}

.ai-review-compare {
  margin-top: $space-3;
}

.ai-review-compare__label {
  margin-bottom: $space-3;
  color: $color-text-secondary;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
}

.ai-review-compare__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-3;
}

.ai-review-compare__col {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-3;
  background: rgba($color-primary, 0.04);
}

.ai-review-compare__col-title {
  margin-bottom: $space-2;
  font-size: $font-size-sm;
  color: $color-text-secondary;
  font-weight: $font-weight-semibold;
}

.ai-review-empty {
  @include centered-page;
}
</style>

