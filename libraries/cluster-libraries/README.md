# Cluster Coordination API

## Overview

This module provides a lightweight, backend-agnostic cluster coordination mechanism based on
**ephemeral sessions**, **periodic heartbeats**, and **level-triggered state observation**.

It is designed to support:
- Cluster membership tracking
- Leader election
- Membership-driven application behavior

The API is intentionally minimal and avoids strong consistency or quorum guarantees. It favors
clarity, determinism, and operational simplicity.

---

## Core Concepts

### Node Identity

Each cluster participant has two distinct identities:

**nodeId**  
Stable, application-level identifier. Persists across restarts.

**sessionId**  
Ephemeral identifier representing a single runtime incarnation.

A node may restart, crash, or temporarily lose connectivity without changing its `nodeId`.
A `sessionId` changes only when a session is explicitly rotated after prolonged heartbeat failure.

---

### Cluster Membership

Cluster membership is defined as:

> A set of sessions that have emitted a heartbeat within a configurable timeout window.

There is no explicit join or leave operation. Membership is inferred solely from recent heartbeats.

---

### Backend Abstraction

The coordination backend is abstracted via the `ClusterCoordinator` interface.

This allows implementations backed by:
- PostgreSQL
- SQL Server
- etcd
- Consul
- ZooKeeper-like systems

The backend is responsible for:
- Recording heartbeats
- Returning a consistent view of cluster membership
- Enforcing heartbeat expiration semantics

---

## Main Components

### ClusterNode

`ClusterNode` represents a **live process instance** participating in a cluster.

Responsibilities:
- Maintain an ephemeral session
- Emit periodic heartbeats
- Observe cluster membership
- Notify application code of membership changes

Key characteristics:
- Thread-safe
- Self-managed background execution
- Isolates coordination logic from user callbacks

---

### ClusterState

`ClusterState` is an immutable snapshot of observed cluster membership.

Properties:
- Represents a point-in-time view
- Contains all visible cluster nodes
- Equality is structural and deterministic

The state is **observer-relative**: it reflects membership as seen at the time of the observer’s
heartbeat.

---

### ClusterNodeState

Represents a single member within a `ClusterState`.

Contains:
- Cluster name
- Session identifier
- Heartbeat timestamp
- Node metadata

Equality is based on `(clusterName, sessionId)`.

---

## State Change Notifications

### Delivery Model

Cluster state change notifications are:

- **Level-triggered**
- **Coalesced**
- **Latest-only**

This means:
- Intermediate states may be skipped
- Only the most recent observed state is delivered
- Notifications are idempotent

There is no guarantee that every membership transition will be observed.

---

### ClusterStateChangeEvent

Each notification delivers a `ClusterStateChangeEvent` containing:
- Cluster name
- Local node identifier
- Local session identifier
- New cluster state
- Previously delivered cluster state

This allows listeners to reason about transitions without relying on ordering guarantees.

---

### ClusterStateChangeListener

User-provided callback invoked when the cluster state changes.

Execution semantics:
- Invoked asynchronously
- Runs on a background worker thread
- Must be fast and non-blocking

Failure semantics:
- Unchecked exceptions are treated as fatal
- A fatal listener error causes the owning node to stop participating

---

## Failure Model

### Heartbeat Failures

- Transient heartbeat failures are tolerated
- A session remains valid until failures exceed `heartbeatTimeout`
- After timeout, the session is rotated

### Listener Failures

- Listener failures are considered unrecoverable
- A fatal failure marks the node unhealthy
- The node stops emitting heartbeats

This prevents silent corruption or inconsistent application behavior.

---

## Health Integration

`ClusterNode` exposes health via `ClusterNodeHealthIndicator`.

Health semantics:
- `UP` while the node is participating normally
- `DOWN` after a fatal listener failure

This integrates cleanly with Spring Boot Actuator and container orchestration systems.

---

## Leader Election

### LeaderElector

Leader election is implemented as a **pure function of cluster state**.

The default implementation:
- Selects the node with the lowest lexical `sessionId`
- Uses UUIDv7 ordering properties
- Requires no additional coordination or storage

Properties:
- Deterministic
- Stateless
- Eventually consistent

This design avoids split-brain prevention guarantees by design. Stronger guarantees belong in a
separate abstraction.

---

## Design Principles

This API deliberately avoids:
- Distributed locks
- Consensus algorithms
- Quorum enforcement
- Exactly-once guarantees

Instead, it provides:
- Clear invariants
- Explicit failure semantics
- Backend portability
- Operational transparency

It is intended as a **foundation**, not a full coordination framework.

---

## Intended Use Cases

- Leader-based scheduling
- Cluster-aware background jobs
- Membership-driven feature toggles
- Service instance coordination

---

## Non-Goals

This module does **not** attempt to:
- Provide linearizability
- Prevent split-brain
- Guarantee event ordering
- Replace consensus systems

If those guarantees are required, a different abstraction should be used.

---

## Summary

This cluster coordination API offers a disciplined, minimal approach to membership and leadership
that scales across backends and deployment environments.

It favors correctness through simplicity and explicit semantics over complexity and implicit
guarantees.
