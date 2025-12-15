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

package com.tsh.toolkit.lifecycle.impl;

import com.tsh.toolkit.core.lifecycle.LifecycleObjectException;
import com.tsh.toolkit.core.lifecycle.impl.AbstractLifecycleObject;
import com.tsh.toolkit.core.utils.Check;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for a life-cycle object.
 *
 * @author Haseem Kheiri
 */
@Slf4j
class AbstractLifecycleObjectTest {

  public class TestLifecycleObject extends AbstractLifecycleObject {
    private AtomicInteger n;
    private ExecutorService worker;
    private boolean terminateNow;

    public TestLifecycleObject(AtomicInteger n, boolean terminateNow) {
      this.n = n;
      this.terminateNow = terminateNow;
    }

    @Override
    protected void onStart() {
      worker = Executors.newSingleThreadExecutor();
      Check.requireNotNull(n, () -> "Supplied integer is null.");
      worker.execute(
          () ->
              whileUp(
                  status -> {
                    n.incrementAndGet();
                    Check.requireTrue(n.get() >= 0, () -> "Integer should be zero or more.");
                  },
                  25,
                  TimeUnit.MILLISECONDS));
    }

    @Override
    protected void onStop() {
      if (terminateNow) {
        terminateNow(worker, 1, TimeUnit.SECONDS);
      } else {
        terminate(worker, 1, TimeUnit.SECONDS);
      }
      terminate(worker, 1, TimeUnit.SECONDS);
      Check.requireNotNull(n, () -> "Supplied integer is null.");
      n = null;
    }
  }

  @Test
  void testStartAndStopFails() {
    final LifecycleObjectException e =
        Assertions.assertThrows(
            LifecycleObjectException.class,
            () -> {
              try (final TestLifecycleObject o = new TestLifecycleObject(null, false)) {
                o.start();
              }
            });
    Assertions.assertEquals("Start up failed.", e.getLocalizedMessage());
  }

  @Test
  void testStartAndStopUsingClosePasses() throws InterruptedException {
    try (final TestLifecycleObject o = new TestLifecycleObject(new AtomicInteger(0), false)) {
      o.start();
      Thread.sleep(100);
      System.err.println(o.n.get());
      Assertions.assertTrue(o.n.get() >= 3);
    }
  }

  @Test
  void testStartAndStopUsingClosePassesTerminateNow() throws InterruptedException {
    try (final TestLifecycleObject o = new TestLifecycleObject(new AtomicInteger(-2), true)) {
      o.start();
      o.start(); // Should have no affect.
      Thread.sleep(100);
      System.err.println(o.n.get());
      Assertions.assertTrue(o.n.get() >= 1);
      o.stop(); // close should have no affect.
    }
  }
}
