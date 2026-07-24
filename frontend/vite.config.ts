import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'
import { resolveDeployBase } from './config/deployBase'

export default defineConfig(({ command }) => ({
  base: resolveDeployBase(command === 'build' ? 'production' : 'development', process.env.VITE_DEPLOY_BASE),
  plugins: [react()],
  server: {
    proxy: { '/api': 'http://127.0.0.1:8080' },
  },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
  },
}))
