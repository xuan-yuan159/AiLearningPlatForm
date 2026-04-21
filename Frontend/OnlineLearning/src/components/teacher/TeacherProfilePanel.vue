<template>
  <el-card class="panel-card" shadow="hover">
    <template #header>
      <strong>个人中心</strong>
    </template>

    <el-form label-width="90px" @submit.prevent>
      <el-form-item label="用户 ID">
        <el-input :model-value="auth.profile.value.userId" disabled />
      </el-form-item>
      <el-form-item label="用户名">
        <el-input :model-value="auth.profile.value.username" disabled />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model.trim="form.nickname" />
      </el-form-item>
      <el-form-item label="头像 URL">
        <el-input v-model.trim="form.avatar" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveProfile">保存资料</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/utils/auth'

const auth = useAuth()
const form = reactive({
  nickname: auth.profile.value.nickname,
  avatar: auth.profile.value.avatar,
})

watch(
  () => auth.profile.value,
  (value) => {
    form.nickname = value.nickname
    form.avatar = value.avatar
  },
  { deep: true },
)

function saveProfile() {
  auth.updateProfile({
    nickname: form.nickname || auth.profile.value.username,
    avatar: form.avatar || auth.profile.value.avatar,
  })
  ElMessage.success('资料已保存')
}
</script>

<style scoped>
.panel-card {
  border-radius: 16px;
}
</style>

