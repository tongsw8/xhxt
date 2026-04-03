<template>
  <el-card>
    <template #header>内容管理</template>

    <el-tabs v-model="tab">
      <el-tab-pane label="轮播图" name="b">
        <div style="margin-bottom:12px"><el-button type="primary" @click="openBanner()">新增轮播图</el-button></div>
        <el-table :data="banners" border>
          <el-table-column prop="sortNo" label="序号" width="90" />
          <el-table-column label="图片" width="130"><template #default="{ row }"><el-image :src="row.imgUrl" fit="cover" style="width:80px;height:44px;border-radius:6px" /></template></el-table-column>
          <el-table-column prop="title" label="标题" />
          <el-table-column label="关联商品" min-width="180"><template #default="{ row }">{{ row.linkUrl ? bannerProductText(row.linkUrl) : '未关联' }}</template></el-table-column>
          <el-table-column label="是否展示" width="120"><template #default="{ row }"><el-switch :model-value="row.status === 1" @change="(v) => toggleBanner(row, v)" /></template></el-table-column>
          <el-table-column label="操作" width="220"><template #default="{ row }"><el-button size="small" @click="openBanner(row)">编辑</el-button><el-button size="small" type="danger" @click="removeBanner(row.id)">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="公告" name="n">
        <div style="margin-bottom:12px"><el-button type="primary" @click="openNotice()">新增公告</el-button></div>
        <el-table :data="notices" border>
          <el-table-column prop="title" label="标题" min-width="220" />
          <el-table-column label="封面" width="120"><template #default="{ row }"><el-image :src="row.coverImg" fit="cover" style="width:72px;height:40px;border-radius:6px" /></template></el-table-column>
          <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status===1 ? 'success' : 'info'">{{ row.status===1 ? '已发布' : '未发布' }}</el-tag></template></el-table-column>
          <el-table-column label="置顶" width="90"><template #default="{ row }"><el-tag :type="row.isTop===1 ? 'danger' : 'info'">{{ row.isTop===1 ? '置顶' : '普通' }}</el-tag></template></el-table-column>
          <el-table-column prop="publishTime" label="发布时间" width="170" />
          <el-table-column prop="updateTime" label="更新时间" width="170" />
          <el-table-column label="操作" width="360">
            <template #default="{ row }">
              <el-button size="small" @click="openNotice(row)">编辑</el-button>
              <el-button size="small" :type="row.status===1 ? 'warning' : 'success'" @click="toggleNoticeStatus(row)">{{ row.status===1 ? '撤销发布' : '发布' }}</el-button>
              <el-button size="small" :type="row.isTop===1 ? 'info' : 'danger'" @click="toggleNoticeTop(row)">{{ row.isTop===1 ? '取消置顶' : '置顶' }}</el-button>
              <el-button size="small" type="danger" @click="removeNotice(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="新闻" name="news"><div style="padding: 12px 0; color:#94a3b8">暂保留原模块</div></el-tab-pane>
    </el-tabs>

    <el-dialog v-model="bannerDialog.visible" :title="bannerDialog.form.id ? '编辑轮播图' : '新增轮播图'" width="620px">
      <el-form :model="bannerDialog.form" label-width="100px">
        <el-form-item label="标题"><el-input v-model="bannerDialog.form.title" /></el-form-item>
        <el-form-item label="图片">
          <el-upload :show-file-list="false" :http-request="uploadBannerImage" accept="image/*"><el-button>上传图片</el-button></el-upload>
          <div v-if="bannerDialog.form.imgUrl" style="margin-top:8px"><el-image :src="bannerDialog.form.imgUrl" fit="cover" style="width:120px;height:68px;border-radius:8px" /></div>
        </el-form-item>
        <el-form-item label="序号"><el-input-number v-model="bannerDialog.form.sortNo" :min="0" /></el-form-item>
        <el-form-item label="关联商品">
          <el-select v-model="bannerDialog.productId" filterable clearable placeholder="可选，选择后用户端点击轮播图直达商品详情" style="width:100%" @change="onBannerProductChange">
            <el-option v-for="p in productOptions" :key="p.id" :label="`${p.productName}（ID:${p.id}）`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否展示"><el-radio-group v-model="bannerDialog.form.status"><el-radio :value="1">展示</el-radio><el-radio :value="0">隐藏</el-radio></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="bannerDialog.visible=false">取消</el-button><el-button type="primary" @click="saveBanner">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="noticeDialog.visible" :title="noticeDialog.form.id ? '编辑公告' : '新增公告'" width="760px">
      <el-form :model="noticeDialog.form" label-width="100px">
        <el-form-item label="标题"><el-input v-model="noticeDialog.form.title" /></el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="noticeDialog.form.coverImg" placeholder="可直接填写服务地址，也可本地上传" />
          <div style="margin-top:8px">
            <el-upload :show-file-list="false" :http-request="uploadNoticeImage" accept="image/*"><el-button>上传图片</el-button></el-upload>
          </div>
          <div v-if="noticeDialog.form.coverImg" style="margin-top:8px"><el-image :src="noticeDialog.form.coverImg" fit="cover" style="width:140px;height:80px;border-radius:8px" /></div>
        </el-form-item>
        <el-form-item label="正文"><el-input v-model="noticeDialog.form.content" type="textarea" :rows="8" /></el-form-item>
        <el-form-item label="发布状态"><el-radio-group v-model="noticeDialog.form.status"><el-radio :value="1">已发布</el-radio><el-radio :value="0">未发布</el-radio></el-radio-group></el-form-item>
        <el-form-item label="置顶状态"><el-radio-group v-model="noticeDialog.form.isTop"><el-radio :value="1">置顶</el-radio><el-radio :value="0">不置顶</el-radio></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="noticeDialog.visible=false">取消</el-button><el-button type="primary" @click="saveNotice">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const tab = ref('b')
