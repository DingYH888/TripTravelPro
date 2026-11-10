<template>
  <div class="tn-auth-page">
    <div class="tn-auth-bar">
      <RouterLink to="/" class="tn-logo">
        <img src="/frontdesk/images/logo.png" alt="去哪旅游网" />
      </RouterLink>
      <RouterLink to="/">
        <span class="glyphicon glyphicon-home"></span>
        首页
      </RouterLink>
    </div>

    <div class="tn-auth-card">
      <h2>账户登录</h2>
      <p class="sub">登录后可收藏线路、管理个人收藏</p>

      <p v-if="error" class="tn-alert error">{{ error }}</p>

      <form @submit.prevent="submit">
        <div class="tn-field">
          <label for="login-username">账号</label>
          <input id="login-username" v-model.trim="username" type="text" placeholder="请输入账号 / 手机 / 邮箱" autocomplete="username" />
        </div>
        <div class="tn-field">
          <label for="login-password">密码</label>
          <input id="login-password" v-model="password" type="password" placeholder="请输入密码" autocomplete="current-password" />
        </div>
        <button class="tn-btn tn-auth-submit" type="submit" :disabled="submitting">
          {{ submitting ? '登录中...' : '登录' }}
        </button>
      </form>

      <div class="tn-auth-foot">
        <span>还没有账号？</span>
        <RouterLink to="/register">立即注册</RouterLink>
      </div>
    </div>

    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteFooter from '../components/SiteFooter.vue'
import { useMemberStore } from '../stores/member'

const route = useRoute()
const router = useRouter()
const member = useMemberStore()

const username = ref('')
const password = ref('')
const submitting = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  if (!username.value || !password.value) {
    error.value = '请输入账号和密码'
    return
  }
  submitting.value = true
  try {
    await member.login(username.value, password.value)
    const redirect = (route.query.redirect as string) || '/'
    router.replace(redirect)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败'
  } finally {
    submitting.value = false
  }
}
</script>
