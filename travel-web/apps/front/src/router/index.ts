import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import { TOKEN_KEY } from '../api/http'

// 统一登录页是唯一入口，注册与激活属于认证流程，保留匿名访问
const PUBLIC_PATHS = new Set(['/login', '/register', '/activate'])

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: Home },
    { path: '/routes', name: 'routes', component: () => import('../views/RouteList.vue') },
    { path: '/routes/:pid', name: 'route-detail', component: () => import('../views/RouteDetail.vue') },
    { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
    { path: '/register', name: 'register', component: () => import('../views/Register.vue') },
    { path: '/favorites', name: 'favorites', component: () => import('../views/Favorites.vue') },
    { path: '/activate', name: 'activate', component: () => import('../views/Activate.vue') },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  if (PUBLIC_PATHS.has(to.path)) {
    return true
  }
  if (!localStorage.getItem(TOKEN_KEY)) {
    return { path: '/login', query: { identity: 'member', redirect: to.fullPath } }
  }
  return true
})

export default router
