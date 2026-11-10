import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import { TOKEN_KEY } from '../api/http'
import { loadPermissions } from '../stores/admin'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: AdminLayout,
    children: [
      {
        path: '',
        name: 'dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'products',
        name: 'products',
        component: () => import('../views/Products.vue'),
        meta: { title: '线路管理' }
      },
      {
        path: 'categories',
        name: 'categories',
        component: () => import('../views/Categories.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'roles',
        name: 'roles',
        component: () => import('../views/Roles.vue'),
        meta: { title: '角色管理', permission: '/role/all' }
      },
      {
        path: 'permissions',
        name: 'permissions',
        component: () => import('../views/Permissions.vue'),
        meta: { title: '权限管理', permission: '/permission/all' }
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('../views/Users.vue'),
        meta: { title: '管理员管理', permission: '/admin/all' }
      },
      {
        path: '403',
        name: 'forbidden',
        component: () => import('../views/Forbidden.vue'),
        meta: { title: '无权访问' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem(TOKEN_KEY)

  if (to.path === '/login') {
    return token ? '/' : true
  }
  if (!token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  const permission = to.meta.permission as string | undefined
  if (permission && !loadPermissions().includes(permission)) {
    return '/403'
  }
  return true
})

export default router
