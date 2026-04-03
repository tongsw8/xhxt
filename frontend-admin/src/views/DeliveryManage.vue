<template>
  <el-card>
    <template #header>订单发货</template>

    <el-tabs v-model="statusTab" @tab-change="load" style="margin-bottom: 12px">
      <el-tab-pane label="全部（待发货+待收货）" name="all" />
      <el-tab-pane label="待发货" name="1" />
      <el-tab-pane label="待收货" name="2" />
    </el-tabs>

    <el-table :data="orders" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="orderNo" label="订单号" min-width="190" />
      <el-table-column prop="receiverName" label="收货人" width="110" />
      <el-table-column label="催发货" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.urgeShip === 1" type="danger" effect="dark">紧急</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'warning' : 'primary'">{{ row.status === 1 ? '待发货' : '待收货' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="expressCompany" label="快递公司" width="130" />
      <el-table-column prop="trackingNo" label="物流单号" width="170" />
      <el-table-column label="操作" width="420">
        <template #default="{ row }">
          <template v-if="row.status === 1">
            <el-input v-model="row.company" placeholder="快递公司" style="width:110px;margin-right:8px" />
            <el-input v-model="row.no" placeholder="单号" style="width:130px;margin-right:8px" />
            <el-button size="small" type="primary" @click="deliver(row)">发货</el-button>
          </template>
          <template v-else>
            <el-button size="small" type="success" @click="finish(row)">送达</el-button>
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
  ElMessage.success('发货成功，状态已变为待收货')
  await load()
}

const finish = async (row) => {
  await http.post(`/staff/order/finish/${row.id}`)
  ElMessage.success('订单已完成')
  await load()
}

onMounted(load)
</script>