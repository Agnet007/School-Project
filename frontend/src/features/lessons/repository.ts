import type { BlockType, LessonBlock, LessonDraft, LessonProblem, LessonVersion } from './model'

export interface LessonRepository {
  create(title: string, description: string): Promise<LessonDraft>
  list(): Promise<LessonDraft[]>
  getDraft(lessonId: string): Promise<LessonDraft>
  updateMetadata(draft: LessonDraft, title: string, description: string): Promise<LessonDraft>
  addBlock(draft: LessonDraft, type: BlockType, configuration: unknown): Promise<LessonDraft>
  updateBlock(draft: LessonDraft, block: LessonBlock): Promise<LessonDraft>
  duplicateBlock(draft: LessonDraft, blockId: string): Promise<LessonDraft>
  removeBlock(draft: LessonDraft, blockId: string): Promise<LessonDraft>
  reorderBlocks(draft: LessonDraft, blockIds: string[]): Promise<LessonDraft>
  publish(draft: LessonDraft): Promise<LessonVersion>
  listVersions(lessonId: string): Promise<LessonVersion[]>
  getVersion(lessonId: string, version: number): Promise<LessonVersion>
}

export class LessonRepositoryError extends Error {
  constructor(public readonly status: number, public readonly problem: LessonProblem) { super(problem.detail ?? problem.code) }
}

export class HttpLessonRepository implements LessonRepository {
  constructor(private readonly baseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api/v1') {}
  create = (title: string, description: string) => this.request<LessonDraft>('/lessons', { method: 'POST', body: JSON.stringify({ title, description }) })
  list = () => this.request<LessonDraft[]>('/lessons')
  getDraft = (id: string) => this.request<LessonDraft>(`/lessons/${id}/draft`)
  updateMetadata = (draft: LessonDraft, title: string, description: string) => this.mutate(draft, `/lessons/${draft.lessonId}/draft`, 'PATCH', { title, description })
  addBlock = (draft: LessonDraft, blockType: BlockType, configuration: unknown) => this.mutate(draft, `/lessons/${draft.lessonId}/draft/blocks`, 'POST', { blockType, configuration })
  updateBlock = (draft: LessonDraft, block: LessonBlock) => this.mutate(draft, `/lessons/${draft.lessonId}/draft/blocks/${block.blockId}`, 'PUT', { blockType: block.blockType, configuration: block.configuration })
  duplicateBlock = (draft: LessonDraft, id: string) => this.mutate(draft, `/lessons/${draft.lessonId}/draft/blocks/${id}/duplicate`, 'POST')
  removeBlock = (draft: LessonDraft, id: string) => this.mutate(draft, `/lessons/${draft.lessonId}/draft/blocks/${id}`, 'DELETE')
  reorderBlocks = (draft: LessonDraft, blockIds: string[]) => this.mutate(draft, `/lessons/${draft.lessonId}/draft/block-order`, 'PUT', { blockIds })
  publish = (draft: LessonDraft) => this.request<LessonVersion>(`/lessons/${draft.lessonId}/publications`, { method: 'POST', headers: { 'If-Match': String(draft.revision) } })
  listVersions = (id: string) => this.request<LessonVersion[]>(`/lessons/${id}/versions`)
  getVersion = (id: string, version: number) => this.request<LessonVersion>(`/lessons/${id}/versions/${version}`)

  private mutate(draft: LessonDraft, path: string, method: string, body?: unknown) {
    return this.request<LessonDraft>(path, { method, headers: { 'If-Match': String(draft.revision) }, body: body === undefined ? undefined : JSON.stringify(body) })
  }

  private async request<T>(path: string, init: RequestInit = {}): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, { ...init, headers: { 'Content-Type': 'application/json', ...init.headers } })
    if (!response.ok) throw new LessonRepositoryError(response.status, await response.json() as LessonProblem)
    return response.json() as Promise<T>
  }
}

