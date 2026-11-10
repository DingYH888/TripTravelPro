<template>
  <div>
    <SiteHeader />

    <main class="tn-container tn-main">
      <p v-if="loading" class="tn-loading">正在加载线路详情...</p>
      <p v-else-if="error" class="tn-error">{{ error }}</p>

      <template v-else-if="product">
        <div class="tn-detail">
          <div class="tn-detail-img">
            <img :src="resolveImage(product.pImage)" :alt="product.productName" />
          </div>
          <div class="tn-detail-info">
            <h1>{{ product.productName }}</h1>
            <div class="tn-detail-price">
              &yen;{{ formatPrice(product.price) }} <span>起</span>
            </div>
            <div class="tn-detail-meta">
              <p>咨询电话：{{ product.hotline || '13569386399' }}</p>
              <p>优惠活动：使用信用卡支付立减 100 元</p>
              <p>服务保障：精选线路 · 透明报价 · 专业客服</p>
            </div>
            <div class="tn-detail-actions">
              <button v-if="!member.isLoggedIn" class="tn-btn" @click="goLogin">登录后收藏</button>
              <button v-else class="tn-btn" :class="{ ghost: favorited }" :disabled="favoriteLoading" @click="toggleFavorite">
                {{ favorited ? '取消收藏' : '点击收藏' }}
              </button>
              <RouterLink to="/routes" class="tn-btn gray">返回列表</RouterLink>
            </div>
            <p v-if="favoriteMessage" class="tn-alert" :class="favoriteError ? 'error' : 'ok'" style="margin-top: 16px">
              {{ favoriteMessage }}
            </p>
          </div>
        </div>

        <section class="tn-desc">
          <h3>线路详情</h3>
          <div v-html="sanitizeHtml(product.productDesc || '<p>暂无详情</p>')"></div>
        </section>

        <section class="tn-desc">
          <h3>旅游须知</h3>
          <p>1、旅行社已投保旅行社责任险，建议游客购买旅游意外保险。</p>
          <p>2、参加潜水、漂流、滑雪、攀岩等高风险项目前，请充分了解安全须知并确保身体状况适合参加。</p>
          <p>3、参加水上活动请穿着救生设备；老人和小孩须有成年人陪同。</p>
          <p>4、境外购物入境可能涉及关税，请遵守海关相关规定。</p>
          <p>5、建议贵重物品随身携带，行李延误等不可抗力情况我司将协助跟进。</p>
        </section>
      </template>
    </main>

    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteFooter from '../components/SiteFooter.vue'
import SiteHeader from '../components/SiteHeader.vue'
import { getProduct } from '../api/catalog'
import { addFavorite, favoriteStatus, removeFavorite } from '../api/favorite'
import type { Product } from '../api/types'
import { useMemberStore } from '../stores/member'
import { formatPrice, resolveImage } from '../utils/format'
import { sanitizeHtml } from '../utils/sanitize'

const route = useRoute()
const router = useRouter()
const member = useMemberStore()
const product = ref<Product | null>(null)
const loading = ref(true)
const error = ref('')
const favorited = ref(false)
const favoriteLoading = ref(false)
const favoriteMessage = ref('')
const favoriteError = ref(false)

watch(
  () => route.params.pid,
  () => {
    load()
  },
  { immediate: true }
)

async function load() {
  loading.value = true
  error.value = ''
  product.value = null
  favorited.value = false
  favoriteMessage.value = ''
  try {
    product.value = await getProduct(route.params.pid as string)
    if (member.isLoggedIn) {
      favorited.value = await favoriteStatus(route.params.pid as string)
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.push({ path: '/login', query: { identity: 'member', redirect: route.fullPath } })
}

async function toggleFavorite() {
  if (!product.value) {
    return
  }
  favoriteLoading.value = true
  favoriteMessage.value = ''
  favoriteError.value = false
  try {
    if (favorited.value) {
      await removeFavorite(product.value.pid)
      favorited.value = false
      favoriteMessage.value = '已取消收藏'
    } else {
      await addFavorite(product.value.pid)
      favorited.value = true
      favoriteMessage.value = '收藏成功'
    }
  } catch (e) {
    favoriteError.value = true
    favoriteMessage.value = e instanceof Error ? e.message : '操作失败'
  } finally {
    favoriteLoading.value = false
  }
}
</script>
