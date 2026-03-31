<template>
  <PageSection title="成绩统计">
    <div class="score-list">
      <div
        v-for="item in scoreRanges"
        :key="item.label"
        class="score-row"
      >
        <span>{{ item.label }}</span>
        <div class="score-row__bar">
          <i :style="{ width: `${item.percent}%` }"></i>
        </div>
        <strong>{{ item.count }}人</strong>
      </div>
    </div>
  </PageSection>
</template>

<script setup>
import PageSection from '@/components/common/PageSection.vue'

/**
 * 成绩统计页面，展示分数区间分布数据。
 */
const scoreRanges = [
  { label: '90-100', count: 42, percent: 76 },
  { label: '80-89', count: 35, percent: 62 },
  { label: '70-79', count: 26, percent: 48 },
  { label: '60-69', count: 13, percent: 28 },
  { label: '<60', count: 6, percent: 14 },
]
</script>

<style lang="scss" scoped>
.score-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.score-row {
  display: grid;
  grid-template-columns: 90px 1fr 56px;
  align-items: center;
  gap: $space-3;
}

.score-row__bar {
  height: 10px;
  background: rgba($color-primary, 0.12);
  border-radius: $radius-pill;
  overflow: hidden;

  i {
    display: block;
    height: 100%;
    background: linear-gradient(90deg, $color-primary, $color-accent);
  }
}
</style>
