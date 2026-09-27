import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { AxiosRequestConfig, AxiosResponse } from 'axios'

/** 后端统一响应结构（与 backend Result.java 对应）。 */
export interface Result<T> {
  code: number
  msg: string
  data: T
}

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
  (response: AxiosResponse<Result<unknown>>) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg))
    }
    return res.data as any
  },
  (error) => {
    ElMessage.error(error.response?.data?.msg || error.message || '网络异常')
    return Promise.reject(error)
  }
)

/** 泛型 GET/POST/PUT/DELETE：返回已解包的 data。 */
export const get = <T>(url: string, config?: AxiosRequestConfig): Promise<T> =>
  request.get(url, config)
export const post = <T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> =>
  request.post(url, data, config)
export const put = <T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> =>
  request.put(url, data, config)
export const del = <T>(url: string, config?: AxiosRequestConfig): Promise<T> =>
  request.delete(url, config)

export default {
  get,
  post,
  put,
  delete: del
}
