<template>
  <div>
    <SiteHeader />

    <section class="tn-hero">
      <img src="/frontdesk/images/1.jpg" alt="旅行" />
      <div class="tn-hero-mask"></div>
      <div class="tn-hero-content">
        <div class="tn-container">
          <h1>发现你的下一段旅程</h1>
          <p>国内外精选线路，透明价格，安心出发</p>
          <div class="tn-hero-search">
            <input v-model.trim="keyword" type="text" placeholder="输入目的地或线路名称" @keyup.enter="search" />
            <button type="button" @click="search">立即搜索</button>
          </div>
        </div>
      </div>
    </section>

    <main class="tn-container tn-main">
      <h2 class="tn-section-title">热门线路</h2>
      <p v-if="loading" class="tn-loading">正在加载线路...</p>
      <p v-else-if="error" class="tn-error">{{ error }}</p>
      <div v-else-if="products.length" class="tn-grid">
        <ProductCard v-for="item in products" :key="item.pid" :product="item" />
      </div>
      <p v-else class="tn-empty">暂无线路</p>

      <h2 class="tn-section-title">主题精选</h2>
      <div class="tn-themes">
        <div v-for="img in themes" :key="img" class="tn-theme">
          <img :src="img" alt="旅行主题" loading="lazy" />
        </div>
      </div>
    </main>

    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ProductCard from '../components/ProductCard.vue'
import SiteFooter from '../components/SiteFooter.vue'
import SiteHeader from '../components/SiteHeader.vue'
import { getProducts } from '../api/catalog'
import type { Product } from '../api/types'

const router = useRouter()
const keyword = ref('')
const products = ref<Product[]>([])
const loading = ref(true)
const error = ref('')

const themes = ['theme1.jpg', 'theme2.jpg', 'theme3.jpg', 'theme4.jpeg', 'theme5.jpg', 'theme6.jpg'].map(
  (name) => `/frontdesk/images/${name}`
)

onMounted(async () => {
  try {
    const page = await getProducts({ page: 1, size: 8 })
    products.value = page.records
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
})

function search() {
  router.push({ path: '/routes', query: keyword.value ? { productName: keyword.value } : {} })
}
</script>
