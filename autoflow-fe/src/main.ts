import { createApp } from 'vue'
import { createPinia } from 'pinia'
import i18n from '@/locales/i18n'

import App from './App.vue'
import router from './router'
import ArcoVue from '@arco-design/web-vue'
import Modal from '@arco-design/web-vue/es/modal/index'
import '@arco-design/web-vue/dist/arco.css'
import './assets/main.css'

const app = createApp(App)
Modal._context = app._context
app.use(createPinia())
app.use(router)
app.use(ArcoVue)
app.use(i18n)
app.mount('#app')
app.provide('$modal', Modal)