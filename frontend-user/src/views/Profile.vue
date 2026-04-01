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

      <el-tab-pane label="收货地址" name="address">
        <el-button type="primary" @click="openAddress(null)">新增地址</el-button>
        <el-table :data="addressList" style="margin-top: 12px">
          <el-table-column prop="receiverName" label="收货人" width="100" />
          <el-table-column prop="receiverPhone" label="电话" width="130" />
          <el-table-column label="地址" min-width="260"><template #default="{ row }">{{ fullAddress(row) }}</template></el-table-column>
          <el-table-column label="默认" width="80"><template #default="{ row }"><el-tag v-if="row.isDefault === 1" type="success">默认</el-tag></template></el-table-column>
          <el-table-column label="操作" width="240">
            <template #default="{ row }">
              <el-button size="small" @click="openAddress(row)">编辑</el-button>
              <el-button size="small" @click="setDefault(row)" v-if="row.isDefault !== 1">设默认</el-button>
              <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" title="收货地址" width="640px">
      <el-form :model="addrForm" label-width="90px">
        <el-form-item label="收货人"><el-input v-model="addrForm.receiverName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="addrForm.receiverPhone" /></el-form-item>
        <el-form-item label="省"><el-input v-model="addrForm.province" /></el-form-item>
        <el-form-item label="市"><el-input v-model="addrForm.city" /></el-form-item>
        <el-form-item label="区"><el-input v-model="addrForm.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addrForm.detailAddress" /></el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="addrForm.isDefaultFlag" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSaveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProfileInfo, updateProfile, changePassword, listAddress, saveAddress, deleteAddress, setDefaultAddress } from '../api/user'

const route = useRoute()
const activeTab = ref('base')
const profile = reactive({ account: '', nickname: '', realName: '', phone: '', email: '', gender: 0 })
const pwd = reactive({ oldPassword: '', newPassword: '' })
const addressList = ref([])
const dialogVisible = ref(false)
const addrForm = reactive({ id: null, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefaultFlag: false })

const fullAddress = (a) => (a.province || '') + (a.city || '') + (a.district || '') + (a.detailAddress || '')

async function loadProfile() {
  const { data } = await getProfileInfo()
  Object.assign(profile, data.data || {})
}

async function loadAddress() {
  const { data } = await listAddress()
  addressList.value = data.data || []
}

async function onSaveProfile() {
  await updateProfile(profile)
  ElMessage.success('保存成功')
  await loadProfile()
}

async function onChangePwd() {
  await changePassword(pwd)
  ElMessage.success('密码修改成功')
  pwd.oldPassword = ''
  pwd.newPassword = ''
}

function openAddress(row) {
  if (row) {
    Object.assign(addrForm, { ...row, isDefaultFlag: row.isDefault === 1 })
  } else {
    Object.assign(addrForm, { id: null, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefaultFlag: false })
  }
  dialogVisible.value = true
}

async function onSaveAddress() {
  await saveAddress({ ...addrForm, isDefault: addrForm.isDefaultFlag ? 1 : 0 })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await loadAddress()
}

async function onDelete(row) {
  await deleteAddress(row.id)
  ElMessage.success('删除成功')
  await loadAddress()
}

async function setDefault(row) {
  await setDefaultAddress(row.id)
  ElMessage.success('设置成功')
  await loadAddress()
}

onMounted(async () => {
  await loadProfile()
  await loadAddress()
  if (route.query.tab === 'address') activeTab.value = 'address'
  if (route.query.openAddress === '1') {
    activeTab.value = 'address'
    await nextTick()
    openAddress(null)
  }
})
</script>
