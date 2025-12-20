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

package com.tsh.toolkit.cluster.coordinator.impl;

import com.tsh.toolkit.cluster.ClusterCoordinator;
import com.tsh.toolkit.cluster.ClusterNodeState;
import com.tsh.toolkit.cluster.ClusterState;
import com.tsh.toolkit.codec.json.impl.JsonCodec;
import com.tsh.toolkit.rdbms.AbstractRdbmsRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * PostgreSQL-backed implementation of {@link ClusterCoordinator} using heartbeat-based membership
 * tracking.
 *
 * <p>This coordinator implements a <em>participate-and-observe</em> primitive:
 *
 * <ul>
 *   <li>The calling node atomically records its own heartbeat.
 *   <li>The same statement returns the set of nodes that are considered alive relative to the
 *       caller's heartbeat timestamp.
 * </ul>
 *
 * <p>The implementation guarantees that the caller's own node is always included in the returned
 * cluster view, and that all other nodes are evaluated relative to the same heartbeat instant. This
 * avoids race conditions, clock skew boundaries, and predicate-based self-exclusion.
 *
 * <p>Time is normalized to epoch milliseconds at the JDBC boundary; the core coordination model
 * remains backend-agnostic.
 */
public class PostgresClusterCoordinator extends AbstractRdbmsRepository
    implements ClusterCoordinator {

  /**
   * SQL statement that:
   *
   * <ol>
   *   <li>Upserts the caller's heartbeat into {@code cluster.obj_heartbeat}.
   *   <li>Materializes the caller's own membership record.
   *   <li>Returns all other nodes whose heartbeats fall within the configured liveness window
   *       relative to the caller.
   * </ol>
   *
   * <p>The caller's own node is returned unconditionally via the {@code self} CTE. Other nodes are
   * included only if their last heartbeat is within {@code heartbeatTimeoutMillis} of the caller's
   * heartbeat timestamp.
   */
  private static final String PARTICIPATE_AND_OBSERVE_SQL =
      """
      WITH self AS (
        INSERT INTO cluster.obj_heartbeat (
          cluster_name, session_id, recorded_at, metadata
        )
        VALUES (?, ?, now(), ?)
        ON CONFLICT (cluster_name, session_id)
        DO UPDATE SET recorded_at = EXCLUDED.recorded_at
        RETURNING cluster_name, session_id, recorded_at, metadata
      )
      SELECT s.session_id, s.recorded_at, s.metadata
      FROM self s

      UNION ALL

      SELECT h.session_id, h.recorded_at, h.metadata
      FROM cluster.obj_heartbeat h
      JOIN self s ON h.cluster_name = s.cluster_name
      WHERE h.session_id <> s.session_id
        AND h.recorded_at >= s.recorded_at - (? * INTERVAL '1 millisecond');
      """;

  private final JsonCodec codec;

  /**
   * Creates a PostgreSQL-backed cluster coordinator.
   *
   * @param dataSource JDBC data source
   * @param codec codec used to serialize node metadata
   */
  public PostgresClusterCoordinator(DataSource dataSource, JsonCodec codec) {
    super(dataSource);
    this.codec = codec;
  }

  /**
   * Records the caller's heartbeat and returns the current cluster membership view.
   *
   * <p>This method performs both actions atomically in a single SQL statement:
   *
   * <ul>
   *   <li>The caller's heartbeat is inserted or updated.
   *   <li>The set of live nodes is returned relative to the caller's heartbeat.
   * </ul>
   *
   * <p>The returned {@link ClusterState} always includes the caller's own node.
   *
   * @param clusterName logical cluster identifier
   * @param sessionId unique session identifier for the calling node
   * @param metadata arbitrary node metadata
   * @param heartbeatTimeoutMillis liveness window in milliseconds
   * @return current cluster membership state
   */
  @Override
  public ClusterState participateAndObserve(
      String clusterName,
      String sessionId,
      Map<String, String> metadata,
      long heartbeatTimeoutMillis) {

    return executeAndReturn(
        connection -> {
          connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

          try (PreparedStatement ps = connection.prepareStatement(PARTICIPATE_AND_OBSERVE_SQL)) {

            ps.setString(1, clusterName);
            ps.setString(2, sessionId);
            ps.setString(3, codec.encodeToString(metadata));
            ps.setLong(4, heartbeatTimeoutMillis);

            try (ResultSet rs = ps.executeQuery()) {
              List<ClusterNodeState> nodeStates = new ArrayList<>();

              while (rs.next()) {
                nodeStates.add(
                    new ClusterNodeState(
                        clusterName,
                        rs.getString(1),
                        rs.getObject(2, OffsetDateTime.class).toInstant().toEpochMilli(),
                        rs.getString(3)));
              }

              return new ClusterState(clusterName, nodeStates);
            }
          }
        });
  }
}
