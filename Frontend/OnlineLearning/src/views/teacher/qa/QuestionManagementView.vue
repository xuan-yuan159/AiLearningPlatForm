<template>
  <div class="qa-page">
    <PageSection title="在线问答管理">
      <div class="qa-layout">
        <div class="qa-layout__list">
          <div class="qa-toolbar">
            <label class="field-label" for="status-filter">状态</label>
            <select
              id="status-filter"
              v-model="statusFilter"
              class="field-select"
            >
              <option value="">全部</option>
              <option value="待回复">待回复</option>
              <option value="已回复">已回复</option>
            </select>
          </div>

          <div class="qa-table-wrap">
            <table class="qa-table">
              <thead>
                <tr>
                  <th>课程</th>
                  <th>章节</th>
                  <th>提问</th>
                  <th>状态</th>
                  <th>时间</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="row in filteredRows"
                  :key="row.id"
                  class="qa-row"
                  :class="{ 'qa-row--active': row.id === activeId }"
                  @click="onSelect(row.id)"
                >
                  <td>{{ row.courseTitle }}</td>
                  <td>{{ row.chapterTitle }}</td>
                  <td class="qa-table__cell-question">{{ row.questionText }}</td>
                  <td>
                    <StatusTag :text="row.status" :type="row.status === '已回复' ? 'success' : 'warning'" />
                  </td>
                  <td class="qa-table__cell-time">{{ row.createdAt }}</td>
                </tr>

                <tr v-if="filteredRows.length === 0">
                  <td colspan="5" class="qa-table__empty">暂无数据</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="qa-layout__detail">
          <template v-if="activeQuestion">
            <div class="qa-detail__top">
              <h3 class="qa-detail__title">问题详情</h3>
              <p class="qa-detail__meta">
                <span>提问者：{{ activeQuestion.questioner }}</span>
              </p>
            </div>

            <div class="qa-detail__card">
              <div class="qa-detail__label">问题</div>
              <pre class="qa-detail__pre">{{ activeQuestion.questionText }}</pre>
            </div>

            <div class="qa-detail__grid">
              <div class="qa-detail__card">
                <div class="qa-detail__label">AI 生成草稿（人工审核）</div>
                <div class="qa-detail__actions">
                  <button
                    class="ai-btn"
                    type="button"
                    :disabled="aiStatus === 'loading'"
                    @click="onGenerateAIDraft"
                  >
                    {{ aiStatus === 'loading' ? '生成中...' : '生成 AI 草稿' }}
                  </button>
                  <button
                    class="ghost-btn"
                    type="button"
                    :disabled="!aiDraft || aiStatus === 'loading'"
                    @click="onAdoptDraft"
                  >
                    一键采纳
                  </button>
                </div>

                <p v-if="aiError" class="note note--error">{{ aiError }}</p>

                <pre v-if="aiDraft" class="qa-detail__pre">{{ aiDraft }}</pre>
                <p v-else class="note">点击“生成 AI 草稿”，进入“AI 生成 -> 人工审核 -> 发布”的流程。</p>
              </div>

              <div class="qa-detail__card">
                <div class="qa-detail__label">教师确认并发布</div>
                <textarea
                  v-model="teacherReply"
                  class="qa-textarea"
                  rows="10"
                  placeholder="在此编辑/确认回复内容"
                ></textarea>

                <div class="qa-detail__actions qa-detail__actions--bottom">
                  <button
                    class="primary-btn"
                    type="button"
                    :disabled="activeQuestion.status === '已回复' || !teacherReply.trim()"
                    @click="onPublishReply"
                  >
                    发布
                  </button>
                  <button
                    class="ghost-btn"
                    type="button"
                    :disabled="activeQuestion.status !== '已回复'"
                    @click="onResetToPublished"
                  >
                    还原到已发布
                  </button>
                </div>

                <div class="qa-diff">
                  <div class="qa-diff__label">差异对比（草稿 vs 教师回复）</div>
                  <div class="qa-diff__grid">
                    <div class="qa-diff__col">
                      <div class="qa-diff__col-title">AI 草稿</div>
                      <pre class="qa-detail__pre qa-detail__pre--diff">{{ aiDraft || '—' }}</pre>
                    </div>
                    <div class="qa-diff__col">
                      <div class="qa-diff__col-title">教师回复</div>
                      <pre class="qa-detail__pre qa-detail__pre--diff">{{ teacherReply || '—' }}</pre>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <template v-else>
            <div class="qa-empty">
              <p>请选择一条问答记录进行处理。</p>
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
import { fetchTeacherQuestionRows } from '@/api/teacherApi'

const questionRows = ref([])
const activeId = ref('')
const statusFilter = ref('')

const filteredRows = computed(() => {
  if (!statusFilter.value) return questionRows.value
  return questionRows.value.filter((row) => row.status === statusFilter.value)
})

