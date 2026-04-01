<template>
  <el-card>
    <template #header>发布帖子</template>
    <el-form :model="form" label-width="90px">
      <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="内容"><el-input type="textarea" v-model="form.content" :rows="8" /></el-form-item>
      <el-button type="primary" @click="submit">发布</el-button>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const form = ref({ categoryId: 1, userId: 1, title: '', content: '' })
const submit = async ()=> {
  await axios.post('/api/forum/post', form.value)
  router.push('/community')
}
</script>