const banners = ref([])
const notices = ref([])
const productOptions = ref([])

const bannerDialog = reactive({
  visible: false,
  productId: null,
  form: { id: null, title: '', imgUrl: '', linkUrl: '', sortNo: 0, status: 1 },
})
const noticeDialog = reactive({ visible: false, form: { id: null, title: '', coverImg: '', content: '', status: 1, isTop: 0 } })

async function loadBanners() {
  const res = await http.get('/content/admin/banner/list')
  banners.value = res.data?.data || []
}
async function loadNotices() {
  const res = await http.get('/content/admin/notice/list')
  notices.value = res.data?.data || []
}
async function loadProducts() {
  const res = await http.get('/admin/product/list', { params: { pageNo: 1, pageSize: 500 } })
  productOptions.value = res.data?.data?.records || []
}

function bannerProductText(linkUrl) {
  const m = String(linkUrl || '').match(/\/shop\/detail\/(\d+)/)
  if (!m) return '未关联'
  const id = Number(m[1])
  const p = productOptions.value.find(v => Number(v.id) === id)
  return p ? `${p.productName}（ID:${p.id}）` : `商品ID:${id}`
}

function openBanner(row) {
  bannerDialog.form = row ? { ...row } : { id: null, title: '', imgUrl: '', linkUrl: '', sortNo: 0, status: 1 }
  const m = String(bannerDialog.form.linkUrl || '').match(/\/shop\/detail\/(\d+)/)
  bannerDialog.productId = m ? Number(m[1]) : null
  bannerDialog.visible = true
}
function onBannerProductChange(v) {
  bannerDialog.form.linkUrl = v ? `/shop/detail/${v}` : ''
}

function openNotice(row) { noticeDialog.form = row ? { ...row } : { id: null, title: '', coverImg: '', content: '', status: 1, isTop: 0 }; noticeDialog.visible = true }

async function uploadBannerImage(option) {
  const form = new FormData(); form.append('file', option.file)
  const res = await http.post('/admin/upload/image', form, { headers: { 'Content-Type': 'multipart/form-data' }, params: { bizType: 'flower' } })
  bannerDialog.form.imgUrl = res.data?.data?.url || ''
}
async function uploadNoticeImage(option) {
  const form = new FormData(); form.append('file', option.file)
  const res = await http.post('/admin/upload/image', form, { headers: { 'Content-Type': 'multipart/form-data' }, params: { bizType: 'notice' } })
  noticeDialog.form.coverImg = res.data?.data?.url || ''
}

async function saveBanner() {
  if (!bannerDialog.form.title || !bannerDialog.form.title.trim()) {
    ElMessage.warning('请输入轮播图标题')
    return
  }
  if (!bannerDialog.form.imgUrl) {
    ElMessage.warning('请上传轮播图图片')
    return
  }
  if (bannerDialog.form.linkUrl && !/^\/shop\/detail\/\d+$/.test(bannerDialog.form.linkUrl)) {
    ElMessage.warning('关联商品链接格式不正确')
    return
  }
  await http.post('/content/admin/banner/save', bannerDialog.form)
  ElMessage.success('保存成功')
  bannerDialog.visible = false
  await loadBanners()
}
async function saveNotice() { await http.post('/content/admin/notice/save', noticeDialog.form); ElMessage.success('保存成功'); noticeDialog.visible = false; await loadNotices() }

async function toggleBanner(row, v) { await http.post('/content/admin/banner/status', null, { params: { id: row.id, status: v ? 1 : 0 } }); ElMessage.success('状态已更新'); await loadBanners() }
async function toggleNoticeStatus(row) { await http.post('/content/admin/notice/status', null, { params: { id: row.id, status: row.status === 1 ? 0 : 1 } }); ElMessage.success('状态已更新'); await loadNotices() }
async function toggleNoticeTop(row) { await http.post('/content/admin/notice/top', null, { params: { id: row.id, isTop: row.isTop === 1 ? 0 : 1 } }); ElMessage.success('置顶状态已更新'); await loadNotices() }

async function removeBanner(id) { await http.delete(`/content/admin/banner/delete/${id}`); ElMessage.success('删除成功'); await loadBanners() }
async function removeNotice(id) { await http.delete(`/content/admin/notice/delete/${id}`); ElMessage.success('删除成功'); await loadNotices() }

onMounted(async () => { await Promise.all([loadBanners(), loadNotices(), loadProducts()]) })
</script>