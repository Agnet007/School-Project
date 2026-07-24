import { describe, expect, it } from 'vitest'
import { resolveRuntimeConfig } from './runtimeConfig'

describe('runtime configuration', () => {
  it('uses the local proxy without selecting localhost', () => {
    expect(resolveRuntimeConfig({})).toEqual({ mode: 'LOCAL_DEVELOPMENT', apiBaseUrl: '/api', isReadOnly: false })
  })

  it('starts GitHub Pages demo without an API URL', () => {
    expect(resolveRuntimeConfig({ VITE_RUNTIME_MODE: 'GITHUB_PAGES_DEMO', VITE_API_BASE_URL: '' })).toEqual({ mode: 'GITHUB_PAGES_DEMO', apiBaseUrl: '', isReadOnly: true })
  })

  it('rejects localhost and missing URLs for remote production APIs', () => {
    expect(() => resolveRuntimeConfig({ VITE_RUNTIME_MODE: 'REMOTE_API', VITE_API_BASE_URL: 'http://127.0.0.1:8080' })).toThrow()
    expect(() => resolveRuntimeConfig({ VITE_RUNTIME_MODE: 'REMOTE_API', VITE_API_BASE_URL: '' })).toThrow()
  })
})