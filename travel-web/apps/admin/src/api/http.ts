import axios, { AxiosError } from 'axios'

export const TOKEN_KEY = 'travel_admin_token'
export const REFRESH_KEY = 'travel_admin_refresh'
export const USERNAME_KEY = 'travel_admin_username'
export const PERMISSIONS_KEY = 'travel_admin_permissions'

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
    if (status === 401) {
      clearAuthStorage()
      const loginPath = `${import.meta.env.BASE_URL}login`
      if (window.location.pathname !== loginPath) {
        window.location.href = loginPath
      }
    }
    let message = error.response?.data?.message || error.message || '网络异常'
    if (status === 403 && !error.response?.data?.message) {
      message = '没有访问权限'
    }
    return Promise.reject(new Error(message))
  }
)

export default http
