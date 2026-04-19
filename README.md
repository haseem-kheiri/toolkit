# Toolkit

This repository contains a set of **infrastructure-grade, JVM-first building blocks** designed to support the construction of distributed systems, control planes, gateways, and internal platforms.

The toolkit is intentionally modular. Each library is usable on its own, while remaining interoperable with others when composed together.

This is **not a framework**. It provides primitives and abstractions, not opinions about application architecture.

---

## Philosophy

This toolkit is built around a small number of core principles:

- **Infrastructure-first design**  
  Optimized for control planes, coordinators, schedulers, gateways, and internal platforms.

- **Explicit correctness over convenience**  
  Failure modes, lifecycle boundaries, and type safety are explicit and enforced.

- **Minimal abstractions**  
  Each module exposes only what is required to solve its problem well.

- **Framework neutrality**  
  Core modules have no dependency on Spring, Kubernetes, or container runtimes.
  Integration layers are additive.

- **Composable by design**  
  Modules can be used independently or combined into larger systems.

---

## What This Toolkit Is (and Is Not)

### This toolkit **is**:

- A collection of low-level infrastructure libraries
- Suitable for JVM-based distributed systems
- Designed for long-running services and control-plane components
- Intended for engineers building platforms, not applications

### This toolkit **is not**:

- An application framework
- A replacement for Kubernetes, Spring, or cloud services
- A monolithic platform with enforced conventions

---

## Module Overview

The toolkit is organized as a set of focused library modules.

```text
libraries
├── ai-libraries
│   └── ai-bridge
│       ├── ai-bridge-core
│       └── ai-bridge-ollama-provider
├── cache-libraries
│   ├── cache-core
│   └── cache-invalidation-postgres
├── cluster-libraries
│   ├── clustering-core
│   └── clustering-coordinator-postgresql
├── codec-core-libraries
│   ├── codec-core
│   ├── codec-json
│   └── codec-message-pack
├── file-core
├── lock-libraries
│   ├── lock-core
│   └── lock-provider-postgres
├── rdbms-core
├── test-core
└── toolkit-core
```

## Module Overview

### ai-libraries

AI integration abstractions and provider implementations:

- **ai-bridge-core**: Provider-agnostic AI text generation interface
- **ai-bridge-ollama-provider**: Ollama backend implementation

Designed for infrastructure applications requiring AI integration.

---

### cache-libraries  

Distributed caching infrastructure with explicit invalidation semantics:

- **cache-core**: Distributed cache invalidation abstractions
- **cache-invalidation-postgres**: PostgreSQL-backed invalidation bus

Ensures cache coherence across multiple application instances.

---

### cluster-libraries

Application-level cluster coordination primitives:

- **clustering-core**: Node identity, heartbeats, and membership tracking
- **clustering-coordinator-postgresql**: PostgreSQL-backed coordination

Used for leaders, schedulers, coordinators, and control-plane logic.

---

### codec-core-libraries

A binary-first serialization SPI with enforced generic safety:

- **codec-core**: Core abstractions and type-capture utilities  
- **codec-json**: Jackson-backed JSON codec  
- **codec-message-pack**: High-performance MessagePack codec  

Designed for infrastructure paths, storage engines, and messaging systems.

---

### file-core

File and object-storage abstractions intended for:

- Gateways and blob access layers
- Infrastructure services requiring explicit IO semantics
- Cross-platform file system operations

---

### lock-libraries

Distributed and local locking primitives:

- **lock-core**: Lease-based exclusive locking abstractions
- **lock-provider-postgres**: PostgreSQL-backed lock implementation

Designed for coordinators, schedulers, and ownership semantics.

---

### rdbms-core

Relational database utilities and abstractions:

- Connection management and lifecycle
- Query building and execution helpers
- Transaction and isolation primitives

Framework-neutral database access layer.

---

### test-core

Testing utilities for infrastructure code:

- Deterministic execution helpers  
- Failure and timing simulation  
- Test-friendly lifecycle abstractions

---

### toolkit-core

Foundational utilities shared across modules:

- Lifecycle primitives and common contracts
- Shared exceptions and error handling
- Cross-cutting internal utilities

This module intentionally contains **no domain-specific behavior**.

---

## Kubernetes and Cloud Environments

This toolkit is designed to run inside Kubernetes-managed workloads but does not depend on Kubernetes.

Typical deployment characteristics:

- Pods managed by Kubernetes  
- Coordination handled at the application layer  
- External systems (etcd, databases, object stores) used explicitly  

The toolkit complements orchestration platforms rather than duplicating them.

---

## Intended Audience

This repository is intended for:

- Platform engineers  
- Distributed systems engineers  
- Infrastructure and control-plane developers  
- Engineers building internal tooling or gateways  

It assumes familiarity with:

- JVM internals  
- Concurrency and failure modes  
- Distributed systems fundamentals  

---

## Repository Structure

Each module contains its own:

- `README.md`  
- License headers  
- Clear public API surface  

Module-level documentation covers detailed design, usage, and configuration.

---

## Licensing and Ownership

This project is licensed under the **Apache License, Version 2.0**.

Copyright © 2020–2025  
Haseem Kheiri

See the `LICENSE` and `NOTICE` files for details.

---

## Status

This toolkit is actively developed and used as a personal infrastructure foundation.  
APIs favor stability and correctness over rapid iteration.
