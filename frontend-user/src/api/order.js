import http from './http'

export function createOrderFromCart(addressId, cardMessage, deliveryExpectTime) {
  return http.post('/user/order/create-from-cart', { addressId, cardMessage, deliveryExpectTime })
}

export function createDirectOrder(addressId, productId, quantity, cardMessage, deliveryExpectTime) {
  return http.post('/user/order/create-direct', { addressId, productId, quantity, cardMessage, deliveryExpectTime })
}

export function getOrderDetail(orderNo) {
  return http.get(`/user/order/detail/${orderNo}`)
}

export function payOrder(orderNo) {
  return http.post(`/user/order/pay/${orderNo}`)
}

export function listOrders() {
  return http.get('/user/order/list')
}

export function urgeOrder(orderNo) {
  return http.post(`/user/order/urge/${orderNo}`)
}

export function listProductReviews(productId) {
  return http.get('/user/order/review/list', { params: { productId } })
}

export function addProductReview(orderNo, productId, content) {
  return http.post('/user/order/review/add', null, { params: { orderNo, productId, content } })
}

export function likeProductReview(commentId) {
  return http.post(`/user/order/review/like/${commentId}`)
}