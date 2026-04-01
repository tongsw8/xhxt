import axios from 'axios'

// 后端统一走 /xhxt/api/**，通过 VITE_BASE_API 控制前后端联调地址
const http = axios.create({
  baseURL: import.meta.env.VITE_BASE_API || '/xhxt/api',
  timeout: 15000,
})

// scaffold：把 token 写进 Authorization，具体登录接口后续再对接
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.success === false) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return response
  },
  (error) => {
    const msg =
      error.response?.data?.message ||
      error.message ||
      '网络错误'
    return Promise.reject(new Error(msg))
  }
)

export default http

