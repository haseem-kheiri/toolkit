# toolkit-core

`toolkit-core` provides low-level, dependency-light primitives that form the foundation of the toolkit.  
It contains shared utilities and lifecycle abstractions intended to be reused across higher-level modules.

## Scope

This module deliberately avoids domain logic. It focuses on:
- Defensive runtime checks
- Conditional execution helpers
- Thread and executor lifecycle management
- A minimal, explicit lifecycle model for long-running components

## Contents

### Runtime validation
Utility methods for enforcing preconditions and invariants at runtime:
- Null, blank, equality, instance, and collection checks
- Lazy error construction via suppliers
- Optional propagation of caller-defined exception types

### Conditional execution helpers
Utilities for executing code paths based on:
- Boolean conditions
- Null / non-null values
- Blank / non-blank strings
- Empty / non-empty collections and maps

These helpers support checked exceptions without forcing wrapping.

### Thread and executor utilities
Centralized helpers for:
- Graceful and forceful `ExecutorService` shutdown
- Retry-based task submission when executors temporarily reject work
- Interrupt-safe thread sleeping

### Lifecycle abstraction
A small lifecycle contract for components with explicit start/stop semantics:
- `LifecycleObject` defines the contract
- `AbstractLifecycleObject` provides:
  - Thread-safe state transitions
  - Idempotent start/stop behavior
  - Structured execution while the component is running
  - Integrated executor shutdown support

## Design intent

- No framework dependencies
- Predictable behavior under concurrency
- Explicit state transitions
- Utilities should be usable in isolation

This module is expected to be stable and shared by most other toolkit components.
