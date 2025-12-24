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

package com.tsh.toolkit.lock.provider.impl;

import com.tsh.toolkit.lock.LockProvider;
import com.tsh.toolkit.lock.impl.LockManager.LockLease;
import com.tsh.toolkit.rdbms.AbstractRdbmsRepository;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * PostgreSQL-based implementation of {@link LockProvider} using a relational table as the
 * authoritative lock registry.
 *
 * <p>This provider implements <b>exclusive, lease-based distributed locks</b> using PostgreSQL
 * transactional semantics.
 *
 * <h2>Design model</h2>
 *
 * <ul>
 *   <li>A lock is considered <b>held</b> if and only if a row exists in {@code lock.obj_lock_lease}
 *       with a matching {@code lock_name}.
 *   <li>Lock ownership is identified by {@code execution_id}.
 *   <li>Lease expiration is enforced via {@code expires_at}.
 *   <li>Expired or released locks are represented by <b>absence of a row</b>.
 * </ul>
 *
 * <h2>Concurrency guarantees</h2>
 *
 * <ul>
 *   <li>All acquisition and renewal operations are atomic.
 *   <li>Lock contention is resolved entirely by PostgreSQL.
 *   <li>No client-side synchronization is required.
 * </ul>
 *
 * <h2>Failure semantics</h2>
 *
 * <ul>
 *   <li>Infrastructure failures (e.g., connection loss) propagate as exceptions.
 *   <li>Lock contention results in {@code null} leases.
 *   <li>Release and renew operations are best-effort and idempotent.
 * </ul>
 */
@Slf4j
public class PostgreSqlLockProvider extends AbstractRdbmsRepository implements LockProvider {

  /**
   * Attempts to acquire a lock, or re-acquire it if the existing lease has expired.
   *
   * <p>Semantics:
   *
   * <ul>
   *   <li>INSERT succeeds if the lock does not exist
   *   <li>UPDATE succeeds only if the existing lease is expired
   *   <li>No rows returned if the lock is currently held
   * </ul>
   *
   * <p>The {@code RETURNING} clause is used to detect successful acquisition.
   */
  private static final String ACQUIRE_OR_RENEW_LOCK_SQL =
      """
        WITH ts AS (
          SELECT now() AS now
        )
        INSERT INTO lock.obj_lock_lease (lock_name, execution_id, expires_at)
        SELECT ?, ?, ts.now + (? * INTERVAL '1 millisecond')
        FROM ts
        ON CONFLICT (lock_name)
        DO UPDATE
        SET execution_id = EXCLUDED.execution_id,
          expires_at   = EXCLUDED.expires_at
        WHERE lock.obj_lock_lease.expires_at <= (SELECT now FROM ts)
        RETURNING lock_name, execution_id, expires_at;
      """;

  /**
   * Renews active lock leases.
   *
   * <p>Only leases that:
   *
   * <ul>
   *   <li>Match both {@code lock_name} and {@code execution_id}
   *   <li>Have not yet expired
   * </ul>
   *
   * <p>are renewed.
   *
   * <p>Expired or released leases are silently ignored.
   */
  private static final String RENEW_LOCK_SQL =
      """
      WITH input(lock_name, execution_id) AS (
          SELECT unnest(?::text[]), unnest(?::text[])
      )
      UPDATE lock.obj_lock_lease l
      SET expires_at = now() + (? * INTERVAL '1 millisecond')
      FROM input i
      WHERE
          l.lock_name = i.lock_name
          AND l.execution_id = i.execution_id
          AND l.expires_at > now()
      RETURNING
          l.lock_name,
          l.execution_id,
          l.expires_at;
      """;

  /**
   * Releases lock leases by deleting their corresponding rows.
   *
   * <p>This operation is idempotent. Non-existent or expired leases are ignored.
   */
  private static final String RELEASE_LOCK_SQL =
      """
      WITH input(lock_name, execution_id) AS (
          SELECT unnest(?::text[]), unnest(?::text[])
      )
      DELETE FROM lock.obj_lock_lease l
      USING input i
      WHERE l.lock_name = i.lock_name
        AND l.execution_id = i.execution_id;
      """;

  /**
   * Releases lock leases by deleting their corresponding rows.
   *
   * <p>This operation is idempotent. Non-existent or expired leases are ignored.
   */
  public PostgreSqlLockProvider(DataSource dataSource) {
    super(dataSource);
  }

