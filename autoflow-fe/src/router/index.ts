import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView/HomeView.vue'
import FlowDesigner from '@/views/FlowDesigner/FlowDesigner.vue'
import WorkflowList from '@/views/Workflow/WorkflowList.vue'
import GlobalVariables from '@/views/Variables/GlobalVariables.vue'
import ServicePlugins from '@/views/Plugins/ServicePlugins.vue'
import Dashboard from '@/views/Dashboard/Dashboard.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      redirect: 'dashboard',
      children: [
        {
          path: '/dashboard',
          name: 'dashboard',
          component: Dashboard
        },
        {
          path: '/workflows',
          name: 'workflows',
          component: WorkflowList
        },
        {
          path: '/flowdesign',
          name: 'flowdesign',
          component: FlowDesigner
        },

        {
          path: '/plugins',
          name: 'plugins',
          component: ServicePlugins
        },
        {
          path: '/variables',
          name: 'variables',
          component: GlobalVariables
        }
      ]
    }
  ]
})

export default router