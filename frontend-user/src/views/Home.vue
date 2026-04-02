<template>
  <div class="dashboard">
    <div class="hero-section">
      <el-carousel height="220px" indicator-position="outside" v-if="banners.length">
        <el-carousel-item v-for="b in banners" :key="b.id">
          <div class="hero-slide" :style="{ backgroundImage: `url(${b.imgUrl})` }" @click="onBannerClick(b)">
            <div class="hero-mask">
              <h1 class="hero-title">{{ b.title || '欢迎来到 XHXT 花卉商城' }}</h1>
              <p class="hero-subtitle">精选鲜花，传递美好祝福</p>
              <el-button type="primary" size="large" @click="go('/shop')">立即购买</el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <div v-else class="hero-fallback">
        <h1 class="hero-title">欢迎来到 XHXT 花卉商城</h1>
        <p class="hero-subtitle">精选鲜花，传递美好祝福</p>
        <el-button type="primary" size="large" @click="go('/shop')">立即购买</el-button>
      </div>
    </div>

    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6"><div class="stat-card" @click="go('/favorites')"><div class="stat-icon">❤️</div><div class="stat-label">我的收藏</div><div class="stat-value">{{ favoriteCount }}</div></div></el-col>
        <el-col :xs="12" :sm="6"><div class="stat-card" @click="go('/cart')"><div class="stat-icon">🛒</div><div class="stat-label">购物车商品</div><div class="stat-value">{{ cartCount }}</div></div></el-col>
        <el-col :xs="12" :sm="6"><div class="stat-card" @click="go('/orders?status=0')"><div class="stat-icon">💳</div><div class="stat-label">待付款订单</div><div class="stat-value">{{ pendingPayCount }}</div></div></el-col>
        <el-col :xs="12" :sm="6"><div class="stat-card" @click="go('/orders?status=2')"><div class="stat-icon">📦</div><div class="stat-label">待收货订单</div><div class="stat-value">{{ pendingReceiveCount }}</div></div></el-col>
      </el-row>
    </div>

    <div class="quick-access">
      <h3 class="section-title">快捷入口</h3>
      <el-space wrap style="margin-bottom: 14px">
        <el-tag effect="dark" @click="go('/shop')">去逛商城</el-tag>
        <el-tag type="success" @click="go('/cart')">购物车下单</el-tag>
        <el-tag type="warning" @click="go('/orders')">订单中心</el-tag>
        <el-tag type="info" @click="go('/profile')">地址与资料</el-tag>
      </el-space>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="8"><div class="quick-card" @click="go('/shop')"><div class="quick-icon">🛍️</div><div class="quick-name">商城</div><div class="quick-desc">浏览精选鲜花</div></div></el-col>
        <el-col :xs="12" :sm="8"><div class="quick-card" @click="go('/community')"><div class="quick-icon">💬</div><div class="quick-name">社区</div><div class="quick-desc">分享养护心得</div></div></el-col>
        <el-col :xs="12" :sm="8"><div class="quick-card" @click="go('/orders')"><div class="quick-icon">📋</div><div class="quick-name">订单中心</div><div class="quick-desc">查看订单状态</div></div></el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCartList, getFavoriteList } from '../api/cart'
import { listOrders } from '../api/order'
import http from '../api/http'

const router = useRouter()
const cartCount = ref(0)
const favoriteCount = ref(0)
const pendingPayCount = ref(0)
const pendingReceiveCount = ref(0)
const banners = ref([])

async function loadBanners() {
  const res = await http.get('/content/banners')
  banners.value = res.data || []
}

async function loadStats() {
  try { const cartRes = await getCartList(); cartCount.value = (cartRes.data.data || []).length } catch (e) { console.error(e) }
  try { const favRes = await getFavoriteList(); favoriteCount.value = (favRes.data.data || []).length } catch (e) { console.error(e) }
  try {
    const orderRes = await listOrders()
    const list = orderRes.data.data || []
    pendingPayCount.value = list.filter(x => x.status === 0).length
    pendingReceiveCount.value = list.filter(x => x.status === 2).length
  } catch (e) { console.error(e) }
}

function go(path) { router.push(path) }

function onBannerClick(b) {
  if (b?.linkUrl && /^\/shop\/detail\/\d+/.test(b.linkUrl)) {
    router.push(b.linkUrl)
    return
  }
  go('/shop')
}

onMounted(async () => {
  await Promise.all([loadBanners(), loadStats()])
})
</script>

<style scoped>
.dashboard { width: 100%; }
.hero-section { margin-bottom: 32px; }
.hero-slide { height: 220px; border-radius: 12px; background-size: cover; background-position: center; overflow: hidden; cursor: pointer; }
.hero-mask { width: 100%; height: 100%; background: linear-gradient(135deg, rgba(29, 78, 216, 0.55), rgba(109, 40, 217, 0.55)); color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.hero-fallback { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 12px; padding: 48px 32px; color: #fff; text-align: center; }
.hero-title { font-size: 32px; font-weight: 700; margin: 0 0 12px; }
.hero-subtitle { font-size: 16px; margin: 0 0 24px; opacity: 0.9; }
.stats-section { margin-bottom: 32px; }
.stat-card { background: #fff; border-radius: 12px; padding: 24px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,.08); transition: all .3s ease; cursor: pointer; }
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 8px 16px rgba(0,0,0,.12); }
.stat-icon { font-size: 32px; margin-bottom: 12px; }
.stat-label { color: #666; font-size: 13px; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 700; color: #333; }
.quick-access { margin-bottom: 16px; }
.section-title { font-size: 18px; font-weight: 700; margin: 0 0 16px; color: #333; }
.quick-access :deep(.el-tag) { cursor: pointer; }
.quick-card { background: #fff; border-radius: 12px; padding: 24px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,.08); transition: all .3s ease; cursor: pointer; border: 2px solid transparent; }
.quick-card:hover { transform: translateY(-4px); box-shadow: 0 8px 16px rgba(0,0,0,.12); border-color: #667eea; }
.quick-icon { font-size: 40px; margin-bottom: 12px; }
.quick-name { font-size: 16px; font-weight: 700; color: #333; margin-bottom: 4px; }
.quick-desc { font-size: 12px; color: #999; }
</style>