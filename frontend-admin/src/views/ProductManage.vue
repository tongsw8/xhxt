<template>
  <div class="product-manage">
    <div class="header">
      <div>
        <h2>商品中心管理</h2>
        <p>商品分类与商品信息统一管理</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="tabs">
      <el-tab-pane label="商品分类" name="category">
        <el-card>
          <div class="toolbar">
            <el-input v-model="categoryQuery.keyword" placeholder="分类名称关键词" clearable style="width: 240px" />
            <el-button type="primary" @click="loadCategories">查询</el-button>
            <el-button type="success" @click="openCategoryDialog()">新增分类</el-button>
          </div>

          <el-table :data="categoryList" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="categoryName" label="分类名称" />
            <el-table-column prop="sortNo" label="排序" width="100" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="openCategoryDialog(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="onDeleteCategory(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="商品信息" name="product">
        <el-card>
          <div class="toolbar wrap">
            <el-input v-model="productQuery.keyword" placeholder="商品名关键词" clearable style="width: 220px" />
            <el-select v-model="productQuery.categoryId" placeholder="分类" clearable style="width: 180px">
              <el-option v-for="c in categoryList" :key="c.id" :label="c.categoryName" :value="c.id" />
            </el-select>
            <el-select v-model="productQuery.status" placeholder="状态" clearable style="width: 140px">
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="0" />
            </el-select>
            <el-button type="primary" @click="onSearchProducts">查询</el-button>
            <el-button type="success" @click="openProductDialog()">新增商品</el-button>
          </div>

          <el-table :data="productList" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="封面" width="100">
              <template #default="{ row }">
                <el-image
                  v-if="row.coverImg"
                  :src="toImageUrl(row.coverImg)"
                  :preview-src-list="[toImageUrl(row.coverImg)]"
                  fit="cover"
                  style="width: 56px; height: 56px; border-radius: 8px"
                />
                <span v-else class="muted">无图</span>
              </template>
            </el-table-column>
            <el-table-column prop="productName" label="商品名称" min-width="160" />
            <el-table-column prop="categoryName" label="分类" width="120" />
            <el-table-column prop="price" label="价格" width="100" />
            <el-table-column prop="stock" label="库存" width="90" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="320" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="openProductDialog(row)">编辑</el-button>
                <el-button size="small" type="warning" @click="toggleStatus(row)">
                  {{ row.status === 1 ? '下架' : '上架' }}
                </el-button>
                <el-button size="small" type="danger" @click="onDeleteProduct(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :total="pager.total"
              :current-page="pager.pageNo"
              :page-size="pager.pageSize"
              :page-sizes="[10, 20, 50]"
              @current-change="onPageChange"
              @size-change="onSizeChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="categoryDialog.visible" :title="categoryDialog.form.id ? '编辑分类' : '新增分类'" width="480px">
      <el-form :model="categoryDialog.form" label-width="90px">
        <el-form-item label="分类名称">
          <el-input v-model="categoryDialog.form.categoryName" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryDialog.form.sortNo" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="categoryDialog.form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitCategory">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="productDialog.visible" :title="productDialog.form.id ? '编辑商品' : '新增商品'" width="680px">
      <el-form :model="productDialog.form" label-width="100px">
        <el-form-item label="商品名称">
          <el-input v-model="productDialog.form.productName" />
        </el-form-item>
        <el-form-item label="所属分类">
          <el-select v-model="productDialog.form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categoryList" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="productDialog.form.price" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="productDialog.form.stock" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-upload :show-file-list="false" :http-request="uploadCover" accept="image/*">
            <el-button :loading="uploading">上传图片</el-button>
          </el-upload>
          <el-progress v-if="uploading" :percentage="uploadPercent" :stroke-width="10" style="width: 220px; margin-top: 10px" />
          <div class="cover-preview" v-if="productDialog.form.coverImg">
            <el-image :src="toImageUrl(productDialog.form.coverImg)" fit="cover" style="width: 72px; height: 72px; border-radius: 8px" />
          </div>
        </el-form-item>
        <el-form-item label="详情">
          <el-input v-model="productDialog.form.detailText" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="productDialog.form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitProduct">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchCategoryList,
  saveCategory,
  fetchCategoryProductCount,
  deleteCategory,
  fetchProductList,
  saveProduct,
  changeProductStatus,
  deleteProduct,
  uploadImage,
} from '../api/product'

const activeTab = ref('category')

const categoryQuery = reactive({ keyword: '' })
const categoryList = ref([])

const productQuery = reactive({ keyword: '', categoryId: null, status: null })
const productList = ref([])
const pager = reactive({ pageNo: 1, pageSize: 10, total: 0 })

const uploading = ref(false)
const uploadPercent = ref(0)

const categoryDialog = reactive({
  visible: false,
  form: { id: null, categoryName: '', sortNo: 0, status: 1 },
})

const productDialog = reactive({
  visible: false,
  form: {
    id: null,
    productName: '',
    categoryId: null,
    price: 0,
    stock: 0,
    coverImg: '',
    detailText: '',
    status: 1,
  },
})

