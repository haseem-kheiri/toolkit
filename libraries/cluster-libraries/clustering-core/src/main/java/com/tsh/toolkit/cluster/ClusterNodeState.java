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
import java.util.Objects;
import lombok.Getter;

/**
 * Immutable value object representing the observed state of a single node participating in a
 * cluster.
 *
 * <p>This class models <em>membership state</em>, not identity alone. Identity is defined strictly
 * by {@code clusterName} and {@code sessionId}; temporal information and metadata are considered
 * mutable state associated with that identity.
 *
 * <p><strong>Equality semantics:</strong>
 *
 * <ul>
 *   <li>{@code equals} and {@code hashCode} are based only on {@code clusterName} and {@code
 *       sessionId}.
 *   <li>{@code recordedAtEpochMillis} and {@code metadata} are intentionally excluded, as they
 *       represent observational state rather than identity.
 * </ul>
 *
 * <p>Time is represented as epoch milliseconds to keep the core coordination model backend-agnostic
 * and free from time zone or calendar semantics.
 */
@Getter
public final class ClusterNodeState {

  /** Logical cluster identifier to which this node belongs. */
  private final String clusterName;

  /** Unique session identifier for the node within the cluster. */
  private final String sessionId;

  /**
   * Epoch timestamp (milliseconds) representing when this node was last observed to be alive.
   *
   * <p>This value is expected to be derived from an absolute instant (for example, a database
   * {@code TIMESTAMPTZ} or an etcd lease timestamp) and must be greater than zero.
   */
  private final long recordedAtEpochMillis;

  /**
   * Opaque metadata associated with the node.
   *
   * <p>The contents are not interpreted by the coordination layer and are intended for higher-level
   * consumers (diagnostics, routing hints, node attributes, etc.).
   */
  private final String metadata;

  /**
   * Creates a new immutable cluster node state.
   *
   * @param clusterName logical cluster identifier; must not be blank
   * @param sessionId unique session identifier; must not be blank
   * @param recordedAtEpochMillis epoch timestamp (milliseconds) when the node was last observed
   *     alive; must be greater than zero
   * @param metadata opaque node metadata; may be {@code null} or empty
   */
  public ClusterNodeState(
      String clusterName, String sessionId, long recordedAtEpochMillis, String metadata) {

    this.clusterName = Check.requireNotBlank(clusterName, () -> "cluster name must not be blank.");

    this.sessionId = Check.requireNotBlank(sessionId, () -> "session id must not be blank.");

    this.recordedAtEpochMillis =
        Check.requireTrue(
            () -> recordedAtEpochMillis,
            t -> t > 0,
            () -> "recordedAtEpochMillis must be greater than 0.");

    this.metadata = metadata == null ? "" : metadata;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ClusterNodeState other = (ClusterNodeState) obj;
    return Objects.equals(clusterName, other.clusterName)
        && Objects.equals(sessionId, other.sessionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clusterName, sessionId);
  }
}
