import http from './http'

export function createOrderFromCart(addressId) {
  return http.post('/user/order/create-from-cart', { addressId })
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
