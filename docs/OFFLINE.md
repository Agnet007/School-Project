# Offline behavior

The application shell and immutable assets use Cache Storage. IndexedDB owns installed lessons, metadata, progress, notes, workspaces, drafts, snapshots, sync operations, conflicts, and schema metadata. Domain data never uses localStorage. Storage migrations preserve originals until validated. UI must expose availability, pending work, conflicts, and permanent failures.