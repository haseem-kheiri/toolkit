# cluster-core

`cluster-core` provides a lightweight, infrastructure-grade **application-level cluster coordination** abstraction for JVM-based systems.

It is designed for environments where nodes must discover each other, exchange heartbeats, persist ephemeral state, and tolerate failures **without tight framework or runtime coupling**.

This module is suitable for building leaders, schedulers, distributed locks, coordinators, and control-plane components. It intentionally does **not** perform process orchestration or workload scheduling.

---

## Overview

`cluster-core` enables applications to reason about:

- Which nodes are currently active
- Whether a node has restarted (reincarnation)
- Application-defined liveness via heartbeats
- Ephemeral or semi-durable per-node coordination state

It operates at the **application coordination layer**, not the infrastructure orchestration layer.

---

## Core Concepts

### Cluster

A `Cluster` represents a logical grouping of nodes identified by a shared cluster name.

Key characteristics:

- Exactly **one local node** per cluster instance
- Zero or more **remote nodes** discovered via heartbeats
- Periodic heartbeat emission and reconciliation
- Explicit lifecycle (`start` / `stop`) via `AbstractLifecycleObject`

Each cluster manages:

- Node identity
- Liveness tracking
- Ephemeral session handling
- Optional persisted node state

---

### Node Identity

Each node has two identifiers:

- **Stable Node ID**  
  A logical identifier that remains constant across restarts.

- **Ephemeral Session ID** (`UUID`)  
  Changes on each reincarnation and is used to detect stale or restarted nodes.

Nodes are uniquely identified by `(clusterName, nodeId)`.

This separation allows the system to distinguish **identity continuity** from **process lifetime**.

---

### Heartbeats

Nodes periodically emit `NodeHeartbeat` records containing:

- Cluster name
- Node ID
- Ephemeral session ID
- Optional node-local state

Heartbeats are:

- Stored with a TTL
- Automatically expired if the node becomes unresponsive
- Used to reconcile cluster membership

Liveness is defined at the **application level**, not inferred from process or container state.

---

### Node State

`ClusterNodeState` represents persisted, node-specific state metadata.

Features:

- Serialization-agnostic
- Designed for ephemeral or semi-durable coordination state
- Extendable for application-specific needs
- Includes timestamping and session correlation

Typical uses:

- Leader metadata
- Work ownership
- Scheduling coordination
- Health or capacity signals

---

## Cluster Repository

### ClusterRepository

`ClusterRepository` is the abstraction responsible for:

- Publishing and retrieving heartbeats
- Persisting node state
- Fetching cluster-wide state
- Managing TTL and expiry semantics

It intentionally avoids prescribing any storage backend.

---

### EtcdClusterRepository

An implementation backed by **etcd** using leases for automatic expiry.

Key behaviors:

- Heartbeats stored as ephemeral keys with TTL
- Node state stored under lease-managed keys
- Lease reuse via time buckets to minimize churn
- Automatic cleanup on node failure

Design considerations:

- Tolerates clock drift
- Reduces etcd lease pressure
- Safe for multi-node deployments

---

## Configuration

### ClusterProperties

Controls cluster timing and tolerance:

- Heartbeat interval
- Maximum missed heartbeats
- State refresh interval
- Repository lease behavior

---

### ClusterRepositoryProperties

Controls lease mechanics:

- Lease bucket span factor
- TTL padding factor for drift and delays

---

## Lifecycle Model

- Heartbeat loop runs on a dedicated executor
- State reconciliation runs independently
- Failures increment missed heartbeat counters
- Ephemeral sessions are renewed on repeated failures
- Repositories are closed on cluster shutdown

---

## Kubernetes Compatibility

This module is **designed to run inside Kubernetes-managed workloads**.

Kubernetes is responsible for:
- Pod scheduling and placement
- Process lifecycle and restarts
- Replica management

`cluster-core` provides:
- Application-level membership
- Coordination semantics
- Leadership and ownership decisions
- Detection of reincarnation beyond pod restarts

The two are **complementary** and commonly used together.

This module does **not** attempt to replace Kubernetes or replicate orchestration behavior.

---

## Design Principles

- **Infrastructure-first**  
  Designed for control planes, not application workflows.

- **Failure-aware**  
  Explicit handling of missed heartbeats and node reincarnation.

- **Serialization-agnostic**  
  No dependency on JSON, MessagePack, or any specific codec.

- **Minimal abstractions**  
  Clear separation between coordination logic and persistence.

- **Framework-neutral core**  
  No Spring, Kubernetes, or container dependency required.

---

## Typical Use Cases

- Leader election
- Distributed schedulers
- Control-plane coordination
- Cluster membership tracking
- Ephemeral metadata propagation
- Building blocks for locks, queues, and orchestrators

---

## Module Structure

```text
cluster-core
├── impl
│   ├── Cluster
│   ├── ClusterNodeState
│   ├── ClusterProperties
│   └── ClusterRepositoryProperties
├── repository
│   ├── ClusterRepository
│   └── impl
│       └── EtcdClusterRepository
```

## License

Licensed under the Apache License, Version 2.0.
