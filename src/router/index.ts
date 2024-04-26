import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import FlowDesigner from '@/views/FlowDesigner/FlowDesigner.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      children: [
        {
          path: '',
          name: 'flow-designer',
          component: FlowDesigner
        }
      ]
    }
  ]
})

export default router
