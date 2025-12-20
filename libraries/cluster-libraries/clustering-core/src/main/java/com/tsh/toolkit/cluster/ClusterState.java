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

import com.tsh.toolkit.core.utils.Check;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;

/**
 * Immutable snapshot of the membership state of a cluster at a specific observation point.
 *
 * <p>A {@code ClusterState} represents a <em>view</em> of the cluster, typically obtained via a
 * single participate-and-observe operation. It is not a live or self-updating structure.
 *
 * <p><strong>Semantic model:</strong>
 *
 * <ul>
 *   <li>Cluster identity is defined by {@code clusterName}.
 *   <li>Membership is a <em>set</em> of {@link ClusterNodeState} instances.
 *   <li>Ordering of nodes is not semantically significant.
 * </ul>
 *
 * <p>This class is fully immutable and thread-safe.
 */
@Getter
public final class ClusterState {

  /** Logical identifier of the cluster. */
  private final String clusterName;

  /**
   * Immutable set of nodes observed to be members of the cluster.
   *
   * <p>Each node represents a distinct {@code (clusterName, sessionId)} identity.
   */
  private final Set<ClusterNodeState> nodes;

  /**
   * Creates a new immutable cluster state snapshot.
   *
   * @param clusterName logical cluster identifier; must not be blank
   * @param nodes collection of observed cluster nodes; must not be {@code null}
   */
  public ClusterState(String clusterName, List<ClusterNodeState> nodes) {
    this.clusterName = Check.requireNotBlank(clusterName, () -> "cluster name must not be blank.");

    Check.requireNotNull(nodes, () -> "nodes must not be null.");
    this.nodes = Set.copyOf(nodes);
  }

  /**
   * Indicates whether the given session identifier is present in this cluster state snapshot.
   *
   * @param sessionId session identifier to check
   * @return {@code true} if a node with the given session id is present
   */
  public boolean containsSession(String sessionId) {
    return nodes.stream().anyMatch(n -> n.getSessionId().equals(sessionId));
  }

  /**
   * Returns the number of nodes in the cluster snapshot.
   *
   * @return cluster size
   */
  public int size() {
    return nodes.size();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ClusterState other = (ClusterState) obj;
    return Objects.equals(clusterName, other.clusterName) && Objects.equals(nodes, other.nodes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clusterName, nodes);
  }
}
