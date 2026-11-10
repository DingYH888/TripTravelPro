import { defineStore } from 'pinia'
import * as authApi from '../api/auth'
import {
  clearAuthStorage,
  PERMISSIONS_KEY,
  REFRESH_KEY,
  TOKEN_KEY,
  USERNAME_KEY
} from '../api/http'

export function loadPermissions(): string[] {
  try {
    const raw = localStorage.getItem(PERMISSIONS_KEY)
    if (!raw) {
      return []
    }
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed.map(String) : []
  } catch {
    return []
  }
}

interface AdminState {
  token: string
  refreshToken: string
  username: string
  permissions: string[]
}

export const useAdminStore = defineStore('admin', {
  state: (): AdminState => ({
    token: '',
    refreshToken: '',
    username: '',
    permissions: []
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    hasPermission: (state) => (permission?: string) => {
      if (!permission) {
        return true
      }
      return state.permissions.includes(permission)
    }
  },
  actions: {
    restore() {
      this.token = localStorage.getItem(TOKEN_KEY) || ''
      this.refreshToken = localStorage.getItem(REFRESH_KEY) || ''
      this.username = localStorage.getItem(USERNAME_KEY) || ''
      this.permissions = loadPermissions()
    },
    persist() {
      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(REFRESH_KEY, this.refreshToken)
      localStorage.setItem(USERNAME_KEY, this.username)
      localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(this.permissions))
    },
    clear() {
      this.token = ''
      this.refreshToken = ''
      this.username = ''
      this.permissions = []
      clearAuthStorage()
    },
    async login(username: string, password: string) {
      const data = await authApi.adminLogin(username, password)
      this.token = data.accessToken
      this.refreshToken = data.refreshToken
      this.username = data.username
      this.permissions = Array.isArray(data.permissions) ? data.permissions : []
      this.persist()
      return data
    },
    async logout() {
      try {
        await authApi.logout(this.refreshToken)
      } catch {
        // 退出接口失败也要清理本地登录态
      }
      this.clear()
    },
    async refreshMe() {
      const me = await authApi.fetchMe()
      this.username = me.username || this.username
      this.permissions = Array.isArray(me.permissions) ? me.permissions : this.permissions
      this.persist()
      return me
    }
  }
})
