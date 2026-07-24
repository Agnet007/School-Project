# ADR 0002: Offline-first

Status: Accepted.

Supported writes commit to IndexedDB before synchronization. Cache Storage holds application and immutable assets. REST failure cannot be the normal action failure path, and unsynchronized user data is never automatically deleted.