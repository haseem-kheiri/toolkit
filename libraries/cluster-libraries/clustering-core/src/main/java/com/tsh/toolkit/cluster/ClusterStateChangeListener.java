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

/**
 * Callback interface for receiving notifications when observed cluster membership changes.
 *
 * <p>{@code ClusterStateChangeListener} is invoked asynchronously by a {@link ClusterNode} whenever
 * the node detects that the cluster state has transitioned to a new value.
 *
 * <p><strong>Delivery semantics:</strong>
 *
 * <ul>
 *   <li>Notifications are <em>level-triggered</em>, not edge-triggered
 *   <li>Intermediate cluster states may be skipped
 *   <li>The listener always receives the most recently observed state
 * </ul>
 *
 * <p><strong>Threading model:</strong>
 *
 * <ul>
 *   <li>Invoked from a background worker thread managed by {@link ClusterNode}
 *   <li>Listener implementations must be thread-safe
 *   <li>Listener execution must be non-blocking and fast
 * </ul>
 *
 * <p><strong>Error handling:</strong>
 *
 * <ul>
 *   <li>{@code java.lang.Error} thrown by the listener are treated as fatal
 *   <li>A fatal listener failure will cause the owning {@link ClusterNode} to stop participating
 *   <li>Listener implementations should handle all recoverable errors internally
 * </ul>
 *
 * <p>This interface is intended for:
 *
 * <ul>
 *   <li>Leader election
 *   <li>Membership-driven scheduling
 *   <li>Cluster-aware feature activation
 * </ul>
 */
@FunctionalInterface
public interface ClusterStateChangeListener {

  /**
   * Invoked when the observed cluster state changes.
   *
   * <p>The supplied event contains both the newly observed cluster state and the previously
   * delivered state, along with contextual identity information for the local node.
   *
   * <p>This method must not block or perform long-running operations.
   *
   * @param event cluster state change event
   */
  void onChange(ClusterStateChangeEvent event);
}
