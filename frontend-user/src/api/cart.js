import http from './http'

export function addToCart(productId, quantity = 1) {
  return http.post('/user/cart/add', null, { params: { productId, quantity } })
}

export function updateCartQuantity(productId, quantity) {
  return http.post('/user/cart/update', null, { params: { productId, quantity } })
}

export function removeFromCart(productId) {
  return http.delete(`/user/cart/remove/${productId}`)
}

export function getCartList() {
  return http.get('/user/cart/list')
}

export function addToFavorite(productId) {
  return http.post('/user/favorite/add', null, { params: { productId } })
}

export function removeFromFavorite(productId) {
  return http.delete(`/user/favorite/remove/${productId}`)
}

export function getFavoriteList() {
  return http.get('/user/favorite/list')
}

export function checkFavorite(productId) {
  return http.get(`/user/favorite/check/${productId}`)
}
