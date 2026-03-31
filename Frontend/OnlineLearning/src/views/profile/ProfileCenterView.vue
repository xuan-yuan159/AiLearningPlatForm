<template>
  <PageSection title="个人中心">
    <div class="profile-center">
      <img :src="profile.avatar" alt="教师头像" />
      <form @submit.prevent="saveProfile">
        <label>姓名<input v-model.trim="editable.name" /></label>
        <label>职称<input v-model.trim="editable.title" /></label>
        <label>邮箱<input v-model.trim="editable.email" /></label>
        <label>电话<input v-model.trim="editable.phone" /></label>
        <button type="submit">保存</button>
      </form>
    </div>
  </PageSection>
</template>

<script setup>
import { reactive } from 'vue'
import PageSection from '@/components/common/PageSection.vue'
import { useAuthStore } from '@/stores/useAuthStore'

/**
 * 个人中心页面，维护教师个人资料。
 */
const authStore = useAuthStore()
const profile = authStore.profile.value
const editable = reactive({
  name: profile.name,
  title: profile.title,
  email: profile.email,
  phone: profile.phone,
})

/**
 * 保存教师资料。
 * @returns {void}
 */
const saveProfile = () => {
  authStore.updateProfile(editable)
}
</script>

<style lang="scss" scoped>
.profile-center {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: $space-5;

  @include down(md) {
    grid-template-columns: 1fr;
  }

  img {
    width: 120px;
    height: 120px;
    object-fit: cover;
    border-radius: $radius-round;
  }
}

form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: $space-3;

  @include down(sm) {
    grid-template-columns: 1fr;
  }
}

label {
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

input {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-3;
}

button {
  @include button-primary;
  width: fit-content;
}
</style>
