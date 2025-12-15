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

package com.tsh.toolkit.core.lifecycle.impl;

import com.tsh.toolkit.core.lifecycle.LifecycleObject;
import com.tsh.toolkit.core.lifecycle.LifecycleObjectException;
import com.tsh.toolkit.core.lifecycle.LifecycleObjectStatusType;
import com.tsh.toolkit.core.utils.Run;
import com.tsh.toolkit.core.utils.ThreadPools;
import com.tsh.toolkit.core.utils.Threads;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/** Abstract life-cycle object. */
@Slf4j
public abstract class AbstractLifecycleObject implements LifecycleObject {
  /** Life-cycle runnable code block. */
  @FunctionalInterface
  public interface LifecycleRunnable<E extends Exception> {
    void run(Supplier<Boolean> isRunning) throws E;
  }

  private final Object mutex = new Object();
  private volatile LifecycleObjectStatusType status = LifecycleObjectStatusType.DOWN;

  @Override
  public final void start() {
    Run.runOnAcquiringLock(
        mutex,
        () -> !isRunning(),
        () -> {
          try {
            status = LifecycleObjectStatusType.UP;
            onStart();
          } catch (Exception e) {
            log.warn("Unexpected error in startup.", e);
            close();
            throw new LifecycleObjectException("Start up failed.", e);
          }
        });
  }

  protected abstract void onStart();

  @Override
  public void close() {
    stop();
  }

  @Override
  public final void stop() {
    Run.runOnAcquiringLock(
        mutex,
        () -> isRunning(),
        () -> {
          status = LifecycleObjectStatusType.DOWN;
          try {
            onStop();
          } catch (Exception e) {
            log.warn("Unexpected error in shutdown.", e);
          }
        });
  }

  protected abstract void onStop();

  protected <E extends Exception> void whileUp(LifecycleRunnable<E> block) {
    whileUp(block, 0, null);
  }

  protected <E extends Exception> void whileUp(
      LifecycleRunnable<E> block, long duration, TimeUnit unit) {
    while (isRunning()) {
      try {
        block.run(() -> isRunning());
      } catch (Exception e) {
        log.warn("Error in execution", e);
      }
      Run.runIfNotNull(unit, (u) -> sleep(duration, u));
    }
  }

  protected boolean isRunning() {
    synchronized (mutex) {
      return status == LifecycleObjectStatusType.UP;
    }
  }

  protected void sleep(long duration, TimeUnit unit) {
    Threads.sleep(duration, unit);
  }

  protected void terminateNow(ExecutorService es) {
    ThreadPools.terminateNow(es, 10, TimeUnit.SECONDS);
  }

  protected void terminateNow(ExecutorService es, long timeout, TimeUnit unit) {
    ThreadPools.terminateNow(es, timeout, unit);
  }

  protected void terminate(ExecutorService es) {
    ThreadPools.terminate(es, 10, TimeUnit.SECONDS);
  }

  protected void terminate(ExecutorService es, long timeout, TimeUnit unit) {
    ThreadPools.terminate(es, timeout, unit);
  }
}
