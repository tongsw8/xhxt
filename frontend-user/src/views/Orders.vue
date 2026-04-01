<template>
  <div class="orders-page" v-loading="loading">
    <el-card>
      <template #header>
        <div class="head"><h2>我的订单</h2></div>
      </template>

      <el-tabs v-model="statusTab" @tab-change="onTabChange" style="margin-bottom: 12px">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待付款" name="0" />
        <el-tab-pane label="已支付" name="1" />
        <el-tab-pane label="待收货" name="2" />
        <el-tab-pane label="已关闭" name="4" />
      </el-tabs>

      <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无订单" />

      <el-table v-else :data="filteredOrders" stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="220" />
        <el-table-column prop="totalQuantity" label="件数" width="100" />
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">¥{{ Number(row.totalAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="goBill(row.orderNo)">查看账单</el-button>
            <el-button v-if="row.status === 0" size="small" type="primary" @click="goBill(row.orderNo)">去支付</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listOrders } from '../api/order'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const orders = ref([])
const statusTab = ref('all')

function statusText(status) {
  if (status === 1) return '已支付'
  if (status === 2) return '待收货'
  if (status === 4) return '已关闭'
  return '待支付'
}

function statusType(status) {
  if (status === 1) return 'success'
  if (status === 2) return 'primary'
  if (status === 4) return 'info'
  return 'warning'
}

const filteredOrders = computed(() => {
  if (statusTab.value === 'all') return orders.value
  const target = Number(statusTab.value)
  return orders.value.filter(x => x.status === target)
})

async function loadOrders() {
  loading.value = true
  try {
    const { data } = await listOrders()
    orders.value = data.data || []
  } catch (e) {
    ElMessage.error(e?.message || '加载订单失败')
  } finally {
    loading.value = false
  }
}

function goBill(orderNo) {
  router.push({ path: '/bill', query: { orderNo } })
}

function onTabChange(name) {
  if (name === 'all') {
    router.replace({ path: '/orders' })
  } else {
    router.replace({ path: '/orders', query: { status: name } })
  }
}

watch(
  () => route.query.status,
  (v) => {
    statusTab.value = v == null ? 'all' : String(v)
  },
  { immediate: true }
)

onMounted(loadOrders)
</script>