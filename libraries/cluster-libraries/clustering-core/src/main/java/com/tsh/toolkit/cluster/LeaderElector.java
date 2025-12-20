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

/**
 * Provides access to the current leadership status as derived from cluster membership.
 *
 * <p>{@code LeaderElector} represents a <em>read-only view</em> of leadership within a cluster.
 * Implementations determine leadership based on observed cluster state rather than by performing
 * coordination or consensus.
 *
 * <p>The leadership model is intentionally lightweight:
 *
 * <ul>
 *   <li>Leadership is derived, not negotiated
 *   <li>No locking, fencing, or quorum guarantees are implied
 *   <li>Temporary split-brain scenarios are possible
 * </ul>
 *
 * <p>This interface is suitable for:
 *
 * <ul>
 *   <li>Leader-only background tasks
 *   <li>Soft coordination and scheduling
 *   <li>Operational decisions that tolerate eventual consistency
 * </ul>
 *
 * <p>Implementations are expected to be thread-safe.
 */
public interface LeaderElector {

  /**
   * Indicates whether the local node is currently considered the leader.
   *
   * <p>The returned value reflects the most recently observed cluster state and may change
   * asynchronously.
   *
   * @return {@code true} if the local node is currently the leader; {@code false} otherwise
   */
  boolean isLeader();

  /**
   * Returns the session identifier of the currently elected leader.
   *
   * <p>The session identifier uniquely identifies a single runtime incarnation of a node. If no
   * leader is currently elected, this method returns {@code null}.
   *
   * @return leader session identifier, or {@code null} if no leader exists
   */
  String getLeaderSessionId();
}
