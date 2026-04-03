<template>
  <el-container class="layout">
    <el-aside width="260px" class="aside">
      <div class="brand">
        <div class="brand-icon">🌸</div>
        <div class="brand-text">
          <div class="brand-name">XHXT</div>
          <div class="brand-sub">花卉商城</div>
        </div>
      </div>
      <el-menu
        class="menu"
        :default-active="activeMenu"
        router
        background-color="transparent"
        text-color="#8b92a9"
        active-text-color="#ff6b9d"
      >
        <el-menu-item index="/" class="menu-item">
          <span class="menu-icon">🏠</span>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/shop" class="menu-item">
          <span class="menu-icon">🛍️</span>
          <span>商城</span>
        </el-menu-item>
        <el-menu-item index="/cart" class="menu-item">
          <span class="menu-icon">🛒</span>
          <span>购物车</span>
        </el-menu-item>
        <el-menu-item index="/community" class="menu-item">
          <span class="menu-icon">💬</span>
          <span>社区</span>
        </el-menu-item>
        <el-menu-item index="/notice" class="menu-item">
          <span class="menu-icon">📢</span>
          <span>公告</span>
        </el-menu-item>
        <el-menu-item index="/orders" class="menu-item">
          <span class="menu-icon">📦</span>
          <span>订单中心</span>
        </el-menu-item>
        <el-menu-item index="/favorites" class="menu-item">
          <span class="menu-icon">❤️</span>
          <span>我的收藏</span>
        </el-menu-item>
        <el-menu-item index="/profile" class="menu-item">
          <span class="menu-icon">👤</span>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="header-title">用户商城</span>
        </div>
        <div class="header-right">
          <div class="user-info" v-if="user">
            <span class="user-avatar">{{ (user.nickname || user.account).charAt(0).toUpperCase() }}</span>
            <span class="user-name">{{ user.nickname || user.account }}</span>
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
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
  gap: 8px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.user-name {
  color: #333;
  font-weight: 600;
  font-size: 14px;
}

.main {
  padding: 16px 20px;
  overflow: auto;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}
</style>
