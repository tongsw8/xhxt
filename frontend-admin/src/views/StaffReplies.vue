<template>
  <el-card>
    <template #header>社区客服互动</template>

    <div class="toolbar">
      <el-select v-model="source" style="width:160px" @change="load">
        <el-option label="全部来源" value="" />
        <el-option label="论坛评论" value="POST" />
        <el-option label="商品评价" value="ORDER_PRODUCT" />
      </el-select>
      <el-select v-model="handled" style="width:160px" @change="load">
        <el-option label="全部状态" :value="null" />
        <el-option label="待处理" :value="0" />
        <el-option label="已处理" :value="1" />
      </el-select>
      <el-input v-model="keyword" placeholder="搜索评论内容" style="width:280px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-tag type="danger">待处理：{{ pendingCount }}</el-tag>
    </div>

    <el-table :data="rows" border>
      <el-table-column prop="content" label="评论内容" min-width="260" />
      <el-table-column prop="userName" label="评论人" width="120" />
      <el-table-column label="来源" width="120"><template #default="{row}">{{ row.source==='POST' ? '论坛' : '商品评价' }}</template></el-table-column>
      <el-table-column prop="sourceTitle" label="来源标题" min-width="180" />
      <el-table-column prop="createTime" label="评论时间" width="170" />
      <el-table-column label="状态" width="110"><template #default="{row}"><el-tag :type="row.hasStaffReply ? 'success' : 'warning'">{{ row.hasStaffReply ? '已处理' : '待处理' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="340">
        <template #default="{ row }">
          <el-button size="small" @click="viewThread(row)">查看对话</el-button>
          <el-button size="small" type="primary" @click="openReply(row)">官方回复</el-button>
          <el-button size="small" type="warning" @click="setStatus(row, 0)">屏蔽</el-button>
          <el-button size="small" @click="jumpSource(row)">查看原文</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="threadVisible" title="评论对话" size="40%">
      <div class="thread-title">来源：{{ thread.sourceTitle || '-' }}</div>
      <div v-for="item in thread.rows || []" :key="item.id" class="thread-item">
        <el-tag :type="item.isStaff===1 ? 'success' : 'info'">{{ item.isStaff===1 ? '官方' : '用户' }}</el-tag>
        <span class="uname">{{ item.userName }}</span>
        <span class="time">{{ item.createTime }}</span>
        <div class="content">{{ item.content }}</div>
      </div>
    </el-drawer>

    <el-dialog v-model="replyVisible" title="官方回复" width="560px">
      <el-input v-model="replyText" type="textarea" :rows="6" placeholder="请输入官方回复内容" />
      <template #footer>
        <el-button @click="replyVisible=false">取消</el-button>
        <el-button type="primary" @click="submitReply">发送</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const router = useRouter()
const source = ref('')
const handled = ref(null)
const keyword = ref('')
const pendingCount = ref(0)
const rows = ref([])

const threadVisible = ref(false)
const thread = ref({ rows: [] })

const replyVisible = ref(false)
const replyRow = ref(null)
const replyText = ref('')

function currentUserId() {
  try { return JSON.parse(localStorage.getItem('user') || '{}')?.id } catch { return null }
}

async function loadPending() {
  const res = await http.get('/staff/reply/pending-count')
  pendingCount.value = Number(res.data?.data || 0)
}

async function load() {
  const params = { source: source.value || undefined, keyword: keyword.value || undefined }
  if (handled.value !== null) params.handled = handled.value
  const res = await http.get('/staff/reply/list', { params })
  rows.value = res.data?.data || []
  await loadPending()
}

async function viewThread(row) {
  const res = await http.get('/staff/reply/thread', { params: { rootId: row.id } })
  thread.value = res.data?.data || { rows: [] }
  threadVisible.value = true
}

function openReply(row) {
  replyRow.value = row
  replyText.value = ''
  replyVisible.value = true
}

async function submitReply() {
  if (!replyText.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  await http.post('/staff/reply/reply', { rootId: replyRow.value.id, staffUserId: currentUserId(), content: replyText.value })
  ElMessage.success('回复成功')
  replyVisible.value = false
  await load()
}

async function setStatus(row, status) {
  await http.post('/staff/reply/status', { id: row.id, status })
  ElMessage.success(status === 0 ? '已屏蔽' : '状态已更新')
  await load()
}

function jumpSource(row) {
  if (row.source === 'POST') {
    window.open(`/community/detail/${row.targetId}`, '_blank')
  } else {
    window.open(`/shop/detail/${row.targetId}`, '_blank')
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar { display:flex; align-items:center; gap:10px; margin-bottom:12px; }
.thread-title { font-weight:700; margin-bottom:10px; }
.thread-item { padding:10px; border:1px solid #e5e7eb; border-radius:8px; margin-bottom:8px; }
.uname { margin-left:8px; font-weight:600; }
.time { margin-left:10px; color:#94a3b8; font-size:12px; }
.content { margin-top:6px; line-height:1.6; color:#334155; }
</style>