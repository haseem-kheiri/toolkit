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

package com.tsh.toolkit.cache.autoconfig;

import com.tsh.toolkit.cache.CacheInvalidationBus;
import com.tsh.toolkit.cache.impl.CacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that provides the default {@link CacheManager}.
 *
 * <p>The cache manager is backed by a {@link CacheInvalidationBus} and is responsible for
 * maintaining cache coherence by publishing and receiving eviction events across the cluster.
 */
@Configuration
public class CacheManagerConfiguration {

  /**
   * Creates and starts the {@link CacheManager}.
   *
   * <p>The bean is started eagerly and stopped automatically when the Spring context is shut down.
   *
   * @param invalidationBus the transport used to propagate cache evictions
   * @return a running {@code CacheManager}
   */
  @Bean(destroyMethod = "stop")
  @ConditionalOnMissingBean
  CacheManager cacheManager(CacheInvalidationBus invalidationBus) {
    final CacheManager cacheManager = new CacheManager(invalidationBus);
    cacheManager.start();
    return cacheManager;
  }
}
