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

class ClusterStateTest {

  @Test
  void test() {
    Assertions.assertEquals(
        "cluster name must not be blank.",
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ClusterState(null, null))
            .getLocalizedMessage());

    String clusterName = "my-cluster";
    Assertions.assertEquals(
        "nodes must not be null.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                  new ClusterState(clusterName, null);
                })
            .getLocalizedMessage());

    final String sessionId = Uuids.uuid7().toString();
    ClusterState s =
        new ClusterState(
            clusterName,
            List.of(new ClusterNodeState(clusterName, sessionId, System.currentTimeMillis(), "")));

    Assertions.assertEquals(1, s.size());
    Assertions.assertTrue(s.containsSession(sessionId));
    Assertions.assertFalse(s.containsSession(Uuids.uuid7().toString()));
  }
}
