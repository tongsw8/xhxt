<template>
  <el-card>
    <template #header>订单列表</template>

    <div class="toolbar">
      <el-input v-model="query.userKeyword" placeholder="按用户账号/昵称/手机号" clearable style="width: 260px" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 150px">
        <el-option label="待支付" :value="0" />
        <el-option label="已支付待发货" :value="1" />
        <el-option label="已发货待收货" :value="2" />
        <el-option label="已完成" :value="3" />
        <el-option label="已关闭" :value="4" />
      </el-select>
      <el-button type="primary" @click="onSearch">查询</el-button>
    </div>

    <el-table :data="list" border>
      <el-table-column prop="orderNo" label="订单号" min-width="220" />
      <el-table-column label="用户" min-width="180">
        <template #default="{ row }">{{ row.userNickname || row.userAccount }}（{{ row.userAccount }}）</template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="120">
        <template #default="{ row }">¥{{ Number(row.totalAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="130">
        <template #default="{ row }"><el-tag>{{ statusText(row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }"><el-button size="small" @click="showDetail(row)">详情</el-button></template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, prev, pager, next" :total="pager.total" :current-page="pager.pageNo" :page-size="pager.pageSize" @current-change="onPageChange" />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="820px">
      <div v-if="detail.order" class="detail-top">
        <p>订单号：{{ detail.order.orderNo }}</p>
        <p>用户：{{ detail.user?.nickname || detail.user?.account }}（{{ detail.user?.account }}）</p>
        <p>收货：{{ detail.order.receiverName }} {{ detail.order.receiverPhone }} {{ detail.order.receiverAddress }}</p>
        <p>贺卡留言：{{ detail.order.cardMessage || '无' }}</p>
        <p>配送时间要求：{{ detail.order.deliveryExpectTime || '无' }}</p>
      </div>
      <el-table :data="detail.items || []" border>
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="productPrice" label="单价" width="120" />
        <el-table-column prop="quantity" label="数量" width="100" />
      </el-table>
      <template #footer><el-button @click="detailVisible=false">关闭</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../api/http'

const query = reactive({ userKeyword: '', status: null })
const pager = reactive({ pageNo: 1, pageSize: 10, total: 0 })
const list = ref([])
const detailVisible = ref(false)
const detail = ref({ order: null, user: null, items: [] })

function statusText(s) {
  if (s === 1) return '已支付待发货'
  if (s === 2) return '已发货待收货'
  if (s === 3) return '已完成'
  if (s === 4) return '已关闭'
  return '待支付'
}

async function loadData() {
  const res = await http.get('/admin/order/page', { params: { ...query, pageNo: pager.pageNo, pageSize: pager.pageSize } })
  const data = res.data?.data || {}
  list.value = data.records || []
  pager.total = Number(data.total || 0)
}

function onSearch() {
  pager.pageNo = 1
  loadData()
}

function onPageChange(p) {
  pager.pageNo = p
  loadData()
}

async function showDetail(row) {
  const res = await http.get(`/admin/order/detail/${row.orderNo}`)
  detail.value = res.data?.data || { order: null, user: null, items: [] }
  detailVisible.value = true
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display:flex; gap:10px; margin-bottom:12px; }
.pager { margin-top:12px; display:flex; justify-content:flex-end; }
.detail-top { margin-bottom: 10px; color:#475569; }
</style>