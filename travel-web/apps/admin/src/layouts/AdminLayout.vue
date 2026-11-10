<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-aside">
      <div class="admin-logo">旅游后台管理</div>
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        background-color="#1f2d3d"
        text-color="#c0c4cc"
        active-text-color="#ffd04b"
        router
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          {{ item.title }}
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div class="admin-header-title">{{ pageTitle }}</div>
        <div class="admin-header-right">
          <span class="admin-username">{{ adminStore.username || '未登录' }}</span>
          <el-button type="primary" link @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '../stores/admin'

interface MenuItem {
  path: string
  title: string
  permission?: string
}

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const allMenus: MenuItem[] = [
  { path: '/', title: '仪表盘' },
  { path: '/products', title: '线路管理' },
  { path: '/categories', title: '分类管理' },
  { path: '/roles', title: '角色管理', permission: '/role/all' },
  { path: '/permissions', title: '权限管理', permission: '/permission/all' },
  { path: '/users', title: '管理员管理', permission: '/admin/all' }
]

const menus = computed(() => allMenus.filter((item) => adminStore.hasPermission(item.permission)))
const activeMenu = computed(() => (route.path === '/' ? '/' : route.path))
const pageTitle = computed(() => (route.meta.title as string) || '旅游后台管理')

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  await adminStore.logout()
  ElMessage.success('已退出登录')
  router.replace('/login')
}
</script>
