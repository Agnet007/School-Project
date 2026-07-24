export type BlockType =
  | 'HEADING' | 'RICH_TEXT' | 'RULE' | 'IMAGE' | 'FORMULA' | 'CHART'
  | 'MULTIPLE_CHOICE' | 'NUMERIC_ANSWER' | 'TEXT_ANSWER' | 'SIMULATION' | 'WORKSPACE_LAUNCHER'

export type TextSpan = { text: string; bold: boolean; italic: boolean; inlineCode: boolean }
export type RichTextElement = { kind: 'paragraph' | 'unordered-list' | 'ordered-list'; spans: TextSpan[]; items: TextSpan[][] }
export type ChartPoint = { x: number; y: number }
export type ChartDataset = { label: string; color: string; points: ChartPoint[] }

export type BlockConfiguration =
  | { type: 'HEADING'; value: { text: string; level: number } }
  | { type: 'RICH_TEXT'; value: { content: RichTextElement[] } }
  | { type: 'RULE'; value: Record<string, never> }
  | { type: 'IMAGE'; value: { url: string; alt: string; caption: string } }
  | { type: 'FORMULA'; value: { expression: string; displayMode: boolean } }
  | { type: 'CHART'; value: { chartType: 'line' | 'bar' | 'scatter'; title: string; xAxisLabel: string; yAxisLabel: string; datasets: ChartDataset[]; tooltip: boolean } }
  | { type: 'MULTIPLE_CHOICE'; value: { prompt: string; options: string[]; allowMultiple: boolean } }
  | { type: 'NUMERIC_ANSWER'; value: { prompt: string; placeholder: string } }
  | { type: 'TEXT_ANSWER'; value: { prompt: string; multiline: boolean } }
  | { type: 'SIMULATION'; value: { pluginId: string; message: string } }
  | { type: 'WORKSPACE_LAUNCHER'; value: { label: string; documentType: string } }

export type LessonBlock<T extends BlockType = BlockType> = {
  blockId: string
  blockType: T
  blockSchemaVersion: 1
  position: number
  configuration: Extract<BlockConfiguration, { type: T }>['value']
}

export type LessonDraft = {
  lessonId: string
  draftId: string
  title: string
  description: string
  revision: number
  blocks: LessonBlock[]
  createdAt: string
  updatedAt: string
}

export type LessonVersion = Omit<LessonDraft, 'draftId' | 'revision' | 'createdAt' | 'updatedAt'> & {
  version: number
  sourceRevision: number
  publishedAt: string
}

export type LessonProblem = { code: string; detail?: string; currentRevision?: number }