<template>
  <PageSection title="高保真视觉稿">
    <div class="mockup">
      <div class="mockup__sidebar"></div>
      <div class="mockup__content">
        <div class="mockup__header"></div>
        <div class="mockup__cards">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <div class="mockup__table"></div>
      </div>
    </div>
    <p class="link-text">
      视觉稿链接：
      <a
        href="https://www.figma.com/community/file/1388391708970375036"
        target="_blank"
        rel="noreferrer"
      >
        Figma 高保真参考稿
      </a>
    </p>
  </PageSection>
</template>

<script setup>
import PageSection from '@/components/common/PageSection.vue'

/**
 * 高保真视觉稿展示页，表达主题色与组件层级。
 */
</script>

<style lang="scss" scoped>
.mockup {
  height: 360px;
  border-radius: $radius-lg;
  overflow: hidden;
  border: 1px solid $color-border;
  display: grid;
  grid-template-columns: 220px 1fr;
}

.mockup__sidebar {
  background: linear-gradient(180deg, $color-bg-dark, #15253d);
}

.mockup__content {
  padding: $space-4;
  background: $color-bg-page;
}

.mockup__header {
  height: 42px;
  border-radius: $radius-md;
  background: $color-white;
  margin-bottom: $space-4;
}

.mockup__cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $space-3;
  margin-bottom: $space-4;

  span {
    display: block;
    height: 72px;
    border-radius: $radius-md;
    background: linear-gradient(120deg, rgba($color-primary, 0.2), rgba($color-accent, 0.2));
  }
}

.mockup__table {
  height: 180px;
  border-radius: $radius-md;
  background: $color-white;
}

.link-text {
  margin: 0;
}
</style>
