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

package com.tsh.toolkit.cache.invalidate.autoconfig;

import com.tsh.toolkit.cache.CacheInvalidationBus;
import com.tsh.toolkit.cache.invalidate.impl.PostgresCacheInvalidationBus;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that provides a PostgreSQL-based {@link CacheInvalidationBus}.
 *
 * <p>This configuration creates a {@link PostgresCacheInvalidationBus} that uses a PostgreSQL
 * database as a transport for publishing and receiving cache eviction events across cluster nodes.
 *
 * <p>The bus relies on database-backed messaging (typically via tables, triggers, or notifications)
 * to ensure that cache invalidation events are reliably delivered to all participating nodes.
 */
@Configuration
public class PostgresCacheInvalidationBusConfiguration {

  /**
   * Creates the PostgreSQL-backed {@link PostgresCacheInvalidationBus}.
   *
   * <p>The provided {@link DataSource} is used both to publish invalidation events and to listen
   * for eviction notifications from other nodes.
   *
   * <p>The bus automatically runs its required schema migrations on startup using the {@code
   * cache_inv_bus} schema to ensure the required tables, indexes, and database objects are present.
   *
   * @param dataSource the PostgreSQL data source used by the invalidation bus
   * @return a fully initialized {@code PostgresCacheInvalidationBus}
   */
  @Bean
  @ConditionalOnMissingBean
  PostgresCacheInvalidationBus postgresCacheInvalidationBus(DataSource dataSource) {
    final PostgresCacheInvalidationBus bus = new PostgresCacheInvalidationBus(dataSource);
    bus.migrate("cache_inv_bus");
    return bus;
  }
}
