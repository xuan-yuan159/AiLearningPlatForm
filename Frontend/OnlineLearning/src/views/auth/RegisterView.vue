<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="hover">
      <div class="auth-card__header">
        <h1>创建账号</h1>
        <p>支持学生与教师身份，便于毕设答辩现场演示</p>
      </div>

      <el-form
ref="formRef"
:model="form"
:rules="rules"
label-position="top"
@submit.prevent="handleRegister">
        <el-form-item label="用户名" prop="username">
          <el-input
v-model.trim="form.username"
placeholder="请输入用户名"
:prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
v-model.trim="form.password"
type="password"
show-password
placeholder="请输入密码"
:prefix-icon="Lock" />
        </el-form-item>
        <el-form-item label="身份" prop="identity">
          <el-radio-group v-model="form.identity">
            <el-radio-button :label="0">学生</el-radio-button>
            <el-radio-button :label="1">教师</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model.trim="form.nickname" placeholder="可选，不填默认与用户名一致" />
        </el-form-item>

        <el-button
type="primary"
class="auth-card__submit"
:loading="submitting"
@click="handleRegister">
          注册并登录
        </el-button>
      </el-form>

      <div class="auth-card__footer">
        已有账号？
        <el-link type="primary" @click="router.push('/login')">返回登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { loginByPassword, registerUser } from '@/api/auth'
import { useAuth } from '@/utils/auth'

const router = useRouter()
const auth = useAuth()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  username: '',
  password: '',
  identity: 0,
  nickname: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleRegister() {
  await formRef.value?.validate()
  submitting.value = true

  try {
    await registerUser(form)
    const loginData = await loginByPassword({
      username: form.username,
      password: form.password,
    })

    const role = loginData.role || (form.identity === 1 ? 'TEACHER' : 'STUDENT')
    auth.login({
      ...loginData,
      role,
      username: form.username,
      nickname: form.nickname || form.username,
    })

    ElMessage.success('注册成功，已自动登录')
    router.push(role === 'TEACHER' ? '/teacher/dashboard' : '/student/home')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '注册失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.auth-card {
  width: min(460px, 100%);
  border-radius: 16px;
}

.auth-card__header {
  margin-bottom: 8px;
  text-align: center;
}

.auth-card__header h1 {
  margin: 0;
  font-size: 28px;
}

.auth-card__header p {
  margin: 10px 0 0;
  color: #606266;
}

.auth-card__submit {
  width: 100%;
  margin-top: 6px;
}

.auth-card__footer {
  margin-top: 16px;
  text-align: center;
  color: #606266;
}
</style>

