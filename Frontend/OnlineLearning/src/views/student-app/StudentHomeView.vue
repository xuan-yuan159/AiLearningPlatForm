<template>
  <div class="student-home">
    <section class="hero-section">
      <h1>开启您的 AI 学习之旅</h1>
      <p>探索由 AI 驱动的课程，获得个性化学习建议</p>
      <div class="search-bar">
        <input type="text" placeholder="搜索您感兴趣的课程..." />
        <button>搜索</button>
      </div>
    </section>

    <section class="course-section">
      <div class="section-header">
        <h2>热门课程</h2>
        <a href="#" class="more">查看全部</a>
      </div>
      <div class="course-grid">
        <div v-for="course in popularCourses" :key="course.id" class="course-card" @click="goToDetail(course.id)">
          <div class="course-cover">
            <img :src="course.cover" :alt="course.title" />
            <div class="tag">{{ course.category }}</div>
          </div>
          <div class="course-info">
            <h3>{{ course.title }}</h3>
            <p class="description">{{ course.description }}</p>
            <div class="footer">
              <span class="students">{{ course.students }} 人在学</span>
              <span class="difficulty" :class="course.difficulty">{{ course.difficultyLabel }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="ai-recommendation">
      <div class="recommendation-card">
        <div class="ai-avatar">AI</div>
        <div class="content">
          <h3>AI 学习建议</h3>
          <p>基于您的学习进度，我们建议您今天复习《Python 机器学习实战》的“监督学习”章节。</p>
          <button class="secondary-btn">开始学习</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const popularCourses = ref([
  {
    id: 'c-1001',
    title: 'Python机器学习实战',
    description: '从基础语法到深度学习框架，带你领略 AI 的魅力。',
    cover: 'https://images.unsplash.com/photo-1527474305487-b87b222841cc?auto=format&fit=crop&w=400&q=80',
    category: '人工智能',
    students: 1240,
    difficulty: 'intermediate',
    difficultyLabel: '中级',
  },
  {
    id: 'c-1002',
    title: '数据结构高频题解',
    description: '面试必备，深入浅出讲解经典数据结构与算法。',
    cover: 'https://images.unsplash.com/photo-1516116216624-53e697fedbea?auto=format&fit=crop&w=400&q=80',
    category: '算法',
    students: 860,
    difficulty: 'easy',
    difficultyLabel: '入门',
  },
  {
    id: 'c-1003',
    title: 'Vue 3 企业级项目开发',
    description: '掌握 Composition API，构建高性能的前端应用。',
    cover: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=400&q=80',
    category: '前端开发',
    students: 520,
    difficulty: 'advanced',
    difficultyLabel: '高级',
  },
])

const goToDetail = (id) => {
  router.push(`/course/${id}`)
}
</script>

<style lang="scss" scoped>
.hero-section {
  padding: $space-10 0;
  text-align: center;
  background: linear-gradient(rgba($color-primary, 0.05), rgba($color-primary, 0.02));
  border-radius: $radius-lg;
  margin-bottom: $space-10;

  h1 {
    font-size: 48px;
    margin-bottom: $space-4;
    color: $color-text-primary;
  }

  p {
    font-size: $font-size-lg;
    color: $color-text-secondary;
    margin-bottom: $space-8;
  }

  .search-bar {
    max-width: 600px;
    margin: 0 auto;
    display: flex;
    gap: $space-2;

    input {
      flex: 1;
      padding: $space-4;
      border: 1px solid $color-border;
      border-radius: $radius-md;
      font-size: $font-size-md;

      &:focus {
        outline: none;
        border-color: $color-primary;
      }
    }

    button {
      @include button-primary;
      padding: 0 $space-8;
    }
  }
}

.course-section {
  margin-bottom: $space-10;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $space-6;

    h2 {
      font-size: $font-size-xxl;
    }

    .more {
      color: $color-primary;
      text-decoration: none;
      font-size: $font-size-sm;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .course-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: $space-6;
  }

  .course-card {
    @include card;
    cursor: pointer;
    transition: transform 0.3s;
    overflow: hidden;

    &:hover {
      transform: translateY(-8px);
    }

    .course-cover {
      height: 160px;
      position: relative;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .tag {
        position: absolute;
        top: $space-2;
        right: $space-2;
        background: rgba(0, 0, 0, 0.6);
        color: #fff;
        padding: $space-1 $space-2;
        border-radius: $radius-sm;
        font-size: $font-size-xs;
      }
    }

    .course-info {
      padding: $space-4;

      h3 {
        font-size: $font-size-lg;
        margin-bottom: $space-2;
      }

      .description {
        font-size: $font-size-sm;
        color: $color-text-secondary;
        margin-bottom: $space-4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: $font-size-xs;

        .students {
          color: $color-text-secondary;
        }

        .difficulty {
          padding: 2px 6px;
          border-radius: 4px;

          &.easy { background: #e6fffa; color: #38b2ac; }
          &.intermediate { background: #ebf8ff; color: #4299e1; }
          &.advanced { background: #fff5f5; color: #f56565; }
        }
      }
    }
  }
}

.ai-recommendation {
  .recommendation-card {
    display: flex;
    align-items: center;
    gap: $space-6;
    background: $color-bg-dark;
    color: #fff;
    padding: $space-8;
    border-radius: $radius-lg;

    .ai-avatar {
      width: 64px;
      height: 64px;
      background: $color-primary;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      font-size: $font-size-xl;
    }

    .content {
      flex: 1;

      h3 {
        margin-bottom: $space-2;
      }

      p {
        color: rgba(255, 255, 255, 0.8);
        margin-bottom: $space-4;
      }

      .secondary-btn {
        background: transparent;
        border: 1px solid #fff;
        color: #fff;
        padding: $space-2 $space-6;
        border-radius: $radius-md;
        cursor: pointer;

        &:hover {
          background: rgba(255, 255, 255, 0.1);
        }
      }
    }
  }
}
</style>
