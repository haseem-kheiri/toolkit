/*
 * Copyright 2020–2025 Haseem Kheiri
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

import com.tsh.toolkit.cluster.impl.Cluster.ClusterNode;
import com.tsh.toolkit.cluster.impl.Cluster.ClusterNodeSnapshot;
import com.tsh.toolkit.cluster.impl.Cluster.NodeHeartbeat;
import com.tsh.toolkit.cluster.impl.Cluster.NodeIdentity;
import com.tsh.toolkit.cluster.impl.Cluster.NodeType;
import com.tsh.toolkit.cluster.repository.ClusterRepository;
import com.tsh.toolkit.cluster.repository.impl.EtcdClusterRepository;
import com.tsh.toolkit.codec.messagepack.impl.MessagePackCodec;
import com.tsh.toolkit.container.etcd.EtcdTestContainer;
import com.tsh.toolkit.container.etcd.EtcdTestContainerFactory;
import com.tsh.toolkit.core.utils.Threads;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = ClusterTest.TestConfig.class)
class ClusterTest {

  @Getter
  @Setter
  static class TestClusterNodeState extends ClusterNodeState {
    private Map<String, String> attributes;
  }

  static class TestCluster extends Cluster<TestClusterNodeState> {

    public TestCluster(
        String name,
        ClusterProperties properties,
        ClusterRepository<TestClusterNodeState> repository) {
      super(name, properties, repository);
    }

    @Override
    protected TestClusterNodeState initNodeState(ClusterNode localNode) {
      return new TestClusterNodeState();
    }
  }

  @Configuration
  static class TestConfig {
    @Bean(destroyMethod = "stop")
    EtcdTestContainer etcdTestContainer() {
      final EtcdTestContainer container =
          new EtcdTestContainerFactory()
              .setDockerImageName("quay.io/coreos/etcd:v3.5.13")
              .setClientPort(2379)
              .setDataDir("/tmp/etcd-data")
              .withEnv("ETCD_ENABLE_V2", "true")
              .enableReuse(true)
              .build();
      container.start();
      return container;
    }
  }

  static class BadRepository implements ClusterRepository<TestClusterNodeState> {
    @Override
    public List<NodeHeartbeat> pushHeartbeats(
        String clusterName, NodeHeartbeat localHeartbeat, long ttlMs) {
      throw new RuntimeException();
    }

    @Override
    public void close() {}

    @Override
    public void persist(ClusterNode localNode, TestClusterNodeState state) {}

    @Override
    public void getState(ClusterNode localNode) {}
  }

  @Autowired private EtcdTestContainer container;

  @Test
  void testCreateClusterBlankOrNullName() {
    Assertions.assertEquals(
        "Cannot create cluster. Name is blank.",
        Assertions.assertThrows(
                IllegalArgumentException.class, () -> new TestCluster("", null, null))
            .getLocalizedMessage());
    Assertions.assertEquals(
        "Cannot create cluster. Name is blank.",
        Assertions.assertThrows(
                IllegalArgumentException.class, () -> new TestCluster(null, null, null))
            .getLocalizedMessage());
  }

  @Test
  void testCreateClusterNullProperties() {
    Assertions.assertEquals(
        "Cluster properties must not be null.",
        Assertions.assertThrows(
                IllegalArgumentException.class, () -> new TestCluster("bad", null, null))
            .getLocalizedMessage());
  }

  @Test
  void testCreateClusterNullRespository() {
    Assertions.assertEquals(
        "Cluster repository must not be null.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new TestCluster("bad", new ClusterProperties(), null))
            .getLocalizedMessage());
  }

  @Test
  void testEphemeralSessionIdChange() {
    final ClusterProperties properties = new ClusterProperties();

    try (final Cluster<TestClusterNodeState> cluster =
        new TestCluster("bad-cluster", properties, new BadRepository())) {
      cluster.start();
      final ClusterNode node = cluster.createNode("nodeId");
      UUID sessionId = node.getEphemeralSessionId();
      Threads.sleep(5, TimeUnit.SECONDS);

      ClusterNodeSnapshot localNodeSnapshot = cluster.getLocalNodeSnapshot();
      Assertions.assertEquals(sessionId, localNodeSnapshot.getEphemeralSessionId());
      Threads.sleep(12, TimeUnit.SECONDS);

      localNodeSnapshot = cluster.getLocalNodeSnapshot();
      Assertions.assertNotEquals(sessionId, localNodeSnapshot.getEphemeralSessionId());
    }
  }

  @Test
  void testCluster() {
    final long ts = System.currentTimeMillis();
    final ClusterProperties properties = new ClusterProperties();

    final Cluster<TestClusterNodeState> cluster1 =
        createClusterWithNode(ts, properties, "testCluster", "Pod1");
    Threads.sleep(10, TimeUnit.SECONDS);

    ClusterNodeSnapshot localNodeSnapshot = cluster1.getLocalNodeSnapshot();
    Map<NodeIdentity, ClusterNodeSnapshot> remoteNodesSnapshots = cluster1.getRemoteNodesSnapshot();
    Assertions.assertNotNull(localNodeSnapshot);
    Assertions.assertEquals(0, remoteNodesSnapshots.size());

    final Cluster<TestClusterNodeState> cluster2 =
        createClusterWithNode(ts, properties, "testCluster", "Pod2");
    Threads.sleep(10, TimeUnit.SECONDS);

    localNodeSnapshot = cluster1.getLocalNodeSnapshot();
    remoteNodesSnapshots = cluster1.getRemoteNodesSnapshot();
    Assertions.assertNotNull(localNodeSnapshot);
    Assertions.assertEquals(1, remoteNodesSnapshots.size());

    final Cluster<TestClusterNodeState> cluster3 =
        createClusterWithNode(ts, properties, "testCluster", "Pod3");
    Threads.sleep(10, TimeUnit.SECONDS);

    localNodeSnapshot = cluster1.getLocalNodeSnapshot();
    remoteNodesSnapshots = cluster1.getRemoteNodesSnapshot();
    Assertions.assertNotNull(localNodeSnapshot);
    Assertions.assertEquals(2, remoteNodesSnapshots.size());

    cluster3.stop();
    Threads.sleep(10, TimeUnit.SECONDS);

    localNodeSnapshot = cluster1.getLocalNodeSnapshot();
    remoteNodesSnapshots = cluster1.getRemoteNodesSnapshot();
    Assertions.assertNotNull(localNodeSnapshot);
    Assertions.assertEquals(1, remoteNodesSnapshots.size());

    cluster2.stop();
    Threads.sleep(10, TimeUnit.SECONDS);

    localNodeSnapshot = cluster1.getLocalNodeSnapshot();
    remoteNodesSnapshots = cluster1.getRemoteNodesSnapshot();
    Assertions.assertNotNull(localNodeSnapshot);
    Assertions.assertEquals(0, remoteNodesSnapshots.size());

    try (final Cluster<TestClusterNodeState> cluster4 =
        new TestCluster(
            "testCluster",
            properties,
            new EtcdClusterRepository<>(
                properties, new MessagePackCodec(), container.getEndpoint()))) {
      cluster4.start();
      Threads.sleep(10, TimeUnit.SECONDS);

      localNodeSnapshot = cluster4.getLocalNodeSnapshot();
      remoteNodesSnapshots = cluster4.getRemoteNodesSnapshot();
      Assertions.assertNull(localNodeSnapshot);
      Assertions.assertEquals(1, remoteNodesSnapshots.size());
    }

    cluster1.stop();
  }

  @Test
  void testClusterSharedState() throws InterruptedException {
    final long ts = System.currentTimeMillis();
    final ClusterProperties properties = new ClusterProperties();

    final Cluster<TestClusterNodeState> cluster1 =
        createClusterWithNode(ts, properties, "testCluster", "Pod1");
    // cluster1.setClusterStateListener(cs -> {});

    final Cluster<TestClusterNodeState> cluster2 =
        createClusterWithNode(ts, properties, "testCluster", "Pod2");
    // cluster2.setClusterStateListener(cs -> {});

    final Cluster<TestClusterNodeState> cluster3 =
        createClusterWithNode(ts, properties, "testCluster", "Pod3");
    // cluster3.setClusterStateListener(cs -> {});

    Thread.sleep(5000);
    cluster1.persistState();
    cluster2.persistState();
    cluster3.persistState();
  }

  private Cluster<TestClusterNodeState> createClusterWithNode(
      final long ts, final ClusterProperties properties, String clusterName, String nodeId) {
    final Cluster<TestClusterNodeState> cluster =
        new TestCluster(
            clusterName,
            properties,
            new EtcdClusterRepository<TestClusterNodeState>(
                properties, new MessagePackCodec(), container.getEndpoint()));
    cluster.start();

    final ClusterNode node = cluster.createNode(nodeId);
    Assertions.assertNotNull(node);
    Assertions.assertNotNull(node.getEphemeralSessionId());

    final NodeIdentity identity = node.getIdentity();
    Assertions.assertNotNull(identity);

    Assertions.assertEquals(clusterName.toLowerCase(), identity.getClusterName());
    Assertions.assertEquals(nodeId.toLowerCase(), identity.getNodeId());
    Assertions.assertTrue(
        ts <= identity.getCreatedAt() && identity.getCreatedAt() <= System.currentTimeMillis());
    Assertions.assertEquals(NodeType.LOCAL, identity.getType());
    return cluster;
  }
}
