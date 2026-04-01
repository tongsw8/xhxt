import http from './http'

export function fetchShopCategories() {
  return http.get('/shop/category/list')
}

export function fetchShopProducts(params) {
  return http.get('/shop/product/list', { params })
}
