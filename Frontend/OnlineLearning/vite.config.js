import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig({
  plugins: [vue(), vueDevTools()],
  server: {
    proxy: {
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/course': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/chapter': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/resource': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/upload': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
      '/learning': {
        target: 'http://localhost:8080', // 学习服务统一经网关转发
        changeOrigin: true,
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: '@import "@/styles/_variables.scss"; @import "@/styles/_mixins.scss";',
      },
    },
  },
})
