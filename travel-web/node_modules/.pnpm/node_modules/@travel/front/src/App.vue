<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMemberStore } from './stores/member'

const router = useRouter()
const member = useMemberStore()

let expiredHandler: (() => void) | null = null

onMounted(() => {
  member.restore()
  expiredHandler = () => {
    member.clear()
    if (router.currentRoute.value.meta.requiresAuth) {
      router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    }
  }
  window.addEventListener('auth-expired', expiredHandler)
})

onUnmounted(() => {
  if (expiredHandler) {
    window.removeEventListener('auth-expired', expiredHandler)
  }
})
</script>
