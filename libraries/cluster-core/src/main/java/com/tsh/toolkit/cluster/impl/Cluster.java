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

import com.tsh.toolkit.cluster.repository.ClusterRepository;
import com.tsh.toolkit.core.lifecycle.impl.AbstractLifecycleObject;
import com.tsh.toolkit.core.utils.Check;
import com.tsh.toolkit.core.utils.ThreadPools;
import com.tsh.toolkit.core.utils.Threads;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a logical cluster of nodes.
 *
 * <p>A {@code Cluster} manages exactly one local node and multiple remote nodes. Each node has:
 *
 * <ul>
 *   <li>a stable identity ({@link NodeIdentity}) that uniquely identifies it across its lifetime;
 *   <li>an ephemeral session identifier ({@link ClusterNode#ephemeralSessionId}) that changes
 *       whenever the node is reincarnated;
 *   <li>optional node-local state ({@link NodeState}) propagated in {@link NodeHeartbeat} for
 *       leader monitoring or workload distribution.
 * </ul>
 *
 * <p>The cluster guarantees uniqueness of nodes by their stable {@code nodeId}. It periodically
 * emits heartbeats for the single local node using a background executor.
 */
@Slf4j
public abstract class Cluster<T extends ClusterNodeState> extends AbstractLifecycleObject {

  /**
   * Snapshot of a node's heartbeat.
   *
   * <p>Contains the cluster name, stable node ID, ephemeral session ID, and optional node-local
   * state. Heartbeats are used by the leader or monitoring system to track liveness and workload
   * distribution.
   */
  public record NodeHeartbeat(
      String clusterName, String nodeId, UUID ephemeralSessionId, NodeState state) {}

  /**
   * Immutable node-local state propagated in heartbeats.
   *
   * <p>Represents metrics or attributes shared with the leader. Thread-safe and immutable.
   */
  @Getter
  public static class NodeState {
    private final Map<String, Object> metrics;

    public NodeState(Map<String, Object> metrics) {
      this.metrics = Map.copyOf(metrics); // immutable copy
    }
  }

  /** Type of node in the cluster. */
  public enum NodeType {
    /** Node created within this cluster instance. */
    LOCAL,
    /** Node discovered from an external source. */
    REMOTE
  }

  /**
   * Stable identity of a cluster node.
   *
   * <p>The {@link #nodeId} uniquely identifies a node across its lifetime. {@link #createdAt}
   * records when the identity was first created. {@link #type} indicates whether the node is local
   * or remote. {@link #clusterName} associates the node with its cluster.
   *
   * <p>Two {@code NodeIdentity} instances are considered equal if they share the same {@code
   * nodeId}.
   */
  @Getter
  public static class NodeIdentity {
    private final long createdAt = System.currentTimeMillis();
    private final String clusterName;
    private final String nodeId;
    private final NodeType type;
    private final int hashCode;

    /**
     * Creates a new {@link NodeIdentity} for a cluster node.
     *
     * <p>A {@code NodeIdentity} represents the stable identity of a node across its lifetime. It
     * consists of the cluster name, a unique node identifier, and the node type (local or remote).
     * The {@code nodeId} is normalized to lowercase for consistency.
     *
     * @param clusterName the name of the cluster this node belongs to; must not be blank
     * @param nodeId the unique identifier of the node within the cluster; must not be blank, and
     *     will be converted to lowercase
     * @param type the type of the node ({@link NodeType#LOCAL} or {@link NodeType#REMOTE}); must
     *     not be {@code null}
     * @throws IllegalArgumentException if {@code clusterName} or {@code nodeId} is blank
     * @throws NullPointerException if {@code type} is {@code null}
     */
    NodeIdentity(String clusterName, String nodeId, NodeType type) {
      this.clusterName =
          Check.requireNotBlank(clusterName, () -> "Cannot create node. Cluster name is blank.");
      this.nodeId =
          Check.requireNotBlank(nodeId, () -> "Cannot create node. Node id is blank.")
              .toLowerCase();
      this.type = type;
      this.hashCode = Objects.hash(clusterName, nodeId);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      NodeIdentity other = (NodeIdentity) obj;
      return Objects.equals(clusterName, other.clusterName) && Objects.equals(nodeId, other.nodeId);
    }
  }

  /**
   * Represents a node in the cluster.
   *
   * <p>Each {@code ClusterNode} has a stable identity, ephemeral session ID, and optional
   * node-local state for leader monitoring or scheduling. Its state can be updated locally and
   * exported as a heartbeat snapshot.
   */
  @Getter
  public static class ClusterNode {
    private final NodeIdentity identity;
    private volatile UUID ephemeralSessionId;
    private volatile boolean healthy;
    private volatile NodeState state;

    private ClusterNode(NodeIdentity identity, UUID ephemeralSessionId) {
      this.identity = identity;
      this.ephemeralSessionId = ephemeralSessionId;
    }

    private ClusterNode(NodeIdentity identity, UUID ephemeralSessionId, NodeState state) {
      this.identity = identity;
      this.ephemeralSessionId = ephemeralSessionId;
      this.state = state;
    }

    /** Renews the ephemeral session identifier for this node. */
    public synchronized void renewSession() {
      this.ephemeralSessionId = UUID.randomUUID();
    }

    /** Updates the node-local state for this node. */
    public void updateState(NodeState state) {
      this.state = state;
    }

    /** Returns a heartbeat snapshot for this node. */
    public NodeHeartbeat getHeartbeat() {
      return new NodeHeartbeat(
          identity.getClusterName(), identity.getNodeId(), ephemeralSessionId, state);
    }

    /** Sets the health status of this node. */
    public void setHealthy(boolean healthy) {
      this.healthy = healthy;
    }
  }

  /**
   * Immutable snapshot of a {@link ClusterNode}.
   *
   * <p>Used for safe external reads of node state without exposing mutable internal fields.
   * Contains the node identity, ephemeral session ID, node-local state, and health status.
   */
  @Getter
  public static class ClusterNodeSnapshot {
    private final NodeIdentity identity;
    private final UUID ephemeralSessionId;
    private final NodeState state;
    private final boolean healthy;

    /**
     * Creates an immutable snapshot of the given {@link ClusterNode}.
     *
     * <p>This constructor copies the node's identity, ephemeral session ID, node-local state, and
     * health status into a snapshot object suitable for safe external access. Modifications to the
     * original {@link ClusterNode} after this call will not affect the snapshot.
     *
     * @param node the {@link ClusterNode} to snapshot; must not be {@code null}
     */
    public ClusterNodeSnapshot(ClusterNode node) {
      this.identity = node.getIdentity();
      this.ephemeralSessionId = node.getEphemeralSessionId();
      this.state = node.getState();
      this.healthy = node.isHealthy();
    }
  }

  private final Object mutex = new Object();

  /** Human-readable name of the cluster (lowercased for consistency). */
  private final String name;

  /** Configuration properties controlling cluster behavior. */
  private final ClusterProperties properties;

  /** Repository for persisting cluster state or heartbeats. */
  private final ClusterRepository<T> repository;

  /** The single local node of this cluster. */
  private ClusterNode localNode;

  /** Remote nodes in this cluster, keyed by their stable identity. */
  private final Map<NodeIdentity, ClusterNode> remoteNodes = new ConcurrentHashMap<>();

  /** Number of consecutive missed heartbeats for the local node. */
  private final AtomicInteger consecutiveMissedHeartbeats = new AtomicInteger(0);

  /** Executor used for emitting heartbeats. */
  private ExecutorService heartbeatExecutor;

  private T nodeState;

  /** Creates a new cluster with the given name, properties, and repository. */
  public Cluster(String name, ClusterProperties properties, ClusterRepository<T> repository) {
    this.name =
        Check.requireNotBlank(name, () -> "Cannot create cluster. Name is blank.").toLowerCase();
    this.properties =
        Check.requireNotNull(properties, () -> "Cluster properties must not be null.");
    this.repository =
        Check.requireNotNull(repository, () -> "Cluster repository must not be null.");
  }

  /** Creates and registers the single local node. */
  public ClusterNode createNode(String nodeId) {
    Check.requireNull(localNode, n -> "Local node already exists for cluster " + name);
    NodeIdentity identity = new NodeIdentity(name, nodeId, NodeType.LOCAL);
    localNode = new ClusterNode(identity, UUID.randomUUID());
    return localNode;
  }

  /** Returns the local node's heartbeat, or null if not created. */
  public NodeHeartbeat getLocalHeartbeat() {
    return localNode == null ? null : localNode.getHeartbeat();
  }

  @Override
  protected void onStart() {
    heartbeatExecutor = Executors.newFixedThreadPool(2);
    ThreadPools.execute(this::heartbeatLoop, this::isRunning, heartbeatExecutor);
    ThreadPools.execute(this::stateChangeLoop, this::isRunning, heartbeatExecutor);
  }

  @Override
  protected void onStop() {
    repository.close();
    ThreadPools.terminateNow(heartbeatExecutor, 30, TimeUnit.SECONDS);
  }

  /**
   * Reconciles the remote nodes with the latest heartbeat snapshot.
   *
   * <p>Removes vanished nodes and updates or adds new nodes, while preserving ephemeral session IDs
   * and node-local state for existing nodes.
   *
   * @param snapshot a map of node identities to {@link ClusterNode} representing the latest remote
   *     nodes state
   */
  private void reconcileRemoteNodes(Map<NodeIdentity, ClusterNode> snapshot) {
    synchronized (mutex) {
      // Remove vanished nodes
      remoteNodes.keySet().removeIf(id -> !snapshot.containsKey(id));

      // Add or update
      snapshot.forEach(
          (id, newNode) ->
              remoteNodes.compute(
                  id,
                  (k, existing) -> {
                    if (existing == null) {
                      return newNode;
                    }
                    existing.ephemeralSessionId = newNode.ephemeralSessionId;
                    existing.state = newNode.state;
                    existing.setHealthy(true);
                    return existing;
                  }));
    }
  }

  /**
   * Background loop that emits the local node's heartbeat at the configured interval.
   *
   * <p>Updates the remote nodes map with the latest heartbeats received from the repository.
   * Handles transient failures and renews the local ephemeral session if the number of consecutive
   * missed heartbeats exceeds the configured threshold.
   */
  private void heartbeatLoop() {
    final long intervalMs =
        properties.getHeartbeatIntervalUnit().toMillis(properties.getHeartbeatInterval());

    while (isRunning()) {
      long start = System.currentTimeMillis();
      try {
        final List<NodeHeartbeat> allNodes =
            repository.pushHeartbeats(name, getLocalHeartbeat(), intervalMs + (intervalMs / 2));

        final Map<NodeIdentity, ClusterNode> snapshot =
            allNodes.stream()
                .filter(
                    hb ->
                        localNode == null
                            || !Objects.equals(hb.nodeId(), localNode.getIdentity().getNodeId()))
                .map(
                    hb -> {
                      // Reuse existing NodeIdentity if possible
                      NodeIdentity existingId =
                          remoteNodes.keySet().stream()
                              .filter(id -> id.getNodeId().equals(hb.nodeId()))
                              .findFirst()
                              .orElse(
                                  new NodeIdentity(hb.clusterName(), hb.nodeId(), NodeType.REMOTE));

                      return new ClusterNode(existingId, hb.ephemeralSessionId(), hb.state());
                    })
                .collect(Collectors.toMap(n -> n.getIdentity(), n -> n));

        reconcileRemoteNodes(snapshot);
        consecutiveMissedHeartbeats.set(0);
        if (localNode != null) {
          localNode.setHealthy(true);
        }
      } catch (Exception e) {
        if (localNode != null) {
          localNode.setHealthy(false);
        }

        if (consecutiveMissedHeartbeats.incrementAndGet() > properties.getMaxMissedHeartbeats()) {
          if (localNode != null) {
            localNode.renewSession();
          }
        }

        if (consecutiveMissedHeartbeats.get() == 1) {
          log.warn("Error emitting heartbeat.", e);
        } else {
          log.debug("Consecutive heartbeat miss: {}", consecutiveMissedHeartbeats.get());
        }
      }

      long elapsed = System.currentTimeMillis() - start;
      long remaining = Math.max(0, intervalMs - elapsed);

      Threads.sleep(remaining);
    }
  }

  /**
   * Returns an immutable snapshot of the local node.
   *
   * @return {@link ClusterNodeSnapshot} of the local node, or {@code null} if the local node is not
   *     created yet
   */
  public ClusterNodeSnapshot getLocalNodeSnapshot() {
    return localNode == null ? null : new ClusterNodeSnapshot(localNode);
  }

  /**
   * Returns immutable snapshots of the current remote nodes.
   *
   * <p>Snapshots are safe for external consumption and do not expose internal mutable state.
   *
   * @return a map of {@link NodeIdentity} to {@link ClusterNodeSnapshot} representing all remote
   *     nodes
   */
  public Map<NodeIdentity, ClusterNodeSnapshot> getRemoteNodesSnapshot() {
    synchronized (mutex) {
      return remoteNodes.values().stream()
          .map(ClusterNodeSnapshot::new)
          .collect(Collectors.toUnmodifiableMap(s -> s.getIdentity(), s -> s));
    }
  }

  /**
   * Persists the given local node state through the repository.
   *
   * <p>This operation only occurs if the local node is marked healthy. The actual persistence
   * behavior (e.g., etcd, database, or memory) is determined by the configured {@link
   * ClusterRepository} implementation.
   */
  public void persistState() {
    if (localNode != null && localNode.isHealthy()) {
      repository.persist(localNode, getNodeState());
    } else {
      throw new IllegalStateException(
          "Node is either unhealthy or has not yet joinded the cluster.");
    }
  }

  private T getNodeState() {
    if (nodeState == null) {
      nodeState = initNodeState(localNode);
    }
    return nodeState;
  }

  protected abstract T initNodeState(ClusterNode localNode);

  /**
   * Background loop that periodically fetches or reconciles cluster state from the repository.
   *
   * <p>This loop runs independently of heartbeats. It allows the cluster to detect configuration or
   * membership changes that are not tied to heartbeat signals (e.g., persisted state updates).
   */
  private void stateChangeLoop() {

    final long intervalMs =
        properties.getStateChangeIntervalUnit().toMillis(properties.getStateChangeInterval());
    while (isRunning()) {
      long start = System.currentTimeMillis();
      try {
        if (localNode != null && localNode.isHealthy()) {
          repository.getState(localNode);
        }

      } catch (Exception e) {
        log.warn("Error watching state change.", e);
      }

      long elapsed = System.currentTimeMillis() - start;
      long remaining = Math.max(0, intervalMs - elapsed);

      Threads.sleep(remaining);
    }
  }
}
