/*
 * Copyright 2025 Haseem Kheiri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND.
 */

package com.tsh.toolkit.cache.invalidate.impl;

import com.tsh.toolkit.cache.CacheInvalidationBus;
import com.tsh.toolkit.cache.impl.CacheManager.EvictionEvent;
import com.tsh.toolkit.rdbms.AbstractRdbmsRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 * PostgreSQL-backed implementation of {@link CacheInvalidationBus}.
 *
 * <p>This class provides a durable, time-ordered eviction log stored in a PostgreSQL table. Each
 * eviction event is persisted and can be replayed by other nodes to converge their local caches.
 *
 * <p>The underlying table ({@code cache_inv_bus.obj_evict_event}) acts as an append-only event log
 * with the following semantics:
 *
 * <ul>
 *   <li>Each insert represents an authoritative eviction
 *   <li>{@code recorded_at} is assigned by PostgreSQL using {@code now()}
 *   <li>Events are retrieved using a strictly monotonic timestamp cursor
 * </ul>
 *
 * <p>This implementation provides:
 *
 * <ul>
 *   <li>durability via PostgreSQL WAL
 *   <li>global clock via {@code now()}
 *   <li>linearizable inserts under {@code READ_COMMITTED}
 * </ul>
 */
public class PostgresCacheInvalidationBus extends AbstractRdbmsRepository
    implements CacheInvalidationBus {

  /**
   * Inserts a new eviction event.
   *
   * <p>{@code recorded_at} is assigned by PostgreSQL and is used to establish the global ordering
   * of events.
   */
  private static final String PUBLISH_EVENT_SQL =
      """
        insert into cache_inv_bus.obj_evict_event (cache_name, cache_key)
        values (?,?)
        """;

  /**
   * Retrieves eviction events newer than a given timestamp for a set of cache names.
   *
   * <p>The {@code IN (...)} clause is dynamically generated at runtime.
   */
  private static final String POLL_EVENT_SQL =
      """
        select cache_name, cache_key, recorded_at
          from cache_inv_bus.obj_evict_event
         where recorded_at > ? and cache_name
        """;

  /**
   * Returns PostgreSQL's authoritative current time.
   *
   * <p>This is used to establish a consistent time base across all cache nodes.
   */
  private static final String NOW_SQL = "select now()";

  /**
   * Creates a new PostgreSQL-backed invalidation bus.
   *
   * @param dataSource the JDBC data source
   */
  public PostgresCacheInvalidationBus(DataSource dataSource) {
    super(dataSource);
  }

  /**
   * Persists eviction events to PostgreSQL.
   *
   * <p>This method performs a batched insert under {@link Connection#TRANSACTION_READ_COMMITTED}
   * isolation.
   *
   * <p>Once committed, the events become visible to all nodes polling the bus.
   *
   * @param events the eviction events to publish
   */
  @Override
  public void publishEviction(List<EvictionEvent> events) {
    execute(
        c -> {
          c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
          try (final PreparedStatement ps = c.prepareStatement(PUBLISH_EVENT_SQL)) {
            executeBatch(
                ps,
                100,
                events,
                (s, event) -> {
                  s.setString(1, event.cacheName());
                  s.setString(2, event.key());
                  return true;
                });
          }
        });
  }

  /**
   * Retrieves eviction events that occurred after the specified timestamp for the given cache
   * names.
   *
   * <p>The returned events are suitable for deterministic replay by cache nodes.
   *
   * <p>Events are fetched under {@link Connection#TRANSACTION_READ_COMMITTED}, which ensures:
   *
   * <ul>
   *   <li>no dirty reads
   *   <li>visibility of all committed evictions
   * </ul>
   *
   * @param names the cache names to retrieve events for
   * @param dt the lower bound (exclusive) timestamp
   * @return eviction events newer than {@code dt}
   */
  @Override
  public List<EvictionEvent> pollEvents(List<String> names, OffsetDateTime dt) {

    return executeAndReturn(
        c -> {
          c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

          return executeQueryWithInClause(
              50,
              names,
              (params) -> {
                final String sql = POLL_EVENT_SQL + generateInClause(params.size());
                return c.prepareStatement(sql);
              },
              (ps, params) -> {
                int index = 1;
                ps.setObject(index++, dt);
                for (String name : names) {
                  ps.setString(index++, name);
                }
              },
              rs -> {
                List<EvictionEvent> events = new ArrayList<>();
                while (rs.next()) {
                  events.add(
                      new EvictionEvent(
                          rs.getString(1), rs.getString(2), rs.getObject(3, OffsetDateTime.class)));
                }
                return events;
              });
        });
  }

  /**
   * Returns PostgreSQL's current timestamp.
   *
   * <p>This value must be used by cache nodes as the reference for subsequent {@link
   * #pollEvents(List, OffsetDateTime)} calls in order to avoid clock skew between machines.
   *
   * @return the database server's current time
   */
  @Override
  public OffsetDateTime getNow() {
    return executeAndReturn(
        c -> {
          c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
          try (final PreparedStatement ps = c.prepareStatement(NOW_SQL)) {
            final ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getObject(1, OffsetDateTime.class) : null;
          }
        });
  }
}
