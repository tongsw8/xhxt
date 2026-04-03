import http from './http'

export function getProfileInfo() {
  return http.get('/user/profile/info')
}

export function updateProfile(data) {
  return http.post('/user/profile/update', data)
}

export function changePassword(data) {
  return http.post('/user/profile/password', data)
}

export function listAddress() {
  return http.get('/user/profile/address/list')
}

export function saveAddress(data) {
  return http.post('/user/profile/address/save', data)
}

export function deleteAddress(id) {
  return http.delete(`/user/profile/address/delete/${id}`)
}

export function setDefaultAddress(id) {
  return http.post(`/user/profile/address/default/${id}`)
}
