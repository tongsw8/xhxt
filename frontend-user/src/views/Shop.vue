<template>
  <div class="shop-page">
    <div class="hero">
      <div>
        <h2>花语商城</h2>
        <p>甄选新鲜花束，每一份都带着温度</p>
      </div>
      <el-input v-model="query.keyword" placeholder="搜索商品名称" clearable style="width: 260px" @keyup.enter="onSearch">
        <template #append><el-button @click="onSearch">搜索</el-button></template>
      </el-input>
    </div>

    <el-card class="category-card" shadow="never">
      <div class="category-wrap">
        <el-tag class="category-tag" :effect="query.categoryId === null ? 'dark' : 'plain'" @click="changeCategory(null)">全部</el-tag>
        <el-tag v-for="c in categoryList" :key="c.id" class="category-tag" :effect="query.categoryId === c.id ? 'dark' : 'plain'" @click="changeCategory(c.id)">{{ c.categoryName }}</el-tag>
      </div>
    </el-card>

    <el-row :gutter="16" class="product-grid" v-loading="loading">
      <el-col v-for="item in productList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="product-card" shadow="hover">
          <el-image class="product-cover" :src="toImageUrl(item.coverImg)" fit="cover" @click="goDetail(item.id)">
            <template #error><div class="img-fallback">暂无图片</div></template>
          </el-image>
          <div class="product-body">
            <div class="name" @click="goDetail(item.id)">{{ item.productName }}</div>
            <div class="meta">{{ item.categoryName || '未分类' }}</div>
            <div class="desc">{{ item.detailText || '暂无商品介绍' }}</div>
            <div class="foot">
              <span class="price">¥{{ Number(item.price || 0).toFixed(2) }}</span>
              <div class="actions">
                <el-button type="primary" size="small" @click="onAddToCart(item)">加入购物车</el-button>
                <el-button size="small" @click="goDetail(item.id)">查看详情</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && productList.length === 0" description="暂无商品" />

    <div class="pager" v-if="pager.total > 0">
      <el-pagination background layout="total, prev, pager, next" :total="pager.total" :current-page="pager.pageNo" :page-size="pager.pageSize" @current-change="onPageChange" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchShopCategories, fetchShopProducts } from '../api/shop'
import { addToCart } from '../api/cart'

const router = useRouter()
const loading = ref(false)
const categoryList = ref([])
const productList = ref([])

const query = reactive({ keyword: '', categoryId: null })
const pager = reactive({ pageNo: 1, pageSize: 12, total: 0 })

const backendOrigin = (() => {
  const base = import.meta.env.VITE_BASE_API || ''
  const m = base.match(/^(https?:\/\/[^/]+)/)
  return m ? m[1] : window.location.origin
})()

async function loadCategories() {
  const { data } = await fetchShopCategories()
  categoryList.value = (data.data || []).filter((x) => x.status === 1)
}

async function loadProducts() {
  loading.value = true
  try {
    const { data } = await fetchShopProducts({ keyword: query.keyword || undefined, categoryId: query.categoryId || undefined, pageNo: pager.pageNo, pageSize: pager.pageSize })
    const pageData = data.data || {}
    productList.value = pageData.records || []
    pager.total = Number(pageData.total || 0)
  } catch (e) {
    ElMessage.error(e?.message || '加载商品失败')
  } finally {
    loading.value = false
  }
}

function onSearch() { pager.pageNo = 1; loadProducts() }
function changeCategory(id) { query.categoryId = id; pager.pageNo = 1; loadProducts() }
function onPageChange(page) { pager.pageNo = page; loadProducts() }

async function onAddToCart(item) {
  try {
    await addToCart(item.id, 1)
    ElMessage.success('已加入购物车')
  } catch (e) {
    ElMessage.error(e?.message || '加入购物车失败')
  }
}

function goDetail(id) { router.push(`/shop/detail/${id}`) }

function toImageUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/')) return `${backendOrigin}${path}`
  return `${backendOrigin}/xhxt/file/flower/${path}`
}

onMounted(async () => { await loadCategories(); await loadProducts() })
</script>

<style scoped>
.shop-page { width: 100%; }
.hero { background:#fff; border-radius:14px; padding:18px 20px; margin-bottom:14px; display:flex; justify-content:space-between; align-items:center; gap:12px; }
.hero h2 { margin: 0; }
.hero p { margin: 6px 0 0; color:#666; }
.category-card { margin-bottom:14px; border-radius:12px; }
.category-wrap { display:flex; flex-wrap:wrap; gap:10px; }
.category-tag { cursor:pointer; }
.product-grid { margin-bottom:16px; }
.product-card { border-radius:12px; margin-bottom:16px; }
.product-cover { width:100%; height:170px; border-radius:10px; overflow:hidden; background:#f5f6fa; cursor:pointer; }
.img-fallback { height:170px; display:flex; align-items:center; justify-content:center; color:#999; }
.product-body { padding-top:10px; }
.name { font-size:15px; font-weight:700; color:#333; cursor:pointer; }
.meta { margin-top:4px; font-size:12px; color:#888; }
.desc { margin-top:8px; min-height:36px; font-size:13px; color:#666; line-height:1.4; }
.foot { margin-top:10px; display:flex; align-items:center; justify-content:space-between; }
.price { color:#e74c3c; font-size:18px; font-weight:700; }
.actions { display:flex; gap:6px; }
.pager { display:flex; justify-content:flex-end; }
</style>