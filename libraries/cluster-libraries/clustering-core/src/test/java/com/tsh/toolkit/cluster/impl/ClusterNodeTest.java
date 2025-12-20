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

import com.tsh.toolkit.cluster.coordinator.impl.PostgresClusterCoordinator;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClusterNodeTest {

  @Test
  void test() {
    Assertions.assertEquals(
        "node id must not be blank.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ClusterNode(null, null, null, null, null, null, null))
            .getLocalizedMessage());

    String nodeId = "node-0";
    Assertions.assertEquals(
        "cluster name must not be blank.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ClusterNode(nodeId, null, null, null, null, null, null))
            .getLocalizedMessage());

    String clusterName = "clusterName";
    Assertions.assertEquals(
        "cluster coordinator must not be null.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ClusterNode(nodeId, clusterName, null, null, null, null, null))
            .getLocalizedMessage());

    Assertions.assertEquals(
        "heartbeat interval must be at least 1 second.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                    new ClusterNode(
                        nodeId,
                        clusterName,
                        new PostgresClusterCoordinator(null, null),
                        null,
                        null,
                        null,
                        null))
            .getLocalizedMessage());

    Assertions.assertEquals(
        "heartbeat interval must be at least 1 second.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                    new ClusterNode(
                        nodeId,
                        clusterName,
                        new PostgresClusterCoordinator(null, null),
                        Duration.ofMillis(1),
                        null,
                        null,
                        null))
            .getLocalizedMessage());

    Assertions.assertEquals(
        "heartbeat timeout must be at least three times the heartbeat interval.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                    new ClusterNode(
                        nodeId,
                        clusterName,
                        new PostgresClusterCoordinator(null, null),
                        Duration.ofMillis(1000),
                        null,
                        null,
                        null))
            .getLocalizedMessage());

    Assertions.assertEquals(
        "heartbeat timeout must be at least three times the heartbeat interval.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                    new ClusterNode(
                        nodeId,
                        clusterName,
                        new PostgresClusterCoordinator(null, null),
                        Duration.ofMillis(1000),
                        Duration.ofMillis(1000),
                        null,
                        null))
            .getLocalizedMessage());

    Assertions.assertEquals(
        "metadata must not be null.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                    new ClusterNode(
                        nodeId,
                        clusterName,
                        new PostgresClusterCoordinator(null, null),
                        Duration.ofMillis(1000),
                        Duration.ofMillis(3000),
                        null,
                        null))
            .getLocalizedMessage());

    ClusterNode n =
        new ClusterNode(
            nodeId,
            clusterName,
            new PostgresClusterCoordinator(null, null),
            Duration.ofMillis(1000),
            Duration.ofMillis(3000),
            Map.of(),
            null);

    Assertions.assertEquals(clusterName, n.getClusterName());
    Assertions.assertEquals(nodeId, n.getNodeId());
    Assertions.assertTrue(n.isHealthy());
  }
}
