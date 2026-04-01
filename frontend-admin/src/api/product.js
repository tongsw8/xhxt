import http from './http'

export function fetchCategoryList(params) {
  return http.get('/admin/product/category/list', { params })
}

export function saveCategory(data) {
  return http.post('/admin/product/category/save', data)
}

export function fetchCategoryProductCount(id) {
  return http.get(`/admin/product/category/product-count/${id}`)
}

export function deleteCategory(id) {
  return http.delete(`/admin/product/category/delete/${id}`)
}

export function fetchProductList(params) {
  return http.get('/admin/product/list', { params })
}

export function saveProduct(data) {
  return http.post('/admin/product/save', data)
}

export function changeProductStatus(id, status) {
  return http.post('/admin/product/status', null, { params: { id, status } })
}

export function deleteProduct(id) {
  return http.delete(`/admin/product/delete/${id}`)
}

export function uploadImage(file, onProgress) {
  const form = new FormData()
  form.append('file', file)
  return http.post('/admin/upload/image', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (evt) => {
      if (!evt || !evt.total || !onProgress) return
      const percent = Math.min(100, Math.round((evt.loaded * 100) / evt.total))
      onProgress(percent)
    },
  })
}
