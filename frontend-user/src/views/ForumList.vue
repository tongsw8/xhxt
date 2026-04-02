<template>
  <div class="forum-list">
    <el-card>
      <template #header>交流论坛</template>

      <el-tabs v-model="tab" @tab-change="load">
        <el-tab-pane label="社区帖子" name="all" />
        <el-tab-pane label="我的帖子" name="mine" />
      </el-tabs>

      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索帖子" style="width:320px" @keyup.enter="load" />
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button type="success" @click="$router.push('/community/post')">发帖</el-button>
      </div>

      <template v-if="tab==='all'">
        <el-empty v-if="posts.length===0" description="暂无帖子" />
        <div v-else class="card-grid">
          <div v-for="row in posts" :key="row.id" class="post-card" @click="goDetail(row.id)">
            <el-image :src="pickCover(row.content) || fallbackCover" fit="cover" class="cover" />
            <div class="card-body">
              <div class="title">{{ row.title }}</div>
              <div class="meta">{{ row.userName }} · {{ row.createTime }}</div>
              <div class="bottom">
                <span>{{ row.productName || '未关联商品' }}</span>
                <span>{{ row.commentCount || 0 }} 评论</span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <template v-else>
        <el-table :data="posts" border>
          <el-table-column prop="title" label="标题" min-width="220" />
          <el-table-column prop="createTime" label="发布时间" width="180" />
          <el-table-column prop="productName" label="关联商品" width="180" />
          <el-table-column prop="commentCount" label="评论数" width="90" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.status===1 ? 'success' : 'info'">{{ row.status===1 ? '可见' : '不可见' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300">
            <template #default="{ row }">
              <el-button size="small" @click="goDetail(row.id)">查看</el-button>
              <el-button size="small" @click="$router.push({ path:'/community/post', query:{ id: row.id } })">编辑</el-button>
              <el-button size="small" :type="row.status===1 ? 'warning':'success'" @click="toggleVisible(row)">{{ row.status===1 ? '设为不可见':'设为可见' }}</el-button>
              <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteForumPost, listForumPosts, toggleForumPostVisibility } from '../api/forum'

const router = useRouter()
const posts = ref([])
const keyword = ref('')
const tab = ref('all')
const fallbackCover = 'https://dummyimage.com/600x360/e2e8f0/64748b&text=FORUM'

function pickCover(content) {
  if (!content) return ''
  const m = String(content).match(/<img[^>]*src=["']([^"']+)["']/i)
  return m ? m[1] : ''
}

async function load() {
  const { data } = await listForumPosts({ keyword: keyword.value, mine: tab.value === 'mine' })
  posts.value = data.data || []
}

function goDetail(id) {
  router.push('/community/detail/' + id)
}

async function toggleVisible(row) {
  await toggleForumPostVisibility(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm('确认删除该帖子？', '提示', { type: 'warning' })
  await deleteForumPost(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { display:flex; gap:8px; align-items:center; margin-bottom:12px; }
.card-grid { display:grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap:14px; }
.post-card { border:1px solid #e5e7eb; border-radius:12px; overflow:hidden; background:#fff; cursor:pointer; transition:.2s; }
.post-card:hover { transform:translateY(-2px); box-shadow:0 8px 20px rgba(15,23,42,.12); }
.cover { width:100%; height:150px; }
.card-body { padding:10px; }
.title { font-weight:700; color:#0f172a; min-height:40px; line-height:1.4; }
.meta { font-size:12px; color:#64748b; margin:8px 0; }
.bottom { display:flex; justify-content:space-between; font-size:12px; color:#334155; }
@media (max-width:1200px){ .card-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
@media (max-width:900px){ .card-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
</style>