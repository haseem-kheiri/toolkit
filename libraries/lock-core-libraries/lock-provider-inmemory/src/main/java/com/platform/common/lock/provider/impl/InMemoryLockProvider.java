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

package com.platform.common.lock.provider.impl;

import com.tsh.toolkit.common.lock.DistributedLockProvider;
import com.tsh.toolkit.common.lock.LockHandle;
import com.tsh.toolkit.common.lock.impl.LockHandleImpl;
import com.tsh.toolkit.core.lifecycle.impl.AbstractLifecycleObject;
import com.tsh.toolkit.core.utils.Check;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** An in-memory lock provider. */
public class InMemoryLockProvider extends AbstractLifecycleObject
    implements DistributedLockProvider {
  private final Object mutex = new Object();
  private final Map<String, Long> locks = new HashMap<>();
  private final Map<String, String> owners = new HashMap<>();
  private final InMemoryLockProviderProperties properties;
  private ExecutorService workers;

  /** Constructs a In memory lock provider. */
  public InMemoryLockProvider(InMemoryLockProviderProperties properties) {
    this.properties = properties;
  }

  @Override
  protected void onStart() {
    workers = Executors.newSingleThreadExecutor();
    workers.execute(
        () ->
            whileUp(
                (status) -> cleanUpExpiredLocks(),
                properties.getCleanupIntervalDuration(),
                properties.getCleanupIntervalDurationUnit()));
  }

  @Override
  protected void onStop() {
    terminate(workers);
  }

  @Override
  public Optional<LockHandle> tryLock(String lockId, Duration ttl) {
    Check.requireNotBlank(lockId, () -> "Cannot acquire lock. Lock id is blank.");
    Check.requireNotNull(ttl, () -> "Cannot acquire lock. Ttl duration is null.");

    synchronized (mutex) {
      if (isLocked(lockId)) {
        return Optional.empty();
      }
      final String ownerId = UUID.randomUUID().toString();
      owners.put(lockId, ownerId);
      locks.put(lockId, System.currentTimeMillis() + ttl.toMillis());
      return Optional.of(new LockHandleImpl(lockId, ownerId));
    }
  }

  @Override
  public boolean releaseLock(LockHandle lockHandle) {
    isValid(lockHandle);
    synchronized (mutex) {
      if (lockHandle.getOwnerId().equals(owners.get(lockHandle.getLockId()))) {
        owners.remove(lockHandle.getLockId());
        locks.remove(lockHandle.getLockId());
        return true;
      }
      return false;
    }
  }

  private void isValid(LockHandle lockHandle) {
    Check.requireNotNull(lockHandle, () -> "Lock handle is null.");
    Check.requireNotBlank(lockHandle.getLockId(), () -> "Lock handle's lockId is blank.");
    Check.requireNotNull(lockHandle.getOwnerId(), () -> "Lock handle's ownerId is blank.");
  }

  @Override
  public boolean extendLock(LockHandle lockHandle, Duration ttl) {
    isValid(lockHandle);
    Check.requireNotNull(ttl, () -> "Ttl duration is null.");
    synchronized (mutex) {
      if (lockHandle.getOwnerId().equals(owners.get(lockHandle.getLockId()))) {
        final long currentExpiry = locks.getOrDefault(lockHandle.getLockId(), 0L);
        long base = Math.max(currentExpiry, System.currentTimeMillis());
        locks.put(lockHandle.getLockId(), base + ttl.toMillis());
        return true;
      }
      return false;
    }
  }

  @Override
  public boolean isLocked(String lockId) {
    synchronized (mutex) {
      Long expiresAt = locks.get(lockId);
      return expiresAt != null && expiresAt > System.currentTimeMillis();
    }
  }

  @Override
  public boolean isLocked(LockHandle lockHandle) {
    isValid(lockHandle);
    synchronized (mutex) {
      if (lockHandle.getOwnerId().equals(owners.get(lockHandle.getLockId()))) {
        return isLocked(lockHandle.getLockId());
      }
      return false;
    }
  }

  @Override
  public Optional<String> getOwner(String lockId) {
    Check.requireNotBlank(lockId, () -> "Lock id is blank.");
    synchronized (mutex) {
      return Optional.ofNullable(owners.get(lockId));
    }
  }

  @Override
  public boolean forceRelease(String lockId) {
    Check.requireNotBlank(lockId, () -> "Lock id is blank.");
    synchronized (mutex) {
      if (isLocked(lockId)) {
        owners.remove(lockId);
        locks.remove(lockId);
        return true;
      }
      return false;
    }
  }

  /** Cleans up expired locks. */
  public void cleanUpExpiredLocks() {
    synchronized (mutex) {
      long now = System.currentTimeMillis();
      for (Iterator<Map.Entry<String, Long>> it = locks.entrySet().iterator(); it.hasNext(); ) {
        Map.Entry<String, Long> entry = it.next();
        if (entry.getValue() <= now) {
          String lockId = entry.getKey();
          it.remove();
          owners.remove(lockId);
        }
      }
    }
  }
}
