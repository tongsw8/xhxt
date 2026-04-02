<template>
  <div class="dashboard">
    <div class="header-section">
      <h1 class="page-title">员工工作台</h1>
      <p class="page-subtitle">今日任务一览，减少完成量</p>
    </div>

    <el-row :gutter="20" class="stats-grid">
      <el-col :xs="12" :sm="6">
        <div class="stat-card teal" @click="go('/staff/inventory')">
          <div class="stat-icon">🗃️</div>
          <div class="stat-info"><div class="stat-label">需盘点商品</div><div class="stat-value">{{ inventoryCheckCount }}</div></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card orange" @click="go('/staff/orders')">
          <div class="stat-icon">🚚</div>
          <div class="stat-info"><div class="stat-label">待发货订单</div><div class="stat-value">{{ pendingShipCount }}</div></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card blue" @click="go('/staff/replies')">
          <div class="stat-icon">💬</div>
          <div class="stat-info"><div class="stat-label">待回复社区</div><div class="stat-value">{{ pendingReplyCount }}</div></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card green" @click="go('/staff/orders')">
          <div class="stat-icon">✅</div>
          <div class="stat-info"><div class="stat-label">今日处理量</div><div class="stat-value">{{ todayProcessed }}</div></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="12">
        <el-card class="action-card">
          <template #header><span class="card-title">🚀 快捷入口</span></template>
          <div class="action-grid">
            <div class="action-item" @click="go('/staff/inventory')"><div class="action-icon">🗃️</div><div class="action-name">库存管理</div></div>
            <div class="action-item" @click="go('/staff/orders')"><div class="action-icon">📋</div><div class="action-name">订单与履约</div></div>
            <div class="action-item" @click="go('/staff/replies')"><div class="action-icon">💬</div><div class="action-name">社区客服</div></div>
            <div class="action-item" @click="go('/staff/profile')"><div class="action-icon">⚙️</div><div class="action-name">个人中心</div></div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card class="task-card">
          <template #header><span class="card-title">📋 今日待办</span></template>
          <div class="task-list">
            <div class="task-item" :class="{ blink: urgentShipCount > 0 }">
              <div class="task-status urgent">紧急</div>
              <div class="task-content"><div class="task-title">发货录入</div><div class="task-count">{{ urgentShipCount }} 单需优先处理</div></div>
            </div>
            <div class="task-item"><div class="task-status normal">普通</div><div class="task-content"><div class="task-title">官方回复</div><div class="task-count">{{ pendingReplyCount }} 条待回复</div></div></div>
            <div class="task-item"><div class="task-status normal">普通</div><div class="task-content"><div class="task-title">库存盘点</div><div class="task-count">{{ inventoryCheckCount }} 件需核实</div></div></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'

const router = useRouter()
const inventoryCheckCount = ref(0)
const pendingShipCount = ref(0)
const urgentShipCount = ref(0)
const pendingReplyCount = ref(0)
const todayProcessed = ref(0)
let timer = null

function go(path) { router.push(path) }

async function loadStats() {
  const pRes = await http.get('/admin/product/list', { params: { pageNo: 1, pageSize: 200 } })
  const products = pRes.data?.data?.records || []
  inventoryCheckCount.value = products.filter(p => Number(p.stock || 0) <= 20).length

  const oRes = await http.get('/staff/order/list')
  const orders = oRes.data?.data || []
  pendingShipCount.value = orders.filter(o => o.status === 1).length
  const shipped = orders.filter(o => o.status === 2).length

  const uRes = await http.get('/staff/order/urge-count')
  urgentShipCount.value = Number(uRes.data?.data || 0)

  pendingReplyCount.value = 0
  todayProcessed.value = shipped
}

onMounted(async () => {
  await loadStats()
  timer = setInterval(loadStats, 8000)
})

onBeforeUnmount(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.dashboard { width: 100%; display: flex; flex-direction: column; gap: 24px; }
.header-section { margin-bottom: 8px; }
.page-title { font-size: 28px; font-weight: 700; margin: 0 0 8px; color: #333; }
.page-subtitle { font-size: 14px; color: #999; margin: 0; }
.stats-grid { margin: 0; }
.stat-card { background: #fff; border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); transition: all 0.3s ease; border-left: 4px solid; cursor: pointer; }
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 8px 16px rgba(0,0,0,0.12); }
.stat-card.teal  { border-left-color: #10b981; }
.stat-card.orange { border-left-color: #f59e0b; }
.stat-card.blue  { border-left-color: #06b6d4; }
.stat-card.green { border-left-color: #059669; }
.stat-icon { font-size: 32px; flex-shrink: 0; }
.stat-info { flex: 1; }
.stat-label { color: #999; font-size: 13px; margin-bottom: 4px; }
.stat-value { font-size: 24px; font-weight: 700; color: #333; }
.action-card, .task-card { border-radius: 12px; border: 1px solid #f0f0f0; height: 100%; }
.card-title { font-size: 16px; font-weight: 700; color: #333; }
.action-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.action-item { padding: 16px; border-radius: 8px; background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%); text-align: center; cursor: pointer; transition: all 0.3s ease; border: 2px solid transparent; }
.action-item:hover { background: linear-gradient(135deg, #059669 0%, #10b981 100%); color: #fff; border-color: #059669; transform: translateY(-2px); }
.action-icon { font-size: 28px; margin-bottom: 8px; }
.action-name { font-size: 13px; font-weight: 600; }
.task-list { display: flex; flex-direction: column; gap: 12px; }
.task-item { display: flex; align-items: center; gap: 12px; padding: 12px; border-radius: 8px; background: #f9fafb; }
.task-item.blink { animation: urgentBlink 1s infinite; box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.35) inset; }
@keyframes urgentBlink { 0%,100% { background: #fff1f2; } 50% { background: #fee2e2; } }
.task-status { padding: 4px 12px; border-radius: 4px; font-size: 11px; font-weight: 700; white-space: nowrap; color: #fff; }
.task-status.urgent { background: #ef4444; }
.task-status.normal { background: #10b981; }
.task-content { flex: 1; }
.task-title { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 2px; }
.task-count { font-size: 12px; color: #999; }
</style>