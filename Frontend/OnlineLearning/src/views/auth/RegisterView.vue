<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-info">
        <div class="brand">
          <div class="logo">AI</div>
          <h1>AI 助教在线学习平台</h1>
        </div>
        <p class="description">基于微服务架构的智能化学习管理系统，为教师与学生提供全方位的教学支持与 AI 辅助分析。</p>
        <ul class="features">
          <li><span class="dot"></span> AI 摘要自动生成</li>
          <li><span class="dot"></span> 智能问答知识库</li>
          <li><span class="dot"></span> 多维度学情统计分析</li>
        </ul>
      </div>

      <div class="login-card">
        <div class="card-header">
          <h2>{{ isRegister ? '创建账号' : '欢迎回来' }}</h2>
          <p>{{ isRegister ? '注册以开始您的 AI 学习之旅' : '请输入您的凭据以访问后台' }}</p>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>用户名</label>
            <div class="input-wrapper">
              <input v-model.trim="form.username" placeholder="请输入用户名" required />
            </div>
          </div>

          <div class="form-group">
            <label>密码</label>
            <div class="input-wrapper">
              <input v-model.trim="form.password" type="password" placeholder="请输入密码" required />
            </div>
          </div>

          <template v-if="isRegister">
            <div class="form-group">
              <label>身份</label>
              <div class="radio-group">
                <label class="radio-item">
                  <input type="radio" v-model="form.identity" :value="0" />
                  <span>学生</span>
                </label>
                <label class="radio-item">
                  <input type="radio" v-model="form.identity" :value="1" />
                  <span>教师</span>
                </label>
              </div>
            </div>

            <div class="form-group">
              <label>昵称 (可选)</label>
              <div class="input-wrapper">
                <input v-model.trim="form.nickname" placeholder="请输入昵称" />
              </div>
            </div>
          </template>

          <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>

          <button class="submit-btn" :disabled="submitting" type="submit">
            {{ submitting ? (isRegister ? '注册中...' : '登录中...') : (isRegister ? '立即注册' : '登录系统') }}
          </button>
        </form>

        <div class="card-footer">
          <p>
            {{ isRegister ? '已有账号？' : '还没有账号？' }}
            <a href="javascript:;" @click="toggleMode">
              {{ isRegister ? '去登录' : '立即注册' }}
            </a>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { loginByPassword, registerUser } from '@/api/authApi'
import { useAuthStore } from '@/stores/useAuthStore'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isRegister = ref(route.name === 'register')
const submitting = ref(false)
const errorMessage = ref('')

const form = reactive({
  username: '',
  password: '',
  identity: 0,
  nickname: '',
})

const toggleMode = () => {
  isRegister.value = !isRegister.value
  errorMessage.value = ''
  if (isRegister.value) {
    router.push('/register')
  } else {
    router.push('/login')
  }
}

const handleSubmit = async () => {
  try {
    submitting.value = true
    errorMessage.value = ''
    
    if (isRegister.value) {
      await registerUser(form)
      // 注册成功后自动登录
      const loginData = await loginByPassword({
        username: form.username,
        password: form.password
      })
      authStore.login(loginData)
      // 根据 role 字符串跳转：TEACHER 为教师，其他为学生
      const targetPath = loginData.role === 'TEACHER' ? '/teacher/dashboard' : '/'
      router.push(targetPath)
    } else {
      const data = await loginByPassword({
        username: form.username,
        password: form.password
      })
      authStore.login(data)
      // 根据 role 字符串跳转：TEACHER 为教师，其他为学生
      const targetPath = data.role === 'TEACHER' ? '/teacher/dashboard' : '/'
      router.push(route.query.redirect || targetPath)
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '操作失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: $space-4;
}

.login-container {
  display: flex;
  width: min(1000px, 100%);
  background: #fff;
  border-radius: $radius-lg;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);

  @include down(md) {
    flex-direction: column;
  }
}

.login-info {
  flex: 1;
  background: $color-bg-dark;
  color: #fff;
  padding: $space-10;
  display: flex;
  flex-direction: column;
  justify-content: center;

  @include down(md) {
    padding: $space-6;
  }

  .brand {
    display: flex;
    align-items: center;
    gap: $space-3;
    margin-bottom: $space-6;

    .logo {
      width: 48px;
      height: 48px;
      background: $color-primary;
      border-radius: $radius-md;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      font-size: $font-size-xl;
    }

    h1 {
      font-size: $font-size-xxl;
      margin: 0;
    }
  }

  .description {
    font-size: $font-size-md;
    line-height: 1.6;
    color: rgba(255, 255, 255, 0.7);
    margin-bottom: $space-8;
  }

  .features {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      display: flex;
      align-items: center;
      gap: $space-2;
      margin-bottom: $space-3;
      font-size: $font-size-sm;
      color: rgba(255, 255, 255, 0.9);

      .dot {
        width: 6px;
        height: 6px;
        background: $color-primary;
        border-radius: 50%;
      }
    }
  }
}

.login-card {
  width: 440px;
  padding: $space-10;
  display: flex;
  flex-direction: column;

  @include down(md) {
    width: 100%;
    padding: $space-6;
  }

  .card-header {
    margin-bottom: $space-8;

    h2 {
      font-size: $font-size-xxl;
      margin-bottom: $space-2;
    }

    p {
      color: $color-text-secondary;
      font-size: $font-size-sm;
    }
  }
}

.form-group {
  margin-bottom: $space-5;

  label {
    display: block;
    margin-bottom: $space-2;
    font-size: $font-size-sm;
    font-weight: 500;
  }

  .input-wrapper {
    input {
      width: 100%;
      padding: $space-3;
      border: 1px solid $color-border;
      border-radius: $radius-md;
      font-size: $font-size-md;
      transition: all 0.3s;

      &:focus {
        outline: none;
        border-color: $color-primary;
        box-shadow: 0 0 0 3px rgba($color-primary, 0.1);
      }
    }
  }

  .radio-group {
    display: flex;
    gap: $space-6;
    padding: $space-2 0;

    .radio-item {
      display: flex;
      align-items: center;
      gap: $space-2;
      cursor: pointer;

      input {
        cursor: pointer;
      }
    }
  }
}

.submit-btn {
  width: 100%;
  @include button-primary;
  padding: $space-3;
  font-size: $font-size-md;
  margin-top: $space-4;
  height: 48px;
}

.error-msg {
  color: $color-error;
  font-size: $font-size-sm;
  margin-bottom: $space-4;
}

.card-footer {
  margin-top: auto;
  padding-top: $space-8;
  text-align: center;
  font-size: $font-size-sm;
  color: $color-text-secondary;

  a {
    color: $color-primary;
    text-decoration: none;
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