const backendOrigin = (() => {
  const base = import.meta.env.VITE_BASE_API || ''
  const m = base.match(/^(https?:\/\/[^/]+)/)
  return m ? m[1] : window.location.origin
})()

async function loadCategories() {
  const { data } = await fetchCategoryList({ keyword: categoryQuery.keyword || undefined })
  categoryList.value = data.data || []
}

async function loadProducts() {
  const { data } = await fetchProductList({
    keyword: productQuery.keyword || undefined,
    categoryId: productQuery.categoryId || undefined,
    status: productQuery.status,
    pageNo: pager.pageNo,
    pageSize: pager.pageSize,
  })
  const pageData = data.data || {}
  productList.value = pageData.records || []
  pager.total = Number(pageData.total || 0)
}

function onSearchProducts() {
  pager.pageNo = 1
  loadProducts()
}

function onPageChange(page) {
  pager.pageNo = page
  loadProducts()
}

function onSizeChange(size) {
  pager.pageSize = size
  pager.pageNo = 1
  loadProducts()
}

function openCategoryDialog(row) {
  if (row) {
    categoryDialog.form = { ...row }
  } else {
    categoryDialog.form = { id: null, categoryName: '', sortNo: 0, status: 1 }
  }
  categoryDialog.visible = true
}

async function submitCategory() {
  await saveCategory(categoryDialog.form)
  ElMessage.success('保存成功')
  categoryDialog.visible = false
  await loadCategories()
}

async function onDeleteCategory(row) {
  const { data } = await fetchCategoryProductCount(row.id)
  const count = Number(data.data?.count || 0)
  await ElMessageBox.confirm(`分类【${row.categoryName}】下共有 ${count} 个商品，确认删除？`, '提示', { type: 'warning' })
  await deleteCategory(row.id)
  ElMessage.success('删除成功')
  await loadCategories()
}

function openProductDialog(row) {
  if (row) {
    productDialog.form = {
      id: row.id,
      productName: row.productName,
      categoryId: row.categoryId,
      price: Number(row.price || 0),
      stock: Number(row.stock || 0),
      coverImg: row.coverImg || '',
      detailText: row.detailText || '',
      status: row.status ?? 1,
    }
  } else {
    productDialog.form = {
      id: null,
      productName: '',
      categoryId: null,
      price: 0,
      stock: 0,
      coverImg: '',
      detailText: '',
      status: 1,
    }
  }
  uploading.value = false
  uploadPercent.value = 0
  productDialog.visible = true
}

async function submitProduct() {
  await saveProduct(productDialog.form)
  ElMessage.success('保存成功')
  productDialog.visible = false
  await loadProducts()
}

async function toggleStatus(row) {
  const target = row.status === 1 ? 0 : 1
  await changeProductStatus(row.id, target)
  ElMessage.success('状态更新成功')
  await loadProducts()
}

async function onDeleteProduct(row) {
  await ElMessageBox.confirm(`确认删除商品【${row.productName}】？`, '提示', { type: 'warning' })
  await deleteProduct(row.id)
  ElMessage.success('删除成功')
  await loadProducts()
}

async function uploadCover(option) {
  const { file } = option
  uploading.value = true
  uploadPercent.value = 0
  try {
    const { data } = await uploadImage(file, (percent) => {
      uploadPercent.value = percent
      if (option.onProgress) option.onProgress({ percent })
    })
    productDialog.form.coverImg = data.data?.url || ''
    if (option.onSuccess) option.onSuccess(data)
    ElMessage.success('上传成功')
  } catch (e) {
    if (option.onError) option.onError(e)
    const raw = e?.message || ''
    let tip = '上传失败'
    if (raw.includes('2MB') || raw.includes('file size') || raw.includes('Maximum upload size')) {
      tip = '上传失败：文件超过 2MB 限制，请压缩后重试'
    } else if (raw.includes('格式') || raw.includes('type') || raw.includes('image')) {
      tip = '上传失败：不支持的文件格式，请上传图片文件'
    } else if (raw.includes('目录') || raw.includes('mkdirs')) {
      tip = '上传失败：服务器存储目录异常，请联系管理员'
    } else if (raw.includes('网络') || raw.includes('timeout') || raw.includes('Network')) {
      tip = '上传失败：网络超时，请稍后重试'
    } else if (raw) {
      tip = `上传失败：${raw}`
    }
    ElMessage.error({ message: tip, duration: 4000 })
  } finally {
    setTimeout(() => {
      uploading.value = false
    }, 300)
  }
}

function toImageUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/')) return `${backendOrigin}${path}`
  return `${backendOrigin}/xhxt/file/flower/${path}`
}

onMounted(async () => {
  await loadCategories()
  await loadProducts()
})
</script>

<style scoped>
.product-manage {
  width: 100%;
}

.header {
  margin-bottom: 12px;
}

.header h2 {
  margin: 0;
  font-size: 24px;
}

.header p {
  margin: 6px 0 0;
  color: #666;
}

.tabs {
  margin-top: 8px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.toolbar.wrap {
  flex-wrap: wrap;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.cover-preview {
  margin-top: 10px;
}

.muted {
  color: #999;
  font-size: 12px;
}
</style>
