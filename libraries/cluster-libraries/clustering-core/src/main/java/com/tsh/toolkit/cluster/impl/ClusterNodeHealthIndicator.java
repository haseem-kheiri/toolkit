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

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Spring Boot {@link HealthIndicator} that exposes the health of a {@link ClusterNode} to the
 * application monitoring and orchestration layer.
 *
 * <p>This indicator acts as a <em>projection</em> of cluster participation health, not as a source
 * of truth for cluster membership itself. It translates the internal health state of a {@code
 * ClusterNode} into Spring Boot Actuator health semantics.
 *
 * <p><strong>Health interpretation:</strong>
 *
 * <ul>
 *   <li>{@link Health#up()} indicates that the node is actively participating in the cluster and
 *       has not encountered a fatal coordination error
 *   <li>{@link Health#down()} indicates that the node has entered an unhealthy state and has
 *       permanently stopped cluster participation
 * </ul>
 *
 * <p><strong>Failure semantics:</strong>
 *
 * <ul>
 *   <li>This indicator reflects only unrecoverable failures detected by {@code ClusterNode}
 *   <li>Transient heartbeat failures do not cause this indicator to report {@code DOWN}
 *   <li>Once {@code DOWN}, the indicator will remain {@code DOWN} for the lifetime of the
 *       application
 * </ul>
 *
 * <p><strong>Intended usage:</strong>
 *
 * <ul>
 *   <li>Integration with Spring Boot Actuator health endpoints
 *   <li>Container orchestrator liveness and readiness probes
 *   <li>Operational dashboards and alerting systems
 * </ul>
 *
 * <p>This class deliberately keeps Spring Boot dependencies out of the core {@code ClusterNode}
 * implementation, preserving separation between coordination logic and application runtime
 * concerns.
 */
public class ClusterNodeHealthIndicator implements HealthIndicator {

  /** Cluster node whose health state is being exposed. */
  private final ClusterNode clusterNode;

  /**
   * Creates a new health indicator for the given cluster node.
   *
   * @param clusterNode the cluster node whose health should be reported; must not be {@code null}
   */
  public ClusterNodeHealthIndicator(ClusterNode clusterNode) {
    this.clusterNode = clusterNode;
  }

  /**
   * Returns the current health status of the associated cluster node.
   *
   * <p>The returned health reflects whether the node is still participating in the cluster without
   * encountering a fatal error.
   *
   * @return {@link Health#up()} if the node is healthy; {@link Health#down()} otherwise
   */
  @Override
  public Health health() {
    if (!clusterNode.isHealthy()) {
      return Health.down().withDetail("cluster", clusterNode.getClusterName()).build();
    }
    return Health.up().build();
  }
}
