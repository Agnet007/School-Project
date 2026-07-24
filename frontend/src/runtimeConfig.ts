export type RuntimeMode = 'LOCAL_DEVELOPMENT' | 'GITHUB_PAGES_DEMO' | 'REMOTE_API'

type RuntimeEnvironment = { VITE_RUNTIME_MODE?: string; VITE_API_BASE_URL?: string }

export function resolveRuntimeConfig(environment: RuntimeEnvironment) {
  const mode = environment.VITE_RUNTIME_MODE ?? 'LOCAL_DEVELOPMENT'
  if (!['LOCAL_DEVELOPMENT', 'GITHUB_PAGES_DEMO', 'REMOTE_API'].includes(mode)) throw new Error(`Invalid runtime mode: ${mode}`)
  const apiBaseUrl = mode === 'GITHUB_PAGES_DEMO' ? '' : environment.VITE_API_BASE_URL ?? '/api'
  if (mode === 'GITHUB_PAGES_DEMO' && environment.VITE_API_BASE_URL) throw new Error('GITHUB_PAGES_DEMO cannot configure an API URL')
  if (mode === 'REMOTE_API' && (!apiBaseUrl || /localhost|127\.0\.0\.1/i.test(apiBaseUrl))) throw new Error('REMOTE_API requires a non-local API URL')
  return { mode: mode as RuntimeMode, apiBaseUrl, isReadOnly: mode === 'GITHUB_PAGES_DEMO' } as const
}

export const runtimeConfig = resolveRuntimeConfig({
  VITE_RUNTIME_MODE: import.meta.env.VITE_RUNTIME_MODE,
  VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL,
})