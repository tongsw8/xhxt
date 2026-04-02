<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>社区客服互动</span>
        <div>
          <el-tag type="danger" style="margin-right:8px">待处理 {{ pendingCount }}</el-tag>
          <el-tag type="success">已处理 {{ handledCount }}</el-tag>
        </div>
      </div>
    </template>

    <div style="display:flex;gap:8px;align-items:center;margin-bottom:12px;flex-wrap:wrap">
      <el-select v-model="source" style="width:150px" @change="load">
        <el-option label="全部来源" value="" />
        <el-option label="论坛评论" value="POST" />
        <el-option label="商品评价" value="ORDER_PRODUCT" />
      </el-select>
      <el-select v-model="handled" style="width:150px" @change="load">
        <el-option label="全部状态" :value="null" />
        <el-option label="待处理" :value="0" />
        <el-option label="已处理" :value="1" />
      </el-select>
      <el-select v-model="mineHandled" style="width:180px" @change="load">
        <el-option label="全部处理人" :value="0" />
        <el-option label="仅看我处理" :value="1" />
      </el-select>
      <el-input v-model="keyword" placeholder="搜索评论内容" style="width:260px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <el-table :data="rows" border>
      <el-table-column prop="content" label="评论内容" min-width="220" show-overflow-tooltip />
      <el-table-column prop="userName" label="评论人" width="120" />
      <el-table-column label="来源" width="110"><template #default="{row}">{{ row.source==='POST'?'论坛':'商品' }}</template></el-table-column>
      <el-table-column prop="sourceTitle" label="来源标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="100"><template #default="{row}"><el-tag :type="row.resolved ? 'success' : 'warning'">{{ row.resolved ? '已处理' : '待处理' }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="评论时间" width="170" />
      <el-table-column label="操作" width="420">
        <template #default="{ row }">
          <el-button size="small" @click="openThread(row)">查看线程</el-button>
          <el-button size="small" type="info" @click="openSource(row)">查看来源</el-button>
          <el-button size="small" type="primary" @click="openReply(row)">官方回复</el-button>
          <el-button size="small" type="success" @click="markHandled(row)">标记处理</el-button>
          <el-button size="small" type="danger" @click="shield(row)">屏蔽</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="threadVisible" title="评论线程" width="760px">
      <div style="margin-bottom:10px;color:#64748b">
        来源：{{ threadData.sourceTitle }}
        <el-button text type="primary" @click="openSourceByThread">查看来源详情</el-button>
      </div>
      <div v-for="it in threadData.rows" :key="it.id" style="padding:10px 0;border-bottom:1px dashed #e5e7eb">
        <div style="font-size:12px;color:#94a3b8">{{ it.userName }} · {{ it.createTime }} <el-tag v-if="it.isStaff===1" size="small" type="success">官方</el-tag></div>
        <div style="margin-top:4px">{{ it.content }}</div>
      </div>
    </el-dialog>

    <el-dialog v-model="replyVisible" title="官方回复" width="560px">
      <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:8px">
        <el-button size="small" @click="useTpl(0)">物流处理中，请耐心等待</el-button>
        <el-button size="small" @click="useTpl(1)">已记录反馈，稍后专员联系您</el-button>
        <el-button size="small" @click="useTpl(2)">感谢您的建议，我们会持续优化</el-button>
      </div>
      <el-input v-model="replyContent" type="textarea" :rows="5" placeholder="请输入官方回复内容" />
      <template #footer>
        <el-button @click="replyVisible=false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const source = ref('')
const handled = ref(null)
const mineHandled = ref(0)
const keyword = ref('')
const rows = ref([])
const threadVisible = ref(false)
const replyVisible = ref(false)
const threadData = ref({ sourceTitle: '', sourcePath: '/', rows: [] })
const currentRootId = ref(null)
const replyContent = ref('')
const replyTpl = [
  '您好，您的反馈我们已收到，订单正在加急处理中，请耐心等待。',
  '您好，已将您的问题记录并转交售后专员，稍后会与您联系。',
  '感谢您的评价与建议，我们会持续优化服务体验。',
]

const pendingCount = computed(() => rows.value.filter(v => !v.resolved).length)
const handledCount = computed(() => rows.value.filter(v => v.resolved).length)

const userBase = import.meta.env.VITE_USER_BASE_URL || 'http://localhost:5173'

function currentUserId() {
  try { return JSON.parse(localStorage.getItem('user') || '{}')?.id } catch { return null }
}

async function load() {
  const params = {
    source: source.value || undefined,
    handled: handled.value,
    keyword: keyword.value || undefined,
    mineHandled: mineHandled.value,
    staffUserId: currentUserId(),
  }
  const { data } = await http.get('/staff/reply/list', { params })
  rows.value = data.data || []
}

async function openThread(row) {
  const { data } = await http.get('/staff/reply/thread', { params: { rootId: row.id } })
  threadData.value = data.data || { sourceTitle: '', sourcePath: '/', rows: [] }
  threadVisible.value = true
}

function openReply(row) {
  currentRootId.value = row.id
  replyContent.value = ''
  replyVisible.value = true
}

function useTpl(idx) {
  replyContent.value = replyTpl[idx] || ''
}

async function submitReply() {
  await http.post('/staff/reply/reply', { rootId: currentRootId.value, staffUserId: currentUserId(), content: replyContent.value })
  ElMessage.success('回复成功')
  replyVisible.value = false
  await load()
}

async function markHandled(row) {
  await http.post('/staff/reply/handled', { rootId: row.id, handled: row.resolved ? 0 : 1, staffUserId: currentUserId() })
  ElMessage.success(row.resolved ? '已取消处理标记' : '已标记处理')
  await load()
}

async function shield(row) {
  await http.post('/staff/reply/status', { id: row.id, status: 0 })
  ElMessage.success('已屏蔽')
  await load()
}

function openSource(row) {
  if (!row?.sourcePath) return
  window.open(`${userBase}${row.sourcePath}`, '_blank')
}

function openSourceByThread() {
  if (!threadData.value?.sourcePath) return
  window.open(`${userBase}${threadData.value.sourcePath}`, '_blank')
}

onMounted(load)
</script>