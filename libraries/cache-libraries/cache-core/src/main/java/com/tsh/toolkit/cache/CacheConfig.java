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

import com.tsh.toolkit.core.utils.Check;
import java.time.Duration;
import lombok.Getter;

/**
 * Configuration object for a cache instance.
 *
 * <p>{@code CacheConfig} defines the two fundamental cache policies:
 *
 * <ul>
 *   <li><b>Time-to-live (TTL)</b> — how long a cached entry may remain valid
 *   <li><b>Maximum size</b> — the upper bound on how many entries the cache may hold
 * </ul>
 *
 * <p>This class is intentionally small and opinionated. It provides safe, production-ready defaults
 * while allowing explicit overrides through a fluent API.
 *
 * <pre>{@code
 * CacheConfig config = new CacheConfig()
 *     .ttl(Duration.ofMinutes(2))
 *     .maxSize(10_000);
 * }</pre>
 *
 * <p>The cache implementation is responsible for enforcing these policies. {@code CacheConfig} is a
 * declaration of intent, not a controller.
 */
@Getter
public class CacheConfig {

  /**
   * The time-to-live (TTL) for cache entries.
   *
   * <p>Entries older than this duration are considered expired and are eligible for eviction.
   * Expiration may be enforced eagerly or lazily depending on the cache implementation.
   *
   * <p>The default value is {@code 5 minutes}.
   */
  private Duration ttl = Duration.ofMinutes(5);

  /**
   * The maximum number of entries allowed in the cache.
   *
   * <p>When this limit is reached, the cache implementation must evict entries according to its
   * eviction policy (for example, LRU) to make room for new ones.
   *
   * <p>The default value is {@code 5000}.
   */
  private Integer maxSize = 5000;

  /**
   * Sets the time-to-live (TTL) for cache entries.
   *
   * <p>This defines the maximum age an entry may reach before it is considered expired and eligible
   * for eviction.
   *
   * @param duration the TTL duration; must not be {@code null}
   * @return this {@code CacheConfig} instance for fluent chaining
   * @throws NullPointerException if {@code duration} is {@code null}
   */
  public CacheConfig ttl(Duration duration) {
    this.ttl = Check.requireNotNull(duration, () -> "duration must not be null.");
    return this;
  }

  /**
   * Sets the maximum number of entries the cache is allowed to hold.
   *
   * <p>The value must be greater than zero and no greater than {@code 100,000}. This upper bound
   * exists to prevent accidental creation of unbounded or dangerously large caches that could
   * exhaust memory.
   *
   * <p>The cache implementation is responsible for enforcing this limit, typically by evicting
   * entries when the limit is exceeded.
   *
   * @param maxSize the maximum number of entries allowed in the cache
   * @return this {@code CacheConfig} instance for fluent chaining
   * @throws IllegalArgumentException if {@code maxSize} is not in the range {@code 1..100000}
   */
  public CacheConfig maxSize(int maxSize) {
    this.maxSize =
        Check.requireTrue(
            () -> maxSize,
            s -> s > 0 && s <= 100000,
            () -> "maxSize should be greater than 0 and less than or equal to 100000");
    return this;
  }
}
