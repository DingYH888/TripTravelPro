import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '127.0.0.1',
    port: 5173,
    proxy: {
      // 开发环境下把后台应用挂在同一源下，保证统一登录后的令牌可被两个应用共享
      '/admin': {
        target: 'http://127.0.0.1:5174',
        changeOrigin: true,
        ws: true
      },
      '/api': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true
      },
      '/upload': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: false
  }
})
