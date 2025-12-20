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

import com.tsh.toolkit.cluster.ClusterCoordinator;
import com.tsh.toolkit.cluster.ClusterState;
import com.tsh.toolkit.cluster.ClusterStateChangeEvent;
import com.tsh.toolkit.cluster.ClusterStateChangeListener;
import com.tsh.toolkit.core.lifecycle.impl.AbstractLifecycleObject;
import com.tsh.toolkit.core.utils.Check;
import com.tsh.toolkit.core.utils.Ref;
import com.tsh.toolkit.core.utils.ThreadPools;
import com.tsh.toolkit.core.utils.Uuids;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Runtime participant in a distributed cluster that maintains membership using periodic heartbeats
 * and continuously observes cluster membership changes.
 *
 * <p>A {@code ClusterNode} models a <em>live process instance</em>, not merely a logical
 * identifier. Its responsibilities include:
 *
 * <ul>
 *   <li>Establishing and maintaining an ephemeral session with the cluster backend
 *   <li>Publishing periodic heartbeats to assert liveness
 *   <li>Retrieving a consistent view of current cluster membership
 *   <li>Detecting and emitting membership changes to an application listener
 * </ul>
 *
 * <p><strong>Identity semantics:</strong>
 *
 * <ul>
 *   <li>{@code nodeId} is a stable, application-level identifier for this node
 *   <li>Session identifiers are ephemeral and represent a single runtime incarnation
 * </ul>
 *
 * <p><strong>Session lifecycle:</strong>
 *
 * <ul>
 *   <li>Transient failures do not immediately invalidate the session
 *   <li>The session is rotated only after heartbeat failures exceed {@code heartbeatTimeout}
 * </ul>
 *
 * <p><strong>State delivery model:</strong>
 *
 * <ul>
 *   <li>Cluster state notifications are level-triggered, not edge-triggered
 *   <li>Multiple intermediate states may be skipped
 *   <li>The listener always receives the most recent known state
 * </ul>
 *
 * <p><strong>Failure model:</strong>
 *
 * <ul>
 *   <li>Heartbeat failures are tolerated up to the configured timeout
 *   <li>Failures in user-provided listeners are considered fatal
 *   <li>A fatal failure transitions the node to an unhealthy state and stops participation
 * </ul>
 *
 * <p>This class is thread-safe and manages its own background execution.
 */
@Slf4j
public class ClusterNode extends AbstractLifecycleObject {

  /**
   * Stable, application-level identifier for this node.
   *
   * <p>This identifier remains constant across restarts and session rotations.
   */
  private final String nodeId;

  /** Logical name of the cluster this node participates in. */
  private final String clusterName;

  /** Backend responsible for coordinating cluster membership and heartbeats. */
  private final ClusterCoordinator clusterCoordinator;

  /** Fixed interval between successive heartbeat attempts. */
  private final Duration heartbeatInterval;

  /**
   * Maximum duration for which heartbeat failures may occur before the current session is
   * considered expired and replaced.
   */
  private final Duration heartbeatTimeout;

  /**
   * Arbitrary metadata published alongside heartbeats.
   *
   * <p>This data is opaque to the coordinator and interpreted only by consumers.
   */
  private final Map<String, String> metadata;

  /**
   * Callback invoked when the observed cluster membership changes.
   *
   * <p>This listener is invoked asynchronously and isolated from heartbeat logic.
   */
  private final ClusterStateChangeListener clusterStateChangeListener;

  /**
   * Executor responsible for running background coordination tasks.
   *
   * <p>Two threads are used:
   *
   * <ul>
   *   <li>Heartbeat emission and membership observation
   *   <li>Cluster state comparison and listener notification
   * </ul>
   */
  private ExecutorService workers;

  /**
   * Most recent cluster state returned by the coordinator.
   *
   * <p>This value represents the latest known membership view, whether or not it has been delivered
   * to the listener.
   */
  private volatile ClusterState lastKnownState;

  /**
   * Session identifier associated with the most recently observed cluster state.
   *
   * <p>This value reflects the session under which the last successful heartbeat was recorded. It
   * is updated only after a successful interaction with the {@link ClusterCoordinator}.
   *
   * <p>The session identifier may change over time as a result of session rotation following
   * prolonged heartbeat failures.
   */
  private volatile String lastKnownSessionId;

  /**
   * Cluster state most recently delivered to the listener.
   *
   * <p>Used to suppress redundant notifications and coalesce rapid changes.
   */
  private volatile ClusterState state;

  /**
   * Indicates whether this node is currently considered healthy.
   *
   * <p>A node becomes unhealthy if a fatal, unrecoverable error occurs. Once unhealthy, the node
   * stops participating and does not recover automatically.
   */
  private volatile boolean healthy = true;

