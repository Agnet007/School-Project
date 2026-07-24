import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import type { BlockType, LessonBlock, LessonDraft, LessonVersion } from './model'
import { LessonRepositoryError, type LessonRepository } from './repository'
import { LessonWorkspace } from './LessonWorkspace'

class MemoryLessonRepository implements LessonRepository {
  draft: LessonDraft | null = null
  versions: LessonVersion[] = []
  conflict = false
  list = async () => this.draft ? [this.draft] : []
  create = async (title: string, description: string) => this.save({ lessonId: 'lesson-1', draftId: 'draft-1', title, description, revision: 0, blocks: [], createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' })
  getDraft = async () => this.draft!
  updateMetadata = async (draft: LessonDraft, title: string, description: string) => {
    if (this.conflict) throw new LessonRepositoryError(409, { code: 'LESSON_REVISION_CONFLICT', currentRevision: 4 })
    return this.save({ ...draft, title, description, revision: draft.revision + 1 })
  }
  addBlock = async (draft: LessonDraft, blockType: BlockType, configuration: unknown) => this.save({ ...draft, revision: draft.revision + 1, blocks: [...draft.blocks, { blockId: `block-${draft.blocks.length + 1}`, blockType, blockSchemaVersion: 1, position: draft.blocks.length, configuration } as LessonBlock] })
  updateBlock = async (draft: LessonDraft, block: LessonBlock) => this.save({ ...draft, revision: draft.revision + 1, blocks: draft.blocks.map(value => value.blockId === block.blockId ? block : value) })
  duplicateBlock = async (draft: LessonDraft, blockId: string) => { const source = draft.blocks.find(value => value.blockId === blockId)!; return this.addBlock(draft, source.blockType, source.configuration) }
  removeBlock = async (draft: LessonDraft, blockId: string) => this.save({ ...draft, revision: draft.revision + 1, blocks: draft.blocks.filter(value => value.blockId !== blockId).map((value, position) => ({ ...value, position })) })
  reorderBlocks = async (draft: LessonDraft, ids: string[]) => this.save({ ...draft, revision: draft.revision + 1, blocks: ids.map((id, position) => ({ ...draft.blocks.find(value => value.blockId === id)!, position })) })
  publish = async (draft: LessonDraft) => { const version = { lessonId: draft.lessonId, title: draft.title, description: draft.description, blocks: draft.blocks, version: this.versions.length + 1, sourceRevision: draft.revision, publishedAt: '2026-01-01T00:00:00Z' }; this.versions.push(version); return version }
  listVersions = async () => this.versions
  getVersion = async (_lessonId: string, version: number) => this.versions.find(value => value.version === version)!
  private save(value: LessonDraft) { this.draft = value; return value }
}

describe('lesson workspace', () => {
  it('creates, edits, previews, and publishes a lesson', async () => {
    const repository = new MemoryLessonRepository(); render(<LessonWorkspace repository={repository} />)
    await screen.findByText('No lessons yet. Create first draft.')
    fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'Algebra' } }); fireEvent.click(screen.getByRole('button', { name: 'Create draft' }))
    await screen.findByText('Lesson editor'); fireEvent.click(screen.getByRole('button', { name: '+ Heading' }))
    await screen.findByText('Heading'); fireEvent.click(screen.getByRole('button', { name: 'Preview' }))
    expect(await screen.findByText('New heading')).toBeInTheDocument(); fireEvent.click(screen.getByRole('button', { name: '← Editor' })); fireEvent.click(screen.getByRole('button', { name: 'Publish' }))
    expect(await screen.findByText('Algebra · Version 1')).toBeInTheDocument()
  })

  it('shows stale revision conflict without replacing draft', async () => {
    const repository = new MemoryLessonRepository(); await repository.create('Current', ''); repository.conflict = true
    render(<LessonWorkspace repository={repository} />); fireEvent.click(await screen.findByRole('button', { name: /Current/ })); await screen.findByText('Lesson editor')
    fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'Stale edit' } }); fireEvent.click(screen.getByRole('button', { name: 'Save details' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('server revision is 4')
    expect(repository.draft?.title).toBe('Current')
  })
})