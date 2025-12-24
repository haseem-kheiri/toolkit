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

package com.tsh.toolkit.lock.provide;

import com.tsh.toolkit.containers.Containers;
import com.tsh.toolkit.core.utils.Threads;
import com.tsh.toolkit.lock.impl.LockManager;
import com.tsh.toolkit.lock.impl.LockManager.LockExecutionResult;
import com.tsh.toolkit.lock.provider.impl.PostgreSqlLockProvider;
import com.tsh.toolkit.rdbms.Rdbms;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(classes = PostgreSqlLockProviderTest.TestConfig.class)
@EnableAutoConfiguration
@Slf4j
class PostgreSqlLockProviderTest {

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

    @Bean
    PostgreSqlLockProvider postgreSqlLockProvider(DataSource dataSource) {
      final PostgreSqlLockProvider postgreSqlLockProvider = new PostgreSqlLockProvider(dataSource);
      postgreSqlLockProvider.migrate("lock");
      return postgreSqlLockProvider;
    }
  }

  private static final String LOCK_NAME = "LOCK_NAME";

  @Autowired private LockManager lockManager;

  @Test
  void test() throws InterruptedException {
    final ExecutorService worker = Executors.newFixedThreadPool(2);
    final AtomicInteger val = new AtomicInteger();

    worker.execute(
        () -> {
          LockExecutionResult<Boolean> result =
              lockManager.tryLock(
                  LOCK_NAME,
                  (lock) -> {
                    val.addAndGet(5);
                    Threads.sleep(40, TimeUnit.SECONDS);
                    return true;
                  });

          log.info(
              "lock for executionId {} {}",
              result.getExecutionId(),
              result.isExecuted() ? "acquired" : "rejected");
          Assertions.assertTrue(result.getResult());
        });

    Threads.sleep(1000);

    worker.execute(
        () -> {
          long ts = System.currentTimeMillis();

          while (System.currentTimeMillis() - ts < 35000) {
            LockExecutionResult<Long> result =
                lockManager.tryLock(
                    LOCK_NAME,
                    (lock) -> {
                      val.addAndGet(-5);
                      return System.currentTimeMillis();
                    });

            log.info(
                "lock for executionId {} {}",
                result.getExecutionId(),
                result.isExecuted() ? "acquired" : "rejected");
            Assertions.assertNull(result.getResult());
            Threads.sleep(1000);
          }
        });

    worker.shutdown();
    worker.awaitTermination(60, TimeUnit.SECONDS);

    Assertions.assertEquals(5, val.get());
  }
}
