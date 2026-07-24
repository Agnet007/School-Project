import type { ComponentType } from 'react'
import type { BlockConfiguration, BlockType, LessonBlock } from './model'

type Capabilities = { renderable: boolean; interactive: boolean; assessable: boolean; generative: boolean; offlineCapable: boolean; snapshotCapable: boolean }
type RendererProps<T extends BlockType = BlockType> = { block: LessonBlock<T> }
type EditorProps<T extends BlockType = BlockType> = { block: LessonBlock<T>; onChange: (configuration: LessonBlock<T>['configuration']) => void }

export type BlockRegistration<T extends BlockType = BlockType> = {
  type: T
  label: string
  capabilities: Capabilities
  defaultConfiguration: Extract<BlockConfiguration, { type: T }>['value']
  validate: (configuration: unknown) => string[]
  Editor: ComponentType<EditorProps<T>>
  Renderer: ComponentType<RendererProps<T>>
}

const baseCapabilities: Capabilities = { renderable: true, interactive: false, assessable: false, generative: false, offlineCapable: true, snapshotCapable: false }
const text = (value: unknown) => typeof value === 'string' && value.trim().length > 0
const field = (value: unknown, key: string): unknown => typeof value === 'object' && value !== null ? (value as Record<string, unknown>)[key] : undefined
const arrayField = (value: unknown, key: string): unknown[] => Array.isArray(field(value, key)) ? field(value, key) as unknown[] : []

function JsonEditor<T extends BlockType>({ block, onChange }: EditorProps<T>) {
  return <textarea aria-label={`${block.blockType} configuration`} rows={7} value={JSON.stringify(block.configuration, null, 2)}
    onChange={(event) => { try { onChange(JSON.parse(event.target.value) as LessonBlock<T>['configuration']) } catch { return } }} />
}

function HeadingRenderer({ block }: RendererProps<'HEADING'>) {
  const level = Math.min(6, Math.max(1, block.configuration.level))
  const Tag = `h${level}` as keyof React.JSX.IntrinsicElements
  return <Tag>{block.configuration.text}</Tag>
}

function RichTextRenderer({ block }: RendererProps<'RICH_TEXT'>) {
  return <div>{block.configuration.content.map((element, index) => {
    const spans = element.spans.map((span, spanIndex) => {
      let content: React.ReactNode = span.text
      if (span.inlineCode) content = <code>{content}</code>
      if (span.italic) content = <em>{content}</em>
      if (span.bold) content = <strong>{content}</strong>
      return <span key={spanIndex}>{content}</span>
    })
    if (element.kind === 'paragraph') return <p key={index}>{spans}</p>
    const List = element.kind === 'ordered-list' ? 'ol' : 'ul'
    return <List key={index}>{element.items.map((item, itemIndex) => <li key={itemIndex}>{item.map(span => span.text).join('')}</li>)}</List>
  })}</div>
}

function RuleRenderer() { return <hr /> }
function ImageRenderer({ block }: RendererProps<'IMAGE'>) { return <figure><img src={block.configuration.url} alt={block.configuration.alt} /><figcaption>{block.configuration.caption}</figcaption></figure> }
function FormulaRenderer({ block }: RendererProps<'FORMULA'>) { return <output className="formula" aria-label="Formula">{block.configuration.expression}</output> }

function ChartRenderer({ block }: RendererProps<'CHART'>) {
  const points = block.configuration.datasets.flatMap(dataset => dataset.points)
  const maxX = Math.max(1, ...points.map(point => point.x)); const maxY = Math.max(1, ...points.map(point => point.y))
  return <figure className="chart"><figcaption>{block.configuration.title}</figcaption><svg viewBox="0 0 420 220" role="img" aria-label={block.configuration.title}>
    <line x1="35" y1="185" x2="405" y2="185" /><line x1="35" y1="10" x2="35" y2="185" />
    {block.configuration.chartType === 'line' && block.configuration.datasets.map(dataset => <polyline key={dataset.label} fill="none" stroke={dataset.color} strokeWidth="3" points={dataset.points.map(point => `${35 + point.x / maxX * 360},${185 - point.y / maxY * 165}`).join(' ')} />)}
    {block.configuration.datasets.map((dataset, datasetIndex) => dataset.points.map((point, index) => {
      const x = 35 + point.x / maxX * 360; const y = 185 - point.y / maxY * 165
      if (block.configuration.chartType === 'bar') return <rect key={`${datasetIndex}-${index}`} x={x - 8} y={y} width="16" height={185 - y} fill={dataset.color} />
      return <circle key={`${datasetIndex}-${index}`} cx={x} cy={y} r="5" fill={dataset.color}><title>{block.configuration.tooltip ? `${dataset.label}: ${point.x}, ${point.y}` : ''}</title></circle>
    }))}
  </svg><small>{block.configuration.xAxisLabel} / {block.configuration.yAxisLabel}</small></figure>
}

function MultipleChoiceRenderer({ block }: RendererProps<'MULTIPLE_CHOICE'>) { return <fieldset><legend>{block.configuration.prompt}</legend>{block.configuration.options.map(option => <label key={option}><input type={block.configuration.allowMultiple ? 'checkbox' : 'radio'} name={block.blockId} /> {option}</label>)}</fieldset> }
function PlaceholderRenderer({ block }: RendererProps) { return <section className="unsupported" aria-label={`${block.blockType} placeholder`}><strong>{block.blockType.replaceAll('_', ' ')}</strong><p>Interaction deferred. Configuration preserved.</p></section> }