  /**
   * Creates a new {@code ClusterNode}.
   *
   * @param nodeId stable logical identifier for the node
   * @param clusterName logical cluster name
   * @param clusterCoordinator coordination backend implementation
   * @param heartbeatInterval interval between heartbeats (minimum 1 second)
   * @param heartbeatTimeout maximum tolerated heartbeat failure duration
   * @param metadata opaque metadata associated with this node
   * @param clusterStateChangeListener listener notified on membership changes
   */
  public ClusterNode(
      String nodeId,
      String clusterName,
      ClusterCoordinator clusterCoordinator,
      Duration heartbeatInterval,
      Duration heartbeatTimeout,
      Map<String, String> metadata,
      ClusterStateChangeListener clusterStateChangeListener) {

    this.nodeId = Check.requireNotBlank(nodeId, () -> "node id must not be blank.");
    this.clusterName = Check.requireNotBlank(clusterName, () -> "cluster name must not be blank.");
    this.clusterCoordinator =
        Check.requireNotNull(clusterCoordinator, () -> "cluster coordinator must not be null.");

    this.heartbeatInterval =
        Check.requireTrue(
            () -> heartbeatInterval,
            hb -> hb != null && hb.toMillis() >= 1000,
            () -> "heartbeat interval must be at least 1 second.");

    this.heartbeatTimeout =
        Check.requireTrue(
            () -> heartbeatTimeout,
            hb -> hb != null && hb.toMillis() >= 3 * this.heartbeatInterval.toMillis(),
            () -> "heartbeat timeout must be at least three times the heartbeat interval.");

    this.metadata = Map.copyOf(Check.requireNotNull(metadata, () -> "metadata must not be null."));
    this.clusterStateChangeListener = clusterStateChangeListener;
  }

  /** Returns the logical cluster name. */
  public String getClusterName() {
    return clusterName;
  }

  /** Returns the stable logical identifier of this node. */
  public String getNodeId() {
    return nodeId;
  }

  /**
   * Indicates whether this node is currently healthy.
   *
   * <p>An unhealthy node has ceased participation and should be treated as unavailable by the
   * hosting application.
   */
  public boolean isHealthy() {
    return healthy;
  }

  @Override
  protected void onStart() {
    workers = Executors.newFixedThreadPool(2);
    ThreadPools.execute(this::emitHeartbeat, this::isRunning, workers);
    ThreadPools.execute(this::scanStateChange, this::isRunning, workers);
  }

  @Override
  protected void onStop() {
    ThreadPools.terminate(workers, 15, TimeUnit.SECONDS);
    workers = null;
    state = null;
    lastKnownState = null;
  }

  /**
   * Background loop responsible for emitting heartbeats and retrieving cluster membership
   * information.
   *
   * <p>The loop maintains a stable session identifier across transient failures and rotates the
   * session only after exceeding the configured timeout.
   *
   * <p>After a successful heartbeat, the current session identifier is recorded as the {@code
   * lastKnownSessionId} and associated with the retrieved cluster membership view.
   */
  private void emitHeartbeat() {
    long heartbeatIntervalMillis = heartbeatInterval.toMillis();

    Ref<String> sessionId = Ref.of(Uuids.uuid7().toString());
    Ref<Long> lastSuccessfulTime = Ref.of(System.currentTimeMillis());

    whileUp(
        running -> {
          try {
            // lastKnownSessionId is the session identifier for this node that is
            // already associated with lastKnownState, provided for convenience
            // when emitting change events.

            lastKnownState =
                clusterCoordinator.participateAndObserve(
                    clusterName, sessionId.get(), metadata, heartbeatTimeout.toMillis());
            lastKnownSessionId = sessionId.get();

            lastSuccessfulTime.set(System.currentTimeMillis());
          } catch (Exception e) {
            log.warn("Heartbeat failed for node {}", nodeId, e);

            if (System.currentTimeMillis() - lastSuccessfulTime.get()
                >= heartbeatTimeout.toMillis()) {
              sessionId.set(Uuids.uuid7().toString());
              lastSuccessfulTime.set(System.currentTimeMillis());
            }
          }
        },
        heartbeatIntervalMillis,
        TimeUnit.MILLISECONDS);
  }

  /**
   * Background loop responsible for detecting cluster state changes and notifying the registered
   * listener.
   *
   * <p>This loop implements a latest-only delivery model. Intermediate states may be skipped, but
   * the listener always observes the most recent state.
   *
   * <p>Listener failures are treated as unrecoverable and will terminate cluster participation.
   *
   * <p>*
   *
   * <p>The emitted {@link ClusterStateChangeEvent} includes the session identifier under which the
   * observed cluster state was obtained, allowing listeners to correlate membership changes with
   * session lifecycle events.
   */
  private void scanStateChange() {
    long heartbeatIntervalMillis = heartbeatInterval.toMillis();

    whileUp(
        running -> {
          if (lastKnownState != null
              && !lastKnownState.equals(state)
              && clusterStateChangeListener != null) {

            ClusterState oldState = state;
            state = lastKnownState;

            try {
              clusterStateChangeListener.onChange(
                  new ClusterStateChangeEvent(
                      clusterName, nodeId, lastKnownSessionId, state, oldState));
            } catch (Exception e) {
              throw e;
            } catch (Throwable t) {
              log.error(
                  "Fatal error in cluster state listener for node {}. Shutting down node.",
                  nodeId,
                  t);
              healthy = false;
              stop();
            }
          }
        },
        heartbeatIntervalMillis,
        TimeUnit.MILLISECONDS);
  }
}
