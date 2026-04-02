<template>
  <div>
    <el-card v-if="post">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <h2>{{ post.title }}</h2>
        <el-tag v-if="post.isTop===1" type="danger">置顶</el-tag>
      </div>
      <div style="color:#94a3b8;font-size:12px;margin-bottom:8px">{{ post.userName }} · {{ post.createTime }}</div>
      <div class="post-content" v-html="post.content"></div>

      <div v-if="post.product" class="product-card" @click="goProduct(post.product.id)">
        <el-image :src="toImage(post.product.coverImg)" fit="cover" class="pimg" />
        <div><div class="pname">{{ post.product.name }}</div><div class="pprice">¥{{ Number(post.product.price || 0).toFixed(2) }}</div></div>
      </div>

      <el-button @click="action('LIKE')">点赞</el-button>
      <el-button @click="action('COLLECT')">收藏</el-button>
    </el-card>

    <el-card style="margin-top:12px">
      <template #header>评论</template>
      <el-input v-model="txt" type="textarea" :rows="3" placeholder="发表评论" />
      <el-button type="primary" style="margin-top:8px" @click="comment(0)">发表评论</el-button>

      <div v-for="c in comments" :key="c.id" class="c-item">
        <div class="c-head">{{ c.userName }} · {{ c.createTime }}</div>
        <div class="c-content">{{ c.content }}</div>
        <div class="c-actions">
          <el-button size="small" text @click="replyTo(c)">回复</el-button>
          <el-button size="small" text type="danger" v-if="c.canDelete" @click="delComment(c.id)">删除</el-button>
        </div>
        <div v-if="replyId===c.id" style="margin:6px 0 8px 0">
          <el-input v-model="replyText" placeholder="回复内容" />
          <el-button size="small" type="primary" style="margin-top:6px" @click="comment(c.id)">发送回复</el-button>
        </div>

        <div v-for="r in c.replies || []" :key="r.id" class="r-item">
          <div class="c-head">{{ r.userName }} · {{ r.createTime }}</div>
          <div class="c-content">{{ r.content }}</div>
          <el-button size="small" text type="danger" v-if="r.canDelete" @click="delComment(r.id)">删除</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addForumComment, deleteForumComment, getForumPostDetail, listForumComments, toggleForumAction } from '../api/forum'

const route = useRoute()
const router = useRouter()
const post = ref(null)
const comments = ref([])
const txt = ref('')
const replyId = ref(0)
const replyText = ref('')

function toImage(path) {
  if (!path) return ''
  if (path.startsWith('http')) return path
  const base = import.meta.env.VITE_BASE_API?.replace('/api', '') || '/xhxt'
  return `${base}/file/flower/${path}`
}

function goProduct(id) { router.push(`/shop/detail/${id}`) }

const load = async () => {
  const id = route.params.id
  post.value = (await getForumPostDetail(id)).data?.data
  comments.value = (await listForumComments(id)).data?.data || []
}

async function comment(parentId) {
  const content = parentId ? replyText.value : txt.value
  if (!content.trim()) return
  await addForumComment(post.value.id, content, parentId)
  if (parentId) { replyId.value = 0; replyText.value = '' } else txt.value = ''
  await load()
}

function replyTo(c) { replyId.value = c.id; replyText.value = '' }

async function delComment(id) {
  await deleteForumComment(id)
  ElMessage.success('删除成功')
  await load()
}

async function action(actionType) {
  await toggleForumAction(post.value.id, 'POST', actionType)
  ElMessage.success(actionType === 'LIKE' ? '点赞状态已切换' : '收藏状态已切换')
}

onMounted(load)
</script>

<style scoped>
.post-content :deep(img){max-width:100%;border-radius:8px}
.product-card{margin:12px 0;padding:10px;border:1px solid #eee;border-radius:10px;display:flex;gap:10px;cursor:pointer;align-items:center}
.pimg{width:88px;height:88px;border-radius:8px}
.pname{font-weight:700}
.pprice{color:#dc2626;margin-top:4px}
.c-item{padding:10px 0;border-bottom:1px dashed #e5e7eb}
.c-head{font-size:12px;color:#94a3b8}
.c-content{margin:4px 0}
.r-item{margin-left:18px;margin-top:8px;padding-left:10px;border-left:2px solid #e5e7eb}
</style>