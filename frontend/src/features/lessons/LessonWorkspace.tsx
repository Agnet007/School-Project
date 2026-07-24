import { lazy, Suspense, useEffect, useState } from 'react'
import { BlockRenderer, blockPalette, blockRegistry } from './blockRegistry'
import type { BlockType, LessonBlock, LessonDraft, LessonVersion } from './model'
import { DemoLessonRepository } from './demoRepository'
import { HttpLessonRepository, LessonRepositoryError, type LessonRepository } from './repository'
import { runtimeConfig } from '../../runtimeConfig'

type View = { kind: 'list' } | { kind: 'editor'; lessonId: string } | { kind: 'preview' } | { kind: 'version'; version: LessonVersion }
type HashView = Extract<View, { kind: 'list' | 'editor' }>
const defaultRepository = runtimeConfig.isReadOnly ? new DemoLessonRepository() : new HttpLessonRepository()
const DemoModeStatus = lazy(() => import('./DemoModeStatus'))

function hashView(): HashView {
  const match = typeof window === 'undefined' ? null : window.location.hash.match(/^#\/lessons\/([^/]+)\/edit$/)
  return match ? { kind: 'editor', lessonId: match[1] } : { kind: 'list' }
}

export function LessonWorkspace({ repository = defaultRepository }: { repository?: LessonRepository }) {
  const [view, setView] = useState<View>(hashView)
  const [lessons, setLessons] = useState<LessonDraft[]>([])
  const [draft, setDraft] = useState<LessonDraft | null>(null)
  const [versions, setVersions] = useState<LessonVersion[]>([])
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const navigate = (next: View) => {
    setView(next)
    if (typeof window !== 'undefined') window.history.pushState(null, '', next.kind === 'editor' ? `#/lessons/${next.lessonId}/edit` : '#/')
  }

  const run = async <T,>(action: () => Promise<T>, apply: (value: T) => void) => {
    setLoading(true); setError(''); setMessage('')
    try { apply(await action()) }
    catch (reason) {
      if (reason instanceof LessonRepositoryError && reason.status === 409) setError(`Conflict: server revision is ${reason.problem.currentRevision}. Reload draft before editing.`)
      else setError(reason instanceof Error ? reason.message : 'Lesson operation failed.')
    } finally { setLoading(false) }
  }

  useEffect(() => {
    const initialView = hashView()
    repository.list().then(nextLessons => {
      setLessons(nextLessons)
      if (initialView.kind !== 'editor') return undefined
      return Promise.all([repository.getDraft(initialView.lessonId), repository.listVersions(initialView.lessonId)]).then(([nextDraft, nextVersions]) => { setDraft(nextDraft); setVersions(nextVersions); setView(initialView) })
    }).catch(reason => setError(reason instanceof Error ? reason.message : 'Cannot load lessons.')).finally(() => setLoading(false))
  }, [repository])

  useEffect(() => {
    const onHashChange = () => {
      const nextView = hashView()
      if (nextView.kind === 'list') { setView(nextView); setDraft(null); return }
      setLoading(true); setError(''); setMessage('')
      void Promise.all([repository.getDraft(nextView.lessonId), repository.listVersions(nextView.lessonId)])
        .then(([nextDraft, nextVersions]) => { setDraft(nextDraft); setVersions(nextVersions); setView(nextView) })
        .catch(reason => setError(reason instanceof Error ? reason.message : 'Cannot load lesson.'))
        .finally(() => setLoading(false))
    }
    window.addEventListener('hashchange', onHashChange)
    window.addEventListener('popstate', onHashChange)
    return () => { window.removeEventListener('hashchange', onHashChange); window.removeEventListener('popstate', onHashChange) }
  }, [repository])

  const openEditor = (lessonId: string) => void run(async () => {
    const [nextDraft, nextVersions] = await Promise.all([repository.getDraft(lessonId), repository.listVersions(lessonId)])
    return { nextDraft, nextVersions }
  }, ({ nextDraft, nextVersions }) => { setDraft(nextDraft); setVersions(nextVersions); navigate({ kind: 'editor', lessonId }) })

  const updateDraft = (action: () => Promise<LessonDraft>, success?: string) => void run(action, value => {
    setDraft(value); setLessons(current => current.map(item => item.lessonId === value.lessonId ? value : item)); if (success) setMessage(success)
  })

  return <main className="shell">
    <header className="topbar"><div><span className="eyebrow">Offline learning</span><h1>School Platform</h1></div>
      {view.kind !== 'list' && <button className="quiet" onClick={() => { navigate({ kind: 'list' }); setDraft(null); void run(() => repository.list(), setLessons) }}>All lessons</button>}
    </header>
    {runtimeConfig.isReadOnly && <Suspense fallback={<div role="status" className="notice">Demo mode</div>}><DemoModeStatus /></Suspense>}
    {loading && <div role="status" className="notice">Loading…</div>}
    {error && <div role="alert" className="notice error">{error}</div>}
    {message && <div role="status" className="notice success">{message}</div>}
    {view.kind === 'list' && <LessonList lessons={lessons} readOnly={runtimeConfig.isReadOnly} onCreate={(title, description) => void run(() => repository.create(title, description), value => { setLessons(current => [value, ...current]); setDraft(value); setVersions([]); navigate({ kind: 'editor', lessonId: value.lessonId }) })} onOpen={openEditor} />}
    {view.kind === 'editor' && draft && <LessonEditor key={`${draft.lessonId}-${draft.revision}`} draft={draft} versions={versions} readOnly={runtimeConfig.isReadOnly} onPreview={() => navigate({ kind: 'preview' })}
      onMetadata={(title, description) => updateDraft(() => repository.updateMetadata(draft, title, description), 'Draft details saved.')}
      onAdd={type => { const registration = blockRegistry.get(type); if (registration) updateDraft(() => repository.addBlock(draft, type, registration.defaultConfiguration)) }}
      onUpdate={block => updateDraft(() => repository.updateBlock(draft, block), 'Block saved.')}
      onMove={(blockId, direction) => { const ids = draft.blocks.map(block => block.blockId); const index = ids.indexOf(blockId); const target = index + direction; if (target < 0 || target >= ids.length) return; [ids[index], ids[target]] = [ids[target], ids[index]]; updateDraft(() => repository.reorderBlocks(draft, ids)) }}
      onDuplicate={blockId => updateDraft(() => repository.duplicateBlock(draft, blockId))}
      onDelete={blockId => updateDraft(() => repository.removeBlock(draft, blockId))}
      onPublish={() => void run(() => repository.publish(draft), version => { setVersions(current => [...current, version]); setMessage(`Published version ${version.version}.`); navigate({ kind: 'version', version }) })}
      onVersion={version => void run(() => repository.getVersion(draft.lessonId, version), value => navigate({ kind: 'version', version: value }))} />}
    {view.kind === 'preview' && draft && <LessonReader title={`${draft.title} · Draft preview`} description={draft.description} blocks={draft.blocks} onBack={() => navigate({ kind: 'editor', lessonId: draft.lessonId })} />}
    {view.kind === 'version' && <LessonReader title={`${view.version.title} · Version ${view.version.version}`} description={view.version.description} blocks={view.version.blocks} onBack={() => draft && navigate({ kind: 'editor', lessonId: draft.lessonId })} />}
  </main>
}

function LessonList({ lessons, readOnly, onCreate, onOpen }: { lessons: LessonDraft[]; readOnly: boolean; onCreate: (title: string, description: string) => void; onOpen: (id: string) => void }) {
  const [title, setTitle] = useState(''); const [description, setDescription] = useState('')
  return <section className="list-layout" aria-labelledby="lessons-heading"><div><h2 id="lessons-heading">Lessons</h2>
    {lessons.length === 0 ? <p className="empty">No lessons yet. Create first draft.</p> : <ul className="lesson-list">{lessons.map(lesson => <li key={lesson.lessonId}><button onClick={() => onOpen(lesson.lessonId)}><strong>{lesson.title}</strong><span>{lesson.blocks.length} blocks · revision {lesson.revision}</span></button></li>)}</ul>}
  </div><form className="create-panel" onSubmit={event => { event.preventDefault(); if (!readOnly && title.trim()) onCreate(title, description) }}><h2>Create lesson</h2><label>Title<input disabled={readOnly} required maxLength={200} value={title} onChange={event => setTitle(event.target.value)} /></label><label>Description<textarea disabled={readOnly} maxLength={4000} rows={4} value={description} onChange={event => setDescription(event.target.value)} /></label><button disabled={readOnly} type="submit">Create draft</button></form></section>
}

type EditorProps = { draft: LessonDraft; versions: LessonVersion[]; readOnly: boolean; onPreview: () => void; onMetadata: (title: string, description: string) => void; onAdd: (type: BlockType) => void; onUpdate: (block: LessonBlock) => void; onMove: (id: string, direction: -1 | 1) => void; onDuplicate: (id: string) => void; onDelete: (id: string) => void; onPublish: () => void; onVersion: (version: number) => void }
function LessonEditor(props: EditorProps) {
  const [title, setTitle] = useState(props.draft.title); const [description, setDescription] = useState(props.draft.description)
  return <div className="editor-layout"><aside className="palette"><h2>Blocks</h2>{blockPalette.map(item => <button disabled={props.readOnly} key={item.type} onClick={() => props.onAdd(item.type)}>+ {item.label}</button>)}</aside>
    <section className="editor"><div className="editor-heading"><div><h2>Lesson editor</h2><span>Revision {props.draft.revision}</span></div><div className="actions"><button className="quiet" onClick={props.onPreview}>Preview</button><button disabled={props.readOnly} onClick={props.onPublish}>Publish</button></div></div>
      <form className="metadata" onSubmit={event => { event.preventDefault(); if (!props.readOnly) props.onMetadata(title, description) }}><label>Title<input disabled={props.readOnly} value={title} required maxLength={200} onChange={event => setTitle(event.target.value)} /></label><label>Description<textarea disabled={props.readOnly} value={description} rows={3} maxLength={4000} onChange={event => setDescription(event.target.value)} /></label><button disabled={props.readOnly} type="submit" className="quiet">Save details</button></form>
      <h3>Ordered blocks</h3>{props.draft.blocks.length === 0 && <p className="empty">Choose block from palette.</p>}
      <ol className="block-list">{props.draft.blocks.map((block, index) => <BlockEditor key={block.blockId} block={block} readOnly={props.readOnly} first={index === 0} last={index === props.draft.blocks.length - 1} onSave={props.onUpdate} onMove={props.onMove} onDuplicate={props.onDuplicate} onDelete={props.onDelete} />)}</ol>
      {props.versions.length > 0 && <nav className="versions" aria-label="Published versions"><h3>Published</h3>{props.versions.map(version => <button className="quiet" key={version.version} onClick={() => props.onVersion(version.version)}>Version {version.version}</button>)}</nav>}
    </section></div>
}

function BlockEditor({ block, readOnly, first, last, onSave, onMove, onDuplicate, onDelete }: { block: LessonBlock; readOnly: boolean; first: boolean; last: boolean; onSave: (block: LessonBlock) => void; onMove: (id: string, direction: -1 | 1) => void; onDuplicate: (id: string) => void; onDelete: (id: string) => void }) {
  const [configuration, setConfiguration] = useState(block.configuration); const registration = blockRegistry.get(block.blockType)
  if (!registration) return <li className="block-card"><BlockRenderer block={block} /></li>
  const Editor = registration.Editor; const candidate = { ...block, configuration } as LessonBlock
  const errors = registration.validate(configuration)
  return <li className="block-card"><header><strong>{registration.label}</strong><div className="icon-actions"><button title="Move up" disabled={readOnly || first} onClick={() => onMove(block.blockId, -1)}>↑</button><button title="Move down" disabled={readOnly || last} onClick={() => onMove(block.blockId, 1)}>↓</button><button title="Duplicate" disabled={readOnly} onClick={() => onDuplicate(block.blockId)}>⧉</button><button title="Delete" disabled={readOnly} onClick={() => onDelete(block.blockId)}>×</button></div></header><Editor block={candidate} onChange={setConfiguration as never} />{errors.map(value => <p className="field-error" key={value}>{value}</p>)}<button disabled={readOnly || errors.length > 0} onClick={() => onSave(candidate)}>Save block</button></li>
}

function LessonReader({ title, description, blocks, onBack }: { title: string; description: string; blocks: LessonBlock[]; onBack: () => void }) {
  return <article className="reader"><button className="quiet" onClick={onBack}>← Editor</button><header><h2>{title}</h2>{description && <p>{description}</p>}</header>{blocks.length === 0 ? <p className="empty">Lesson has no blocks.</p> : blocks.map(block => <section className="rendered-block" key={block.blockId}><BlockRenderer block={block} /></section>)}</article>
}