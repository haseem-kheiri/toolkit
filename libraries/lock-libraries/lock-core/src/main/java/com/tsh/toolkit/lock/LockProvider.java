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

package com.tsh.toolkit.lock;

import com.tsh.toolkit.lock.impl.LockManager;
import com.tsh.toolkit.lock.impl.LockManager.LockLease;
import java.time.Duration;
import java.util.List;

/**
 * LockProvider is a low-level abstraction responsible for implementing distributed <b>exclusive</b>
 * locks backed by an external shared system (e.g., a relational database, key-value store, or
 * coordination service).
 *
 * <p>This interface is intentionally minimal and designed to be driven exclusively by {@link
 * LockManager}. It does not expose blocking, fencing, or fairness guarantees.
 *
 * <p><b>Semantics and guarantees</b>:
 *
 * <ul>
 *   <li>All locks are <b>exclusive</b>; shared or read locks are not supported
 *   <li>Lock ownership is represented via time-bounded leases
 *   <li>Lease acquisition, renewal, and release are <b>best-effort</b>
 *   <li>Expired leases must be treated as not held
 * </ul>
 *
 * <p><b>Failure model</b>:
 *
 * <ul>
 *   <li>Lock contention is represented by a {@code null} return value
 *   <li>Infrastructure or provider failures must be surfaced via exceptions
 *   <li>Implementations must tolerate concurrent calls and lifecycle transitions
 * </ul>
 */
public interface LockProvider {

  /**
   * Attempts to acquire an exclusive lock for the given logical name.
   *
   * <p>If the lock is successfully acquired, a {@link LockLease} representing the newly acquired
   * lease is returned.
   *
   * <p>If the lock is currently held by another owner or an unexpired lease exists, {@code null}
   * must be returned.
   *
   * <p>Implementations must ensure that only <b>non-expired</b> leases are returned.
   *
   * <p>If an infrastructure or provider-level failure occurs (for example, database unavailability
   * or transaction failure), an exception must be thrown.
   *
   * @param lockName logical name identifying the lock
   * @param executionId correlation identifier associated with this execution attempt; this value
   *     must be persisted with the lease but does not grant fencing or ownership guarantees outside
   *     the provider
   * @param leaseDuration requested duration for which the lock lease should remain valid
   * @return a {@link LockLease} if the lock was acquired; {@code null} if not acquired
   * @throws Exception on provider or infrastructure failure
   */
  LockLease acquireLock(String lockName, String executionId, Duration leaseDuration);

  /**
   * Releases one or more previously acquired lock leases.
   *
   * <p>This operation must be <b>idempotent</b>. Releasing an already released, expired, or unknown
   * lease must not result in an error.
   *
   * <p>Implementations must tolerate concurrent invocation with {@link #renew}.
   *
   * <p>Releasing a lease does not guarantee that another caller will immediately acquire the same
   * lock.
   *
   * @param leases lock leases to release
   */
  void release(List<LockLease> leases);

  /**
   * Renews one or more active lock leases.
   *
   * <p>Renewal is <b>best-effort</b>. Implementations may silently ignore leases that are already
   * expired, released, or unknown.
   *
   * <p>Only leases that are successfully renewed must be returned. Returned leases must reflect
   * updated expiration timestamps.
   *
   * <p>Implementations must tolerate:
   *
   * <ul>
   *   <li>Duplicate renewal requests
   *   <li>Partial renewal success
   *   <li>Concurrent calls to {@link #release}
   * </ul>
   *
   * @param leases lock leases to renew
   * @param leaseDuration requested duration for which renewed leases should remain valid
   * @return list of successfully renewed {@link LockLease} instances; never {@code null}
   */
  List<LockLease> renew(List<LockLease> leases, Duration leaseDuration);
}
