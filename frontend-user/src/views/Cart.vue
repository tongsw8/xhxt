<template>
  <div class="cart-page">
    <div class="header">
      <h2>购物车</h2>
      <p v-if="cartList.length > 0">共 {{ cartList.length }} 件商品</p>
      <p v-else>购物车为空</p>
    </div>

    <el-empty v-if="!loading && cartList.length === 0" description="购物车为空，去商城逛逛吧" style="margin-top: 40px">
      <el-button type="primary" @click="$router.push('/shop')">去购物</el-button>
    </el-empty>

    <el-table v-else :data="cartList" stripe v-loading="loading" style="margin-bottom: 20px">
      <el-table-column label="商品" min-width="300">
        <template #default="{ row }">
          <div class="product-cell">
            <el-image :src="toImageUrl(row.coverImg)" fit="cover" style="width: 60px; height: 60px; border-radius: 6px" />
            <div class="product-info">
              <div class="name">{{ row.productName }}</div>
              <div class="price">¥{{ Number(row.price || 0).toFixed(2) }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="数量" width="140">
        <template #default="{ row }">
          <el-input-number v-model="row.quantity" :min="1" @change="onQuantityChange(row)" />
        </template>
      </el-table-column>
      <el-table-column label="小计" width="120">
        <template #default="{ row }">¥{{ (Number(row.price || 0) * row.quantity).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }"><el-button type="danger" size="small" text @click="onRemove(row)">删除</el-button></template>
      </el-table-column>
    </el-table>

    <div v-if="cartList.length > 0" class="address-box">
      <div class="label">选择收货地址：</div>
      <el-radio-group v-model="selectedAddressId" v-if="addressList.length > 0">
        <el-radio v-for="a in addressList" :key="a.id" :label="a.id">
          {{ a.receiverName }} {{ a.receiverPhone }} {{ fullAddress(a) }}
          <span v-if="a.isDefault === 1">（默认）</span>
        </el-radio>
      </el-radio-group>
      <el-empty v-else description="暂无收货地址，请先到个人中心添加">
        <el-button type="primary" @click="goCreateAddress">去新增地址</el-button>
      </el-empty>
    </div>

    <div v-if="cartList.length > 0" class="summary">
      <div class="total"><span>合计：</span><span class="amount">¥{{ totalAmount.toFixed(2) }}</span></div>
      <el-button type="primary" size="large" :disabled="!selectedAddressId" @click="onCheckout">去结算</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCartList, updateCartQuantity, removeFromCart } from '../api/cart'
import { createOrderFromCart } from '../api/order'
import { listAddress } from '../api/user'

const router = useRouter()
const loading = ref(false)
const cartList = ref([])
const addressList = ref([])
const selectedAddressId = ref(null)

const backendOrigin = (() => {
  const base = import.meta.env.VITE_BASE_API || ''
  const m = base.match(/^(https?:\/\/[^/]+)/)
  return m ? m[1] : window.location.origin
})()

const totalAmount = computed(() => cartList.value.reduce((sum, item) => sum + Number(item.price || 0) * item.quantity, 0))
const fullAddress = (a) => (a.province || '') + (a.city || '') + (a.district || '') + (a.detailAddress || '')

async function loadCart() {
  loading.value = true
  try {
    const { data } = await getCartList()
    cartList.value = data.data || []
  } catch (e) {
    ElMessage.error(e?.message || '加载购物车失败')
  } finally {
    loading.value = false
  }
}

async function loadAddress() {
  try {
    const { data } = await listAddress()
    addressList.value = data.data || []
    const def = addressList.value.find((x) => x.isDefault === 1)
    selectedAddressId.value = def ? def.id : addressList.value[0]?.id || null
  } catch {
    addressList.value = []
    selectedAddressId.value = null
  }
}

async function onQuantityChange(row) {
  try {
    await updateCartQuantity(row.productId, row.quantity)
    ElMessage.success('更新成功')
  } catch (e) {
    ElMessage.error(e?.message || '更新失败')
    await loadCart()
  }
}

async function onRemove(row) {
  try {
    await removeFromCart(row.productId)
    ElMessage.success('删除成功')
    await loadCart()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}

function goCreateAddress() {
  router.push({ path: '/profile', query: { tab: 'address', openAddress: '1' } })
}

async function onCheckout() {
  if (!selectedAddressId.value) {
    ElMessage.warning('请先选择收货地址')
    return goCreateAddress()
  }
  try {
    const { data } = await createOrderFromCart(selectedAddressId.value)
    const orderNo = data.data?.orderNo
    if (!orderNo) return ElMessage.error('创建订单失败')
    router.push({ path: '/bill', query: { orderNo } })
  } catch (e) {
    ElMessage.error(e?.message || '创建订单失败')
  }
}

function toImageUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/')) return `${backendOrigin}${path}`
  return `${backendOrigin}/xhxt/file/flower/${path}`
}

onMounted(async () => {
  await loadCart()
  await loadAddress()
})
</script>

<style scoped>
.cart-page { width: 100%; }
.header { background: #fff; border-radius: 12px; padding: 16px 20px; margin-bottom: 16px; }
.header h2 { margin: 0; font-size: 20px; }
.header p { margin: 6px 0 0; color: #666; font-size: 14px; }
.product-cell { display: flex; gap: 12px; align-items: center; }
.product-info { flex: 1; }
.product-info .name { font-weight: 600; color: #333; }
.product-info .price { margin-top: 4px; color: #e74c3c; font-weight: 700; }
.address-box { background: #fff; border-radius: 12px; padding: 16px 20px; margin-bottom: 16px; }
.label { font-weight: 600; margin-bottom: 8px; }
.summary { background: #fff; border-radius: 12px; padding: 16px 20px; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 16px; font-weight: 600; }
.amount { color: #e74c3c; font-size: 24px; margin-left: 8px; }
</style>
