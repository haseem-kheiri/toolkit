# Cache Libraries

The `cache-libraries` module provides **distributed caching infrastructure** with explicit invalidation semantics and eventual consistency guarantees.

This collection of libraries is designed for **infrastructure applications** where cache coherence across multiple nodes is critical for correctness.

---

## Philosophy

- **Invalidation-first design**  
  Focuses on cache coherence rather than data distribution.

- **Explicit consistency semantics**  
  Clear guarantees about when and how caches converge.

- **Backend-agnostic abstractions**  
  Core interfaces work with multiple storage and transport layers.

- **Infrastructure-grade reliability**  
  Handles failures, network partitions, and rolling deployments gracefully.

---

## Modules

```text
cache-libraries
├── cache-core
└── cache-invalidation-postgres
```

Each module has a focused responsibility and can be consumed independently.

---

## cache-core

The `cache-core` module defines the **fundamental caching abstractions** and invalidation protocols.

### Key Features

- **Distributed invalidation bus**: Event-driven cache coordination
- **Pluggable backends**: Support for multiple transport mechanisms
- **Eventual consistency**: Monotonic convergence with timestamp ordering
- **At-least-once delivery**: Reliable event propagation guarantees

### Core Interfaces

```java
// Simple cache abstraction
public interface Cache<K, V> {
  V get(K key);
  void put(K key, V value);
  void evict(K key);
}

// Distributed invalidation coordination
public interface CacheInvalidationBus {
  void publish(InvalidationEvent event);
  void subscribe(InvalidationEventHandler handler);
}
```

---

## cache-invalidation-postgres

**PostgreSQL-backed implementation** of the cache invalidation system with ACID event persistence and database-driven notifications.

### Key Features

- **ACID event storage**: Transactional consistency for invalidation events
- **Database triggers**: Efficient notification of cache changes
- **Automatic cleanup**: Configurable retention of processed events
- **Spring Boot integration**: Auto-configuration and health checks

---

## Design Principles

### Event-Driven Architecture

Cache invalidation uses an **event sourcing pattern**:

1. Local cache operations generate events
2. Events are persisted and published via the invalidation bus  
3. Remote nodes consume events and apply changes locally
4. System converges to consistent state across all nodes

```text
Local Cache → Invalidation Event → Event Bus → Remote Caches
     ↑                                             ↓
 Application                                  Convergence
```

### Explicit Consistency Model

The system provides **eventual consistency** with clear semantics:

- **Monotonic convergence**: All nodes converge to the same final state
- **Causal ordering**: Related events are processed in logical order
- **Partition tolerance**: Temporary failures do not break convergence

### Separation of Concerns

- **Local caching**: Each node manages its own memory and eviction policies
- **Coordination**: The invalidation bus handles only cache coherence
- **Transport**: Backend implementations manage persistence and delivery

---

## Usage Patterns

### Multi-Tier Applications

```text
Load Balancer
    ↓
┌─────────────────────────────────────┐
│ Application Tier (Multiple Pods)   │
│ ┌─────────┐ ┌─────────┐ ┌─────────┐ │
│ │App Pod 1│ │App Pod 2│ │App Pod N│ │
│ │ Cache   │ │ Cache   │ │ Cache   │ │
│ └─────────┘ └─────────┘ └─────────┘ │
└─────────────┬───────────────────────┘
              ↓
        ┌─────────────┐
        │ PostgreSQL  │
        │ Event Store │
        └─────────────┘
```

### Microservices Architecture

```text
Service A    Service B    Service C
┌─────────┐ ┌─────────┐ ┌─────────┐
│ Cache   │ │ Cache   │ │ Cache   │
│ Manager │ │ Manager │ │ Manager │
└─────────┘ └─────────┘ └─────────┘
     │           │           │
     └───────────┼───────────┘
                 ↓
         ┌───────────────┐
         │ Shared Cache  │
         │ Invalidation  │
         │ Infrastructure│
         └───────────────┘
```

---

## Configuration

### Spring Boot Auto-Configuration

```yaml
cache:
  invalidation:
    postgres:
      poll-interval: 2s
      event-retention: 12h
      batch-size: 50
      
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/myapp
    username: myuser
    password: mypass
```

### Programmatic Setup

```java
@Configuration
public class CacheConfiguration {

  @Bean
  public CacheInvalidationBus invalidationBus(DataSource dataSource) {
    return new PostgresCacheInvalidationBus(dataSource);
  }

  @Bean  
  public CacheManager cacheManager(CacheInvalidationBus bus) {
    return new CacheManager(bus);
  }
}
```

---

## Operational Characteristics

### Performance

- **Local operations**: Sub-millisecond cache access
- **Invalidation propagation**: 1-5 seconds (configurable)
- **Throughput**: Limited by backend write capacity
- **Memory overhead**: Proportional to active cache size

### Reliability

- **Event durability**: Survives application and database restarts
- **Failure recovery**: Automatic retry with exponential backoff
- **Split-brain handling**: Timestamp-based conflict resolution
- **Rolling deployments**: Zero-downtime updates supported

### Scalability

- **Horizontal scaling**: Linear with number of application instances
- **Storage efficiency**: Automatic cleanup of processed events
- **Network efficiency**: Batched event processing

---

## Monitoring

### Key Metrics

- **Cache hit/miss rates**: Per-cache performance metrics
- **Invalidation latency**: Time from eviction to propagation
- **Event queue depth**: Backlog of unprocessed invalidations
- **Database health**: Connection pool and query performance

### Health Checks

- **Bus connectivity**: Invalidation transport health
- **Event processing**: Queue processing status  
- **Cache consistency**: Divergence detection and alerting

---

## Integration

### Framework Support

- **Spring Boot**: Auto-configuration and dependency injection
- **Spring Cache**: Integration with `@Cacheable` annotations
- **Micrometer**: Metrics and observability integration

### Infrastructure

- **Kubernetes**: Pod-aware coordination and service discovery
- **Docker**: Container-friendly configuration and lifecycle
- **Cloud environments**: Works with managed PostgreSQL services

---

## Testing

### Unit Testing

```java
@Test
void shouldEvictFromMultipleCaches() {
  MockInvalidationBus bus = new MockInvalidationBus();
  CacheManager manager = new CacheManager(bus);
  
  Cache<String, String> cache1 = manager.getCache("cache1");
  Cache<String, String> cache2 = manager.getCache("cache2");
  
  cache1.put("key", "value");
  cache2.put("key", "value");
  
  cache1.evict("key");
  
  // Verify event was published
  assertThat(bus.getPublishedEvents()).hasSize(1);
}
```

### Integration Testing

Integration tests use **Testcontainers** to validate real PostgreSQL coordination between multiple cache manager instances.

---

## Status

This module is **production-ready** and actively used in distributed systems.

APIs are stable with strong backward compatibility guarantees.

---

## License

Licensed under the Apache License, Version 2.0.  
Copyright © 2025 Haseem Kheiri