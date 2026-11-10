<template>
  <div class="tn-auth-page">
    <div class="tn-auth-bar">
      <RouterLink to="/" class="tn-logo">
        <img src="/images/brand-logo.png" alt="去哪旅游网" />
      </RouterLink>
      <RouterLink to="/">
        <span class="glyphicon glyphicon-home"></span>
        首页
      </RouterLink>
    </div>

    <div class="tn-auth-card">
      <h2>账号激活</h2>
      <p class="sub">激活成功即可登录</p>

      <p v-if="loading" class="tn-loading">正在激活...</p>
      <p v-else-if="message" class="tn-alert" :class="failed ? 'error' : 'ok'" v-html="sanitizeHtml(message)"></p>
      <p v-if="failed && !message" class="tn-alert error">激活失败，请检查激活链接是否完整或已过期</p>

      <div class="tn-auth-foot">
        <RouterLink to="/login">返回登录</RouterLink>
        <RouterLink to="/">返回首页</RouterLink>
      </div>
    </div>

    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import SiteFooter from '../components/SiteFooter.vue'
import { activate } from '../api/auth'
import { sanitizeHtml } from '../utils/sanitize'

const route = useRoute()
const loading = ref(true)
const failed = ref(false)
const message = ref('')

onMounted(async () => {
  const code = (route.query.code as string) || ''
  if (!code) {
    loading.value = false
    failed.value = true
    return
  }
  try {
    message.value = await activate(code)
  } catch (e) {
    failed.value = true
    message.value = e instanceof Error ? e.message : '激活失败'
  } finally {
    loading.value = false
  }
})
</script>
