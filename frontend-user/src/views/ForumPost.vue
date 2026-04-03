<template>
  <el-card>
    <template #header>{{ form.id ? '编辑帖子' : '发布帖子' }}</template>
    <el-form :model="form" label-width="90px">
      <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="关联商品">
        <el-select v-model="form.productId" clearable filterable placeholder="可选，选择要关联的鲜花商品" style="width:100%">
          <el-option v-for="p in products" :key="p.id" :label="`${p.productName}（¥${Number(p.price||0).toFixed(2)}）`" :value="p.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="正文">
        <el-input type="textarea" v-model="form.content" :rows="10" placeholder="支持直接写文本，也支持插入图片URL" />
      </el-form-item>
      <el-form-item label="插入图片">
        <el-upload :show-file-list="false" :http-request="uploadPostImage" accept="image/*"><el-button>上传图片</el-button></el-upload>
      </el-form-item>
      <el-button type="primary" @click="submit">{{ form.id ? '保存修改' : '发布' }}</el-button>
    </el-form>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { getForumPostDetail, saveForumPost } from '../api/forum'
import { fetchShopProducts } from '../api/shop'

const route = useRoute()
const router = useRouter()
const form = ref({ id: null, productId: null, title: '', content: '' })
const products = ref([])

async function loadProducts() {
  const { data } = await fetchShopProducts({ pageNo: 1, pageSize: 200 })
  products.value = data.data?.records || []
}

async function loadEdit() {
  const id = route.query.id
  if (!id) return
  const { data } = await getForumPostDetail(id)
  const p = data.data || {}
  form.value = { id: p.id, productId: p.product?.id || null, title: p.title || '', content: p.content || '' }
}

async function uploadPostImage(option) {
  const fd = new FormData(); fd.append('file', option.file)
  const res = await http.post('/admin/upload/image', fd, { headers: { 'Content-Type': 'multipart/form-data' }, params: { bizType: 'post' } })
  const url = res.data?.data?.url
  if (url) {
    form.value.content = `${form.value.content || ''}\n<img src="${url}" alt="post-image" />`
  }
}

async function submit() {
  await saveForumPost(form.value)
  ElMessage.success(form.value.id ? '修改成功' : '发布成功')
  router.push('/community')
}

onMounted(async () => { await Promise.all([loadProducts(), loadEdit()]) })
</script>