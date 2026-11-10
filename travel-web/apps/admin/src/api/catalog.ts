import http, { TOKEN_KEY } from './http'

export interface Category {
  cid: number
  cname: string
}

export interface Product {
  pid: number
  productName: string
  price: number
  hotline: string
  status: boolean
  productDesc: string
  pImage: string
  cid: number
  category?: Category | null
}

export interface ProductPayload {
  productName: string
  price: number
  hotline: string
  status: boolean
  productDesc: string
  pImage: string
  cid: number
}

export interface PageResult<T> {
  total: number
  pages: number
  current: number
  size: number
  records: T[]
}

export function getProducts(page: number, size: number) {
  return http.get<any, PageResult<Product>>('/admin/products', { params: { page, size } })
}

export function getProduct(pid: number) {
  return http.get<any, Product>(`/admin/products/${pid}`)
}

export function createProduct(data: ProductPayload) {
  return http.post<any, Product>('/admin/products', data)
}

export function updateProduct(pid: number, data: ProductPayload) {
  return http.put<any, Product>(`/admin/products/${pid}`, data)
}

export function toggleProductStatus(pid: number) {
  return http.patch<any, boolean>(`/admin/products/${pid}/status`)
}

export function uploadProductImage(file: File) {
  const form = new FormData()
  form.append('file', file)
  return http.post<any, { url: string }>('/admin/products/upload', form, {
    headers: {
      'Content-Type': 'multipart/form-data',
      Authorization: `Bearer ${localStorage.getItem(TOKEN_KEY) || ''}`
    }
  })
}

export function getCategories(page: number, size: number) {
  return http.get<any, PageResult<Category>>('/admin/categories', { params: { page, size } })
}

export function getAllCategories() {
  return http.get<any, Category[]>('/admin/categories/all')
}

export function createCategory(cname: string) {
  return http.post<any, Category>('/admin/categories', { cname })
}

export function updateCategory(cid: number, cname: string) {
  return http.put<any, Category>(`/admin/categories/${cid}`, { cname })
}

export function deleteCategory(cid: number) {
  return http.delete<any, unknown>(`/admin/categories/${cid}`)
}
