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
          <div class="stat-info"><div class="stat-label">注册用户数</div><div class="stat-value">{{ stats.userCount }}</div></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card warning" @click="go('/admin/content')">
          <div class="stat-icon">💬</div>
          <div class="stat-info"><div class="stat-label">待审核评论</div><div class="stat-value">{{ stats.pendingCommentCount }}</div></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card success" @click="go('/admin/products')">
          <div class="stat-icon">📦</div>
          <div class="stat-info"><div class="stat-label">上架商品</div><div class="stat-value">{{ stats.onSaleProductCount }}</div></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card info" @click="go('/admin/orders')">
          <div class="stat-icon">📋</div>
          <div class="stat-info"><div class="stat-label">今日订单</div><div class="stat-value">{{ stats.todayOrderCount }}</div></div>
        </div>
      </el-col>
    </el-row>

    <el-card class="quick-tags" shadow="never">
      <template #header>快捷入口</template>
      <el-space wrap>
        <el-tag effect="dark" @click="go('/admin/orders')">订单列表</el-tag>
        <el-tag type="success" @click="go('/admin/products')">商品中心</el-tag>
        <el-tag type="warning" @click="go('/admin/content')">评论审核</el-tag>
        <el-tag type="info" @click="go('/admin/users')">用户管理</el-tag>
      </el-space>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'

const router = useRouter()
const stats = reactive({ userCount: 0, pendingCommentCount: 0, onSaleProductCount: 0, todayOrderCount: 0 })
function go(path) { router.push(path) }
async function loadStats() { const res = await http.get('/admin/dashboard/stats'); Object.assign(stats, res.data?.data || {}) }
onMounted(loadStats)
</script>

<style scoped>
.dashboard { width: 100%; }
.header-section { margin-bottom: 32px; }
.page-title { font-size: 28px; font-weight: 700; margin: 0 0 8px; color: #333; }
.page-subtitle { font-size: 14px; color: #999; margin: 0; }
.stats-grid { margin-bottom: 20px; }
.quick-tags { margin-bottom: 16px; border-radius: 12px; }
.quick-tags :deep(.el-tag) { cursor: pointer; }
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
</style>