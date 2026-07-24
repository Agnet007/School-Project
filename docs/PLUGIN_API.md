# Simulation plugin API

A built-in plugin supplies a manifest, configuration validation, initial state, action reducer, render lifecycle, serialization, deserialization, sequential state migration, snapshots, optional assessment evaluation, and disposal. The manifest versions plugin, engine requirement, configuration/state schemas, capabilities, and requested host permissions.

The host provides `requestRender`, `emitEvent`, `saveSnapshot`, `loadAsset`, and `deterministicRandom`. It never provides tokens, unrestricted network/storage, application DOM access, or unrelated user data. Static trusted registration is the MVP extension mechanism.