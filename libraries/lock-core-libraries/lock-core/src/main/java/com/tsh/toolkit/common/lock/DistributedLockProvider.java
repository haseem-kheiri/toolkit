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

package com.tsh.toolkit.common.lock;

import java.time.Duration;
import java.util.Optional;

/**
 * Contract for a distributed lock provider supporting pluggable implementations (e.g., Redis,
 * RDBMS, file-based).
 *
 * @author Haseem Kheiri
 */
public interface DistributedLockProvider {

  /**
   * Attempts to acquire a lock with the given TTL (time-to-live). If successful, returns a {@link
   * LockHandle} representing the lock ownership.
   *
   * @param lockId the unique identifier for the lock
   * @param ttl the duration the lock will be held before expiring automatically
   * @return an {@code Optional} containing the {@code LockHandle} if the lock was acquired, or
   *     empty otherwise
   */
  Optional<LockHandle> tryLock(String lockId, Duration ttl);

  /**
   * Releases the lock if it is currently held by the provided {@link LockHandle}'s owner.
   *
   * @param lockHandle the handle representing the lock and its owner
   * @return true if the lock was successfully released
   */
  boolean releaseLock(LockHandle lockHandle);

  /**
   * Extends the TTL (time-to-live) of the lock to a new value, if it is still held by the owner
   * represented by the given {@link LockHandle}.
   *
   * @param lockHandle the handle representing the lock and its owner
   * @param ttl the new TTL duration to apply
   * @return true if the TTL was successfully updated
   */
  boolean extendLock(LockHandle lockHandle, Duration ttl);

  /**
   * Returns whether the lock with the given ID is currently held by any owner.
   *
   * @param lockId the unique identifier of the lock
   * @return true if the lock is currently held
   */
  boolean isLocked(String lockId);

  /**
   * Returns whether the lock is currently held by the owner represented by the given {@link
   * LockHandle}.
   *
   * @param lockHandle the handle representing the lock and its owner
   * @return true if the lock is currently held by this owner
   */
  boolean isLocked(LockHandle lockHandle);

  /**
   * Returns the current owner ID of the lock, if held.
   *
   * @param lockId the unique identifier of the lock
   * @return an {@code Optional} containing the owner ID if held, or empty if not
   */
  Optional<String> getOwner(String lockId);

  /**
   * Forcefully releases the lock regardless of ownership. Intended for administrative or recovery
   * purposes.
   *
   * @param lockId the unique identifier of the lock
   * @return true if the lock was deleted or did not exist
   */
  boolean forceRelease(String lockId);
}
