export function normalizeDeployBase(value: string): string {
  const base = value.trim()
  let decoded: string
  try { decoded = decodeURIComponent(base) } catch { throw new Error(`Invalid deployment base: ${value}`) }
  if (!base.startsWith('/') || base.startsWith('//') || !base.endsWith('/') || /[\\?#]/.test(base) || decoded.split('/').some(segment => segment === '..')) {
    throw new Error(`Invalid deployment base: ${value}`)
  }
  return base
}

export function resolveDeployBase(mode: 'development' | 'production', configured?: string): string {
  return normalizeDeployBase(configured || (mode === 'production' ? '/School-Project/' : '/'))
}