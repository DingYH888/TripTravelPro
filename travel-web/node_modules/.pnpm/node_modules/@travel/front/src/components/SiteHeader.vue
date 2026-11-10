<template>
  <header class="tn-header">
    <div class="tn-shortcut">
      <div class="tn-container tn-shortcut-inner">
        <div>
          <span>欢迎来到去哪旅游网</span>
        </div>
        <div>
          <template v-if="member.isLoggedIn">
            <span>欢迎回来，{{ member.username }}</span>
            <RouterLink to="/favorites">我的收藏</RouterLink>
            <a href="javascript:void(0)" @click="handleLogout">退出</a>
          </template>
          <template v-else>
            <RouterLink to="/login">登录</RouterLink>
            |
            <RouterLink to="/register">注册</RouterLink>
          </template>
        </div>
      </div>
    </div>

    <div class="tn-container tn-header-main">
      <RouterLink to="/" class="tn-logo">
        <img src="/frontdesk/images/logo.png" alt="去哪旅游网" />
      </RouterLink>

      <div class="tn-search">
        <input v-model.trim="keyword" type="text" placeholder="搜索线路 / 目的地" @keyup.enter="search" />
        <button type="button" @click="search">搜索</button>
      </div>

      <div class="tn-hotline">
        客服热线（9:00-18:00）
        <strong>13569386399</strong>
      </div>
    </div>

    <nav class="tn-nav">
      <div class="tn-container tn-nav-inner">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink v-for="item in categories" :key="item.cid" :to="{ path: '/routes', query: { cid: item.cid } }">
          {{ item.cname }}
        </RouterLink>
      </div>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories } from '../api/catalog'
import type { Category } from '../api/types'
import { useMemberStore } from '../stores/member'

const categories = ref<Category[]>([])
const keyword = ref('')
const router = useRouter()
const member = useMemberStore()

onMounted(async () => {
  member.restore()
  try {
    categories.value = await getCategories()
  } catch {
    categories.value = []
  }
})

function search() {
  router.push({ path: '/routes', query: keyword.value ? { productName: keyword.value } : {} })
}

async function handleLogout() {
  await member.logout()
  router.push('/')
}
</script>
