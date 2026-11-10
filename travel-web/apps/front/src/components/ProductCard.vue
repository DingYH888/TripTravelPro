<template>
  <article class="tn-card">
    <RouterLink :to="`/routes/${product.pid}`" class="tn-card-img">
      <img :src="resolveImage(product.pImage)" :alt="product.productName" loading="lazy" />
    </RouterLink>
    <div class="tn-card-body">
      <RouterLink :to="`/routes/${product.pid}`" class="tn-card-title">{{ product.productName }}</RouterLink>
      <div class="tn-card-meta">
        <span class="tn-price">&yen;{{ formatPrice(product.price) }}<small>起</small></span>
        <RouterLink :to="`/routes/${product.pid}`" class="tn-btn">查看详情</RouterLink>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import type { Product } from '../api/types'
import { formatPrice, resolveImage } from '../utils/format'

defineProps<{ product: Product }>()
</script>
