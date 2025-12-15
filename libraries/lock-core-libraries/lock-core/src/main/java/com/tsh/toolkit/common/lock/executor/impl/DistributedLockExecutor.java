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

package com.tsh.toolkit.common.lock.executor.impl;

import com.tsh.toolkit.common.lock.DistributedLockProvider;
import com.tsh.toolkit.common.lock.LockHandle;
import java.time.Duration;
import java.util.Optional;
import lombok.Getter;

/**
 * Facilitates execution of code protected by a distributed lock. Ensures proper acquisition and
 * release semantics.
 */
public class DistributedLockExecutor {

  /**
   * Represents the result of a distributed lock-protected execution.
   *
   * @param <T> the return type of the executed function
   */
  @Getter
  public static class LockResult<T> {
    private final boolean acquired;
    private final LockHandle lockHandle;
    private final T result;

    private LockResult(boolean acquired, LockHandle lockHandle, T result) {
      this.acquired = acquired;
      this.lockHandle = lockHandle;
      this.result = result;
    }
  }

  /**
   * A callback that returns a result when executed under a lock.
   *
   * @param <T> the return type of the callback
   * @param <E> the exception type the callback may throw
   */
  @FunctionalInterface
  public interface LockCallback<T, E extends Exception> {
    T execute(String lockOwnerId) throws E;
  }

  /**
   * A callback that returns no result (void) when executed under a lock.
   *
   * @param <E> the exception type the callback may throw
   */
  @FunctionalInterface
  public interface VoidLockCallback<E extends Exception> {
    void execute(String lockOwnerId) throws E;
  }

  private final DistributedLockProvider lockProvider;

  public DistributedLockExecutor(DistributedLockProvider lockProvider) {
    this.lockProvider = lockProvider;
  }

  /**
   * Attempts to acquire a lock and execute the given callback.
   *
   * @param lockId a unique identifier for the lock
   * @param ttl time-to-live for the lock
   * @param callback logic to execute once lock is acquired
   * @param <T> the return type of the callback
   * @param <E> exception type the callback may throw
   * @return result of the callback, wrapped in a {@link LockResult}
   * @throws E if the callback throws an exception
   */
  public <T, E extends Exception> LockResult<T> runWithLock(
      String lockId, Duration ttl, LockCallback<T, E> callback) throws E {

    Optional<LockHandle> maybeHandle = lockProvider.tryLock(lockId, ttl);

    if (maybeHandle.isPresent()) {
      LockHandle handle = maybeHandle.get();
      try {
        T result = callback.execute(handle.getOwnerId());
        return new LockResult<>(true, handle, result);
      } finally {
        lockProvider.releaseLock(handle);
      }
    }

    return new LockResult<>(false, null, null);
  }

  /**
   * Attempts to acquire a lock and execute the given void-returning callback.
   *
   * @param lockId a unique identifier for the lock
   * @param ttl time-to-live for the lock
   * @param callback logic to execute once lock is acquired
   * @param <E> exception type the callback may throw
   * @return result of the execution, wrapped in a {@link LockResult}
   * @throws E if the callback throws an exception
   */
  public <E extends Exception> LockResult<Void> runWithLock(
      String lockId, Duration ttl, VoidLockCallback<E> callback) throws E {

    return runWithLock(
        lockId,
        ttl,
        ownerId -> {
          callback.execute(ownerId);
          return null;
        });
  }
}
