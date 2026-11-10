<template>
  <div>
    <SiteHeader />

    <main class="tn-container tn-main">
      <h2 class="tn-section-title">我的收藏</h2>

      <p v-if="loading" class="tn-loading">正在加载收藏...</p>
      <p v-else-if="error" class="tn-error">{{ error }}</p>
      <p v-else-if="!products.length" class="tn-empty">还没有收藏线路，去挑选心仪的线路吧</p>

      <div v-else class="tn-list">
        <article v-for="item in products" :key="item.pid" class="tn-list-item">
          <RouterLink :to="`/routes/${item.pid}`">
            <img :src="resolveImage(item.pImage)" :alt="item.productName" loading="lazy" />
          </RouterLink>
          <div class="tn-list-info">
            <RouterLink :to="`/routes/${item.pid}`">
              <h3>{{ item.productName }}</h3>
            </RouterLink>
            <p>收藏的线路会保留在这里，方便随时查看。</p>
            <div class="tn-list-bottom">
              <span class="tn-price">&yen;{{ formatPrice(item.price) }}<small>起</small></span>
              <div style="display: flex; gap: 10px">
                <RouterLink :to="`/routes/${item.pid}`" class="tn-btn">查看详情</RouterLink>
                <button class="tn-btn gray" :disabled="removing === item.pid" @click="remove(item.pid)">
                  {{ removing === item.pid ? '处理中...' : '取消收藏' }}
                </button>
              </div>
            </div>
          </div>
        </article>
      </div>

      <div v-if="totalPages > 1" class="tn-pagination">
        <button :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
        <button v-for="p in totalPages" :key="p" :class="{ active: p === page }" @click="changePage(p)">{{ p }}</button>
        <button :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
      </div>
    </main>

    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import SiteFooter from '../components/SiteFooter.vue'
import SiteHeader from '../components/SiteHeader.vue'
import { listFavorites, removeFavorite } from '../api/favorite'
import type { Product } from '../api/types'
import { formatPrice, resolveImage } from '../utils/format'

const products = ref<Product[]>([])
const loading = ref(true)
const error = ref('')
const page = ref(1)
const totalPages = ref(0)
const removing = ref<number | null>(null)

onMounted(load)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const result = await listFavorites(page.value, 8)
    products.value = result.records
    totalPages.value = result.pages
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

function changePage(target: number) {
  if (target < 1 || target > totalPages.value) {
    return
  }
  page.value = target
  load()
}

async function remove(pid: number) {
  removing.value = pid
  try {
    await removeFavorite(pid)
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '取消失败'
  } finally {
    removing.value = null
  }
}
</script>
