<template>
  <div class="notice-list-page" v-loading="loading">
    <el-card>
      <template #header><h2 style="margin:0">公告中心</h2></template>
      <el-empty v-if="!loading && notices.length===0" description="暂无公告" />
      <div v-for="n in notices" :key="n.id" class="notice-item" @click="goDetail(n.id)">
        <el-image :src="n.coverImg" fit="cover" class="cover" />
        <div class="body">
          <div class="title-row">
            <h3>{{ n.title }}</h3>
            <el-tag v-if="n.isTop===1" type="danger" size="small">置顶</el-tag>
          </div>
          <p class="meta">发布时间：{{ n.publishTime || n.createTime }}</p>
          <p class="summary">{{ strip(n.content) }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'

const router = useRouter()
const loading = ref(false)
const notices = ref([])

function strip(text) {
  if (!text) return ''
  return String(text).replace(/<[^>]+>/g, '').slice(0, 110)
}

function goDetail(id) {
  router.push(`/notice/${id}`)
}

async function load() {
  loading.value = true
  try {
    const res = await http.get('/content/notices')
    notices.value = res.data?.data || []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.notice-item { display:flex; gap:12px; padding:12px 0; border-bottom:1px solid #eee; cursor:pointer; }
.cover { width: 140px; height: 80px; border-radius: 8px; flex-shrink: 0; }
.body { flex:1; min-width:0; }
.title-row { display:flex; align-items:center; gap:8px; }
.title-row h3 { margin:0; font-size:18px; }
.meta { margin:6px 0; color:#94a3b8; font-size:12px; }
.summary { margin:0; color:#475569; line-height:1.6; }
</style>