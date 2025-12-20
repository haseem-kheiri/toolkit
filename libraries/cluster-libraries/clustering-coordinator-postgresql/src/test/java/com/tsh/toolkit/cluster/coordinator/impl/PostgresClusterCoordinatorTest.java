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

package com.tsh.toolkit.cluster.coordinator.impl;

import com.tsh.toolkit.cluster.impl.Cluster;
import com.tsh.toolkit.cluster.impl.ClusterNode;
import com.tsh.toolkit.codec.json.impl.JsonCodec;
import com.tsh.toolkit.containers.Containers;
import com.tsh.toolkit.core.utils.Threads;
import com.tsh.toolkit.rdbms.Rdbms;
import java.time.Duration;
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

@SpringBootTest(classes = PostgresClusterCoordinatorTest.TestConfig.class)
@EnableAutoConfiguration
@Slf4j
class PostgresClusterCoordinatorTest {

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

  @Autowired private JsonCodec jsonCodec;
  @Autowired private DataSource dataSource;

  @Test
  void createCluster() {
    PostgresClusterCoordinator clusterCoordinator =
        new PostgresClusterCoordinator(dataSource, jsonCodec);
    clusterCoordinator.migrate("cluster");

    AtomicInteger running = new AtomicInteger();

    ClusterNode node1 =
        Cluster.join("payments-cluster")
            .asNode("payments-worker-01")
            .withMetadata("zone", "us-east-1")
            .usingCoordinator(clusterCoordinator)
            .withHeartbeatInterval(Duration.ofSeconds(1))
            .withHeartbeatTimeout(Duration.ofSeconds(3))
            .onClusterStateChanged(
                (event) -> {
                  running.set(event.getState().getNodes().size());
                })
            .start();
    Threads.sleep(3, TimeUnit.SECONDS);
    log.info("Verify condition 1.");
    Assertions.assertEquals(1, running.get());

    ClusterNode node2 =
        Cluster.join("payments-cluster")
            .asNode("payments-worker-02")
            .withMetadata("zone", "us-east-1")
            .usingCoordinator(clusterCoordinator)
            .withHeartbeatInterval(Duration.ofSeconds(1))
            .withHeartbeatTimeout(Duration.ofSeconds(3))
            .onClusterStateChanged(
                (event) -> {
                  running.set(event.getState().getNodes().size());
                })
            .start();
    Threads.sleep(3, TimeUnit.SECONDS);
    log.info("Verify condition 2.");
    Assertions.assertEquals(2, running.get());
    node1.stop();

    Threads.sleep(4, TimeUnit.SECONDS);
    log.info("Verify condition 3.");
    Assertions.assertEquals(1, running.get());
    node1.start();

    Threads.sleep(3, TimeUnit.SECONDS);
    log.info("Verify condition 4.");
    Assertions.assertEquals(2, running.get());
    node1.stop();
    node2.stop();
  }
}
