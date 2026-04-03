<template>
  <div class="profile-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本资料" name="base">
        <el-form :model="profile" label-width="90px" style="max-width: 560px">
          <el-form-item label="账号"><el-input v-model="profile.account" disabled /></el-form-item>
          <el-form-item label="昵称"><el-input v-model="profile.nickname" /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="profile.realName" /></el-form-item>
          <el-form-item label="手机"><el-input v-model="profile.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="profile.email" /></el-form-item>
          <el-form-item label="性别">
            <el-select v-model="profile.gender" style="width: 180px">
              <el-option :value="0" label="未知" />
              <el-option :value="1" label="男" />
              <el-option :value="2" label="女" />
            </el-select>
          </el-form-item>
          <el-form-item><el-button type="primary" @click="onSaveProfile">保存资料</el-button></el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="修改密码" name="password">
        <el-form :model="pwd" label-width="90px" style="max-width: 560px">
          <el-form-item label="旧密码"><el-input v-model="pwd.oldPassword" show-password type="password" /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwd.newPassword" show-password type="password" /></el-form-item>
          <el-form-item><el-button type="primary" @click="onChangePwd">修改密码</el-button></el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const activeTab = ref('base')
const profile = reactive({ account: '', nickname: '', realName: '', phone: '', email: '', gender: 0, role: 'STAFF' })
const pwd = reactive({ oldPassword: '', newPassword: '' })

async function loadProfile() {
  const { data } = await http.get('/user/profile/info')
  Object.assign(profile, data.data || {})
}

async function onSaveProfile() {
  await http.post('/user/profile/update', {
    nickname: profile.nickname,
    realName: profile.realName,
    phone: profile.phone,
    email: profile.email,
    gender: profile.gender,
  })
  const raw = localStorage.getItem('user')
  let u = {}
  try { u = raw ? JSON.parse(raw) : {} } catch { u = {} }
  u = { ...u, nickname: profile.nickname, realName: profile.realName, phone: profile.phone, email: profile.email, gender: profile.gender }
  localStorage.setItem('user', JSON.stringify(u))
  window.dispatchEvent(new Event('user-updated'))
  ElMessage.success('保存成功')
  await loadProfile()
}

async function onChangePwd() {
  await http.post('/user/profile/password', {
    oldPassword: pwd.oldPassword,
    newPassword: pwd.newPassword,
  })
  ElMessage.success('密码修改成功')
  pwd.oldPassword = ''
  pwd.newPassword = ''
}

onMounted(loadProfile)
</script>