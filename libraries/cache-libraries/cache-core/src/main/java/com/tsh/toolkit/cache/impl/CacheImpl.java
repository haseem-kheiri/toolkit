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

package com.tsh.toolkit.cache.impl;

import com.tsh.toolkit.cache.Cache;

/**
 * Default {@link Cache} implementation backed by Ehcache and integrated with a {@link CacheManager}
 * for distributed eviction propagation.
 *
 * <p>This class acts as the bridge between:
 *
 * <ul>
 *   <li>the local in-process Ehcache instance
 *   <li>the cluster-wide invalidation mechanism
 * </ul>
 *
 * <p>All local mutations that represent authoritative removals (i.e. {@link #evict(Object)}) are
 * published to the {@link CacheManager} so that other cache instances may converge.
 *
 * <p>Pure removals triggered by remote invalidation must use {@link #remove(Object)} to avoid
 * feedback loops.
 *
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class CacheImpl<K, V> implements Cache<K, V> {

  private final CacheManager manager;
  private final String name;
  private final Class<K> keyType;
  private final org.ehcache.Cache<K, V> cache;

  /**
   * Creates a new cache instance.
   *
   * @param manager the cache manager responsible for eviction propagation
   * @param name the logical name of this cache (used in eviction events)
   * @param keyType the runtime type of the cache key
   * @param cache the underlying Ehcache instance
   * @throws NullPointerException if any argument is {@code null}
   */
  public CacheImpl(
      CacheManager manager, String name, Class<K> keyType, org.ehcache.Cache<K, V> cache) {

    this.manager = manager;
    this.name = name;
    this.keyType = keyType;
    this.cache = cache;
  }

  /**
   * Retrieves a value from the local Ehcache.
   *
   * <p>This method does not interact with the {@link CacheManager} and does not trigger any
   * invalidation behavior.
   *
   * @param key the cache key
   * @return the cached value, or {@code null} if not present
   */
  @Override
  public V get(K key) {
    return cache.get(key);
  }

  /**
   * Stores a value in the local Ehcache.
   *
   * <p>This operation is local only and does not generate invalidation events.
   *
   * @param key the cache key
   * @param value the value to store
   */
  @Override
  public void put(K key, V value) {
    cache.put(key, value);
  }

  /**
   * Removes a key from the cache and publishes an eviction event.
   *
   * <p>This method represents an authoritative eviction and must be used when the current node
   * initiates the removal.
   *
   * <p>It performs the following steps:
   *
   * <ol>
   *   <li>Removes the key from the local Ehcache
   *   <li>Publishes an eviction event via the {@link CacheManager}
   * </ol>
   *
   * @param key the key to evict
   */
  @Override
  public void evict(K key) {
    remove(key);
    manager.fireEvictionEvent(name, key);
  }

  /**
   * Removes a key from the local Ehcache without publishing an eviction event.
   *
   * <p>This method is intended to be used when processing eviction events that originated from
   * other nodes. Using this method avoids infinite invalidation loops.
   *
   * <p>The parameter is typed as {@code Object} to allow integration with generic event payloads
   * and deserialization pipelines.
   *
   * @param key the key to remove
   * @throws ClassCastException if the key is not of type {@code K}
   */
  @SuppressWarnings("unchecked")
  public void remove(Object key) {
    cache.remove((K) key);
  }

  /**
   * Returns the runtime key type of this cache.
   *
   * @return the key class
   */
  @Override
  public Class<K> getKeyType() {
    return keyType;
  }
}
