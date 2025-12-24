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

package com.tsh.toolkit.lock.impl;

import com.tsh.toolkit.core.lifecycle.impl.AbstractLifecycleObject;
import com.tsh.toolkit.core.utils.Check;
import com.tsh.toolkit.core.utils.ThreadPools;
import com.tsh.toolkit.core.utils.Uuids;
import com.tsh.toolkit.lock.LockException;
import com.tsh.toolkit.lock.LockProvider;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * LockManager provides scoped, exclusive execution using distributed locks.
 *
 * <p>Locks are backed by an external {@link LockProvider} (e.g., PostgreSQL) and are held only for
 * the duration of a user-supplied execution block.
 *
 * <p>The manager is responsible for:
 *
 * <ul>
 *   <li>Acquiring exclusive locks
 *   <li>Executing user code while the lock is held
 *   <li>Renewing lock leases in the background
 *   <li>Releasing locks on completion or shutdown
 * </ul>
 *
 * <p>All locks are <b>exclusive</b>. Shared or read locks are intentionally not supported.
 *
 * <p><b>Failure semantics</b>:
 *
 * <ul>
 *   <li>Infrastructure failures (e.g., database unavailable) are propagated as {@link
 *       LockException}
 *   <li>Lock contention results in a non-executed result
 *   <li>User block exceptions are propagated to the caller
 * </ul>
 */
@Slf4j
public class LockManager extends AbstractLifecycleObject {
  private static final long RENEW_LEASE_POLL_DURATION = 3;
  private static final TimeUnit RENEW_LEASE_POLL_DURATION_UNIT = TimeUnit.SECONDS;
  private static final long RENEWAL_THRESHOLD_MILLIS =
      RENEW_LEASE_POLL_DURATION_UNIT.toMillis(RENEW_LEASE_POLL_DURATION) * 3;
  private static final Duration LOCK_LEASE_DURATION =
      Duration.ofMillis(RENEW_LEASE_POLL_DURATION_UNIT.toMillis(RENEW_LEASE_POLL_DURATION) * 10);

  /**
   * Result of attempting to execute code under an exclusive lock.
   *
   * <p>If {@link #executed} is {@code false}, the lock could not be acquired and the execution
   * block was not run.
   *
   * <p>If {@link #executed} is {@code true}, the execution block ran successfully under lock
   * ownership.
   *
   * <p>The {@link #executionId} is a correlation identifier associated with this execution. It is
   * <b>not</b> a lock token and does not grant any synchronization or ownership rights.
   */
  @Getter
  public static class LockExecutionResult<T> {

    /** Whether the execution block was run under an acquired lock. */
    private final boolean executed;

    /**
     * Correlation identifier associated with this execution.
     *
     * <p>This identifier may be used to mark or retrieve data produced during the locked execution.
     */
    private final String executionId;

    /** Result returned by the execution block. */
    private final T result;

    private LockExecutionResult(String executionId) {
      this.executed = false;
      this.executionId = executionId;
      this.result = null;
    }

    private LockExecutionResult(String executionId, T result) {
      this.executed = true;
      this.executionId = executionId;
      this.result = result;
    }
  }

  /**
   * Represents a held lock lease.
   *
   * <p>This object is valid only for the duration of the execution block and must not be retained
   * or used outside of it.
   *
   * <p>The lease does not expose renewal or release operations; these responsibilities belong
   * exclusively to {@link LockManager}.
   */
  @Getter
  public static class LockLease {

    /** Logical name of the lock. */
    private final String name;

    /** Execution correlation identifier associated with this lease. */
    private final String executionId;

    /** Epoch timestamp (milliseconds) at which the lease expires. */
    private final long expiresAt;

    /**
     * Creates a new lock lease representation.
     *
     * <p>This object represents the caller's current view of a held lock lease as returned by the
     * {@link LockProvider}. It is a snapshot of lease state at a point in time and does not confer
     * any ownership guarantees by itself.
     *
     * @param name logical name of the lock
     * @param executionId correlation identifier associated with this execution attempt; this value
     *     is not a lock token and must not be used for fencing or synchronization
     * @param expiresAt epoch timestamp (milliseconds) at which the provider considers the lease
     *     expired; this value is informational and may not precisely align with the local system
     *     clock
     */
    public LockLease(String name, String executionId, long expiresAt) {
      this.name = name;
      this.executionId = executionId;
      this.expiresAt = expiresAt;
    }

