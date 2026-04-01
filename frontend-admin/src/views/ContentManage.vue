<template>
  <el-card>
    <template #header>内容管理</template>

    <el-tabs v-model="tab">
      <el-tab-pane label="轮播图" name="b">
        <div style="margin-bottom:12px">
          <el-button type="primary" @click="openBanner()">新增轮播图</el-button>
        </div>
        <el-table :data="banners" border>
          <el-table-column prop="sortNo" label="序号" width="90" />
          <el-table-column label="图片" width="130">
            <template #default="{ row }">
              <el-image :src="row.imgUrl" fit="cover" style="width:80px;height:44px;border-radius:6px" />
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" />
          <el-table-column label="是否展示" width="120">
            <template #default="{ row }">
              <el-switch :model-value="row.status === 1" @change="(v) => toggleBanner(row, v)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <el-button size="small" @click="openBanner(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeBanner(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="公告" name="n"><div style="padding: 12px 0; color:#94a3b8">暂保留原模块</div></el-tab-pane>
      <el-tab-pane label="新闻" name="news"><div style="padding: 12px 0; color:#94a3b8">暂保留原模块</div></el-tab-pane>
    </el-tabs>

    <el-dialog v-model="bannerDialog.visible" :title="bannerDialog.form.id ? '编辑轮播图' : '新增轮播图'" width="620px">
      <el-form :model="bannerDialog.form" label-width="100px">
        <el-form-item label="标题"><el-input v-model="bannerDialog.form.title" /></el-form-item>
        <el-form-item label="图片">
          <el-upload :show-file-list="false" :http-request="uploadBannerImage" accept="image/*">
            <el-button>上传图片</el-button>
          </el-upload>
          <div v-if="bannerDialog.form.imgUrl" style="margin-top:8px">
            <el-image :src="bannerDialog.form.imgUrl" fit="cover" style="width:120px;height:68px;border-radius:8px" />
          </div>
        </el-form-item>
        <el-form-item label="序号"><el-input-number v-model="bannerDialog.form.sortNo" :min="0" /></el-form-item>
        <el-form-item label="是否展示">
          <el-radio-group v-model="bannerDialog.form.status">
            <el-radio :value="1">展示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bannerDialog.visible=false">取消</el-button>
        <el-button type="primary" @click="saveBanner">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const tab = ref('b')
const banners = ref([])

const bannerDialog = reactive({
  visible: false,
  form: { id: null, title: '', imgUrl: '', sortNo: 0, status: 1 },
})

async function loadBanners() {
  const res = await http.get('/content/admin/banner/list')
  banners.value = res.data?.data || []
}

function openBanner(row) {
  if (row) bannerDialog.form = { ...row }
  else bannerDialog.form = { id: null, title: '', imgUrl: '', sortNo: 0, status: 1 }
  bannerDialog.visible = true
}

async function uploadBannerImage(option) {
  const form = new FormData()
  form.append('file', option.file)
  const res = await http.post('/admin/upload/image', form, { headers: { 'Content-Type': 'multipart/form-data' } })
  bannerDialog.form.imgUrl = res.data?.data?.url || ''
}

async function saveBanner() {
  await http.post('/content/admin/banner/save', bannerDialog.form)
  ElMessage.success('保存成功')
  bannerDialog.visible = false
  await loadBanners()
}

async function toggleBanner(row, v) {
  await http.post('/content/admin/banner/status', null, { params: { id: row.id, status: v ? 1 : 0 } })
  ElMessage.success('状态已更新')
  await loadBanners()
}

async function removeBanner(id) {
  await http.delete(`/content/admin/banner/delete/${id}`)
  ElMessage.success('删除成功')
  await loadBanners()
}

onMounted(loadBanners)
</script>