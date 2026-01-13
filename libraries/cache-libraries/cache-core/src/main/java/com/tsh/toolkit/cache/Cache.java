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

/**
 * A generic key–value cache abstraction.
 *
 * <p>A {@code Cache} provides fast, in-memory (or memory-like) access to values associated with
 * keys. Implementations may apply eviction policies (e.g., LRU, LFU, TTL), persistence, or
 * distribution, but must preserve the semantic contracts defined by this interface.
 *
 * <p>This interface is intentionally minimal and is designed to be embeddable into higher-level
 * systems such as request caches, message deduplication layers, or computation memoization engines.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public interface Cache<K, V> {

  /**
   * Returns the value associated with the given key, or {@code null} if no mapping exists.
   *
   * <p>This method must not create or load a value if one is missing. It is a pure lookup
   * operation.
   *
   * <p>Implementations may update internal access statistics (e.g., LRU, LFU counters) as a
   * side-effect of this call.
   *
   * @param key the key whose associated value is to be returned
   * @return the cached value, or {@code null} if the key is not present
   * @throws NullPointerException if {@code key} is {@code null}
   */
  V get(K key);

  /**
   * Associates the given value with the given key in the cache.
   *
   * <p>If the cache previously contained a mapping for the key, the old value is replaced by the
   * new value.
   *
   * <p>Implementations may trigger eviction of other entries as a result of this operation (e.g.,
   * capacity limits).
   *
   * @param key the key with which the value is to be associated
   * @param value the value to cache
   * @throws NullPointerException if {@code key} or {@code value} is {@code null}
   */
  void put(K key, V value);

  /**
   * Removes the mapping for the given key from the cache if it exists.
   *
   * <p>If the key is not present, this method performs no action. This operation is idempotent.
   *
   * @param key the key whose mapping is to be removed
   * @throws NullPointerException if {@code key} is {@code null}
   */
  void evict(K key);

  /**
   * Returns the runtime {@link Class} object representing the key type supported by this cache.
   *
   * <p>This is primarily intended for:
   *
   * <ul>
   *   <li>serialization and deserialization
   *   <li>type-safe routing and registry lookups
   *   <li>diagnostics and schema inspection
   * </ul>
   *
   * <p>Implementations must return the concrete class corresponding to {@code K}, not a supertype
   * such as {@code Object}.
   *
   * @return the runtime class of the cache's key type
   */
  Class<K> getKeyType();
}
