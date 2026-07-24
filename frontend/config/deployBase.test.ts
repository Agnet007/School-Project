import { describe, expect, it } from 'vitest'
import { normalizeDeployBase, resolveDeployBase } from './deployBase'

describe('deployment base policy', () => {
  it('uses the project base in production and root in development', () => {
    expect(resolveDeployBase('production')).toBe('/School-Project/')
    expect(resolveDeployBase('development')).toBe('/')
  })

  it.each(['School-Project/', '/School-Project', 'https://example.com/', '//example.com/', '/../', '/%2e%2e/', '/bad?path/'])('rejects malformed bases: %s', value => {
    expect(() => normalizeDeployBase(value)).toThrow()
  })
})