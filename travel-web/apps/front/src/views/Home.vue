<template>
  <div>
    <SiteHeader />

    <section class="tn-hero" @mouseenter="pause" @mouseleave="resume">
      <img
        v-for="(slide, index) in slides"
        :key="slide"
        :src="slide"
        alt="旅行"
        :class="['tn-hero-slide', { active: index === current }]"
      />
      <div class="tn-hero-mask"></div>
      <div class="tn-hero-content">
        <div class="tn-container">
          <h1>发现你的下一段旅程</h1>
          <p>国内外精选线路，透明价格，安心出发</p>
        </div>
      </div>
      <button type="button" class="tn-hero-arrow prev" aria-label="上一张" @click="prev">‹</button>
      <button type="button" class="tn-hero-arrow next" aria-label="下一张" @click="next">›</button>
      <div class="tn-hero-dots">
        <span
          v-for="(slide, index) in slides"
          :key="index"
          :class="{ active: index === current }"
          @click="go(index)"
        ></span>
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
import { onMounted, onUnmounted, ref } from 'vue'
import ProductCard from '../components/ProductCard.vue'
import SiteFooter from '../components/SiteFooter.vue'
import SiteHeader from '../components/SiteHeader.vue'
import { getProducts } from '../api/catalog'
import type { Product } from '../api/types'

const slides = ['/frontdesk/images/1.jpg', '/frontdesk/images/2.jpg', '/frontdesk/images/3.jpg']
const current = ref(0)
const products = ref<Product[]>([])
const loading = ref(true)
const error = ref('')

const themes = ['theme1.jpg', 'theme2.jpg', 'theme3.jpg', 'theme4.jpeg', 'theme5.jpg', 'theme6.jpg'].map(
  (name) => `/frontdesk/images/${name}`
)

let timer: number | undefined

function go(index: number) {
  current.value = (index + slides.length) % slides.length
}

function next() {
  go(current.value + 1)
}

function prev() {
  go(current.value - 1)
}

function pause() {
  if (timer) {
    window.clearInterval(timer)
    timer = undefined
  }
}

function resume() {
  pause()
  timer = window.setInterval(next, 4500)
}

onMounted(async () => {
  resume()
  try {
    const page = await getProducts({ page: 1, size: 8 })
    products.value = page.records
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
})

onUnmounted(pause)
</script>
