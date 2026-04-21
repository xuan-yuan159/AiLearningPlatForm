<template>
    <div class="auth-page">
        <el-card class="auth-card" shadow="hover">
            <div class="auth-card__header">
                <h1>AI 助教在线学习平台</h1>
                <p>毕业设计演示版 · 登录后可进入教师后台或学生首页</p>
            </div>

            <el-form
                ref="formRef"
                :model="form"
                :rules="rules"
                label-position="top"
                @submit.prevent="handleLogin"
            >
                <el-form-item label="用户名" prop="username">
                    <el-input
                        v-model.trim="form.username"
                        placeholder="请输入用户名"
                        :prefix-icon="User"
                    />
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input
                        v-model.trim="form.password"
                        type="password"
                        show-password
                        placeholder="请输入密码"
                        :prefix-icon="Lock"
                    />
                </el-form-item>

                <el-button
                    type="primary"
                    class="auth-card__submit"
                    :loading="submitting"
                    @click="handleLogin"
                >
                    登录系统
                </el-button>
            </el-form>

            <div class="auth-card__footer">
                还没有账号？
                <el-link type="primary" @click="router.push('/register')">立即注册</el-link>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { loginByPassword } from '@/api/auth'
import { useAuth } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuth()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
    username: '',
    password: '',
})

const rules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
    await formRef.value?.validate()
    submitting.value = true

    try {
        const data = await loginByPassword(form)
        const role = data.role || 'STUDENT'
        auth.login({
            ...data,
            role,
            username: form.username,
            nickname: data.nickname || form.username,
        })

        ElMessage.success('登录成功')
        const defaultPath = role === 'TEACHER' ? '/teacher/dashboard' : '/student/home'
        router.push(route.query.redirect || defaultPath)
    } catch (error) {
        ElMessage.error(error instanceof Error ? error.message : '登录失败，请稍后重试')
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

