import http from './http'

/**
 * @returns {Promise<import('axios').AxiosResponse<any>>} 原始 axios 响应，body 为后端 Result
 */
export function login(account, password) {
  return http.post('/auth/login', { account, password })
}

export function register(account, password, nickname) {
  return http.post('/auth/register', { account, password, nickname })
}
