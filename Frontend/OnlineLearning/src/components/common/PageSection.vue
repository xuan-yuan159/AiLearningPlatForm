<template>
  <section class="page-section">
    <header class="page-section__header">
      <h3 class="page-section__title">{{ title }}</h3>
      <slot name="actions" />
    </header>
    <div class="page-section__content">
      <slot />
    </div>
  </section>
</template>

<script setup>
/**
 * 统一页面区块容器组件。
 */
defineProps({
  title: {
    type: String,
    required: true,
  },
})
</script>

<style lang="scss" scoped>
.page-section {
  @include card;
}

.page-section__header {
  @include flex-between;
  margin-bottom: $space-4;
}

.page-section__title {
  margin: 0;
  font-size: $font-size-lg;
}

.page-section__content {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}
</style>
