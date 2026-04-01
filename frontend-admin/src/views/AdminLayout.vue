<template>
  <el-container class="layout">
    <el-aside width="260px" class="aside">
      <div class="brand">
        <div class="brand-icon">⚙️</div>
        <div class="brand-text">
          <div class="brand-name">XHXT</div>
          <div class="brand-sub">管理中心</div>
        </div>
      </div>
      <el-menu
        class="menu"
        :default-active="activeMenu"
        router
        background-color="transparent"
        text-color="#8b92a9"
        active-text-color="#00d4ff"
      >
        <el-menu-item index="/admin" class="menu-item">
          <span class="menu-icon">📊</span>
          <span>首页概览</span>
        </el-menu-item>
        <el-menu-item index="/admin/users" class="menu-item">
          <span class="menu-icon">👥</span>
          <span>用户与权限</span>
        </el-menu-item>
        <el-menu-item index="/admin/products" class="menu-item">
          <span class="menu-icon">📦</span>
          <span>商品中心</span>
        </el-menu-item>
        <el-menu-item index="/admin/content" class="menu-item">
          <span class="menu-icon">📝</span>
          <span>内容与社区</span>
        </el-menu-item>
        <el-menu-item index="/admin/stats" class="menu-item">
          <span class="menu-icon">📈</span>
          <span>财务与数据</span>
        </el-menu-item>
        <el-menu-item index="/admin/profile" class="menu-item">
          <span class="menu-icon">⚙️</span>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="header-title">管理后台</span>
        </div>
        <div class="header-right">
          <div class="user-info" v-if="user">
            <span class="user-avatar">{{ (user.nickname || user.account).charAt(0).toUpperCase() }}</span>
            <div class="user-details">
              <div class="user-name">{{ user.nickname || user.account }}</div>
              <div class="user-role">{{ user.role === 'ADMIN' ? '管理员' : '员工' }}</div>
            </div>
          </div>
          <el-button type="danger" text @click="logout">退出</el-button>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const user = computed(() => {
  const raw = localStorage.getItem('user')
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
})

const activeMenu = computed(() => route.path)

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('user')
  ElMessage.info('已退出')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100vh;
  width: 100%;
}

.aside {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: #fff;
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.brand-icon {
  font-size: 32px;
}

.brand-text {
  flex: 1;
}

.brand-name {
  font-weight: 700;
  font-size: 16px;
  color: #fff;
}

.brand-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 2px;
}

.menu {
  border-right: none;
  background: transparent;
  padding: 12px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.menu-icon {
  font-size: 18px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-title {
  font-weight: 700;
  font-size: 18px;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  color: #333;
  font-weight: 600;
  font-size: 14px;
}

.user-role {
  color: #999;
  font-size: 12px;
}

.main {
  padding: 16px 20px;
  overflow: auto;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}
</style>
