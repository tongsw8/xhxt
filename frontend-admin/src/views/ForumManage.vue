<template>
  <el-card>
    <template #header>论坛管理</template>
    <el-table :data="posts" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column prop="userName" label="发布者" width="120" />
      <el-table-column prop="productName" label="关联商品" width="160" />
      <el-table-column prop="commentCount" label="评论数" width="90" />
      <el-table-column label="状态" width="100"><template #default="{row}"><el-tag :type="row.status===1?'success':'info'">{{ row.status===1?'可见':'不可见' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="340">
        <template #default="{ row }">
          <el-button size="small" @click="patch(row, { isTop: row.isTop===1?0:1 })">置顶/取消</el-button>
          <el-button size="small" @click="patch(row, { isBest: row.isBest===1?0:1 })">精华/取消</el-button>
          <el-button size="small" :type="row.status===1?'warning':'success'" @click="patch(row, { status: row.status===1?0:1 })">可见/不可见</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const posts = ref([])
const load = async()=> { posts.value = (await http.get('/forum/admin/posts')).data?.data || [] }
const patch = async (row, patchObj)=> {
  await http.post('/forum/admin/post/update', { id: row.id, isTop: patchObj.isTop ?? row.isTop, isBest: patchObj.isBest ?? row.isBest, status: patchObj.status ?? row.status })
  ElMessage.success('更新成功')
  await load()
}
const remove = async (id)=> { await http.delete('/forum/admin/post/' + id); ElMessage.success('删除成功'); await load() }
onMounted(load)
</script>