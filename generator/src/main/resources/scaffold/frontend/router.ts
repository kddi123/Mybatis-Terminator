import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// @ts-ignore

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    // @ts-ignore
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/',
    // @ts-ignore
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        // @ts-ignore
        component: () => import('@/views/dashboard/index.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
