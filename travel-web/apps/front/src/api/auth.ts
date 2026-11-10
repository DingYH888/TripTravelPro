import http from './http'
import type { CaptchaPayload, RegisterPayload, TokenPayload } from './types'

export function memberLogin(username: string, password: string) {
  return http.post<unknown, TokenPayload>('/auth/member/login', { username, password })
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
