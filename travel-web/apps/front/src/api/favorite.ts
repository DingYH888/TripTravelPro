import http from './http'
import type { PageResult, Product } from './types'

export function listFavorites(page = 1, size = 8) {
  return http.get<unknown, PageResult<Product>>('/member/favorites', { params: { page, size } })
}

export function favoriteStatus(pid: number | string) {
  return http.get<unknown, boolean>(`/member/favorites/${pid}/status`)
}

export function addFavorite(pid: number | string) {
  return http.post<unknown, boolean>(`/member/favorites/${pid}`)
}

export function removeFavorite(pid: number | string) {
  return http.delete<unknown, boolean>(`/member/favorites/${pid}`)
}
