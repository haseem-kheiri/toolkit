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

import com.tsh.toolkit.cache.Cache;
import com.tsh.toolkit.cache.impl.CacheManager;
import com.tsh.toolkit.containers.Containers;
import com.tsh.toolkit.core.utils.Threads;
import com.tsh.toolkit.rdbms.Rdbms;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(classes = PostgresCacheInvalidationBusTest.TestConfig.class)
@EnableAutoConfiguration
class PostgresCacheInvalidationBusTest {

  @Configuration
  static class TestConfig {
    final String userName = "testuser";
    final String password = "testpassword";

    @Bean(destroyMethod = "stop")
    PostgreSQLContainer<?> postgreSQLContainer() {
      PostgreSQLContainer<?> postgreSql =
          Containers.postgreSqlFactory()
              .setDockeImageName("postgres:16")
              .setDatabaseName("platform")
              .setUserName(userName)
              .setPassword(password)
              .build();
      postgreSql.start();
      return postgreSql;
    }

    @Bean(destroyMethod = "close")
    DataSource dataSource(PostgreSQLContainer<?> postgreSql) {
      return Rdbms.hikariCpDatasourceFactory()
          .setJdbcUrl(postgreSql.getJdbcUrl())
          .setUsername(userName)
          .setPassword(password)
          .setMaximumPoolSize(3)
          .build();
    }
  }

  @Autowired private CacheManager cacheManager;

  @Test
  void test() {
    final Cache<String, String> cache =
        cacheManager
            .<String, String>create("test-cache")
            .ofTypes(String.class, String.class)
            .config(c -> c.maxSize(5).ttl(Duration.ofMinutes(5)))
            .build();

    Assertions.assertNull(cache.get("key"));
    cache.put("key", "value");
    Assertions.assertEquals("value", cache.get("key"));

    cache.evict("key");
    Assertions.assertNull(cache.get("key"));
    Threads.sleep(5, TimeUnit.SECONDS);
  }
}
