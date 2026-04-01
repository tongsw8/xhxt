<template>
  <div>
    <el-card v-if="post">
      <h2>{{ post.title }}</h2>
      <p>{{ post.content }}</p>
      <el-button @click="action('LIKE')">点赞</el-button>
      <el-button @click="action('COLLECT')">收藏</el-button>
    </el-card>
    <el-card style="margin-top:12px">
      <template #header>评论</template>
      <el-input v-model="txt" type="textarea" />
      <el-button type="primary" style="margin-top:8px" @click="comment">发表评论</el-button>
      <div v-for="c in comments" :key="c.id" style="margin-top:10px">{{ c.content }}</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const post = ref(null)
const comments = ref([])
const txt = ref('')

const load = async ()=> {
  const id = route.params.id
  post.value = (await axios.get('/api/forum/post/' + id)).data
  comments.value = (await axios.get('/api/forum/comments', { params: { targetId: id, type: 'POST' } })).data || []
}
const comment = async ()=> {
  await axios.post('/api/forum/comment', { targetId: post.value.id, targetType: 'POST', userId: 1, content: txt.value })
  txt.value = ''
  await load()
}
const action = async (actionType)=> {
  await axios.post('/api/forum/action', null, { params: { userId: 1, targetId: post.value.id, targetType: 'POST', actionType } })
}
onMounted(load)
</script>
