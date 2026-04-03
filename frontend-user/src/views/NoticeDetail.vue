<template>
  <div class="notice-detail-page" v-loading="loading">
    <el-card v-if="notice">
      <template #header>
        <div class="head">
          <h2>{{ notice.title }}</h2>
          <el-tag v-if="notice.isTop===1" type="danger">置顶</el-tag>
        </div>
      </template>
      <p class="meta">发布时间：{{ notice.publishTime || notice.createTime }}</p>
      <el-image v-if="notice.coverImg" :src="notice.coverImg" fit="cover" class="cover" />
      <div class="content" v-html="notice.content"></div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import http from '../api/http'

const route = useRoute()
const loading = ref(false)
const notice = ref(null)

async function load() {
  loading.value = true
  try {
    const res = await http.get(`/content/notice/${route.params.id}`)
    notice.value = res.data?.data || null
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.head { display:flex; align-items:center; justify-content:space-between; }
.head h2 { margin:0; }
.meta { color:#94a3b8; font-size:12px; margin:0 0 12px; }
.cover { width:100%; max-height: 360px; border-radius: 10px; margin-bottom: 12px; }
.content :deep(p) { line-height: 1.8; color: #334155; }
</style>