    /**
     * Indicates whether the lease is still valid at the time of invocation.
     *
     * <p>This check is advisory only and does not provide fencing guarantees.
     */
    public boolean isValid() {
      return System.currentTimeMillis() < expiresAt;
    }
  }

  /** Exclusive execution block without a return value. */
  @FunctionalInterface
  public interface ExclusiveBlock {
    void run(LockLease lock);
  }

  /** Exclusive execution block with a return value. */
  @FunctionalInterface
  public interface ExclusiveSupplier<T> {
    T run(LockLease lock);
  }

  private final LockProvider lockProvider;
  private ExecutorService worker;

  /**
   * Active lock leases currently held by this manager.
   *
   * <p>This set is used exclusively for lease renewal and shutdown cleanup.
   */
  private final Map<String, LockLease> activeLocks = new ConcurrentHashMap<>();

  public LockManager(LockProvider lockProvider) {
    this.lockProvider = Check.requireNotNull(lockProvider, () -> "lock provider must not be null.");
  }

  @Override
  protected void onStart() {
    worker = Executors.newFixedThreadPool(1);
    ThreadPools.execute(this::renewLeases, this::isRunning, worker);
  }

  @Override
  protected void onStop() {
    lockProvider.release(new ArrayList<>(activeLocks.values()));
    ThreadPools.terminate(worker, 15, TimeUnit.SECONDS);
  }

  /**
   * Periodically renews leases that are approaching expiration.
   *
   * <p>Renewal is best-effort. Failure to renew may result in lock loss, which callers must
   * tolerate.
   */
  private void renewLeases() {
    whileUp(
        (running) -> {
          final long now = System.currentTimeMillis();
          Map<String, LockLease> snapshot;
          synchronized (activeLocks) {
            snapshot = Map.copyOf(activeLocks);
          }

          Map<String, LockLease> renew =
              snapshot.values().stream()
                  .filter(l -> l.expiresAt - now <= RENEWAL_THRESHOLD_MILLIS)
                  .collect(Collectors.toMap(l -> l.getExecutionId(), l -> l));

          Map<String, LockLease> renewed =
              lockProvider.renew(new ArrayList<>(renew.values()), LOCK_LEASE_DURATION).stream()
                  .collect(Collectors.toMap(l -> l.executionId, l -> l));

          renew
              .entrySet()
              .forEach(
                  e -> {
                    final String key = e.getKey();
                    if (renewed.containsKey(key)) {
                      activeLocks.put(key, renewed.get(key));
                    } else {
                      activeLocks.remove(key);
                    }
                  });
        },
        RENEW_LEASE_POLL_DURATION,
        RENEW_LEASE_POLL_DURATION_UNIT);
  }

  /**
   * Attempts to execute the supplied block under an exclusive lock.
   *
   * @param lockName logical lock name
   * @param block execution block
   * @return execution result
   */
  public LockExecutionResult<Void> tryLock(String lockName, ExclusiveBlock block) {
    return tryLock(
        lockName,
        (lock) -> {
          block.run(lock);
          return null;
        });
  }

  /**
   * Attempts to execute the supplied block under an exclusive lock.
   *
   * <p>If the lock cannot be acquired due to contention, the block is not executed and {@link
   * LockExecutionResult#executed} will be {@code false}.
   *
   * @param lockName logical lock name
   * @param block execution block
   * @return execution result
   * @throws LockException if the lock provider fails
   */
  public <T> LockExecutionResult<T> tryLock(String lockName, ExclusiveSupplier<T> block) {

    final String executionId = Uuids.uuid7().toString();
    LockLease lock = null;

    try {
      try {
        lock = lockProvider.acquireLock(lockName, executionId, LOCK_LEASE_DURATION);
        if (lock != null) {
          activeLocks.put(lock.executionId, lock);
        }
      } catch (Exception e) {
        throw new LockException("Unable to acquire lock.", e);
      }

      if (lock == null) {
        return new LockExecutionResult<>(executionId);
      }

      return new LockExecutionResult<>(executionId, block.run(lock));

    } finally {
      if (lock != null) {
        try {
          activeLocks.remove(lock.executionId);
          lockProvider.release(List.of(lock));
        } catch (Exception e) {
          log.warn("Error releasing lock.", e);
        }
      }
    }
  }
}
