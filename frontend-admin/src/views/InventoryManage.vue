<template>
  <el-card>
    <template #header>库存维护</template>
    <el-table :data="products" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="productName" label="商品" />
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column label="调整" width="260">
        <template #default="{ row }">
          <el-input-number v-model="row.delta" :min="1" size="small" />
          <el-button size="small" type="success" @click="chg(row,1)">增加</el-button>
          <el-button size="small" type="warning" @click="chg(row,-1)">减少</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const products = ref([])

const load = async () => {
  const res = await http.get('/admin/product/list', { params: { pageNo: 1, pageSize: 200 } })
  const rows = res.data?.data?.records || []
  products.value = rows.map(v => ({ ...v, delta: 1 }))
}

const chg = async (row, sign) => {
  await http.post('/admin/product/stock', null, { params: { id: row.id, amount: row.delta * sign } })
  ElMessage.success('库存已更新')
  await load()
}

onMounted(load)
</script>