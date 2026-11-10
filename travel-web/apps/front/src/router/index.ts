import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: Home },
    { path: '/routes', name: 'routes', component: () => import('../views/RouteList.vue') },
    { path: '/routes/:pid', name: 'route-detail', component: () => import('../views/RouteDetail.vue') },
    { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
    { path: '/register', name: 'register', component: () => import('../views/Register.vue') },
    { path: '/favorites', name: 'favorites', component: () => import('../views/Favorites.vue'), meta: { requiresAuth: true } },
    { path: '/activate', name: 'activate', component: () => import('../views/Activate.vue') },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem('travel_member_token')) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
