<template>
  <div class="module">
    <div class="module-hero">
      <div class="module-hero-content">
        <div class="module-icon">{{ iconMap[title] || '🔧' }}</div>
        <div class="module-info">
          <h2 class="module-title">{{ title }}</h2>
          <p class="module-desc">{{ desc }}</p>
        </div>
      </div>
      <el-tag type="warning" size="large">开发中</el-tag>
    </div>

    <el-row :gutter="20" class="cards-row">
      <el-col :xs="24" :sm="12">
        <el-card class="info-card">
          <template #header>
            <span class="card-title">📋 功能清单</span>
          </template>
          <ul class="feature-list">
            <li><span class="dot done"></span> 菜单导航与路由已接入</li>
            <li><span class="dot pending"></span> 列表查询与分页</li>
            <li><span class="dot pending"></span> 新增 / 编辑表单</li>
            <li><span class="dot pending"></span> 审核 / 删除操作</li>
            <li><span class="dot pending"></span> 后端接口联调</li>
          </ul>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card class="info-card">
          <template #header>
            <span class="card-title">⚡ 示例操作</span>
          </template>
          <div class="action-buttons">
            <el-button size="large" @click="noop"><span>🔍</span> 查看列表</el-button>
            <el-button type="primary" size="large" @click="noop"><span>➕</span> 新增</el-button>
            <el-button type="success" size="large" @click="noop"><span>✅</span> 审核</el-button>
          </div>
          <div class="api-tip">
            <span class="api-label">API 地址：</span>
            <code>/xhxt/api/**</code>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="back-bar">
      <el-button @click="goHome">← 返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const title = computed(() => route.meta?.title || '模块')
const desc = computed(() => route.meta?.desc || '功能模块开发中...')

const iconMap = {
  '商城': '🛍️',
  '社区': '💬',
  '订单中心': '📦',
  '我的收藏': '❤️',
  '个人中心': '👤',
}

function goHome() {
  router.push('/')
}

function noop() {}
</script>

<style scoped>
.module {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.module-hero {
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border-left: 5px solid #667eea;
}

.module-hero-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.module-icon {
  font-size: 48px;
}

.module-title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0 0 6px;
}

.module-desc {
  color: #888;
  font-size: 14px;
  margin: 0;
}

.cards-row {
  margin: 0;
}

.info-card {
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}

.card-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #555;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot.done {
  background: #10b981;
}

.dot.pending {
  background: #d1d5db;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.api-tip {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 13px;
  color: #666;
}

.api-label {
  font-weight: 600;
  color: #333;
}

code {
  background: #e5e7eb;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  color: #667eea;
}

.back-bar {
  display: flex;
  justify-content: flex-start;
}
</style>