const registrations = [
  { type: 'HEADING', label: 'Heading', capabilities: baseCapabilities, defaultConfiguration: { text: 'New heading', level: 2 }, validate: (value: unknown) => text(field(value, 'text')) && Number(field(value, 'level')) >= 1 && Number(field(value, 'level')) <= 6 ? [] : ['Heading text and level are required.'], Editor: JsonEditor, Renderer: HeadingRenderer },
  { type: 'RICH_TEXT', label: 'Rich text', capabilities: baseCapabilities, defaultConfiguration: { content: [{ kind: 'paragraph', spans: [{ text: 'New paragraph', bold: false, italic: false, inlineCode: false }], items: [] }] }, validate: (value: unknown) => arrayField(value, 'content').length ? [] : ['Rich text needs content.'], Editor: JsonEditor, Renderer: RichTextRenderer },
  { type: 'RULE', label: 'Rule', capabilities: baseCapabilities, defaultConfiguration: {}, validate: () => [], Editor: JsonEditor, Renderer: RuleRenderer },
  { type: 'IMAGE', label: 'Image', capabilities: baseCapabilities, defaultConfiguration: { url: '/assets/example.png', alt: 'Educational illustration', caption: '' }, validate: (value: unknown) => text(field(value, 'url')) && text(field(value, 'alt')) ? [] : ['Image URL and alt text are required.'], Editor: JsonEditor, Renderer: ImageRenderer },
  { type: 'FORMULA', label: 'Formula', capabilities: baseCapabilities, defaultConfiguration: { expression: 'x = 1', displayMode: true }, validate: (value: unknown) => text(field(value, 'expression')) ? [] : ['Formula is required.'], Editor: JsonEditor, Renderer: FormulaRenderer },
  { type: 'CHART', label: 'Chart', capabilities: { ...baseCapabilities, interactive: true, snapshotCapable: true }, defaultConfiguration: { chartType: 'line', title: 'Sample chart', xAxisLabel: 'x', yAxisLabel: 'y', datasets: [{ label: 'Series', color: '#b8482c', points: [{ x: 0, y: 0 }, { x: 1, y: 1 }] }], tooltip: true }, validate: (value: unknown) => ['line', 'bar', 'scatter'].includes(String(field(value, 'chartType'))) && arrayField(value, 'datasets').length ? [] : ['Chart type and datasets are required.'], Editor: JsonEditor, Renderer: ChartRenderer },
  { type: 'MULTIPLE_CHOICE', label: 'Multiple choice', capabilities: { ...baseCapabilities, interactive: true, assessable: true }, defaultConfiguration: { prompt: 'Choose an answer', options: ['Option A', 'Option B'], allowMultiple: false }, validate: (value: unknown) => text(field(value, 'prompt')) && arrayField(value, 'options').length >= 2 ? [] : ['Prompt and two options are required.'], Editor: JsonEditor, Renderer: MultipleChoiceRenderer },
  { type: 'NUMERIC_ANSWER', label: 'Numeric answer', capabilities: { ...baseCapabilities, interactive: true, assessable: true }, defaultConfiguration: { prompt: 'Enter a number', placeholder: '0' }, validate: (value: unknown) => text(field(value, 'prompt')) ? [] : ['Prompt is required.'], Editor: JsonEditor, Renderer: PlaceholderRenderer },
  { type: 'TEXT_ANSWER', label: 'Text answer', capabilities: { ...baseCapabilities, interactive: true, assessable: true }, defaultConfiguration: { prompt: 'Enter an answer', multiline: true }, validate: (value: unknown) => text(field(value, 'prompt')) ? [] : ['Prompt is required.'], Editor: JsonEditor, Renderer: PlaceholderRenderer },
  { type: 'SIMULATION', label: 'Simulation', capabilities: { ...baseCapabilities, interactive: true, offlineCapable: false, snapshotCapable: true }, defaultConfiguration: { pluginId: 'built-in.deferred', message: 'Simulation interaction is deferred.' }, validate: (value: unknown) => text(field(value, 'pluginId')) ? [] : ['Plugin ID is required.'], Editor: JsonEditor, Renderer: PlaceholderRenderer },
  { type: 'WORKSPACE_LAUNCHER', label: 'Workspace launcher', capabilities: { ...baseCapabilities, interactive: true, offlineCapable: false, snapshotCapable: true }, defaultConfiguration: { label: 'Open workspace', documentType: 'NOTE' }, validate: (value: unknown) => text(field(value, 'label')) ? [] : ['Label is required.'], Editor: JsonEditor, Renderer: PlaceholderRenderer },
] as unknown as BlockRegistration[]

export const blockRegistry = new Map(registrations.map(registration => [registration.type, registration]))
export const blockPalette = registrations

export function BlockRenderer({ block }: { block: LessonBlock }) {
  const registration = blockRegistry.get(block.blockType)
  if (!registration) return <PlaceholderRenderer block={block} />
  const Renderer = registration.Renderer
  return <Renderer block={block} />
}