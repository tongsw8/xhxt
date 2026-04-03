<template>
  <div class="page">
    <h2>管理端登录</h2>
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
    <p class="tip">使用数据库中的管理员（admin）或员工（staff）账号登录，角色由服务端返回。</p>
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
    const role = user?.role
    if (role !== 'ADMIN' && role !== 'STAFF') {
      ElMessage.warning('请使用管理员或员工账号登录管理端')
      return
    }
    localStorage.setItem('token', token)
    localStorage.setItem('role', role)
    localStorage.setItem('user', JSON.stringify(user))
    ElMessage.success('登录成功')
    if (role === 'ADMIN') {
      router.push('/admin')
    } else {
      router.push('/staff')
    }
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
.tip {
  margin-top: 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
