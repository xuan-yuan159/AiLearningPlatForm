<template>
  <div class="dashboard">
    <section class="metric-grid">
      <article
        v-for="metric in metrics"
        :key="metric.key"
        class="metric-card"
      >
        <h3>{{ metric.label }}</h3>
        <p class="metric-card__value">{{ metric.value }}</p>
        <span class="metric-card__trend">{{ metric.trend }}</span>
      </article>
    </section>

    <PageSection title="关键提醒">
      <ul class="dashboard__notice-list">
        <li>课程摘要审核中：待处理 18 项，请优先完成待审核摘要。</li>
        <li>在线问答待处理：27 条尚未回复，建议优先跟进高频问题。</li>
        <li>本周学生提问响应时间平均 17 分钟，较上周下降 12%。</li>
      </ul>
    </PageSection>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import PageSection from '@/components/common/PageSection.vue'
import { fetchDashboardMetrics } from '@/api/teacherApi'

/**
 * 仪表盘页面，展示全局教学指标与提醒信息。
 */
const metrics = ref([])

/**
 * 加载仪表盘指标数据。
 * @returns {Promise<void>}
 */
const loadMetrics = async () => {
  metrics.value = await fetchDashboardMetrics()
}

onMounted(loadMetrics)
</script>

<style lang="scss" scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: $space-4;

  @include down(xl) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  @include down(sm) {
    grid-template-columns: 1fr;
  }
}

.metric-card {
  @include card;

  h3 {
    margin: 0 0 $space-2;
    color: $color-text-secondary;
    font-size: $font-size-sm;
  }
}

.metric-card__value {
  margin: 0;
  font-size: $font-size-xxl;
  font-weight: $font-weight-semibold;
}

.metric-card__trend {
  color: $color-success;
  font-size: $font-size-sm;
}

.dashboard__notice-list {
  margin: 0;
  padding-left: $space-5;
  color: $color-text-secondary;
  display: flex;
  flex-direction: column;
  gap: $space-2;
}
</style>
