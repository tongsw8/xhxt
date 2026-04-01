<template>
  <el-card>
    <template #header>订单发货</template>

    <el-tabs v-model="statusTab" @tab-change="load" style="margin-bottom: 12px">
      <el-tab-pane label="全部（待发货+送达中）" name="all" />
      <el-tab-pane label="待发货" name="1" />
      <el-tab-pane label="送达中" name="2" />
    </el-tabs>

    <el-table :data="orders" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="orderNo" label="订单号" />
      <el-table-column prop="receiverName" label="收货人" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'warning' : 'success'">{{ row.status === 1 ? '待发货' : '送达中' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="expressCompany" label="快递公司" width="130" />
      <el-table-column prop="trackingNo" label="物流单号" width="170" />
      <el-table-column label="操作" width="360">
        <template #default="{ row }">
          <template v-if="row.status === 1">
            <el-input v-model="row.company" placeholder="快递公司" style="width:110px;margin-right:8px" />
            <el-input v-model="row.no" placeholder="单号" style="width:130px;margin-right:8px" />
            <el-button size="small" type="primary" @click="deliver(row)">发货</el-button>
          </template>
          <template v-else>
            <span style="color:#67c23a">已发货，送达中</span>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const orders = ref([])
const statusTab = ref('all')

const load = async () => {
  const params = {}
  if (statusTab.value !== 'all') params.status = Number(statusTab.value)
  const res = await http.get('/staff/order/list', { params })
  const rows = res.data?.data || []
  orders.value = rows.map(v => ({ ...v, company: v.expressCompany || '', no: v.trackingNo || '' }))
}

const deliver = async (row) => {
  if (!row.company || !row.no) {
    ElMessage.warning('请填写快递公司和单号')
    return
  }
  await http.post('/staff/order/delivery', null, { params: { id: row.id, company: row.company, no: row.no } })
  ElMessage.success('发货成功，状态已变为送达中')
  await load()
}

onMounted(load)
</script>