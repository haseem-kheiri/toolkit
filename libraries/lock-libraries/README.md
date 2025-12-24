# Distributed Lock API

This module provides a **best-effort distributed exclusive locking abstraction** backed by an external shared system (e.g., a relational database).

It is designed for coordination, not correctness under arbitrary failures. Callers must treat locks as leases with bounded lifetime.

## Core Concepts
### Lock

A lock is identified by a logical lockName. At most one execution may hold a lock at any given time.

### Lease

A **lock lease** represents temporary ownership of a lock.

A lease is defined by:

- name – logical lock name
- executionId – identifier of the holder
- expiresAt – absolute expiration timestamp (milliseconds since epoch)

Leases expire automatically and may be taken over by another execution once expired.

## Design Principles

- Exclusive only: shared or read locks are not supported
- Lease-based: no permanent ownership; expiration is mandatory
- Best-effort: operations may partially succeed
- Idempotent lifecycle: release and renew tolerate duplicates
- Provider-agnostic: semantics must work across different backends

This API intentionally avoids strong guarantees such as fencing tokens or linearizability.

## API Overview
```
LockLease acquireLock(String lockName, String executionId, Duration leaseDuration);

void release(List<LockLease> leases);

List<LockLease> renew(List<LockLease> leases, Duration leaseDuration); 
```

## Acquire Semantics
acquireLock(...)

Attempts to acquire an exclusive lock.

### Behavior:

- If the lock is unheld or expired, it is acquired and a LockLease is returned
- If the lock is currently held and unexpired, null is returned
- On provider or infrastructure failure, an exception is thrown

### Notes:

- Acquisition is atomic at the provider level
- Expired leases may be transparently replaced
- Callers must handle null as a normal contention signal

## Renew Semantics
`renew(...)`

Attempts to extend the expiration of active leases.

**Behavior:**

- Only leases that are:
  - known to the provider
  - owned by the caller (executionId)
  - not yet expired  
  will be renewed
- Expired, released, or unknown leases are silently ignored
- The returned list contains only successfully renewed leases

**Important:**

- Absence from the result set means the lease is no longer valid
- Renewal is best-effort and may partially succeed

## Release Semantics
`release(...)`

Releases one or more leases.

**Behavior:**

- Release is idempotent
- Releasing:
  - already released
  - expired
  - unknown  
  leases must not fail
- No return value; success is defined as no exception

**Concurrency:**

- Release may race with renewal
- Providers must tolerate concurrent lifecycle transitions

## Failure Model

This API assumes:

- Processes may crash
- Networks may partition
- Providers may transiently fail

As a result:

- Locks may be lost after expiration
- Renewals may fail silently
- Callers must design for eventual loss of ownership

**Never assume a lock is held indefinitely.**

##Correct Usage Pattern
```
LockManager lockManager = ...
LockExecutionResult<Void> result = lockManager.tryLock(
                  "my-lock",
                  (lock) -> {
                    // Code to execute if lock acquired      
                    // may or may not a value
                  });
```

## Non-Goals
This API does **not** provide:

- Fencing tokens
- Fairness guarantees
- Deadlock detection
- Strong consistency under partitions

If you need those, use a coordination system designed for them.

## Summary
This Lock API provides:

- Simple exclusive coordination
- Explicit lifecycle via leases
- Clear failure semantics
- Portability across providers

It is intentionally minimal—**correctness belongs in the caller.**
