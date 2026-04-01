import http from './http'

export function login(account, password) {
  return http.post('/auth/login', { account, password })
}
