import http from './http'

export interface AdminLoginResult {
  accessToken: string
  refreshToken: string
  expiresIn: number
  username: string
  permissions: string[]
}

export interface AdminMe {
  uid: number
  username: string
  permissions: string[]
}

export function adminLogin(username: string, password: string) {
  return http.post<any, AdminLoginResult>('/auth/admin/login', { username, password })
}

export function logout(refreshToken: string) {
  return http.post<any, unknown>('/auth/logout', { refreshToken })
}

export function fetchMe() {
  return http.get<any, AdminMe>('/admin/me')
}
