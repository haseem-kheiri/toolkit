# lock-core-libraries

A small, composable set of libraries providing a **distributed locking abstraction** with pluggable provider implementations and execution helpers.

This project is part of the toolkit and is designed to be embedded into applications and services that require explicit, time-bound lock coordination.

---

## Overview

`lock-core-libraries` separates distributed locking into three distinct concerns:

1. **Lock contracts** – stable interfaces that define locking semantics
2. **Execution helpers** – utilities that safely execute code under lock ownership
3. **Provider implementations** – concrete backends implementing the lock contract

This separation allows applications to depend on a single API while swapping lock backends without changing application logic.

---

## Modules

### lock-core

Defines the core distributed locking API and execution utilities.

#### DistributedLockProvider

A non-blocking distributed lock contract with explicit ownership and TTL semantics.

Key capabilities:
- Attempt-based lock acquisition (`tryLock`)
- Time-to-live enforcement
- Owner-aware release and extension
- Administrative force-release support
- Ownership inspection

The interface is intentionally minimal to support multiple backend implementations.

#### LockHandle

Represents lock ownership:
- Associates a `lockId` with a generated `ownerId`
- Used to validate release and extension operations
- Prevents accidental or unauthorized unlocks

#### DistributedLockExecutor

Utility for executing code under a distributed lock with guaranteed release.

Features:
- Automatic lock acquisition and release
- Safe execution via `try / finally`
- Support for both value-returning and void callbacks
- Clear signaling when a lock could not be acquired

This component removes repetitive lock-handling boilerplate from application code.

---

### lock-provider-inmemory

Provides a JVM-local, in-memory implementation of `DistributedLockProvider`.

#### InMemoryLockProvider

Characteristics:
- JVM-scoped locking (single process)
- TTL-based expiration
- Owner validation
- Thread-safe synchronization
- Background cleanup of expired locks

This provider is intended for:
- Local development
- Tests
- Single-node applications

It is **not suitable for multi-process or distributed coordination**.

#### Spring Boot auto-configuration

Includes Spring Boot auto-configuration for easy integration.

Features:
- Conditional bean creation
- Externalized configuration
- Automatic lifecycle management

Configuration properties:
```properties
com.platform.lock.provider.inmemory.cleanup-interval-duration=10
com.platform.lock.provider.inmemory.cleanup-interval-duration-unit=SECONDS


## Usage Example

```java
DistributedLockExecutor executor =
    new DistributedLockExecutor(lockProvider);

LockResult<Void> result =
    executor.runWithLock("job-42", Duration.ofSeconds(30), owner -> {
        performWork(owner);
    });

if (!result.isAcquired()) {
  // lock was already held
}
```
## Key Guarantees

- The lock is released automatically after execution
- Lock ownership is explicit and verifiable
- TTL protects against orphaned locks

## Design Principles

- **Backend-agnostic**  
  No assumptions about storage, coordination, or frameworks.

- **Fail-safe execution**  
  Locks are released in `finally` blocks to prevent leaks.

- **Explicit ownership**  
  Lock identity and ownership are first-class concepts.

- **Minimal surface area**  
  The API is intentionally small to remain stable over time.

## When to Use

Use this module when you need:

- Deterministic coordination across threads or nodes
- Explicit lock ownership and TTL-based safety
- A portable abstraction that can evolve with different providers

## License

Licensed under the Apache License, Version 2.0.

