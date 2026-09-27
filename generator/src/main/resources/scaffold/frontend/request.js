import axios from 'axios'
import { ElMessage } from 'element-plus'

/** 后端统一响应结构（与 backend Result.java 对应）。 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：附加 token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一解包 Result
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg))
    }
    return res.data
  },
  (error) => {
    ElMessage.error(error.response?.data?.msg || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export const get = (url, config) => request.get(url, config)
export const post = (url, data, config) => request.post(url, data, config)
export const put = (url, data, config) => request.put(url, data, config)
export const del = (url, config) => request.delete(url, config)

export default {
  get,
  post,
  put,
  delete: del
}
