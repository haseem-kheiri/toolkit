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
import com.tsh.toolkit.cluster.ClusterStateChangeListener;
import com.tsh.toolkit.core.utils.Check;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Cluster node builder. */
public class ClusterNodeBuilder {

  private String clusterName;
  private String nodeId;
  private Map<String, String> metadata = new HashMap<>();
  private ClusterCoordinator clusterCoordinator;
  private Duration heartbeatInterval;
  private Duration heartbeatTimeout;
  private ClusterStateChangeListener clusterStateChangeListerner;

  public ClusterNodeBuilder(String clusterName) {
    this.clusterName = Check.requireNotBlank(clusterName, () -> "cluster name must not be blank.");
  }

  public ClusterNodeBuilder asNode(String nodeId) {
    this.nodeId = Check.requireNotBlank(nodeId, () -> "node id must not be blank.");
    return this;
  }

  /** sets metadata. */
  public ClusterNodeBuilder withMetadata(String key, String value) {
    this.metadata.put(
        Check.requireNotBlank(key, () -> "metadata key must not be blank."),
        Check.requireNotNull(value, () -> "metadata value must not be null."));
    return this;
  }

  /** sets coordinator. */
  public ClusterNodeBuilder usingCoordinator(ClusterCoordinator clusterCoordinator) {
    this.clusterCoordinator =
        Check.requireNotNull(clusterCoordinator, () -> "cluster coordinator must not be null.");
    return this;
  }

  /** set heartbeat interval. */
  public ClusterNodeBuilder withHeartbeatInterval(Duration heartbeatInterval) {
    this.heartbeatInterval =
        Check.requireTrue(
            () -> heartbeatInterval,
            hb -> hb != null && hb.toMillis() >= 1000,
            () -> "heartbeat interval must not be null or less than 1 second.");
    return this;
  }

  /** sets heartbeat timeout. */
  public ClusterNodeBuilder withHeartbeatTimeout(Duration heartbeatTimeout) {
    this.heartbeatTimeout =
        Check.requireNotNull(heartbeatTimeout, () -> "heartbeat timeout must not be null.");
    return this;
  }

  public ClusterNodeBuilder onClusterStateChanged(
      ClusterStateChangeListener clusterStateChangeListerner) {
    this.clusterStateChangeListerner = clusterStateChangeListerner;
    return this;
  }

  /** start the node. */
  public ClusterNode start() {
    ClusterNode node =
        new ClusterNode(
            nodeId,
            clusterName,
            clusterCoordinator,
            heartbeatInterval,
            heartbeatTimeout,
            metadata,
            clusterStateChangeListerner);

    node.start();
    return node;
  }
}
