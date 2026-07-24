import { afterEach, describe, expect, it, vi } from 'vitest'
import type { LessonDraft } from './model'
import { HttpLessonRepository, LessonRepositoryError } from './repository'

const draft: LessonDraft = { lessonId: 'lesson-1', draftId: 'draft-1', title: 'Title', description: '', revision: 7, blocks: [], createdAt: '', updatedAt: '' }

describe('HTTP lesson repository', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('sends optimistic concurrency revision through If-Match', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ ...draft, revision: 8 }), { status: 200, headers: { 'Content-Type': 'application/json' } }))
    vi.stubGlobal('fetch', fetchMock)
    await new HttpLessonRepository('https://api.example/api/v1').updateMetadata(draft, 'Next', '')
    expect(fetchMock).toHaveBeenCalledWith('https://api.example/api/v1/lessons/lesson-1/draft', expect.objectContaining({ method: 'PATCH', headers: expect.objectContaining({ 'If-Match': '7' }) }))
  })

  it('exposes conflict problem details', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({ code: 'LESSON_REVISION_CONFLICT', currentRevision: 9 }), { status: 409, headers: { 'Content-Type': 'application/problem+json' } })))
    await expect(new HttpLessonRepository('https://api.example/api/v1').updateMetadata(draft, 'Next', '')).rejects.toEqual(expect.objectContaining<Partial<LessonRepositoryError>>({ status: 409 }))
  })
})