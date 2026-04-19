# RDBMS Core

The `rdbms-core` module provides **relational database utilities and abstractions** designed for infrastructure applications requiring reliable data persistence.

It focuses on **connection management**, **repository patterns**, and **schema migration** while remaining framework-neutral.

---

## Overview

This module offers foundational RDBMS utilities for:

- **Connection pool management** with HikariCP integration
- **Repository base classes** with transaction support
- **Schema migration** via Flyway integration  
- **Infrastructure-grade error handling** and retry logic

The design favors **explicit transactions** and **predictable failure modes** over convenience abstractions.

---

## Key Components

### DataSource Management

#### `HikariCpDatasourceFactory`

Factory for creating production-ready HikariCP connection pools:

```java
DataSource dataSource = Rdbms.hikariCpDatasourceFactory()
    .jdbcUrl("jdbc:postgresql://localhost:5432/mydb")
    .username("user")
    .password("password")
    .maximumPoolSize(20)
    .connectionTimeout(Duration.ofSeconds(30))
    .build();
```

### Repository Abstractions  

#### `RdbmsRepository`

Comprehensive interface for database repositories providing:

- **Functional interfaces**: `RdbmsFunction<R>`, `RdbmsConsumer`, `RdbmsParamFunction<P>`
- **Transaction control**: Explicit autocommit and rollback handling
- **Batch operations**: Sophisticated batching with size limits and error handling
- **IN clause optimization**: Dynamic generation of parameterized IN clauses
- **Connection management**: Safe resource handling with try-with-resources pattern

Key method signatures:
```java
public interface RdbmsRepository {
  <R> R executeAndReturn(DataSource dataSource, RdbmsFunction<R> fn, boolean autocommit);
  <P> List<Integer> executeBatch(PreparedStatement ps, int batchSize, List<P> params, RdbmsParamFunction<P> fn);
  default String generateInClause(int inClauseLiteralSizeLimit);
  <T, R> R executeQueryWithInClause(int maxInClauseSize, List<T> params, /* ... */);
}
```

#### `AbstractRdbmsRepository`

Abstract base class implementing `RdbmsRepository` with additional features:

- **Flyway integration**: Built-in database migration support with schema-specific paths
- **Simplified method signatures**: Convenience overloads with sensible defaults  
- **DataSource management**: Encapsulates DataSource and provides safe access patterns

```java
public class UserRepository extends AbstractRdbmsRepository {
  
  public UserRepository(DataSource dataSource) {
    super(dataSource);
    // Run migrations for this module's schema
    migrate("user_schema");
  }
  
  public User findById(String userId) {
    return executeAndReturn(conn -> {
      // Automatic transaction management and rollback
      try (PreparedStatement ps = conn.prepareStatement(
          "SELECT id, name, email FROM users WHERE id = ?")) {
        ps.setString(1, userId);
        try (ResultSet rs = ps.executeQuery()) {
          return rs.next() ? mapUser(rs) : null;
        }
      }
    });
  }
  
  public void saveBatch(List<User> users) {
    execute(conn -> {
      try (PreparedStatement ps = conn.prepareStatement(
          "INSERT INTO users (id, name, email) VALUES (?, ?, ?)")) {
        executeBatch(ps, 100, users, (stmt, user) -> {
          stmt.setString(1, user.getId());
          stmt.setString(2, user.getName());
          stmt.setString(3, user.getEmail());
          return true; // Include in batch
        });
      }
    });
  }
}
```

### Error Handling

#### `RdbmsRepositoryException`

Specialized exception for repository operations:

- **Wraps SQL exceptions** with context
- **Preserves stack traces** for debugging
- **Categorizes failure types** (connection, constraint, timeout)

---

## Design Principles

### Explicit Transaction Control

- **No implicit transactions**: Callers control transaction boundaries
- **Connection-per-operation**: Each operation uses a fresh connection
- **Fail-fast behavior**: Errors are propagated immediately

### Framework Neutrality

- **No Spring dependencies**: Works with any DI container
- **Standard JDBC**: Uses only javax.sql interfaces
- **Pluggable configuration**: Supports any connection pool implementation

### Infrastructure Focus

- **Production-ready defaults**: Optimized for long-running services
- **Observability support**: Structured logging and metrics hooks
- **Failure resilience**: Retry logic and circuit breaker patterns

---

## Schema Migration

### Flyway Integration

#### Built-in Migration Support

`AbstractRdbmsRepository` includes built-in Flyway integration:

```java
public class MyRepository extends AbstractRdbmsRepository {
  public MyRepository(DataSource dataSource) {
    super(dataSource);
    // Run schema-specific migrations
    migrate("my_module");
  }
}
```

Migration files are located at `classpath:db/migration/{schema}/`:

