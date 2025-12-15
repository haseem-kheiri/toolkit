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

package com.tsh.toolkit.cluster.repository;

import com.tsh.toolkit.cluster.impl.Cluster.ClusterNode;
import com.tsh.toolkit.cluster.impl.Cluster.NodeHeartbeat;
import com.tsh.toolkit.cluster.impl.ClusterNodeState;
import java.util.List;

/**
 * Repository abstraction for managing cluster membership and node state.
 *
 * <p>A {@code ClusterRepository} provides persistence and coordination for cluster-related
 * operations such as publishing heartbeats, persisting node state, and retrieving shared state
 * across nodes. Implementations may back this interface with systems like etcd, Zookeeper,
 * databases, or any other coordination service.
 *
 * @param <T> the type of the persisted state object associated with each node
 */
public interface ClusterRepository<T extends ClusterNodeState> extends AutoCloseable {

  /**
   * Publishes the local node’s heartbeat and retrieves all active heartbeats within the same
   * cluster.
   *
   * <p>This operation typically performs two actions atomically:
   *
   * <ol>
   *   <li>Registers or refreshes the local node’s heartbeat with the specified TTL.
   *   <li>Fetches and returns all currently active heartbeats in the cluster, including the local
   *       node.
   * </ol>
   *
   * <p>The TTL defines how long the node’s heartbeat remains visible before it expires. Nodes must
   * periodically re-publish their heartbeats to maintain presence in the cluster.
   *
   * @param clusterName the logical name of the cluster to which the node belongs
   * @param localHeartbeat the local node’s heartbeat information to store
   * @param ttlMs the time-to-live (in milliseconds) for the heartbeat entry before expiration
   * @return a list of all active {@link NodeHeartbeat} entries in the specified cluster
   * @throws IllegalArgumentException if any parameter is invalid
   * @throws Exception if persistence or retrieval fails
   */
  List<NodeHeartbeat> pushHeartbeats(String clusterName, NodeHeartbeat localHeartbeat, long ttlMs);

  /** Closes this repository and releases any underlying resources. */
  void close();

  /**
   * Persists the given node’s application-level state.
   *
   * <p>Implementations may use this to store transient or durable data related to the node (e.g.,
   * leader information, workload assignments, or health metrics).
   *
   * @param localNode the local cluster node whose state should be persisted
   * @param state the node-specific state object to persist
   */
  void persist(ClusterNode localNode, T state);

  /**
   * Retrieves or refreshes cluster-wide state for the specified node.
   *
   * <p>This method is invoked periodically to check for configuration or coordination changes
   * independent of heartbeat publishing.
   *
   * @param localNode the local node requesting the latest state view
   */
  void getState(ClusterNode localNode);
}
