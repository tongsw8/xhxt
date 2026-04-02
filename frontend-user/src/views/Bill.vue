<template>
  <div class="bill-page" v-loading="loading">
    <el-card class="bill-card">
      <template #header>
        <div class="bill-head">
          <h2>订单账单</h2>
          <el-tag :type="statusTag.type">{{ statusTag.text }}</el-tag>
        </div>
      </template>

      <div v-if="orderNo" class="order-no">订单号：{{ orderNo }}</div>
      <div class="order-no">收货人：{{ receiverName }} {{ receiverPhone }}</div>
      <div class="order-no">收货地址：{{ receiverAddress }}</div>
      <div class="tip">请在 30 分钟内完成支付，超时订单将自动关闭。剩余：{{ countdownText }}</div>

      <el-table :data="items" stripe>
        <el-table-column prop="productName" label="商品" min-width="220" />
        <el-table-column prop="productPrice" label="单价" width="120">
          <template #default="{ row }">¥{{ Number(row.productPrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column label="小计" width="120">
          <template #default="{ row }">¥{{ (Number(row.productPrice || 0) * Number(row.quantity || 0)).toFixed(2) }}</template>
        </el-table-column>
      </el-table>

      <div class="summary">
        <div>商品总数：<b>{{ totalQuantity }}</b></div>
        <div>应付金额：<span class="amount">¥{{ totalAmount.toFixed(2) }}</span></div>
      </div>

      <div class="actions">
        <el-button @click="onBack">返回（稍后支付）</el-button>
        <el-button type="primary" :disabled="status !== 0" @click="onPay">立即支付</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, payOrder } from '../api/order'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const orderNo = ref('')
const items = ref([])
const totalQuantity = ref(0)
const totalAmount = ref(0)
const status = ref(0)
const receiverName = ref('')
const receiverPhone = ref('')
const receiverAddress = ref('')
const expireMs = ref(0)
const nowMs = ref(Date.now())
let timer = null

const statusTag = computed(() => {
  if (status.value === 1) return { text: '已支付', type: 'success' }
  if (status.value === 4) return { text: '已关闭', type: 'info' }
  return { text: '待支付', type: 'warning' }
})

const countdownText = computed(() => {
  if (status.value !== 0) return '--'
  const left = Math.max(0, expireMs.value - nowMs.value)
  const m = Math.floor(left / 60000)
  const s = Math.floor((left % 60000) / 1000)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

async function loadDetail() {
  orderNo.value = String(route.query.orderNo || '')
  if (!orderNo.value) {
    ElMessage.error('订单号为空')
    router.replace('/cart')
    return
  }
  loading.value = true
  try {
    const { data } = await getOrderDetail(orderNo.value)
    const order = data.data?.order || {}
    items.value = data.data?.items || []
    totalQuantity.value = Number(data.data?.totalQuantity || 0)
    totalAmount.value = Number(order.totalAmount || 0)
    status.value = Number(order.status || 0)
    receiverName.value = order.receiverName || ''
    receiverPhone.value = order.receiverPhone || ''
    receiverAddress.value = order.receiverAddress || ''
    expireMs.value = order.expireTime ? new Date(order.expireTime).getTime() : 0
  } catch (e) {
    ElMessage.error(e?.message || '加载账单失败')
  } finally {
    loading.value = false
  }
}

async function onPay() {
  try {
    await payOrder(orderNo.value)
    ElMessage.success('支付成功')
    router.push('/orders')
  } catch (e) {
    ElMessage.error(e?.message || '支付失败')
  }
}

function onBack() {
  ElMessage.info('订单已保留，可在 30 分钟内支付')
  router.push('/orders')
}

onMounted(async () => {
  await loadDetail()
  timer = setInterval(() => {
    nowMs.value = Date.now()
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.bill-card { border-radius: 12px; }
.bill-head { display: flex; justify-content: space-between; align-items: center; }
.bill-head h2 { margin: 0; }
.order-no { margin-bottom: 8px; color: #666; }
.tip { margin-bottom: 12px; color: #e67e22; font-size: 13px; }
.summary { margin-top: 14px; display: flex; justify-content: space-between; font-size: 16px; }
.amount { color: #e74c3c; font-size: 24px; font-weight: 700; }
.actions { margin-top: 16px; display: flex; justify-content: flex-end; gap: 10px; }
</style>
