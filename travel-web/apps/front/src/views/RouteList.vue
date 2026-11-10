<template>
  <div>
    <SiteHeader />

    <main class="tn-container tn-main">
      <h2 class="tn-section-title">{{ pageTitle }}</h2>

      <div class="tn-list-layout">
        <div>
          <p v-if="loading" class="tn-loading">正在加载...</p>
          <p v-else-if="error" class="tn-error">{{ error }}</p>
          <div v-else-if="products.length" class="tn-list">
            <article v-for="item in products" :key="item.pid" class="tn-list-item">
              <RouterLink :to="`/routes/${item.pid}`">
                <img :src="resolveImage(item.pImage)" :alt="item.productName" loading="lazy" />
              </RouterLink>
              <div class="tn-list-info">
                <RouterLink :to="`/routes/${item.pid}`">
                  <h3>{{ item.productName }}</h3>
                </RouterLink>
                <p>咨询热线：{{ item.hotline || '13569386399' }}</p>
                <p>精选线路 · 透明报价 · 安心出行</p>
                <div class="tn-list-bottom">
                  <span class="tn-price">&yen;{{ formatPrice(item.price) }}<small>起</small></span>
                  <RouterLink :to="`/routes/${item.pid}`" class="tn-btn">查看详情</RouterLink>
                </div>
              </div>
            </article>
          </div>
          <p v-else class="tn-empty">没有找到符合条件的线路</p>

          <div v-if="totalPages > 1" class="tn-pagination">
            <button :disabled="current <= 1" @click="goPage(current - 1)">上一页</button>
            <button
              v-for="page in visiblePages"
              :key="page"
              :class="{ active: page === current }"
              @click="goPage(page)"
            >
              {{ page }}
            </button>
            <button :disabled="current >= totalPages" @click="goPage(current + 1)">下一页</button>
          </div>
        </div>

        <aside class="tn-side">
          <h3>热门推荐</h3>
          <div v-for="(hot, index) in hotList" :key="hot.name" class="tn-side-item">
            <img :src="hot.image" :alt="hot.name" loading="lazy" />
            <div>
              <span>{{ hot.name }}</span>
              <p class="tn-price" style="font-size: 15px; margin: 4px 0 0">&yen;{{ hot.price }}</p>
            </div>
          </div>
        </aside>
      </div>
    </main>

    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteFooter from '../components/SiteFooter.vue'
import SiteHeader from '../components/SiteHeader.vue'
import { getProducts } from '../api/catalog'
import type { Product } from '../api/types'
import { formatPrice, resolveImage } from '../utils/format'

const route = useRoute()
const router = useRouter()
const products = ref<Product[]>([])
const loading = ref(false)
const error = ref('')
const current = ref(1)
const totalPages = ref(0)

const hotList = [
  { name: '喀纳斯8日游', price: 6980, image: '/frontdesk/images/top1.jpg' },
  { name: '黄果树梵净山11日游', price: 3280, image: '/frontdesk/images/top2.jpg' },
  { name: '庐山3日游', price: 1817, image: '/frontdesk/images/top3.jpeg' },
  { name: '张家界凤凰4日游', price: 2780, image: '/frontdesk/images/top4.jpg' },
  { name: '拉萨布宫羊湖9日游', price: 7780, image: '/frontdesk/images/top5.jpg' }
]

const productName = computed(() => (route.query.productName as string) || '')
const pageTitle = computed(() => (productName.value ? `搜索：${productName.value}` : '精选线路'))

const visiblePages = computed(() => {
  const pages: number[] = []
  const start = Math.max(1, current.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i += 1) {
    pages.push(i)
  }
  return pages
})

watch(
  () => route.query,
  () => {
    current.value = Number(route.query.page) || 1
    load()
  },
  { immediate: true }
)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const page = await getProducts({
      cid: (route.query.cid as string) || undefined,
      productName: productName.value || undefined,
      page: current.value,
      size: 8
    })
    products.value = page.records
    totalPages.value = page.pages
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

function goPage(page: number) {
  if (page < 1 || page > totalPages.value) {
    return
  }
  router.push({ path: '/routes', query: { ...route.query, page: String(page) } })
}
</script>
