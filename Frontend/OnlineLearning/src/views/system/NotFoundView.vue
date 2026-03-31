<template>
  <div class="state-page">
    <h1>404</h1>
    <p>页面不存在或已被移动。</p>
    <RouterLink to="/dashboard">返回首页</RouterLink>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router'

/**
 * 404 页面，处理未知路由访问。
 */
</script>

<style lang="scss" scoped>
.state-page {
  @include centered-page;
}
</style>
