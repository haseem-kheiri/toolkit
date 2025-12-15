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

import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for a {@code Cluster}.
 *
 * <p>Defines timing and tolerance parameters that govern cluster node behavior — including how
 * frequently nodes send heartbeats, how long a node is tolerated after missing multiple heartbeats,
 * and how often the cluster checks for changes in state.
 */
@Getter
@Setter
public class ClusterProperties {

  /**
   * Maximum number of consecutive heartbeats a node can miss before being considered inactive.
   *
   * <p>If a node fails to send this many heartbeats in a row, it may be marked as inactive or
   * removed from the cluster’s active membership.
   */
  private int maxMissedHeartbeats = 3;

  /**
   * Interval between successive heartbeats.
   *
   * <p>Specifies how often a node sends a heartbeat signal to indicate liveness. The effective
   * duration is computed by combining this value with {@link #heartbeatIntervalUnit}.
   *
   * <p>Example: if {@code heartbeatInterval = 5} and {@code heartbeatIntervalUnit = SECONDS}, the
   * node sends a heartbeat every 5 seconds.
   */
  private long heartbeatInterval = 5;

  /** Time unit corresponding to {@link #heartbeatInterval}. */
  private TimeUnit heartbeatIntervalUnit = TimeUnit.SECONDS;

  /**
   * Interval between consecutive cluster state checks.
   *
   * <p>Determines how frequently the cluster scans for state changes. This setting is independent
   * of any state persistence logic.
   */
  private long stateChangeInterval = 5;

  /** Time unit corresponding to {@link #stateChangeInterval}. */
  private TimeUnit stateChangeIntervalUnit = TimeUnit.SECONDS;

  /** Repository-level configuration properties controlling lease timing and behavior. */
  private ClusterRepositoryProperties repository = new ClusterRepositoryProperties();
}
