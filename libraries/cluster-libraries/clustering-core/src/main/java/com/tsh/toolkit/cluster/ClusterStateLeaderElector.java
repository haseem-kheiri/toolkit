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

import java.util.Comparator;
import java.util.UUID;

/**
 * Derives cluster leadership from observed cluster membership.
 *
 * <p>{@code ClusterStateLeaderElector} is a passive, deterministic leader elector that computes
 * leadership purely from {@link ClusterState} updates. It does not perform coordination, locking,
 * or state mutation.
 *
 * <h2>Leadership rule</h2>
 *
 * <p>The leader is defined as the cluster member with the <em>lexicographically smallest session
 * identifier</em>.
 *
 * <p>Session identifiers are expected to be UUIDv7 values encoded in canonical string form. UUIDv7
 * preserves creation-time ordering when compared lexicographically, making the oldest active
 * session the leader.
 *
 * <h2>Operational semantics</h2>
 *
 * <ul>
 *   <li>Leadership is recalculated on every cluster state change
 *   <li>Leadership is stable unless the current leader leaves or expires
 *   <li>If no cluster members exist, no leader is elected
 * </ul>
 *
 * <h2>Failure and consistency model</h2>
 *
 * <ul>
 *   <li>This elector provides <em>soft leadership</em>
 *   <li>Temporary split-brain conditions may result in multiple leaders
 *   <li>No fencing or quorum enforcement is performed
 * </ul>
 *
 * <p>This class is thread-safe. Leadership state is updated atomically on cluster state change
 * events and may be queried concurrently.
 */
public class ClusterStateLeaderElector implements ClusterStateChangeListener, LeaderElector {

  /**
   * Session identifier of the currently elected leader.
   *
   * <p>{@code null} if no leader is currently elected.
   */
  private volatile String leaderSessionId;

  /** Indicates whether the local node is the current leader. */
  private volatile boolean leader;

  /** Returns {@code true} if the local node is currently the elected leader. */
  @Override
  public boolean isLeader() {
    return leader;
  }

  /**
   * Returns the session identifier of the currently elected leader.
   *
   * @return leader session identifier, or {@code null} if no leader exists
   */
  @Override
  public String getLeaderSessionId() {
    return leaderSessionId;
  }

  /**
   * Recomputes leadership in response to a cluster membership change.
   *
   * <p>The leader is selected as the node with the smallest session identifier. If no cluster
   * members exist, leadership is cleared.
   *
   * @param event cluster state change event
   */
  @Override
  public void onChange(ClusterStateChangeEvent event) {
    ClusterState state = event.getState();

    ClusterNodeState leaderNode =
        state.getNodes().stream()
            .min(Comparator.comparing(n -> UUID.fromString(n.getSessionId())))
            .orElse(null);

    if (leaderNode == null) {
      leaderSessionId = null;
      leader = false;
      return;
    }

    leaderSessionId = leaderNode.getSessionId();
    leader = leaderSessionId.equals(event.getSessionId());
  }
}
