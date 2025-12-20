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

package com.tsh.toolkit.cluster;

import com.tsh.toolkit.core.utils.Uuids;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClusterStateLeaderElectorTest {

  @Test
  void test() {

    final String clusterName = "my-cluster";
    final String sessionId = Uuids.uuid7().toString();
    List<ClusterNodeState> nodes =
        List.of(
            new ClusterNodeState(clusterName, sessionId, System.currentTimeMillis(), ""),
            new ClusterNodeState(
                clusterName, Uuids.uuid7().toString(), System.currentTimeMillis(), ""));
    ClusterState state = new ClusterState(clusterName, nodes);
    ClusterState oldState = null;
    ClusterStateChangeEvent event =
        new ClusterStateChangeEvent(clusterName, "node-0", sessionId, state, oldState);
    final ClusterStateLeaderElector e = new ClusterStateLeaderElector();
    e.onChange(event);

    Assertions.assertTrue(e.isLeader());
    Assertions.assertEquals(sessionId, e.getLeaderSessionId());

    state = new ClusterState(clusterName, List.of());
    event = new ClusterStateChangeEvent(clusterName, "node-0", sessionId, state, oldState);
    e.onChange(event);

    Assertions.assertFalse(e.isLeader());
    Assertions.assertNull(e.getLeaderSessionId());
  }
}
