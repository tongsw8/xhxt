<template>
  <div>
    <el-card>
      <template #header>用户与权限管理</template>

      <el-radio-group v-model="role" @change="load">
        <el-radio-button label="USER">用户</el-radio-button>
        <el-radio-button label="STAFF">员工</el-radio-button>
        <el-radio-button label="ADMIN">管理员</el-radio-button>
      </el-radio-group>

      <el-table :data="list" style="margin-top: 12px" border>
        <el-table-column prop="account" label="账号" min-width="160" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button size="small" @click="toggle(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="primary" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="用户详情" width="640px">
      <el-descriptions :column="2" border v-if="detail.user">
        <el-descriptions-item label="账号">{{ detail.user.account || '-' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detail.user.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.user.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detail.user.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ detail.user.role || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.user.status === 1 ? '启用' : '禁用' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ fmt(detail.user.regTime) }}</el-descriptions-item>
        <el-descriptions-item label="已支付订单数">{{ detail.paidOrderCount }}</el-descriptions-item>
        <el-descriptions-item label="已支付订单总金额">¥{{ Number(detail.paidTotalAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="最近更新时间">{{ fmt(detail.user.updateTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const role = ref('USER')
const list = ref([])
const detailVisible = ref(false)
const detail = ref({ user: null, paidOrderCount: 0, paidTotalAmount: 0 })

const load = async () => {
  const res = await http.get('/admin/user/list', { params: { role: role.value } })
  list.value = res.data?.data || []
}

const toggle = async (row) => {
  const status = row.status === 1 ? 0 : 1
  await http.post('/admin/user/status', null, { params: { id: row.id, status } })
  ElMessage.success('状态已更新')
  await load()
}

const showDetail = async (row) => {
  const res = await http.get(`/admin/user/detail/${row.id}`)
  detail.value = res.data?.data || { user: null, paidOrderCount: 0, paidTotalAmount: 0 }
  detailVisible.value = true
}

const fmt = (v) => {
  if (!v) return '-'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  return d.toLocaleString()
}

onMounted(load)
</script>