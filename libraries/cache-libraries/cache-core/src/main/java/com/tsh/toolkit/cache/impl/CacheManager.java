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
import com.tsh.toolkit.cache.CacheConfig;
import com.tsh.toolkit.cache.CacheInvalidationBus;
import com.tsh.toolkit.codec.json.impl.JsonUtils;
import com.tsh.toolkit.core.lifecycle.impl.AbstractLifecycleObject;
import com.tsh.toolkit.core.utils.Check;
import com.tsh.toolkit.core.utils.ThreadPools;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

/**
 * Central coordinator for all local caches and their distributed invalidation.
 *
 * <p>The {@code CacheManager} is responsible for:
 *
 * <ul>
 *   <li>creating and registering named caches
 *   <li>owning the Ehcache backing store
 *   <li>publishing local eviction events
 *   <li>consuming remote eviction events
 * </ul>
 *
 * <p>When configured with a {@link CacheInvalidationBus}, the manager participates in a
 * cluster-wide cache coherency protocol using time-ordered eviction events.
 *
 * <p>The design follows a <b>log-based invalidation model</b>:
 *
 * <ul>
 *   <li>Local evictions append to an event log
 *   <li>Remote evictions are replayed from the bus
 *   <li>Each node converges independently
 * </ul>
 *
 * <p>The manager is a lifecycle-bound component; distributed synchronization starts on {@link
 * #onStart()} and stops on {@link #onStop()}.
 */
public class CacheManager extends AbstractLifecycleObject {

  /**
   * Immutable representation of a cache eviction.
   *
   * <p>Each event represents the authoritative removal of a single key from a named cache at a
   * specific point in time.
   *
   * <p>The key is serialized as JSON to allow transport across heterogeneous runtimes and
   * deserialization back into the appropriate cache key type.
   */
  public static record EvictionEvent(String cacheName, String key, OffsetDateTime recordedAt) {

    /**
     * Creates an eviction event without a timestamp.
     *
     * <p>This constructor is used when publishing events locally. The {@link CacheInvalidationBus}
     * is responsible for assigning the authoritative {@code recordedAt} timestamp.
     */
    public EvictionEvent(String cacheName, String cacheKey) {
      this(cacheName, cacheKey, null);
    }
  }

  /**
   * Strategy for mapping a cache entry into a string key representation.
   *
   * <p>This abstraction allows applications to control how complex keys are serialized when
   * participating in eviction propagation.
   */
  @FunctionalInterface
  public interface CacheKeyMapper<K, V> {
    String map(K key, V value);
  }

  /**
   * Fluent builder for creating and registering caches.
   *
   * <p>Each cache:
   *
   * <ul>
   *   <li>is globally named
   *   <li>is backed by Ehcache
   *   <li>is automatically registered with this manager
   *   <li>participates in invalidation if a bus is configured
   * </ul>
   */
  public class CacheBuilder<K, V> {
    private final CacheManager manager;
    private final String name;
    private Class<K> keyType;
    private Class<V> valueType;
    private CacheConfig config;

    public CacheBuilder(CacheManager manager, String name) {
      this.manager = manager;
      this.name = name;
    }

    /**
     * Defines the runtime key and value types of the cache.
     *
     * <p>The key type is required for deserializing eviction events.
     */
    public CacheBuilder<K, V> ofTypes(Class<K> keyType, Class<V> valueType) {
      this.keyType = keyType;
      this.valueType = valueType;

      return this;
    }

    /** Configures TTL and size limits for the cache. */
    public CacheBuilder<K, V> config(Consumer<CacheConfig> consumer) {
      if (consumer != null) {
        this.config = new CacheConfig();
        consumer.accept(this.config);
      }
      return this;
    }