```
src/main/resources/db/migration/my_module/
├── V1__Create_initial_tables.sql
├── V2__Add_indexes.sql  
└── V3__Add_constraints.sql
```

#### Migration Characteristics

- **Schema-specific**: Each module can manage its own schema migrations
- **Classpath-based**: Migrations are bundled with the application
- **Idempotent**: Safe to call migrate() multiple times
- **Automatic execution**: Migrations run when repository is constructed

---

## Connection Pool Configuration

### Production Settings

```java
DataSource dataSource = Rdbms.hikariCpDatasourceFactory()
    // Connection settings
    .jdbcUrl("jdbc:postgresql://db.example.com:5432/production")
    .username("app_user")
    .password("secure_password")
    
    // Pool sizing
    .maximumPoolSize(20)
    .minimumIdle(5)
    
    // Timeouts
    .connectionTimeout(Duration.ofSeconds(30))
    .idleTimeout(Duration.ofMinutes(10))
    .maxLifetime(Duration.ofMinutes(30))
    
    // Validation
    .validationTimeout(Duration.ofSeconds(5))
    .connectionTestQuery("SELECT 1")
    
    // Performance
    .prepStmtCacheSize(256)
    .prepStmtCacheSqlLimit(2048)
    .build();
```

### Health Checks

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
  
  private final RdbmsRepository repository;
  
  @Override
  public Health health() {
    try {
      repository.queryForObject(
        "SELECT 1 as health_check", 
        rs -> rs.getInt("health_check")
      );
      return Health.up().build();
    } catch (Exception e) {
      return Health.down(e).build();
    }
  }
}
```

---

## Transaction Patterns

### Programmatic Transactions

```java
@Service
public class UserService {
  
  private final DataSource dataSource;
  private final UserRepository userRepository;
  
  @Transactional
  public void transferUser(String fromGroupId, String toGroupId, String userId) {
    try (Connection conn = dataSource.getConnection()) {
      conn.setAutoCommit(false);
      
      try {
        userRepository.removeFromGroup(conn, fromGroupId, userId);
        userRepository.addToGroup(conn, toGroupId, userId);
        
        conn.commit();
      } catch (Exception e) {
        conn.rollback();
        throw new UserTransferException("Failed to transfer user", e);
      }
    }
  }
}
```

### Batch Operations

```java
public void saveBatch(List<User> users) {
  String sql = "INSERT INTO users (id, name, email) VALUES (?, ?, ?)";
  
  executeBatch(sql, users, (stmt, user) -> {
    stmt.setString(1, user.getId());
    stmt.setString(2, user.getName());
    stmt.setString(3, user.getEmail());
  });
}
```

---

## Testing Support

### In-Memory Databases

```java
@TestConfiguration
public class TestDatabaseConfig {
  
  @Bean
  @Primary
  public DataSource testDataSource() {
    return Rdbms.hikariCpDatasourceFactory()
        .jdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
        .username("sa")
        .password("")
        .maximumPoolSize(1)
        .build();
  }
}
```

### Repository Testing

```java
@DataJpaTest
class UserRepositoryTest {
  
  @Autowired
  private DataSource dataSource;
  
  private UserRepository userRepository;
  
  @BeforeEach
  void setUp() {
    userRepository = new UserRepository(dataSource);
  }
  
  @Test
  void shouldFindUserById() {
    User user = new User("123", "John Doe", "john@example.com");
    userRepository.save(user);
    
    User found = userRepository.findById("123");
    
    assertThat(found).isEqualTo(user);
  }
}
```

---

## Performance Considerations

### Connection Pooling

- **Pool size**: Set based on expected concurrent operations
- **Connection validation**: Use lightweight queries (SELECT 1)
- **Leak detection**: Enable in development environments

### Query Optimization

- **Prepared statements**: Always use parameterized queries
- **Statement caching**: Enable driver-level statement caching  
- **Batch operations**: Group multiple writes for efficiency

### Monitoring

- **Connection metrics**: Pool utilization, wait times, errors
- **Query metrics**: Execution time, row counts, cache hits
- **Error rates**: Failed connections, timeouts, deadlocks

---

## Dependencies

### Required

- **HikariCP**: Production-ready connection pooling
- **PostgreSQL Driver**: Primary database support
- **toolkit-core**: Foundation utilities

### Optional  

- **Flyway**: Schema migration management
- **Spring Boot**: Auto-configuration support
- **Micrometer**: Metrics collection

---

## Status

This module is **production-ready** and actively used in distributed systems requiring reliable data persistence.

APIs are stable with backward compatibility guarantees.

---

## License

Licensed under the Apache License, Version 2.0.  
Copyright © 2025 Haseem Kheiri