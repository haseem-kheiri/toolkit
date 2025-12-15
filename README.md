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

The toolkit is organized as a set of focused core libraries.

```text
toolkit
├── cluster-core
├── codec-core
│   ├── codec-core
│   ├── codec-json
│   └── codec-message-pack
├── file-core
├── lock-core
├── test-core
└── toolkit-core
```

## Module Overview

### cluster-core

Application-level cluster coordination primitives:

- Node identity and reincarnation detection  
- Heartbeats and liveness tracking  
- Ephemeral and semi-durable coordination state  
- Repository-backed membership and state  

Used for leaders, schedulers, coordinators, and control-plane logic.

---

### codec-core

A binary-first serialization SPI with enforced generic safety.

Includes:

- **codec-core**: core abstractions and type-capture utilities  
- **codec-json**: Jackson-backed JSON codec  
- **codec-message-pack**: high-performance MessagePack codec  

Designed for infrastructure paths, storage engines, and messaging systems.

---

### file-core

File and object-storage abstractions intended for:

- Gateways  
- Blob access layers  
- Infrastructure services that require explicit IO semantics  

---

### lock-core

Distributed and local locking primitives designed for:

- Coordinators  
- Schedulers  
- Ownership and exclusion semantics  

---

### test-core

Testing utilities for infrastructure code:

- Deterministic execution helpers  
- Failure and timing simulation  
- Test-friendly lifecycle abstractions  

---

### toolkit-core

Foundational utilities shared across modules, including:

- Lifecycle primitives  
- Common exceptions  
- Shared contracts and internal utilities  

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