const activeQuestion = computed(() => questionRows.value.find((q) => q.id === activeId.value) || null)

const teacherReply = ref('')
const aiDraft = ref('')
const aiStatus = ref('idle') // idle | loading | error | ready
const aiError = ref('')

const syncDetailFromActive = () => {
  const q = activeQuestion.value
  teacherReply.value = q?.teacherAnswer || ''
  aiDraft.value = ''
  aiStatus.value = 'idle'
  aiError.value = ''
}

onMounted(async () => {
  questionRows.value = await fetchTeacherQuestionRows()
  if (questionRows.value.length > 0) {
    activeId.value = questionRows.value[0].id
    syncDetailFromActive()
  }
})

const onSelect = (id) => {
  activeId.value = id
  syncDetailFromActive()
}

const mockGenerateAIDraft = async () => {
  // 模拟 AI 接口超时：失败后需要可重试与降级提示
  await new Promise((r) => setTimeout(r, 900))
  if (Math.random() < 0.25) {
    throw new Error('AI接口超时：请稍后重试，或使用人工回复继续发布。')
  }
  const q = activeQuestion.value
  return `AI草稿建议：\n` + (q ? `围绕“${q.questionText}”给出结构化回答：\n` : '') +
    `- 要点1：... \n- 要点2：... \n- 示例：... \n- 总结：...`
}

const onGenerateAIDraft = async () => {
  if (!activeQuestion.value) return
  aiStatus.value = 'loading'
  aiError.value = ''
  aiDraft.value = ''
  try {
    aiDraft.value = await mockGenerateAIDraft()
    aiStatus.value = 'ready'
  } catch (err) {
    aiStatus.value = 'error'
    aiDraft.value = ''
    aiError.value = err instanceof Error ? err.message : '生成失败'
  }
}

const onAdoptDraft = () => {
  teacherReply.value = aiDraft.value
}

const onPublishReply = () => {
  if (!activeQuestion.value) return
  const idx = questionRows.value.findIndex((q) => q.id === activeQuestion.value.id)
  if (idx === -1) return

  const updated = {
    ...questionRows.value[idx],
    teacherAnswer: teacherReply.value,
    status: '已回复',
  }
  questionRows.value.splice(idx, 1, updated)
}

const onResetToPublished = () => {
  syncDetailFromActive()
}
</script>

<style lang="scss" scoped>
.qa-page {
  display: flex;
  flex-direction: column;
  gap: $space-5;
}

.qa-layout {
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: $space-5;

  @include down(md) {
    grid-template-columns: 1fr;
  }
}

.qa-toolbar {
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

.qa-table-wrap {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  overflow: hidden;
}

.qa-table {
  width: 100%;
  border-collapse: collapse;
}

.qa-table th,
.qa-table td {
  padding: $space-3;
  border-bottom: 1px solid $color-border;
  text-align: left;
  font-size: $font-size-sm;
}

.qa-table th {
  color: $color-text-secondary;
  background: rgba($color-primary, 0.04);
}

.qa-row {
  cursor: pointer;
}

.qa-row:hover {
  background: rgba($color-primary, 0.05);
}

.qa-row--active {
  background: rgba($color-primary, 0.10);
}

.qa-table__cell-question {
  max-width: 320px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.qa-table__cell-time {
  white-space: nowrap;
}

.qa-table__empty {
  text-align: center;
  color: $color-text-secondary;
  padding: $space-5;
}

.qa-layout__detail {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.qa-detail__top {
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

.qa-detail__title {
  margin: 0;
  font-size: $font-size-lg;
}

.qa-detail__meta {
  margin: 0;
  color: $color-text-secondary;
  font-size: $font-size-sm;
}

.qa-detail__card {
  @include card;
}

.qa-detail__label {
  color: $color-text-secondary;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  margin-bottom: $space-3;
}

.qa-detail__pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.55;
}

.qa-detail__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-4;

  @include down(sm) {
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

.qa-detail__actions {
  margin-bottom: $space-3;
  display: flex;
  gap: $space-3;
  flex-wrap: wrap;
}

.qa-detail__actions--bottom {
  margin-top: $space-3;
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

.qa-textarea {
  width: 100%;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-3;
  resize: vertical;
  font-family: inherit;
  font-size: $font-size-sm;
}

.qa-diff {
  margin-top: $space-4;
}

.qa-diff__label {
  color: $color-text-secondary;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  margin-bottom: $space-3;
}

.qa-diff__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-3;
}

.qa-diff__col {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-3;
  background: rgba($color-primary, 0.04);
}

.qa-diff__col-title {
  margin-bottom: $space-2;
  font-size: $font-size-sm;
  color: $color-text-secondary;
  font-weight: $font-weight-semibold;
}

.qa-detail__pre--diff {
  max-height: 220px;
  overflow: auto;
}

.qa-empty {
  @include centered-page;
}
</style>

