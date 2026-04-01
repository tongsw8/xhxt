<template>
  <el-card>
    <template #header>论坛管理</template>
    <el-table :data="posts" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="isTop" label="置顶" width="80" />
      <el-table-column prop="isBest" label="精华" width="80" />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button size="small" @click="tag(row,'top')">置顶/取消</el-button>
          <el-button size="small" @click="tag(row,'best')">精华/取消</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
const posts = ref([])
const load = async()=> { posts.value = (await axios.get('/api/forum/posts')).data || [] }
const tag = async (row,t) => {
  if (t==='top') row.isTop = row.isTop===1?0:1
  if (t==='best') row.isBest = row.isBest===1?0:1
  await axios.post('/api/forum/post', row)
  await load()
}
const remove = async (id)=> { await axios.delete('/api/forum/admin/post/' + id); await load() }
onMounted(load)
</script>
