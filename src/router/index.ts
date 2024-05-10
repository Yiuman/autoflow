import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView/HomeView.vue'
import FlowDesigner from '@/views/FlowDesigner/FlowDesigner.vue'
import Workflow from '@/views/Workflow/Workflow.vue'
import Variables from '@/views/Variables/Variables.vue'
import Plugins from '@/views/Plugins/Plugins.vue'

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
        },
        {
          path: '/workflows',
          name: 'workflow',
          component: Workflow
        },
        {
          path: '/plugins',
          name: 'plugins',
          component: Plugins
        },
        {
          path: '/variables',
          name: 'variables',
          component: Variables
        }
      ]
    }
  ]
})

export default router
