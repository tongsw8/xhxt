<template>
  <div class="forum-list">
    <el-card>
      <template #header>交流论坛</template>
      <el-input v-model="keyword" placeholder="搜索帖子" style="width:300px" @keyup.enter="load" />
      <el-button type="primary" style="margin-left:8px" @click="$router.push('/community/post')">发帖</el-button>
      <el-table :data="posts" style="margin-top:12px" border>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="viewCount" label="阅读" width="100" />
        <el-table-column label="标签" width="140">
          <template #default="{ row }">
            <el-tag v-if="row.isTop" size="small" type="danger">置顶</el-tag>
            <el-tag v-if="row.isBest" size="small" type="success">精华</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push('/community/detail/' + row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const posts = ref([])
const keyword = ref('')
const load = async () => {
  const { data } = await axios.get('/api/forum/posts', { params: { keyword: keyword.value } })
  posts.value = data || []
}
onMounted(load)
</script>
