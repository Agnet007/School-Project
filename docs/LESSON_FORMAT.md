# Lesson format

Lessons are structured versioned documents, never arbitrary HTML blobs. Each block has a stable ID, type, schema version, order, configuration, and optional assessment configuration. Initial registered types are heading, rich text, rule, image, formula, chart, multiple choice, numeric answer, text answer, simulation, and workspace launcher. Rich text requires a structured format or sanitized allowlisted HTML.

Phase 1 names block types with uppercase API constants. Configuration uses strict per-type JSON Schema, Java sealed records, and TypeScript discriminated unions. Unknown configuration fields are rejected in version 1; forward compatibility requires a new schema version. Backend validation is authoritative.

Rich text contains paragraphs, ordered/unordered lists, and plain spans marked bold, italic, or inline code. Text containing HTML delimiters is rejected by backend validation and rendered as text by React. Images accept HTTP(S) URLs or `/assets/` references and require alt text. Formula expressions render as text. Charts support line, bar, and scatter data with labels, datasets, numeric points, and optional tooltips; generation is deferred.

A `LessonDraft` is editable and revisioned. Each successful mutation advances its revision. Publication creates a complete immutable `LessonVersion` snapshot and monotonically increasing version. Later draft edits and publications cannot alter earlier snapshots. Numeric/text answers, simulations, and workspace launchers have typed placeholder renderers; their interactions are explicitly deferred.