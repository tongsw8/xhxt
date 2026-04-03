<template>
  <div class="favorite-page">
    <div class="header">
      <h2>我的收藏</h2>
      <p v-if="favoriteList.length > 0">共 {{ favoriteList.length }} 件商品</p>
      <p v-else>暂无收藏</p>
    </div>

    <el-empty v-if="!loading && favoriteList.length === 0" description="暂无收藏，去商城逛逛吧" style="margin-top: 40px">
      <el-button type="primary" @click="$router.push('/shop')">去购物</el-button>
    </el-empty>

    <el-row v-else :gutter="16" class="product-grid" v-loading="loading">
      <el-col v-for="item in favoriteList" :key="item.productId" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="product-card" shadow="hover">
          <el-image
            class="product-cover"
            :src="toImageUrl(item.coverImg)"
            fit="cover"
          >
            <template #error>
              <div class="img-fallback">暂无图片</div>
            </template>
          </el-image>
          <div class="product-body">
            <div class="name">{{ item.productName }}</div>
            <div class="desc">{{ item.detailText || '暂无介绍' }}</div>
            <div class="foot">
              <span class="price">¥{{ Number(item.price || 0).toFixed(2) }}</span>
              <el-button type="danger" size="small" text @click="onRemove(item)">取消收藏</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getFavoriteList, removeFromFavorite } from '../api/cart'

const loading = ref(false)
const favoriteList = ref([])

const backendOrigin = (() => {
  const base = import.meta.env.VITE_BASE_API || ''
  const m = base.match(/^(https?:\/\/[^/]+)/)
  return m ? m[1] : window.location.origin
})()

async function loadFavorites() {
  loading.value = true
  try {
    const { data } = await getFavoriteList()
    favoriteList.value = data.data || []
  } catch (e) {
    ElMessage.error(e?.message || '加载收藏失败')
  } finally {
    loading.value = false
  }
}

async function onRemove(item) {
  try {
    await removeFromFavorite(item.productId)
    ElMessage.success('已取消收藏')
    await loadFavorites()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

function toImageUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/')) return `${backendOrigin}${path}`
  return `${backendOrigin}/xhxt/file/flower/${path}`
}

onMounted(loadFavorites)
</script>

<style scoped>
.favorite-page {
  width: 100%;
}

.header {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
}

.header h2 {
  margin: 0;
  font-size: 20px;
}

.header p {
  margin: 6px 0 0;
  color: #666;
  font-size: 14px;
}

.product-grid {
  margin-bottom: 16px;
}

.product-card {
  border-radius: 12px;
  margin-bottom: 16px;
}

.product-cover {
  width: 100%;
  height: 170px;
  border-radius: 10px;
  overflow: hidden;
  background: #f5f6fa;
}

.img-fallback {
  height: 170px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.product-body {
  padding-top: 10px;
}

.name {
  font-size: 15px;
  font-weight: 700;
  color: #333;
}

.desc {
  margin-top: 8px;
  min-height: 36px;
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

.foot {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price {
  color: #e74c3c;
  font-size: 18px;
  font-weight: 700;
}
</style>
