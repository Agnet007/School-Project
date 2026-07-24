import type { LessonDraft, LessonVersion } from './model'
import type { LessonRepository } from './repository'
import { LessonRepositoryError } from './repository'

const demoDraft: LessonDraft = {
  lessonId: 'demo-lesson', draftId: 'demo-draft', title: 'A short lesson in demo mode',
  description: 'This read-only lesson is available without the Java backend.', revision: 0, blocks: [],
  createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z',
}

const unavailable = <T,>(): Promise<T> => Promise.reject(new LessonRepositoryError(503, { code: 'BACKEND_UNAVAILABLE', detail: 'Demo mode is read-only. Connect a backend to save or publish.' }))

export class DemoLessonRepository implements LessonRepository {
  create(): Promise<LessonDraft> { return unavailable() }
  list(): Promise<LessonDraft[]> { return Promise.resolve([demoDraft]) }
  getDraft(): Promise<LessonDraft> { return Promise.resolve(demoDraft) }
  updateMetadata(): Promise<LessonDraft> { return unavailable() }
  addBlock(): Promise<LessonDraft> { return unavailable() }
  updateBlock(): Promise<LessonDraft> { return unavailable() }
  duplicateBlock(): Promise<LessonDraft> { return unavailable() }
  removeBlock(): Promise<LessonDraft> { return unavailable() }
  reorderBlocks(): Promise<LessonDraft> { return unavailable() }
  publish(): Promise<LessonVersion> { return unavailable() }
  listVersions(): Promise<LessonVersion[]> { return Promise.resolve([]) }
  getVersion(): Promise<LessonVersion> { return unavailable() }
}