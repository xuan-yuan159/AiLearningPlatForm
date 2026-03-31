<template>
  <div class="state-page">
    <h1>无权限访问</h1>
    <p>当前账号暂无该页面访问权限，请联系系统管理员。</p>
    <RouterLink to="/dashboard">返回仪表盘</RouterLink>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router'

/**
 * 无权限页面，提示访问控制结果。
 */
</script>

<style lang="scss" scoped>
.state-page {
  @include centered-page;
}
</style>
