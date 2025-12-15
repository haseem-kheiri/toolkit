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

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents the minimal persisted state metadata for a cluster node.
 *
 * <p>This base class captures identifying and temporal information that uniquely associates a
 * node’s persisted state with its cluster membership and current ephemeral session. Implementations
 * may extend this class to include application-specific fields or metrics.
 *
 * <p><strong>Serialization-agnostic:</strong> this class contains no codec-specific annotations or
 * dependencies. It can be serialized using MessagePack, JSON, Avro, or any other format chosen by
 * the {@code ClusterRepository} implementation.
 *
 * <p>Typical uses include:
 *
 * <ul>
 *   <li>Tracking which node owns or last updated a persisted state record.
 *   <li>Detecting reincarnations of a node across restarts via {@link #ephemeralSessionId}.
 *   <li>Determining staleness or freshness of data using {@link #updatedAt}.
 * </ul>
 */
@Getter
@Setter
@ToString
public class ClusterNodeState {

  /**
   * The logical name of the cluster to which this node belongs.
   *
   * <p>Used to scope persisted state within multi-cluster deployments.
   */
  private String clusterName;

  /**
   * The unique identifier of this node within the cluster.
   *
   * <p>This identifier remains stable across restarts and uniquely distinguishes nodes within the
   * same cluster namespace.
   */
  private String nodeId;

  /**
   * The ephemeral session identifier for the node.
   *
   * <p>This value changes each time the node restarts or re-joins the cluster, allowing detection
   * of reincarnations even when {@link #nodeId} remains constant.
   */
  private UUID ephemeralSessionId;

  /**
   * The epoch timestamp, in milliseconds, when this state was last updated.
   *
   * <p>Used to determine freshness of persisted state data.
   */
  private long updatedAt;

  /** Creates an empty instance for deserialization or subclassing. */
  public ClusterNodeState() {}
}
