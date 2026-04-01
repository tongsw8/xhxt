<template>
  <div class="dashboard">
    <div class="header-section">
      <h1 class="page-title">管理员概览</h1>
      <p class="page-subtitle">实时掌握平台运营数据</p>
    </div>

    <el-row :gutter="20" class="stats-grid">
      <el-col :xs="12" :sm="6">
        <div class="stat-card primary" @click="go('/admin/users')">
          <div class="stat-icon">👥</div>
          <div class="stat-info">
            <div class="stat-label">注册用户数</div>
            <div class="stat-value">{{ stats.userCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card warning" @click="go('/admin/content')">
          <div class="stat-icon">💬</div>
          <div class="stat-info">
            <div class="stat-label">待审核评论</div>
            <div class="stat-value">{{ stats.pendingCommentCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card success" @click="go('/admin/products')">
          <div class="stat-icon">📦</div>
          <div class="stat-info">
            <div class="stat-label">上架商品</div>
            <div class="stat-value">{{ stats.onSaleProductCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card info" @click="go('/admin/stats')">
          <div class="stat-icon">📋</div>
          <div class="stat-info">
            <div class="stat-label">今日订单</div>
            <div class="stat-value">{{ stats.todayOrderCount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="content-grid">
      <el-col :xs="24" :sm="12">
        <el-card class="action-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">🚀 快捷入口</span>
              <span class="card-subtitle">快速访问常用功能</span>
            </div>
          </template>
          <div class="action-grid">
            <div class="action-item" @click="go('/admin/users')"><div class="action-icon">👥</div><div class="action-name">用户与权限管理</div></div>
            <div class="action-item" @click="go('/admin/products')"><div class="action-icon">📦</div><div class="action-name">商品中心管理</div></div>
            <div class="action-item" @click="go('/admin/content')"><div class="action-icon">📝</div><div class="action-name">内容与社区管理</div></div>
            <div class="action-item" @click="go('/admin/stats')"><div class="action-icon">📈</div><div class="action-name">财务与数据面板</div></div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card class="task-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">📋 待办任务</span>
              <span class="card-subtitle">需要处理的事项</span>
            </div>
          </template>
          <div class="task-list">
            <div class="task-item"><div class="task-status pending">待处理</div><div class="task-content"><div class="task-title">评论审核</div><div class="task-count">{{ stats.pendingCommentCount }} 条待审核</div></div></div>
            <div class="task-item"><div class="task-status info">进行中</div><div class="task-content"><div class="task-title">商品上架维护</div><div class="task-count">上架商品 {{ stats.onSaleProductCount }} 件</div></div></div>
            <div class="task-item"><div class="task-status info">进行中</div><div class="task-content"><div class="task-title">今日订单跟进</div><div class="task-count">今日新增 {{ stats.todayOrderCount }} 单</div></div></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'

const router = useRouter()
const stats = reactive({ userCount: 0, pendingCommentCount: 0, onSaleProductCount: 0, todayOrderCount: 0 })

function go(path) {
  router.push(path)
}

async function loadStats() {
  const res = await http.get('/admin/dashboard/stats')
  Object.assign(stats, res.data?.data || {})
}

onMounted(loadStats)
</script>

<style scoped>
.dashboard { width: 100%; }
.header-section { margin-bottom: 32px; }
.page-title { font-size: 28px; font-weight: 700; margin: 0 0 8px; color: #333; }
.page-subtitle { font-size: 14px; color: #999; margin: 0; }
.stats-grid { margin-bottom: 32px; }
.stat-card { background: #fff; border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 16px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); transition: all 0.3s ease; border-left: 4px solid; cursor: pointer; }
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12); }
.stat-card.primary { border-left-color: #667eea; }
.stat-card.warning { border-left-color: #f59e0b; }
.stat-card.success { border-left-color: #10b981; }
.stat-card.info { border-left-color: #06b6d4; }
.stat-icon { font-size: 32px; flex-shrink: 0; }
.stat-info { flex: 1; }
.stat-label { color: #999; font-size: 13px; margin-bottom: 4px; }
.stat-value { font-size: 24px; font-weight: 700; color: #333; }
.content-grid { margin-bottom: 24px; }
.action-card, .task-card { border-radius: 12px; border: 1px solid #f0f0f0; height: 100%; }
.card-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px; }
.card-title { font-size: 16px; font-weight: 700; color: #333; }
.card-subtitle { font-size: 12px; color: #999; }
.action-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.action-item { padding: 16px; border-radius: 8px; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); text-align: center; cursor: pointer; transition: all 0.3s ease; border: 2px solid transparent; }
.action-item:hover { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; border-color: #667eea; transform: translateY(-2px); }
.action-icon { font-size: 28px; margin-bottom: 8px; }
.action-name { font-size: 13px; font-weight: 600; }
.task-list { display: flex; flex-direction: column; gap: 12px; }
.task-item { display: flex; align-items: center; gap: 12px; padding: 12px; border-radius: 8px; background: #f9fafb; transition: all 0.3s ease; }
.task-item:hover { background: #f0f0f0; }
.task-status { padding: 4px 12px; border-radius: 4px; font-size: 11px; font-weight: 700; white-space: nowrap; color: #fff; }
.task-status.pending { background: #f59e0b; }
.task-status.info { background: #06b6d4; }
.task-content { flex: 1; }
.task-title { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 2px; }
.task-count { font-size: 12px; color: #999; }
</style>
