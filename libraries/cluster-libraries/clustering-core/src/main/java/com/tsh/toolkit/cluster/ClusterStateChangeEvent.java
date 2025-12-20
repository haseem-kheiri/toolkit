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

import com.tsh.toolkit.cluster.impl.ClusterNode;
import lombok.Getter;

/**
 * Immutable event representing a change in observed cluster state for a specific node session.
 *
 * <p>A {@code ClusterStateChangeEvent} is emitted by a {@link ClusterNode} when the cluster
 * membership view transitions from one state to another. The event captures both the newly observed
 * state and the previously delivered state.
 *
 * <p><strong>Session semantics:</strong>
 *
 * <ul>
 *   <li>The {@code sessionId} identifies the ephemeral runtime session of the node at the time the
 *       state was observed.
 *   <li>The session identifier is already associated with the reported {@code state} as returned by
 *       the cluster coordinator and is provided explicitly for convenience and observability.
 * </ul>
 *
 * <p><strong>Delivery semantics:</strong>
 *
 * <ul>
 *   <li>Events are <em>level-triggered</em>, not edge-triggered.
 *   <li>Intermediate cluster states may be skipped.
 *   <li>The event always represents the most recent known cluster state.
 * </ul>
 *
 * <p>This class is a simple value object and is fully thread-safe.
 */
@Getter
public class ClusterStateChangeEvent {

  /** Logical name of the cluster in which the change occurred. */
  private final String clusterName;

  /** Stable, application-level identifier of the node emitting the event. */
  private final String nodeId;

  /**
   * Ephemeral session identifier of the node associated with the reported state.
   *
   * <p>This session identifier corresponds to the same session already reflected in {@link
   * #getState()} and is included explicitly for logging, auditing, and downstream correlation.
   */
  private final String sessionId;

  /** The newly observed cluster state. */
  private final ClusterState state;

  /**
   * The previously delivered cluster state.
   *
   * <p>May be {@code null} if no prior state has been delivered.
   */
  private final ClusterState oldState;

  /**
   * Creates a new cluster state change event.
   *
   * @param clusterName logical cluster name
   * @param nodeId stable logical identifier of the node
   * @param sessionId ephemeral session identifier associated with {@code state}
   * @param state newly observed cluster state
   * @param oldState previously delivered cluster state, or {@code null} if none
   */
  public ClusterStateChangeEvent(
      String clusterName,
      String nodeId,
      String sessionId,
      ClusterState state,
      ClusterState oldState) {

    this.clusterName = clusterName;
    this.nodeId = nodeId;
    this.sessionId = sessionId;
    this.state = state;
    this.oldState = oldState;
  }
}
