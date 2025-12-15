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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;

class NodeIdentityTest {

  @Test
  void testEqualsReflexive() {
    Cluster.NodeIdentity id1 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.LOCAL);
    assertEquals(id1, id1, "Object must be equal to itself");
  }

  @Test
  void testEqualsSymmetric() {
    Cluster.NodeIdentity id1 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.LOCAL);
    Cluster.NodeIdentity id2 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.REMOTE);

    assertEquals(id1, id2);
    assertEquals(id2, id1);
  }

  @Test
  void testEqualsTransitive() {
    Cluster.NodeIdentity id1 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.LOCAL);
    Cluster.NodeIdentity id2 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.REMOTE);
    Cluster.NodeIdentity id3 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.REMOTE);

    assertEquals(id1, id2);
    assertEquals(id2, id3);
    assertEquals(id1, id3);
  }

  @Test
  void testEqualsConsistent() {
    Cluster.NodeIdentity id1 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.LOCAL);
    Cluster.NodeIdentity id2 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.REMOTE);

    for (int i = 0; i < 10; i++) {
      assertEquals(id1, id2);
    }
  }

  @Test
  void testNotEqualsToNull() {
    Cluster.NodeIdentity id1 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.LOCAL);
    assertNotEquals(null, id1);
  }

  @Test
  void testNotEqualsDifferentClusterName() {
    Cluster.NodeIdentity id1 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.LOCAL);
    Cluster.NodeIdentity id2 =
        new Cluster.NodeIdentity("clusterB", "node1", Cluster.NodeType.LOCAL);

    assertNotEquals(id1, id2);
  }

  @Test
  void testNotEqualsDifferentNodeId() {
    Cluster.NodeIdentity id1 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.LOCAL);
    Cluster.NodeIdentity id2 =
        new Cluster.NodeIdentity("clusterA", "node2", Cluster.NodeType.LOCAL);

    assertNotEquals(id1, id2);
  }

  @Test
  void testHashCodeConsistentWithEquals() {
    Cluster.NodeIdentity id1 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.LOCAL);
    Cluster.NodeIdentity id2 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.REMOTE);

    assertEquals(id1.hashCode(), id2.hashCode(), "Equal objects must have equal hashCodes");
  }

  @Test
  void testHashCodeConsistency() {
    Cluster.NodeIdentity id1 =
        new Cluster.NodeIdentity("clusterA", "node1", Cluster.NodeType.LOCAL);
    int initialHash = id1.hashCode();
    for (int i = 0; i < 5; i++) {
      assertEquals(initialHash, id1.hashCode(), "hashCode must remain consistent across calls");
    }
  }
}
