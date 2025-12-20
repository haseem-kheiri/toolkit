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

package com.tsh.toolkit.cluster.impl;

import com.tsh.toolkit.cluster.ClusterCoordinator;
import com.tsh.toolkit.cluster.ClusterNodeState;
import com.tsh.toolkit.cluster.ClusterState;
import com.tsh.toolkit.core.utils.Threads;
import com.tsh.toolkit.core.utils.Uuids;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClusterTest {

  @Test
  void test() {
    final String clusterName = "payments-cluster";
    final Duration heartbeatTimeout = Duration.ofSeconds(3);

    final ClusterCoordinator clusterCoordinator =
        new ClusterCoordinator() {

          @Override
          public ClusterState participateAndObserve(
              String clusterName,
              String sessionId,
              Map<String, String> metadata,
              long heartbeatTimeoutMillis) {

            return new ClusterState(
                clusterName,
                List.of(
                    new ClusterNodeState(
                        clusterName, Uuids.uuid7().toString(), heartbeatTimeout.toMillis(), "")));
          }
        };

    AtomicInteger running = new AtomicInteger();

    try (ClusterNode node1 =
        Cluster.join(clusterName)
            .asNode("payments-worker-01")
            .withMetadata("zone", "us-east-1")
            .usingCoordinator(clusterCoordinator)
            .withHeartbeatInterval(Duration.ofSeconds(1))
            .withHeartbeatTimeout(heartbeatTimeout)
            .onClusterStateChanged(
                (event) -> {
                  running.set(event.getState().getNodes().size());
                })
            .start()) {
      Threads.sleep(3, TimeUnit.SECONDS);
      Assertions.assertEquals(1, running.get());
    }
  }
}
