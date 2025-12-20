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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

class ClusterNodeHealthIndicatorTest {

  @Test
  void testHealthy() {
    ClusterNode node = mock(ClusterNode.class);
    when(node.isHealthy()).thenReturn(true);

    ClusterNodeHealthIndicator hi = new ClusterNodeHealthIndicator(node);

    Assertions.assertEquals(Status.UP, hi.health().getStatus());
  }

  @Test
  void testUnhealthy() {
    ClusterNode node = mock(ClusterNode.class);
    when(node.isHealthy()).thenReturn(false);
    when(node.getClusterName()).thenReturn("my-cluster");

    ClusterNodeHealthIndicator hi = new ClusterNodeHealthIndicator(node);
    Assertions.assertEquals(Status.DOWN, hi.health().getStatus());
  }
}
