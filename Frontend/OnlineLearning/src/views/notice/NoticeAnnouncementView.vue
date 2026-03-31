<template>
  <PageSection title="通知公告">
    <template #actions>
      <button class="primary-btn" type="button">发布公告</button>
    </template>
    <div class="notice-list">
      <article
        v-for="item in notices"
        :key="item.id"
        class="notice-card"
      >
        <header>
          <h4>{{ item.title }}</h4>
          <StatusTag :text="item.status" :type="item.statusType" />
        </header>
        <p>{{ item.summary }}</p>
        <footer>发布时间：{{ item.publishAt }} · 阅读率：{{ item.readRate }}</footer>
      </article>
    </div>
  </PageSection>
</template>

<script setup>
import PageSection from '@/components/common/PageSection.vue'
import StatusTag from '@/components/common/StatusTag.vue'

/**
 * 通知公告页面，维护公告发布与阅读状态。
 */
const notices = [
  {
    id: 'n-1',
    title: '清明假期课程安排',
    summary: '4 月 4 日至 4 月 6 日直播暂停，录播学习与答疑正常进行。',
    publishAt: '2026-03-30',
    readRate: '92%',
    status: '已发布',
    statusType: 'success',
  },
  {
    id: 'n-2',
    title: '期中测试说明',
    summary: '请各班级在 4 月 10 日前完成线上期中测试并提交错题总结。',
    publishAt: '2026-03-28',
    readRate: '68%',
    status: '草稿',
    statusType: 'warning',
  },
]
</script>

<style lang="scss" scoped>
.primary-btn {
  @include button-primary;
}

.notice-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: $space-4;

  @include down(md) {
    grid-template-columns: 1fr;
  }
}

.notice-card {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-4;

  header {
    @include flex-between;
    margin-bottom: $space-2;
  }

  h4 {
    margin: 0;
  }

  p {
    margin: 0 0 $space-3;
    color: $color-text-secondary;
  }

  footer {
    color: $color-text-secondary;
    font-size: $font-size-xs;
  }
}
</style>
