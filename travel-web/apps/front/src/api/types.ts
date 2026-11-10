export interface Category {
  cid: number
  cname: string
}

export interface Product {
  pid: number
  productName: string
  price: number
  pImage?: string
  hotline?: string
  productDesc?: string
  cid?: number
  status?: boolean
}

export interface PageResult<T> {
  total: number
  pages: number
  current: number
  size: number
  records: T[]
}

export interface TokenPayload {
  tokenType: string
  accessToken: string
  refreshToken: string
  expiresIn: number
  username: string
  permissions: string[]
}

export interface CaptchaPayload {
  key: string
  image: string
}

export interface RegisterPayload {
  username: string
  password: string
  email: string
  phoneNum: string
  sex?: string
  birthday?: string
  checkCodeKey: string
  checkCode: string
}
