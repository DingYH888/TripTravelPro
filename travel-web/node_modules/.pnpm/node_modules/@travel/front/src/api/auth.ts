import http from './http'
import type { CaptchaPayload, RegisterPayload, TokenPayload } from './types'

// 管理员登录态沿用后台应用既有的存储键，保证同一入口下两个应用可共享
export const ADMIN_TOKEN_KEY = 'travel_admin_token'
export const ADMIN_REFRESH_KEY = 'travel_admin_refresh'
export const ADMIN_USERNAME_KEY = 'travel_admin_username'
export const ADMIN_PERMISSIONS_KEY = 'travel_admin_permissions'

export function memberLogin(username: string, password: string) {
  return http.post<unknown, TokenPayload>('/auth/member/login', { username, password })
}

export function adminLogin(username: string, password: string) {
  return http.post<unknown, TokenPayload>('/auth/admin/login', { username, password })
}

export function saveAdminSession(data: TokenPayload) {
  localStorage.setItem(ADMIN_TOKEN_KEY, data.accessToken)
  localStorage.setItem(ADMIN_REFRESH_KEY, data.refreshToken)
  localStorage.setItem(ADMIN_USERNAME_KEY, data.username || '')
  localStorage.setItem(ADMIN_PERMISSIONS_KEY, JSON.stringify(data.permissions || []))
}

export function memberRegister(payload: RegisterPayload) {
  return http.post<unknown, void>('/auth/member/register', payload)
}

export function getCaptcha() {
  return http.get<unknown, CaptchaPayload>('/auth/captcha')
}

export function activate(code: string) {
  return http.get<unknown, string>('/auth/member/activate', { params: { code } })
}

export function refreshToken(refreshToken: string) {
  return http.post<unknown, TokenPayload>('/auth/refresh', { refreshToken })
}

export function logout(refreshToken?: string) {
  return http.post<unknown, void>('/auth/logout', { refreshToken: refreshToken || '' })
}
