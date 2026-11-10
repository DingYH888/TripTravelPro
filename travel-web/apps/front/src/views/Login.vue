<template>
  <div class="tn-auth-page">
    <div class="tn-auth-bar">
      <RouterLink to="/login" class="tn-logo">
        <img src="/images/brand-logo.png" alt="去哪旅游网" />
      </RouterLink>
      <span class="tn-auth-bar-tip">统一登录入口</span>
    </div>

    <div class="tn-auth-card">
      <h2>欢迎登录</h2>
      <p class="sub">请先选择登录身份，再输入对应账号密码</p>

      <div class="tn-auth-tabs" role="tablist">
        <button
          type="button"
          role="tab"
          :aria-selected="identity === 'member'"
          :class="{ active: identity === 'member' }"
          @click="switchIdentity('member')"
        >
          用户登录
        </button>
        <button
          type="button"
          role="tab"
          :aria-selected="identity === 'admin'"
          :class="{ active: identity === 'admin' }"
          @click="switchIdentity('admin')"
        >
          管理员登录
        </button>
      </div>

      <p class="tn-auth-hint">
        {{ identity === 'member' ? '登录后可浏览线路、收藏心仪行程' : '请使用后台管理员账号登录，登录后进入管理后台' }}
      </p>

      <p v-if="error" class="tn-alert error">{{ error }}</p>

      <form @submit.prevent="submit">
        <div class="tn-field">
          <label for="login-username">{{ identity === 'member' ? '账号' : '管理员账号' }}</label>
          <input
            id="login-username"
            v-model.trim="username"
            type="text"
            :placeholder="identity === 'member' ? '请输入账号 / 手机 / 邮箱' : '请输入管理员账号'"
            autocomplete="username"
          />
        </div>
        <div class="tn-field">
          <label for="login-password">密码</label>
          <input
            id="login-password"
            v-model="password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
          />
        </div>
        <button class="tn-btn tn-auth-submit" type="submit" :disabled="submitting">
          {{ submitting ? '登录中...' : '登录' }}
        </button>
      </form>

      <div v-if="identity === 'member'" class="tn-auth-foot">
        <span>还没有账号？</span>
        <RouterLink to="/register">立即注册</RouterLink>
      </div>
      <div v-else class="tn-auth-foot">
        <span>管理员账号由系统管理员统一分配</span>
      </div>
    </div>

    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteFooter from '../components/SiteFooter.vue'
import { adminLogin, saveAdminSession } from '../api/auth'
import { useMemberStore } from '../stores/member'

type Identity = 'member' | 'admin'

const route = useRoute()
const router = useRouter()
const member = useMemberStore()

const identity = ref<Identity>(route.query.identity === 'admin' ? 'admin' : 'member')
const username = ref('')
const password = ref('')
const submitting = ref(false)
const error = ref('')

function switchIdentity(target: Identity) {
  if (identity.value === target || submitting.value) {
    return
  }
  identity.value = target
  error.value = ''
  password.value = ''
}

// 只允许站内相对路径，避免登录后被重定向到外部站点
function safeRedirect(value: unknown, fallback: string): string {
  if (typeof value !== 'string' || !value.startsWith('/') || value.startsWith('//')) {
    return fallback
  }
  return value
}

async function submit() {
  error.value = ''
  if (!username.value || !password.value) {
    error.value = '请输入账号和密码'
    return
  }
  submitting.value = true
  try {
    if (identity.value === 'member') {
      await member.login(username.value, password.value)
      const redirect = safeRedirect(route.query.redirect, '/')
      router.replace(redirect.startsWith('/admin') ? '/' : redirect)
      return
    }

    const data = await adminLogin(username.value, password.value)
    saveAdminSession(data)
    const redirect = safeRedirect(route.query.redirect, '/admin/')
    window.location.replace(redirect.startsWith('/admin') ? redirect : '/admin/')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败'
  } finally {
    submitting.value = false
  }
}
</script>
