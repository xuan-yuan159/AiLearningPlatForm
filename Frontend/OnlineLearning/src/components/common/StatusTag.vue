<template>
  <span class="status-tag" :class="`status-tag--${type}`">{{ text }}</span>
</template>

<script setup>
/**
 * 通用状态标签组件。
 */
defineProps({
  text: {
    type: String,
    required: true,
  },
  type: {
    type: String,
    default: 'default',
  },
})
</script>

<style lang="scss" scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 72px;
  padding: $space-1 $space-2;
  border-radius: $radius-pill;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
}

.status-tag--success {
  background: rgba($color-success, 0.1);
  color: $color-success;
}

.status-tag--warning {
  background: rgba($color-warning, 0.12);
  color: $color-warning;
}

.status-tag--error {
  background: rgba($color-error, 0.12);
  color: $color-error;
}

.status-tag--default {
  background: rgba($color-primary, 0.12);
  color: $color-primary;
}
</style>
