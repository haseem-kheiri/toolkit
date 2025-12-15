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

package com.tsh.toolkit.core.utils;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/** Utility methods for thread operations. */
@Slf4j
public final class Threads {
  private Threads() {}

  public static void sleep(long duration, TimeUnit unit) {
    sleep(unit.toMillis(duration));
  }

  /**
   * Causes the current thread to sleep for the given time.
   *
   * <p>If interrupted, this method preserves the thread's interrupt status and logs the
   * interruption at debug level (since this is often expected during shutdown).
   */
  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // preserve interrupt flag
      log.debug("Thread interrupted during sleep", e);
    }
  }
}
