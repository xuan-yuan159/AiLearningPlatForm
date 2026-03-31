<template>
  <PageSection title="低保真原型">
    <div class="prototype-grid">
      <article v-for="item in blocks" :key="item.title">
        <h4>{{ item.title }}</h4>
        <p>{{ item.desc }}</p>
      </article>
    </div>
    <p class="link-text">
      交互原型链接：
      <a
        href="https://www.figma.com/community/file/1388391708970375036"
        target="_blank"
        rel="noreferrer"
      >
        Figma 低保真参考稿
      </a>
    </p>
  </PageSection>
</template>

<script setup>
import PageSection from '@/components/common/PageSection.vue'

/**
 * 低保真原型展示页，描述核心信息架构与流程。
 */
const blocks = [
  { title: '导航结构', desc: '左侧菜单 + 顶部标题 + 主内容区，支持快速切换。' },
  { title: '数据模块', desc: '统一卡片与表格模块，突出待办与异常提醒。' },
  { title: '状态覆盖', desc: '所有页面具备加载、空状态、错误态和权限态。' },
]
</script>

<style lang="scss" scoped>
.prototype-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: $space-4;

  @include down(md) {
    grid-template-columns: 1fr;
  }

  article {
    border: 1px dashed $color-border;
    border-radius: $radius-md;
    padding: $space-4;
  }

  h4 {
    margin: 0 0 $space-2;
  }

  p {
    margin: 0;
    color: $color-text-secondary;
  }
}

.link-text {
  margin: 0;
}
</style>
