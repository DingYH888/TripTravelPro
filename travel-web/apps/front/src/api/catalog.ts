import http from './http'
import type { Category, PageResult, Product } from './types'

export function getCategories() {
  return http.get<unknown, Category[]>('/public/categories')
}

export function getProducts(params: { cid?: number | string; productName?: string; page?: number; size?: number }) {
  return http.get<unknown, PageResult<Product>>('/public/products', { params })
}

export function getProduct(pid: number | string) {
  return http.get<unknown, Product>(`/public/products/${pid}`)
}
