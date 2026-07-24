import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { BlockRenderer, blockPalette, blockRegistry } from './blockRegistry'

describe('block registry', () => {
  it('registers all Phase 1 block types with validators and capabilities', () => {
    expect(blockPalette).toHaveLength(11)
    expect(blockRegistry.get('CHART')?.capabilities.generative).toBe(false)
    expect(blockRegistry.get('HEADING')?.validate({ text: '', level: 2 })).not.toHaveLength(0)
  })

  it('renders structured content without interpreting raw HTML', () => {
    render(<BlockRenderer block={{ blockId: '1', blockType: 'RICH_TEXT', blockSchemaVersion: 1, position: 0,
      configuration: { content: [{ kind: 'paragraph', spans: [{ text: '<b>plain</b>', bold: false, italic: false, inlineCode: false }], items: [] }] } }} />)
    expect(screen.getByText('<b>plain</b>')).toBeInTheDocument()
    expect(document.querySelector('b')).toBeNull()
  })

  it('renders chart data as SVG', () => {
    render(<BlockRenderer block={{ blockId: '2', blockType: 'CHART', blockSchemaVersion: 1, position: 0,
      configuration: { chartType: 'scatter', title: 'Growth', xAxisLabel: 'Time', yAxisLabel: 'Value', tooltip: true,
        datasets: [{ label: 'A', color: '#123456', points: [{ x: 1, y: 2 }] }] } }} />)
    expect(screen.getByRole('img', { name: 'Growth' })).toBeInTheDocument()
  })
})