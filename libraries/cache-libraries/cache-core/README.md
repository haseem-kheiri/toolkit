# Cache Core

The `cache-core` module provides a **distributed cache invalidation system** with explicit eviction semantics and eventual consistency guarantees.

It is designed for **infrastructure applications** where cache coherence across multiple nodes is critical.

---

## Overview

This module implements a distributed caching system that focuses on **invalidation propagation** rather than data storage. Local caches maintain their own data while participating in a global invalidation protocol.

---

## Core Components

### Cache

A minimal key–value store interface with explicit eviction semantics:

```java
public interface Cache<K, V> {
  V get(K key);
  void put(K key, V value);
  void evict(K key);
  Class<K> getKeyType();
}
```

The actual implementation is backed by **EhCache** for high-performance local storage.

### CacheManager

Coordinates multiple named caches and propagates evictions through a distributed bus:

- Manages lifecycle of EhCache-backed cache instances
- Routes eviction events to appropriate caches
- Publishes local evictions via the invalidation bus
- Consumes and replays remote eviction events
- Extends `AbstractLifecycleObject` for proper start/stop semantics

### CacheInvalidationBus

Abstract transport layer for eviction events with global ordering:

```java
public interface CacheInvalidationBus {
  void publishEviction(List<EvictionEvent> events);
  List<EvictionEvent> pollEvents(List<String> names, OffsetDateTime dt);
  OffsetDateTime getNow();
}
```

- Publishes local eviction events to remote nodes
- Subscribes to remote eviction events via polling
- Provides authoritative timestamps to avoid clock skew

---

## Architecture

The system uses an **event-driven invalidation model**:

1. **Local Eviction**: When a cache evicts a key locally, an event is generated
2. **Event Publishing**: The event is written to a persistent log and published via the bus
3. **Event Propagation**: Remote nodes receive and replay eviction events
4. **Monotonic Convergence**: Timestamp-based ordering ensures consistent replay

```text
Node A                    Event Bus                    Node B
┌─────────┐              ┌─────────┐                  ┌─────────┐
│ Cache   │──evict(k)──→ │ Events  │ ──distribute──→ │ Cache   │
│ Manager │              │   Log   │                  │ Manager │
└─────────┘              └─────────┘                  └─────────┘
```

---

## Consistency Guarantees

### At-Least-Once Delivery

- Eviction events are delivered to all participating nodes
- Duplicate delivery is tolerated through idempotent operations
- No events are permanently lost under normal operation

### Monotonic Convergence

- Events are applied in timestamp order
- Nodes converge to the same final state
- Late-arriving events are handled correctly

### Loop-Free Propagation

- Events include originator information
- Nodes do not re-propagate received events
- Prevents infinite propagation cycles

---

## Usage Patterns

### Basic Setup

```java
// Create cache manager with invalidation bus
CacheInvalidationBus bus = new PostgresInvalidationBus(dataSource);
CacheManager manager = new CacheManager(bus);

// Register named caches
Cache<String, Object> userCache = manager.getCache("users");
Cache<String, Object> configCache = manager.getCache("config");
```

### Eviction Propagation

```java
// Local eviction automatically propagates
userCache.evict("user:123");

// Remote nodes will receive and apply the eviction
// No explicit coordination required
```

### Lifecycle Management

```java
// Start the manager to begin event processing
manager.start();

// Stop cleanly to flush pending events
manager.stop();
```

---

## Implementation Notes

### Backend Agnostic

The core module defines interfaces only. Concrete implementations handle:

- **Event persistence**: Database, message queue, or distributed log
- **Transport mechanism**: Polling, push notifications, or streaming
- **Clock synchronization**: NTP, logical clocks, or hybrid approaches

### Memory Management

- **EhCache backing**: Uses EhCache as the high-performance local storage engine
- **Configurable policies**: Support for TTL, capacity limits, and eviction strategies
- **Off-heap storage**: Optional off-heap memory allocation for large datasets
- **The invalidation system only handles coordination**: Local cache policies are managed by EhCache

### Dependencies

### Required

- **EhCache**: High-performance local caching engine
- **Jackson**: JSON serialization for event keys
- **toolkit-core**: Foundation utilities and lifecycle management

### Optional

- **Spring Boot**: Auto-configuration and dependency injection support

### Failure Handling

- Temporary bus failures do not affect local cache operation
- Events are retried with exponential backoff
- Persistent failures trigger circuit breaker behavior

---

## Integration

### With Application Frameworks

The cache system integrates with:

- **Spring Boot**: Auto-configuration and dependency injection
- **Kubernetes**: Pod-level coordination and service discovery
- **Observability**: Metrics, tracing, and health checks

### With Storage Systems

Event persistence can use:

- **PostgreSQL**: ACID guarantees with trigger-based notification
- **Apache Kafka**: High-throughput streaming with partition ordering
- **Redis Streams**: Low-latency coordination with built-in persistence

---

## Performance Characteristics

### Latency

- Local cache operations: **Sub-millisecond**
- Invalidation propagation: **10-100ms** (network dependent)
- Event processing: **Batched** for efficiency

### Throughput

- Local operations: **Limited by memory bandwidth**
- Event publishing: **Limited by backend capacity**
- Convergence time: **Proportional to cluster size**

---

## Status

This module is production-ready and actively used in distributed systems.

APIs are stable with backward compatibility guarantees.

---

## License

Licensed under the Apache License, Version 2.0.  
Copyright © 2025 Haseem Kheiri