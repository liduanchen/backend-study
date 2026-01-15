import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import ProductManagement from '../components/ProductManagement.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    name: 'ProductManagement',
    component: ProductManagement,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 检查登录状态
router.beforeEach(async (to, from, next) => {
  if (to.meta.requiresAuth) {
    try {
      const response = await fetch('/api/auth/check', {
        credentials: 'include'
      })
      const data = await response.json()
      
      if (data.authenticated) {
        next()
      } else {
        next('/login')
      }
    } catch (error) {
      console.error('检查登录状态失败:', error)
      next('/login')
    }
  } else {
    next()
  }
})

export default router
