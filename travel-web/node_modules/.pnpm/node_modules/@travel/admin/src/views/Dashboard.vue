<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>当前账号</template>
          <div class="info-row">
            <span class="info-label">账号：</span>
            <span>{{ adminStore.username || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">登录状态：</span>
            <el-tag type="success" size="small">已登录</el-tag>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>权限列表</template>
          <template v-if="adminStore.permissions.length">
            <el-tag v-for="item in adminStore.permissions" :key="item" class="permission-tag">
              {{ item }}
            </el-tag>
          </template>
          <el-empty v-else description="暂无权限数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mt">
      <template #header>快捷入口</template>
      <el-space wrap>
        <el-button
          v-for="item in shortcuts"
          :key="item.path"
          type="primary"
          plain
          @click="router.push(item.path)"
        >
          {{ item.title }}
        </el-button>
      </el-space>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '../stores/admin'

const router = useRouter()
const adminStore = useAdminStore()

const allShortcuts = [
  { path: '/products', title: '线路管理' },
  { path: '/categories', title: '分类管理' },
  { path: '/roles', title: '角色管理', permission: '/role/all' },
  { path: '/permissions', title: '权限管理', permission: '/permission/all' },
  { path: '/users', title: '管理员管理', permission: '/admin/all' }
]

const shortcuts = computed(() =>
  allShortcuts.filter((item) => adminStore.hasPermission(item.permission))
)

onMounted(async () => {
  try {
    await adminStore.refreshMe()
  } catch {
    // 权限信息刷新失败时沿用本地缓存
  }
})
</script>
