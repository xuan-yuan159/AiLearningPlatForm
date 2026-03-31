<template>
  <div class="login-page">
    <form class="login-card" @submit.prevent="submitLogin">
      <h1>教师后台登录</h1>
      <p>支持账号密码登录，鉴权成功后进入教学管理工作台。</p>
      <label>
        账号
        <input v-model.trim="form.account" placeholder="请输入邮箱或手机号" />
      </label>
      <label>
        密码
        <input
          v-model.trim="form.password"
          type="password"
          placeholder="请输入密码"
        />
      </label>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
      <button :disabled="submitting" type="submit">{{ submitting ? '登录中...' : '登录' }}</button>
      <p class="hint">演示账号：teacher@ailearning.com / 123456</p>
    </form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { loginByPassword } from '@/api/authApi'
import { useAuthStore } from '@/stores/useAuthStore'

/**
 * 教师登录页，负责账号登录与异常提示。
 */
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const submitting = ref(false)
const errorMessage = ref('')
const form = reactive({
  account: 'teacher@ailearning.com',
  password: '123456',
})

/**
 * 登录提交处理。
 * @returns {Promise<void>}
 */
const submitLogin = async () => {
  try {
    submitting.value = true
    errorMessage.value = ''
    const data = await loginByPassword(form)
    authStore.login(data)
    router.push(route.query.redirect || '/dashboard')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, rgba($color-primary, 0.12), rgba($color-accent, 0.08));
}

.login-card {
  width: min(440px, 92vw);
  @include card;
  display: flex;
  flex-direction: column;
  gap: $space-3;

  h1 {
    margin: 0;
  }

  p {
    margin: 0;
    color: $color-text-secondary;
  }
}

label {
  display: flex;
  flex-direction: column;
  gap: $space-2;
  font-size: $font-size-sm;
}

input {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-3;
}

button {
  @include button-primary;
}

.error {
  color: $color-error;
  font-size: $font-size-sm;
}

.hint {
  font-size: $font-size-xs;
}
</style>
