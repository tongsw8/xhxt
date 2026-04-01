import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/xhxt': {
        target: 'http://localhost:8866',
        changeOrigin: true,
      },
    },
  },
})