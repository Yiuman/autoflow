import { fileURLToPath, URL } from 'node:url'
import { resolve } from 'node:path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'

import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ArcoResolver } from 'unplugin-vue-components/resolvers'
import svgLoader from 'vite-svg-loader'
// https://vitejs.dev/config/
export default defineConfig({
  css: {
    preprocessorOptions: {
      less: {
        modifyVars: {},
        javascriptEnabled: true
      }
    }
  },
  plugins: [
    vue(),
    svgLoader(),
    vueJsx(),
    AutoImport({
      imports: ['vue', '@vueuse/core', 'vue/macros'],
      dts: resolve('src/auto-imports.d.ts'),
      resolvers: [ArcoResolver()]
    }),
    Components({
      resolvers: [
        ArcoResolver({
          importStyle: 'less',
          sideEffect: true
        })
      ]
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    // 设置反向代理，跨域
    proxy: {
      '/api': {
        // 后台地址
        target: 'http://localhost:8096/',
        changeOrigin: true
      }
    }
  }
})
