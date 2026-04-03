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
            <div class="product-info"><div class="name">{{ row.productName }}</div><div class="price">¥{{ Number(row.price || 0).toFixed(2) }}</div></div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="数量" width="140"><template #default="{ row }"><el-input-number v-model="row.quantity" :min="1" @change="onQuantityChange(row)" /></template></el-table-column>
      <el-table-column label="小计" width="120"><template #default="{ row }">¥{{ (Number(row.price || 0) * row.quantity).toFixed(2) }}</template></el-table-column>
      <el-table-column label="操作" width="100"><template #default="{ row }"><el-button type="danger" size="small" text @click="onRemove(row)">删除</el-button></template></el-table-column>
    </el-table>

    <div v-if="cartList.length > 0" class="address-box">
      <div class="address-head">
        <div class="label">选择收货地址：</div>
        <el-button size="small" type="primary" plain @click="openAddressDialog">新增地址</el-button>
      </div>
      <el-radio-group v-model="selectedAddressId" v-if="addressList.length > 0">
        <el-radio v-for="a in addressList" :key="a.id" :value="a.id">{{ a.receiverName }} {{ a.receiverPhone }} {{ fullAddress(a) }} <span v-if="a.isDefault === 1">（默认）</span></el-radio>
      </el-radio-group>
      <el-empty v-else description="暂无收货地址，请先新增" />

      <el-form class="extra-form" label-width="110px">
        <el-form-item label="贺卡留言">
          <el-input v-model="cardMessage" maxlength="200" show-word-limit placeholder="请输入给收件人的祝福语（可选）" />
        </el-form-item>
        <el-form-item label="配送时间要求">
          <el-date-picker v-model="deliveryExpectTime" type="datetime" placeholder="选择配送时间（可选）" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
      </el-form>
    </div>

    <div v-if="cartList.length > 0" class="summary">
      <div class="total"><span>合计：</span><span class="amount">¥{{ totalAmount.toFixed(2) }}</span></div>
      <el-button type="primary" size="large" :disabled="!selectedAddressId" @click="onCheckout">去结算</el-button>
    </div>

    <el-dialog v-model="addressDialogVisible" title="新增收货地址" width="560px">
      <el-form :model="addressForm" label-width="90px">
        <el-form-item label="收货人"><el-input v-model="addressForm.receiverName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="addressForm.receiverPhone" /></el-form-item>
        <el-form-item label="省"><el-input v-model="addressForm.province" /></el-form-item>
        <el-form-item label="市"><el-input v-model="addressForm.city" /></el-form-item>
        <el-form-item label="区"><el-input v-model="addressForm.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addressForm.detailAddress" /></el-form-item>
        <el-form-item label="设为默认"><el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitAddress">保存地址</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCartList, updateCartQuantity, removeFromCart } from '../api/cart'
import { createOrderFromCart } from '../api/order'
import { listAddress, saveAddress } from '../api/user'

const router = useRouter()
const loading = ref(false)
const cartList = ref([])
const addressList = ref([])
const selectedAddressId = ref(null)
const cardMessage = ref('')
const deliveryExpectTime = ref('')
const addressDialogVisible = ref(false)
const addressForm = reactive({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: 1 })

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
  } finally { loading.value = false }
}

async function loadAddress() {
  const { data } = await listAddress()
  addressList.value = data.data || []
  const def = addressList.value.find((x) => x.isDefault === 1)
  selectedAddressId.value = def ? def.id : addressList.value[0]?.id || null
}

async function onQuantityChange(row) {
  try { await updateCartQuantity(row.productId, row.quantity); ElMessage.success('更新成功') }
  catch { ElMessage.error('更新失败'); await loadCart() }
}

async function onRemove(row) {
  try { await removeFromCart(row.productId); ElMessage.success('删除成功'); await loadCart() }
  catch { ElMessage.error('删除失败') }
}

function openAddressDialog() {
  Object.assign(addressForm, { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: addressList.value.length ? 0 : 1 })
  addressDialogVisible.value = true
}

async function submitAddress() {
  await saveAddress(addressForm)
  ElMessage.success('地址保存成功')
  addressDialogVisible.value = false
  await loadAddress()
}

async function onCheckout() {
  if (!selectedAddressId.value) return ElMessage.warning('请先选择收货地址')
  const { data } = await createOrderFromCart(selectedAddressId.value, cardMessage.value, deliveryExpectTime.value)
  const orderNo = data.data?.orderNo
  if (!orderNo) return ElMessage.error('创建订单失败')
  router.push({ path: '/bill', query: { orderNo } })
}

function toImageUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/')) return `${backendOrigin}${path}`
  return `${backendOrigin}/xhxt/file/flower/${path}`
}

onMounted(async () => { await loadCart(); await loadAddress() })
</script>

<style scoped>
.cart-page { width: 100%; }
.header { background: #fff; border-radius: 12px; padding: 16px 20px; margin-bottom: 16px; }
.header h2 { margin: 0; font-size: 20px; }
.header p { margin: 6px 0 0; color: #666; font-size: 14px; }
.product-cell { display: flex; gap: 12px; align-items: center; }
.product-info .name { font-weight: 600; color: #333; }
.product-info .price { margin-top: 4px; color: #e74c3c; font-weight: 700; }
.address-box { background: #fff; border-radius: 12px; padding: 16px 20px; margin-bottom: 16px; }
.address-head { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
.label { font-weight: 600; }
.extra-form { margin-top: 14px; }
.summary { background: #fff; border-radius: 12px; padding: 16px 20px; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 16px; font-weight: 600; }
.amount { color: #e74c3c; font-size: 24px; margin-left: 8px; }
</style>