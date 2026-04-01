import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_BASE_API || '/xhxt/api',
  timeout: 15000,
})

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

