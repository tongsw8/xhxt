<template>
  <div class="detail-page" v-loading="loading">
    <el-card v-if="product" class="detail-card">
      <div class="top">
        <div class="gallery">
          <el-carousel height="340px" indicator-position="outside" v-if="imageList.length">
            <el-carousel-item v-for="(img, idx) in imageList" :key="idx">
              <el-image :src="toImageUrl(img)" fit="cover" class="img" />
            </el-carousel-item>
          </el-carousel>
        </div>
        <div class="info">
          <h2>{{ product.productName }}</h2>
          <p class="desc">{{ product.detailText || '暂无详情' }}</p>
          <div class="price">¥{{ Number(product.price || 0).toFixed(2) }}</div>
          <div class="stock">库存：{{ product.stock }}</div>
          <div class="buy-row">
            <span>购买数量：</span>
            <el-input-number v-model="quantity" :min="1" :max="Math.max(1, Number(product.stock || 0))" />
          </div>
          <div class="actions">
            <el-button @click="addCart">加入购物车</el-button>
            <el-button type="primary" @click="buyNow">立即购买</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="review-card" v-if="product">
      <template #header>
        <div class="review-head">
          <span>商品评价</span>
          <el-tag type="info">共 {{ reviews.length }} 条</el-tag>
        </div>
      </template>
      <el-empty v-if="reviews.length === 0" description="暂无评价" />
      <div v-for="r in reviews" :key="r.id" class="review-item">
        <div class="r-head">{{ r.userName }} · {{ r.createTime }}</div>
        <div class="r-content">{{ r.content }}</div>
        <el-button size="small" text @click="onLikeReview(r)">👍 {{ r.likeCount || 0 }}{{ r.liked ? '（已赞）' : '' }}</el-button>
        <div v-if="(r.replies || []).length" class="reply-block">
          <div v-for="rp in r.replies" :key="rp.id" class="reply-item">
            <span class="reply-tag">官方回复</span>
            <span>{{ rp.content }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="addressVisible" title="选择收货地址" width="720px">
      <el-radio-group v-model="selectedAddressId" style="display:flex;flex-direction:column;gap:8px">
        <el-radio v-for="a in addressList" :key="a.id" :value="a.id">
          {{ a.receiverName }} {{ a.receiverPhone }} {{ fullAddress(a) }}
        </el-radio>
      </el-radio-group>
      <el-form label-width="110px" style="margin-top:12px">
        <el-form-item label="贺卡留言">
          <el-input v-model="cardMessage" maxlength="200" show-word-limit placeholder="可选" />
        </el-form-item>
        <el-form-item label="配送时间要求">
          <el-date-picker v-model="deliveryExpectTime" type="datetime" placeholder="可选" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBuy">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchProductDetail } from '../api/shop'
import { addToCart } from '../api/cart'
import { listAddress } from '../api/user'
import { createDirectOrder, likeProductReview, listProductReviews } from '../api/order'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const product = ref(null)
const quantity = ref(1)
const addressVisible = ref(false)
const addressList = ref([])
const selectedAddressId = ref(null)
const cardMessage = ref('')
const deliveryExpectTime = ref('')
const reviews = ref([])

const backendOrigin = (() => {
  const base = import.meta.env.VITE_BASE_API || ''
  const m = base.match(/^(https?:\/\/[^/]+)/)
  return m ? m[1] : window.location.origin
})()

const imageList = computed(() => {
  const arr = []
  if (product.value?.coverImg) arr.push(product.value.coverImg)
  const imgs = product.value?.detailImgs || []
  imgs.forEach((x) => { if (x && !arr.includes(x)) arr.push(x) })
  return arr
})

async function loadReviews() {
  if (!product.value?.id) return
  const { data } = await listProductReviews(product.value.id)
  reviews.value = data.data || []
}

async function loadDetail() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await fetchProductDetail(id)
    product.value = res.data?.data || null
    await loadReviews()
  } finally {
    loading.value = false
  }
}

async function onLikeReview(row) {
  await likeProductReview(row.id)
  await loadReviews()
}

async function addCart() {
  if (!product.value) return
  await addToCart(product.value.id, quantity.value)
  ElMessage.success('已加入购物车')
}

async function buyNow() {
  const { data } = await listAddress()
  addressList.value = data.data || []
  if (!addressList.value.length) {
    ElMessage.warning('请先在购物车下单页新增收货地址')
    router.push('/cart')
    return
  }
  selectedAddressId.value = addressList.value.find(x => x.isDefault === 1)?.id || addressList.value[0].id
  addressVisible.value = true
}

async function confirmBuy() {
  if (!selectedAddressId.value || !product.value) return
  const res = await createDirectOrder(selectedAddressId.value, product.value.id, quantity.value, cardMessage.value, deliveryExpectTime.value)
  const orderNo = res.data?.data?.orderNo
  addressVisible.value = false
  if (orderNo) router.push({ path: '/bill', query: { orderNo } })
}

function fullAddress(a) {
  return (a.province || '') + (a.city || '') + (a.district || '') + (a.detailAddress || '')
}

function toImageUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/')) return `${backendOrigin}${path}`
  return `${backendOrigin}/xhxt/file/flower/${path}`
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-card { border-radius: 12px; }
.top { display: grid; grid-template-columns: 1.1fr 1fr; gap: 20px; }
.img { width: 100%; height: 340px; border-radius: 10px; }
.info h2 { margin: 0 0 8px; }
.desc { color: #64748b; line-height: 1.6; min-height: 88px; white-space: pre-wrap; }
.price { color: #dc2626; font-size: 30px; font-weight: 700; margin-top: 10px; }
.stock { color: #475569; margin-top: 8px; }
.buy-row { margin-top: 12px; display: flex; align-items: center; gap: 8px; }
.actions { margin-top: 16px; display: flex; gap: 10px; }
.review-card { margin-top: 14px; border-radius: 12px; }
.review-head { display: flex; justify-content: space-between; align-items: center; }
.review-item { padding: 10px 0; border-bottom: 1px dashed #e5e7eb; }
.r-head { color:#64748b; font-size: 12px; margin-bottom: 4px; }
.r-content { color:#0f172a; margin-bottom: 6px; }
</style>