# Cache Invalidation - PostgreSQL Provider

The `cache-invalidation-postgres` module provides a **PostgreSQL-backed implementation** of the distributed cache invalidation system defined in `cache-core`.

It uses **database triggers and polling** to achieve reliable, ordered delivery of cache invalidation events across multiple application instances.

---

## Overview

This module implements the `CacheInvalidationBus` interface using PostgreSQL as the event transport mechanism. It provides:

- **ACID event persistence** via database transactions
- **Ordered event delivery** using sequence-based timestamps  
- **Efficient polling** with database notifications
- **Automatic cleanup** of processed events

---

## Architecture

### Event Storage

Cache invalidation events are stored in a dedicated PostgreSQL table with schema:

```sql
CREATE SCHEMA cache_inv_bus;

CREATE TABLE cache_inv_bus.obj_evict_event (
  id BIGSERIAL PRIMARY KEY,
  cache_name VARCHAR(255) NOT NULL,
  cache_key TEXT NOT NULL,
  recorded_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_evict_event_lookup ON cache_inv_bus.obj_evict_event 
  (recorded_at, cache_name);
```

Key characteristics:
- **Schema-qualified**: Uses `cache_inv_bus` schema for organization
- **Auto-timestamping**: `recorded_at` is set by PostgreSQL `now()` function
- **Optimized indexing**: Composite index for efficient time-range queries

### Event Flow

1. **Local Eviction**: Application evicts a cache key locally
2. **Event Persistence**: Event written to PostgreSQL table within transaction
3. **Notification**: Database trigger sends NOTIFY signal
4. **Remote Polling**: Other instances receive notification and poll for new events
5. **Event Replay**: Remote instances apply evictions to their local caches
6. **Cleanup**: Processed events are deleted after configurable retention

```text
App Instance A                PostgreSQL                App Instance B
┌─────────────┐             ┌─────────────┐            ┌─────────────┐
│evict(key)   │             │ Events      │            │             │
│   ↓         │─── INSERT ─→│ Table       │←── POLL ───│ Listen for  │
│event created│             │             │            │ NOTIFY      │
└─────────────┘             │ NOTIFY ────────────────→ │   ↓         │
                            │ trigger     │            │ apply evict │
                            └─────────────┘            └─────────────┘
```

---

## Key Components

### PostgresCacheInvalidationBus

Main implementation class that extends `AbstractRdbmsRepository`:

- Implements the `CacheInvalidationBus` interface
- Uses batched inserts with configurable batch sizes (default 100)
- Performs queries with `IN` clause optimization for multiple cache names
- Uses `READ_COMMITTED` isolation level for consistency
- Leverages PostgreSQL's `now()` function for authoritative timestamps

Key implementation details:
- **Batched publishing**: Groups multiple eviction events into single transaction
- **Dynamic IN clauses**: Optimizes queries when polling multiple cache names
- **Schema-aware**: Uses `cache_inv_bus.obj_evict_event` table

### Configuration

The PostgreSQL invalidation bus is configured primarily through the DataSource connection:

```java
@Bean
public CacheInvalidationBus invalidationBus(DataSource dataSource) {
  return new PostgresCacheInvalidationBus(dataSource);
}
```

No additional configuration properties are currently exposed. Behavior is controlled by:
- **Batch size**: Hardcoded to 100 events per batch
- **Transaction isolation**: `READ_COMMITTED` for consistency
- **Query optimization**: Automatic IN clause batching (max 50 parameters)

### Database Schema

The module includes schema migration scripts for:

- Event storage table creation
- Indexes for efficient querying  
- Database triggers for notifications
- Cleanup procedures for old events

---

## Usage

### Spring Boot Integration

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/myapp
    username: myuser
    password: mypass
```

The module provides auto-configuration through `PostgresCacheInvalidationBusConfiguration` when the required dependencies are on the classpath.

### Programmatic Configuration

```java
@Configuration
public class CacheConfig {

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

### Basic Usage

```java
@Service
public class UserService {

  private final Cache<String, User> userCache;

  public UserService(CacheManager cacheManager) {
    this.userCache = cacheManager.getCache("users");
  }

  public User getUser(String userId) {
    return userCache.computeIfAbsent(userId, this::loadUserFromDatabase);
  }

  public void updateUser(User user) {
    updateUserInDatabase(user);
    // Eviction automatically propagates to other instances
    userCache.evict(user.getId());
  }
}
```

---

## Operational Characteristics

### Performance

- **Event publishing**: ~1-2ms (single database write)
- **Event propagation**: 1-5s depending on poll interval
- **Throughput**: Limited by database write capacity
- **Memory usage**: O(batch_size) events buffered per instance

### Reliability

- **Durability**: Events survive application restarts
- **Ordering**: ACID transactions ensure consistent event ordering
- **At-least-once delivery**: Events may be delivered multiple times
- **Partition tolerance**: Temporary database unavailability is handled gracefully

### Scalability

- **Horizontal scaling**: Linear with number of application instances
- **Database load**: Proportional to eviction rate × instance count
- **Storage growth**: Managed by automatic event cleanup

---

## Monitoring and Observability

### Metrics

The module exposes metrics for:

- Event publishing rate and latency
- Event consumption rate and lag
- Database connection health
- Error rates and retry counts

### Health Checks

Built-in health indicators for:

- Database connectivity
- Event processing status  
- Queue depth and lag metrics

### Logging

Structured logging for:

- Event lifecycle (publish, consume, error)
- Configuration changes
- Database connection issues

---

## Testing

### Integration Tests

The module includes comprehensive integration tests using:

- **Testcontainers**: Embedded PostgreSQL for realistic testing
- **Multi-instance simulation**: Tests coordination between multiple cache managers
- **Failure scenarios**: Database failures, network partitions, instance restarts

### Test Example

```java
@SpringBootTest
@Testcontainers
class PostgresCacheInvalidationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");

  @Test
  void shouldPropagateEvictionBetweenInstances() {
    // Create two cache managers with same database
    CacheManager manager1 = createCacheManager();
    CacheManager manager2 = createCacheManager();
    
    Cache<String, String> cache1 = manager1.getCache("test");
    Cache<String, String> cache2 = manager2.getCache("test");
    
    // Put value in both caches
    cache1.put("key", "value");
    cache2.put("key", "value");
    
    // Evict from first cache
    cache1.evict("key");
    
    // Wait for propagation and verify eviction
    await().until(() -> cache2.get("key") == null);
  }
}
```

---

## Migration and Deployment

### Database Setup

1. **Create schema**: Run provided SQL migration scripts
2. **Configure connection**: Set up database connection pool
3. **Verify permissions**: Ensure read/write access to events table

### Rolling Deployment

The system supports rolling deployments:

- Old instances continue processing existing events
- New instances start processing from current position
- No coordination required between versions

### Backup and Recovery

- **Event data**: Included in regular database backups
- **Recovery**: Events are automatically replayed from backup
- **Point-in-time recovery**: Supported via database PITR

---

## Dependencies

### Required

- **PostgreSQL**: 10.0 or later
- **Spring Boot**: 2.7 or later  
- **JDBC Driver**: PostgreSQL JDBC driver
- **cache-core**: Core cache invalidation interfaces

### Optional

- **Spring Boot Actuator**: For health checks and metrics
- **Micrometer**: For custom metrics collection
- **Testcontainers**: For integration testing

---

## Status

This module is **production-ready** and actively used in distributed systems requiring reliable cache coherence.

APIs are stable with backward compatibility guarantees.

---

## License

Licensed under the Apache License, Version 2.0.  
Copyright © 2025 Haseem Kheiri