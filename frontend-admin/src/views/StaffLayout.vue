<template>
  <el-container class="layout">
    <el-aside width="260px" class="aside">
      <div class="brand">
        <div class="brand-icon">🏢</div>
        <div class="brand-text"><div class="brand-name">XHXT</div><div class="brand-sub">员工中心</div></div>
      </div>
      <el-menu class="menu" :default-active="activeMenu" router background-color="transparent" text-color="#8b92a9" active-text-color="#00d4ff">
        <el-menu-item index="/staff" class="menu-item"><span class="menu-icon">📊</span><span>首页概览</span></el-menu-item>
        <el-menu-item index="/staff/inventory" class="menu-item"><span class="menu-icon">📦</span><span>库存管理</span></el-menu-item>
        <el-menu-item index="/staff/orders" class="menu-item"><span class="menu-icon">📋</span><span>订单与履约</span></el-menu-item>
        <el-menu-item index="/staff/replies" class="menu-item"><span class="menu-icon">💬</span><span>社区客服</span></el-menu-item>
        <el-menu-item index="/staff/profile" class="menu-item"><span class="menu-icon">⚙️</span><span>个人中心</span></el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left"><span class="header-title">员工工作台</span></div>
        <div class="header-right">
          <el-badge :value="notifyCount" :hidden="notifyCount===0" class="notify-badge">
            <el-button circle class="bell-btn" :class="{ ring: notifyCount > 0 }" @click="openNotify">🔔</el-button>
          </el-badge>
          <div class="user-info" v-if="user">
            <span class="user-avatar">{{ (user.nickname || user.account).charAt(0).toUpperCase() }}</span>
            <div class="user-details"><div class="user-name">{{ user.nickname || user.account }}</div><div class="user-role">员工</div></div>
          </div>
          <el-button type="danger" text @click="logout">退出</el-button>
        </div>
      </el-header>

      <el-main class="main"><router-view /></el-main>
    </el-container>

    <el-dialog v-model="notifyVisible" title="新订单通知" width="900px">
      <el-alert v-if="notifyList.length" type="warning" :closable="false" title="您有新的订单，请及时处理" show-icon style="margin-bottom:10px" />
      <el-table :data="notifyList" border max-height="420">
        <el-table-column prop="orderNo" label="订单编号" min-width="180" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="address" label="收货地址" min-width="180" />
        <el-table-column label="商品信息" min-width="180"><template #default="{row}">{{ row.items?.map(i=>`${i.productName}x${i.quantity}`).join('，') }}</template></el-table-column>
        <el-table-column prop="amount" label="订单金额" width="120" />
        <el-table-column prop="createTime" label="下单时间" width="160" />
        <el-table-column prop="cardMessage" label="贺卡留言" min-width="120" />
        <el-table-column prop="deliveryExpectTime" label="配送要求" min-width="120" />
        <el-table-column label="操作" width="90"><template #default="{row}"><el-button size="small" type="primary" @click="markRead(row)">已读</el-button></template></el-table-column>
      </el-table>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const route = useRoute()
const router = useRouter()
const notifyVisible = ref(false)
const notifyList = ref([])
const notifyCount = computed(() => notifyList.value.length)
let timer = null
let lastCount = 0

function loadUser() {
  const raw = localStorage.getItem('user')
  if (!raw) return null
  try { return JSON.parse(raw) } catch { return null }
}

const user = ref(loadUser())
const activeMenu = computed(() => route.path)

function refreshUser() { user.value = loadUser() }

async function loadNotify() {
  const res = await http.get('/staff/order/notify/list')
  notifyList.value = res.data?.data || []
  if (notifyList.value.length > lastCount && notifyList.value.length > 0) {
    ElMessage.warning('您有新的订单，请及时处理')
  }
  lastCount = notifyList.value.length
}

function openNotify() { notifyVisible.value = true }

async function markRead(row) {
  await http.post(`/staff/order/notify/read/${row.orderNo}`)
  await loadNotify()
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('user')
  ElMessage.info('已退出')
  router.push('/login')
}

onMounted(async () => {
  window.addEventListener('user-updated', refreshUser)
  await loadNotify()
  timer = setInterval(loadNotify, 8000)
})

onBeforeUnmount(() => {
  window.removeEventListener('user-updated', refreshUser)
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.layout { height: 100vh; width: 100%; }
.aside { background: linear-gradient(135deg, #059669 0%, #10b981 100%); color: #fff; overflow-y: auto; box-shadow: 2px 0 8px rgba(0,0,0,.15); }
.brand { display: flex; align-items: center; gap: 12px; padding: 24px 16px; border-bottom: 1px solid rgba(255,255,255,.1); }
.brand-icon { font-size: 32px; }
.brand-name { font-weight: 700; font-size: 16px; color: #fff; }
.brand-sub { font-size: 12px; color: rgba(255,255,255,.7); margin-top: 2px; }
.menu { border-right: none; background: transparent; padding: 12px 0; }
.menu-item { display: flex; align-items: center; gap: 10px; margin: 4px 12px; border-radius: 8px; transition: all .3s ease; }
.menu-icon { font-size: 18px; }
.header { display:flex; align-items:center; justify-content:space-between; padding:0 24px; background:#fff; border-bottom:1px solid #f0f0f0; box-shadow:0 2px 4px rgba(0,0,0,.05); }
.header-title { font-weight: 700; font-size: 18px; color: #333; }
.header-right { display:flex; align-items:center; gap:16px; }
.bell-btn.ring { animation: ring 1s infinite; }
@keyframes ring { 0%{transform:rotate(0)} 15%{transform:rotate(15deg)} 30%{transform:rotate(-12deg)} 45%{transform:rotate(10deg)} 60%{transform:rotate(-8deg)} 75%{transform:rotate(6deg)} 100%{transform:rotate(0)} }
.user-info { display:flex; align-items:center; gap:12px; }
.user-avatar { width:40px; height:40px; border-radius:50%; background: linear-gradient(135deg, #059669 0%, #10b981 100%); color:#fff; display:flex; align-items:center; justify-content:center; font-weight:700; font-size:16px; }
.user-details { display:flex; flex-direction:column; gap:2px; }
.user-name { color:#333; font-weight:600; font-size:14px; }
.user-role { color:#999; font-size:12px; }
.main { padding:16px 20px; overflow:auto; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); }
</style>