    /**
     * Builds and registers the cache.
     *
     * <p>This method creates the Ehcache instance, wraps it in {@link CacheImpl}, and registers it
     * with the manager.
     *
     * @return the constructed cache
     */
    public Cache<K, V> build() {
      Check.requireNotBlank(name, () -> "name must not be blank.");
      Check.requireNotNull(keyType, () -> "key type must not be null.");
      Check.requireNotNull(valueType, () -> "value type must not be null.");
      Check.requireNotNull(config, () -> "config must not be null.");

      return manager.register(
          name,
          new CacheImpl<>(
              manager,
              name,
              keyType,
              cacheManager.createCache(
                  name,
                  CacheConfigurationBuilder.newCacheConfigurationBuilder(
                          keyType, valueType, ResourcePoolsBuilder.heap(config.getMaxSize()))
                      .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(config.getTtl()))
                      .build())));
    }
  }

  private final org.ehcache.CacheManager cacheManager =
      CacheManagerBuilder.newCacheManagerBuilder().build(true);
  private final Map<String, CacheImpl<?, ?>> caches = new ConcurrentHashMap<>();
  private final CacheInvalidationBus cacheInvalidationBus;
  private final List<EvictionEvent> evictionEvents =
      Collections.synchronizedList(new ArrayList<>());
  private ExecutorService workers;
  private OffsetDateTime lastPollAt;

  public CacheManager() {
    this(null);
  }

  public CacheManager(CacheInvalidationBus cacheInvalidationBus) {
    this.cacheInvalidationBus = cacheInvalidationBus;
  }

  /**
   * Registers a cache with this manager.
   *
   * <p>Cache names must be globally unique within the manager.
   */
  private <K, V> Cache<K, V> register(String name, CacheImpl<K, V> cacheImpl) {
    Check.requireFalse(
        caches.containsKey(name),
        () -> String.format("Cache already registered user name %s.", name));
    caches.put(name, cacheImpl);
    return cacheImpl;
  }

  /**
   * Starts the distributed invalidation workers.
   *
   * <p>Two background loops are launched:
   *
   * <ul>
   *   <li>one to publish locally generated eviction events
   *   <li>one to poll and apply remote eviction events
   * </ul>
   */
  @Override
  protected void onStart() {
    if (cacheInvalidationBus != null) {
      lastPollAt = cacheInvalidationBus.getNow();
      workers = Executors.newFixedThreadPool(2);
      ThreadPools.execute(() -> publishEvents(), () -> isRunning(), workers);
      ThreadPools.execute(() -> pollForEvents(), () -> isRunning(), workers);
    }
  }

  /** Stops background workers and waits for termination. */
  @Override
  protected void onStop() {
    if (cacheInvalidationBus != null) {
      ThreadPools.terminate(workers, 15, TimeUnit.SECONDS);
    }
  }

  /**
   * Polls the invalidation bus for new eviction events and applies them to local caches.
   *
   * <p>Events are applied idempotently using {@link CacheImpl#remove(Object)} to avoid feedback
   * loops.
   *
   * <p>The {@code lastPollAt} timestamp acts as a replay cursor.
   */
  private void pollForEvents() {
    whileUp(
        (running) -> {
          final List<String> names = new ArrayList<>(Set.copyOf(caches.keySet()));
          if (!names.isEmpty()) {
            List<EvictionEvent> events = cacheInvalidationBus.pollEvents(names, lastPollAt);
            if (!events.isEmpty()) {
              for (EvictionEvent event : events) {
                final CacheImpl<?, ?> cache = caches.get(event.cacheName());
                cache.remove(JsonUtils.map(om -> om.readValue(event.key(), cache.getKeyType())));

                final OffsetDateTime r = event.recordedAt();
                if (r.compareTo(lastPollAt) > 0) {
                  lastPollAt = r;
                }
              }
            }
          }
        },
        2,
        TimeUnit.SECONDS);
  }

  /**
   * Publishes locally generated eviction events to the invalidation bus.
   *
   * <p>Events are buffered and retried on failure to guarantee at-least-once delivery.
   */
  private void publishEvents() {
    whileUp(
        (running) -> {
          final List<EvictionEvent> events = new ArrayList<>();
          synchronized (evictionEvents) {
            events.addAll(evictionEvents);
            evictionEvents.clear();
          }

          if (!events.isEmpty()) {
            try {
              cacheInvalidationBus.publishEviction(events);
            } catch (Exception e) {
              evictionEvents.addAll(events);
            }
          }
        },
        2,
        TimeUnit.SECONDS);
  }

  /** Creates a new cache builder for the given cache name. */
  public <K, V> CacheBuilder<K, V> create(String name) {
    return new CacheBuilder<>(this, name);
  }

  /**
   * Records a local eviction so it can be propagated cluster-wide.
   *
   * <p>This method is invoked by {@link CacheImpl#evict(Object)}.
   */
  public <K> void fireEvictionEvent(String cacheName, K key) {
    evictionEvents.add(new EvictionEvent(cacheName, JsonUtils.stringify(key)));
  }
}