  /**
   * Attempts to acquire an exclusive lock.
   *
   * <p>If the lock is available or expired, a new lease is created and returned. If the lock is
   * currently held by another execution, {@code null} is returned.
   *
   * @param lockName logical lock name
   * @param executionId execution correlation identifier
   * @param lockDuration requested lease duration
   * @return a {@link LockLease} if acquired; {@code null} otherwise
   */
  @Override
  public LockLease acquireLock(String lockName, String executionId, Duration lockDuration) {
    return executeAndReturn(
        c -> {
          c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
          try (final PreparedStatement ps = c.prepareStatement(ACQUIRE_OR_RENEW_LOCK_SQL)) {
            ps.setString(1, lockName);
            ps.setString(2, executionId);
            ps.setLong(3, lockDuration.toMillis());

            try (ResultSet rs = ps.executeQuery()) {
              if (rs.next()) {
                return new LockLease(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getObject(3, OffsetDateTime.class).toInstant().toEpochMilli());
              }
            }

            return null;
          }
        });
  }

  /**
   * Releases previously acquired lock leases.
   *
   * <p>This operation is:
   *
   * <ul>
   *   <li><b>Idempotent</b>
   *   <li><b>Best-effort</b>
   *   <li>Safe to call with expired or unknown leases
   * </ul>
   *
   * <p>Leases are released in partitions to avoid oversized SQL arrays.
   *
   * @param leases lock leases to release
   */
  @Override
  public void release(List<LockLease> leases) {
    List<List<LockLease>> partitions = partition(leases, 100);

    for (List<LockLease> partition : partitions) {
      releasePartition(partition);
    }
  }

  /**
   * Releases a partition of lock leases in a single SQL statement.
   *
   * <p>Only rows matching both {@code lock_name} and {@code execution_id} are deleted.
   */
  private void releasePartition(List<LockLease> leases) {
    String[] lockNames = new String[leases.size()];
    String[] executionIds = new String[leases.size()];

    for (int i = 0; i < leases.size(); i++) {
      LockLease lease = leases.get(i);
      lockNames[i] = lease.getName();
      executionIds[i] = lease.getExecutionId();
    }

    execute(
        c -> {
          Array arrayOfName = null;
          Array arrayOfIds = null;
          try (PreparedStatement ps = c.prepareStatement(RELEASE_LOCK_SQL)) {
            arrayOfName = c.createArrayOf("text", lockNames);
            arrayOfIds = c.createArrayOf("text", executionIds);

            ps.setArray(1, arrayOfName);
            ps.setArray(2, arrayOfIds);

            ps.executeUpdate();
          } finally {
            freeUp(arrayOfName);
            freeUp(arrayOfIds);
          }
        },
        true);
  }

  /**
   * Renews active lock leases.
   *
   * <p>Only leases that are:
   *
   * <ul>
   *   <li>Still present in the database
   *   <li>Owned by the provided execution IDs
   *   <li>Not yet expired
   * </ul>
   *
   * <p>will be renewed.
   *
   * <p>Expired or released leases are omitted from the returned list.
   *
   * @param list leases to renew
   * @param lockDuration requested new lease duration
   * @return list of successfully renewed leases
   */
  @Override
  public List<LockLease> renew(List<LockLease> list, Duration lockDuration) {
    List<List<LockLease>> partitions = partition(list, 100);

    List<LockLease> renewals = new ArrayList<>();
    for (List<LockLease> partition : partitions) {
      renewals.addAll(renewPartition(partition, lockDuration));
    }

    return renewals;
  }

  /**
   * Renews a partition of lock leases.
   *
   * <p>The returned list contains only successfully renewed leases.
   */
  private List<LockLease> renewPartition(List<LockLease> leases, Duration lockDuration) {
    String[] lockNames = new String[leases.size()];
    String[] executionIds = new String[leases.size()];

    for (int index = 0; index < leases.size(); index++) {
      LockLease lease = leases.get(index);
      lockNames[index] = lease.getName();
      LockLease lockLease2 = leases.get(index);
      executionIds[index] = lockLease2.getExecutionId();
    }

    return executeAndReturn(
        c -> {
          Array lockNameArray = null;
          Array executionIdArray = null;
          try (final PreparedStatement ps = c.prepareStatement(RENEW_LOCK_SQL)) {
            lockNameArray = c.createArrayOf("text", lockNames);
            executionIdArray = c.createArrayOf("text", executionIds);

            ps.setArray(1, lockNameArray);
            ps.setArray(2, executionIdArray);
            ps.setLong(3, lockDuration.toMillis());

            try (ResultSet rs = ps.executeQuery()) {
              final List<LockLease> renewals = new ArrayList<>();
              while (rs.next()) {
                renewals.add(
                    new LockLease(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getObject(3, OffsetDateTime.class).toInstant().toEpochMilli()));
              }
              return renewals;
            }
          } finally {
            freeUp(lockNameArray);
            freeUp(executionIdArray);
          }
        },
        true);
  }

  /**
   * Safely frees a JDBC {@link Array} instance.
   *
   * <p>Errors are logged and suppressed as this method is used in cleanup paths.
   *
   * @param array SQL array to free; may be {@code null}
   */
  private void freeUp(Array array) {
    if (array != null) {
      try {
        array.free();
      } catch (SQLException e) {
        log.warn("Error freeing up sql array.", e);
      }
    }
  }
}
