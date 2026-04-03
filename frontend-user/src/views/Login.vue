<template>
  <div class="page">
    <h2>用户登录</h2>
    <el-form :model="form" label-width="70px" class="form">
      <el-form-item label="账号">
        <el-input v-model="form.account" placeholder="请输入账号" autocomplete="username" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          show-password
          autocomplete="current-password"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="onLogin">登录</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  account: '',
  password: '',
})

async function onLogin() {
  if (!form.account.trim() || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const { data: body } = await login(form.account.trim(), form.password)
    if (!body?.success || !body.data) {
      ElMessage.error(body?.message || '登录失败')
      return
    }
    const { token, user } = body.data
    if (user?.role && user.role !== 'USER') {
      ElMessage.warning('当前为用户端，请使用普通用户账号登录')
      return
    }
    localStorage.setItem('token', token)
    localStorage.setItem('role', user.role || 'USER')
    localStorage.setItem('user', JSON.stringify(user))
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page {
  max-width: 520px;
  margin: 40px auto;
  padding: 20px;
  text-align: left;
}
.form {
  margin-top: 16px;
}
</style>
