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

package com.tsh.toolkit.core;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import com.tsh.toolkit.core.utils.Threads;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ThreadsTest {

  @Test
  void sleepShouldWaitApproximatelySpecifiedDuration() {
    long start = System.currentTimeMillis();
    Threads.sleep(100, TimeUnit.MILLISECONDS);
    long elapsed = System.currentTimeMillis() - start;

    // Assert that sleep took at least ~100ms (allowing small timing variation)
    assertTrue(
        elapsed >= 90 && elapsed < 300,
        "Sleep duration should be around 100ms, but was " + elapsed + "ms");
  }

  @Test
  void sleepShouldPreserveInterruptedStatus() throws InterruptedException {
    Thread testThread =
        new Thread(
            () -> {
              // Interrupt this thread before calling sleep
              Thread.currentThread().interrupt();
              Threads.sleep(50);
              // After sleep, interrupt flag should still be set
              assertTrue(
                  Thread.currentThread().isInterrupted(),
                  "Interrupt flag should be preserved after interrupted sleep");
            });

    testThread.start();
    testThread.join();
  }

  @Test
  void sleepShouldPreserveInterruptedStatusWhenInterruptedDuringSleep()
      throws InterruptedException {
    Thread testThread =
        new Thread(
            () -> {
              try {
                Threads.sleep(500);
                fail("Sleep should have been interrupted");
              } catch (Exception e) {
                fail("No exception should propagate: " + e);
              }
            });

    testThread.start();
    // Interrupt during sleep
    Thread.sleep(100);
    testThread.interrupt();

    testThread.join();
    // The thread should have preserved its interrupted flag
    assertTrue(testThread.isInterrupted(), "Thread should remain interrupted after sleep");
  }
}
