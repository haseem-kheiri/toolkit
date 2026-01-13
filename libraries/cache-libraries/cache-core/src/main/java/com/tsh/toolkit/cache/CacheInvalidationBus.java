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

package com.tsh.toolkit.cache;

import com.tsh.toolkit.cache.impl.CacheManager.EvictionEvent;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * A distributed invalidation channel for propagating cache eviction events across multiple JVMs,
 * services, or cache managers.
 *
 * <p>The {@code CacheInvalidationBus} provides a durable, time-ordered event stream of {@link
 * EvictionEvent}s that allows independent cache instances to converge toward a consistent eviction
 * state.
 *
 * <p>This abstraction is intentionally transport-agnostic and may be backed by messaging systems
 * (Kafka, Redis, Artemis, etc.), databases, or in-memory clusters.
 */
public interface CacheInvalidationBus {

  /**
   * Publishes one or more cache eviction events to the invalidation bus.
   *
   * <p>Each event represents the authoritative removal of a key from a named cache. Implementations
   * must ensure that published events are made visible to all subscribers that subsequently poll
   * the bus.
   *
   * <p>This operation is typically invoked by the cache node that performed the original eviction.
   *
   * @param events the eviction events to publish
   * @throws NullPointerException if {@code events} or any contained event is {@code null}
   */
  void publishEviction(List<EvictionEvent> events);

  /**
   * Retrieves eviction events for the given cache names that occurred after the specified
   * timestamp.
   *
   * <p>The returned events must satisfy:
   *
   * <pre>
   *     event.getTimestamp() &gt; dt
   * </pre>
   *
   * <p>Implementations should return events in ascending timestamp order to allow callers to replay
   * invalidations deterministically.
   *
   * <p>This method is typically invoked by cache nodes to synchronize their local caches with
   * evictions performed elsewhere.
   *
   * @param names the cache names to retrieve eviction events for
   * @param dt the lower bound (exclusive) timestamp for event retrieval
   * @return a list of eviction events newer than {@code dt} for the given cache names
   * @throws NullPointerException if {@code names} or {@code dt} is {@code null}
   */
  List<EvictionEvent> pollEvents(List<String> names, OffsetDateTime dt);

  /**
   * Returns the authoritative current time of the invalidation bus.
   *
   * <p>This timestamp defines the clock against which eviction event timestamps are generated and
   * compared. Using a bus-provided time avoids clock skew between distributed cache nodes.
   *
   * <p>Callers should use this value as the reference point for subsequent calls to {@link
   * #pollEvents(List, OffsetDateTime)}.
   *
   * @return the current bus time
   */
  OffsetDateTime getNow();
}
