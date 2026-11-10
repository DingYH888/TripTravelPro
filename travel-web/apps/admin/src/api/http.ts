import axios, { AxiosError } from 'axios'

export const TOKEN_KEY = 'travel_admin_token'
export const REFRESH_KEY = 'travel_admin_refresh'
export const USERNAME_KEY = 'travel_admin_username'
export const PERMISSIONS_KEY = 'travel_admin_permissions'

// 全局统一登录入口：生产环境与后台同源，开发环境跳转到前台开发服务器
const UNIFIED_LOGIN_URL =
  (import.meta.env.VITE_LOGIN_URL as string | undefined) ||
  (import.meta.env.DEV ? 'http://127.0.0.1:5173/login' : '/login')

// 登录接口本身返回 401 属于认证失败，不应触发全局登录失效逻辑
const AUTH_ENTRY_PATTERN = /\/auth\/(member|admin)\/login$/

export function unifiedLoginUrl(identity: 'member' | 'admin', redirect?: string) {
  const params = new URLSearchParams({ identity })
  if (redirect) {
    params.set('redirect', redirect)
  }
  return `${UNIFIED_LOGIN_URL}?${params.toString()}`
}

export function redirectToUnifiedLogin(redirect?: string) {
  window.location.replace(unifiedLoginUrl('admin', redirect))
}

export function clearAuthStorage() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
  localStorage.removeItem(USERNAME_KEY)
  localStorage.removeItem(PERMISSIONS_KEY)
}

const http = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) {
        return body.data
      }
      throw new Error(body.message || '请求失败')
    }
    return body
  },
  (error: AxiosError<{ code?: number; message?: string }>) => {
    const status = error.response?.status
    if (status === 401 && !AUTH_ENTRY_PATTERN.test(error.config?.url || '')) {
      clearAuthStorage()
      redirectToUnifiedLogin(`${window.location.pathname}${window.location.search}${window.location.hash}`)
    }
    let message = error.response?.data?.message || error.message || '网络异常'
    if (status === 403 && !error.response?.data?.message) {
      message = '没有访问权限'
    }
    return Promise.reject(new Error(message))
  }
)

export default http
