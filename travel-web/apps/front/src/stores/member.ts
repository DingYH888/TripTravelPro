import { defineStore } from 'pinia'
import * as authApi from '../api/auth'
import { REFRESH_KEY, TOKEN_KEY, USER_KEY } from '../api/http'

interface MemberState {
  accessToken: string
  refreshToken: string
  username: string
}

export const useMemberStore = defineStore('member', {
  state: (): MemberState => ({
    accessToken: '',
    refreshToken: '',
    username: ''
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.accessToken)
  },
  actions: {
    restore() {
      this.accessToken = localStorage.getItem(TOKEN_KEY) || ''
      this.refreshToken = localStorage.getItem(REFRESH_KEY) || ''
      this.username = localStorage.getItem(USER_KEY) || ''
    },
    setAuth(payload: { accessToken: string; refreshToken: string; username?: string }) {
      this.accessToken = payload.accessToken
      this.refreshToken = payload.refreshToken
      this.username = payload.username || this.username
      localStorage.setItem(TOKEN_KEY, this.accessToken)
      localStorage.setItem(REFRESH_KEY, this.refreshToken)
      if (this.username) {
        localStorage.setItem(USER_KEY, this.username)
      }
    },
    clear() {
      this.accessToken = ''
      this.refreshToken = ''
      this.username = ''
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(REFRESH_KEY)
      localStorage.removeItem(USER_KEY)
    },
    async login(username: string, password: string) {
      const data = await authApi.memberLogin(username, password)
      this.setAuth(data)
      return data
    },
    async logout() {
      try {
        await authApi.logout(this.refreshToken)
      } catch {
        // 退出接口失败也要清理本地状态
      }
      this.clear()
    }
  }
})
