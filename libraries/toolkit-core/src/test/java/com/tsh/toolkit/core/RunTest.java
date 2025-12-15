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

import com.tsh.toolkit.core.utils.Run;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for Run utility.
 *
 * @author Haseem Kheiri
 */
class RunTest {
  @Test
  void testRunIfNull() {
    final Boolean[] b = {null};
    Run.runIfNull("not null", () -> b[0] = true);
    Assertions.assertEquals(null, b[0]);

    Run.runIfNull(null, () -> b[0] = true);
    Assertions.assertEquals(true, b[0]);
  }

  @Test
  void testRunIfNotNull() {
    final Boolean[] b = {null};
    Run.runIfNotNull(null, o -> b[0] = o != null);
    Assertions.assertEquals(null, b[0]);

    Run.runIfNotNull("", o -> b[0] = o != null);
    Assertions.assertEquals(true, b[0]);
  }

  @Test
  void testRunIfBlank() {
    final Boolean[] b = {null};
    Run.runIfBlank("not blank", () -> b[0] = true);
    Assertions.assertEquals(null, b[0]);

    Run.runIfBlank(null, () -> b[0] = true);
    Assertions.assertEquals(true, b[0]);

    Run.runIfBlank("", () -> b[0] = true);
    Assertions.assertEquals(true, b[0]);
  }

  @Test
  void testRunIfNotBlank() {
    final Boolean[] b = {null};
    Run.runIfNotBlank(null, s -> b[0] = true);
    Assertions.assertEquals(null, b[0]);

    Run.runIfNotBlank("", s -> b[0] = true);
    Assertions.assertEquals(null, b[0]);

    Run.runIfNotBlank("Not blank", s -> b[0] = true);
    Assertions.assertEquals(true, b[0]);
  }

  @Test
  void testReturnIfNull() {
    Boolean b = Run.runAndReturnIfNull("not null", () -> true);
    Assertions.assertEquals(null, b);
    b = Run.runAndReturnIfNull(null, () -> true);
    Assertions.assertEquals(true, b.booleanValue());
  }

  @Test
  void testReturnIfNotNull() {
    Boolean b = Run.runAndReturnIfNotNull(null, o -> true);
    Assertions.assertEquals(null, b);
    b = Run.runAndReturnIfNotNull("", o -> true);
    Assertions.assertEquals(true, b.booleanValue());
  }

  @Test
  void testRunAndReturnIfBlank() {
    Boolean b = Run.runAndReturnIfBlank("not blank", () -> true);
    Assertions.assertEquals(null, b);
    b = Run.runAndReturnIfBlank(null, () -> true);
    Assertions.assertEquals(true, b.booleanValue());
    b = Run.runAndReturnIfBlank("", () -> true);
    Assertions.assertEquals(true, b.booleanValue());
  }

  @Test
  void testRunAndReturnIfNotBlank() {
    final Boolean[] b = {null};
    Run.runAndReturnIfNotBlank(null, s -> b[0] = s == null);
    Assertions.assertEquals(null, b[0]);

    Run.runAndReturnIfNotBlank("", s -> b[0] = s != null);
    Assertions.assertEquals(null, b[0]);

    Run.runAndReturnIfNotBlank("Not blank", s -> b[0] = s != null && !s.isBlank());
    Assertions.assertEquals(true, b[0]);
  }

  @Test
  void testRunOnNotNullNorEmpty() {
    final Boolean[] b = {null};
    Run.runIfCollectionNotNullNorEmpty((List<?>) null, o -> b[0] = o != null);
    Assertions.assertEquals(null, b[0]);

    Run.runIfMapNotNullNorEmpty((Map<?, ?>) null, o -> b[0] = o != null);
    Assertions.assertEquals(null, b[0]);

    Run.runIfCollectionNotNullNorEmpty(List.of(), o -> b[0] = o != null);
    Assertions.assertEquals(null, b[0]);

    Run.runIfMapNotNullNorEmpty(Map.of(), o -> b[0] = o != null);
    Assertions.assertEquals(null, b[0]);

    Run.runIfCollectionNotNullNorEmpty(List.of("values"), o -> b[0] = o != null && !o.isEmpty());
    Assertions.assertEquals(true, b[0]);

    Run.runIfMapNotNullNorEmpty(Map.of("keys", "values"), o -> b[0] = o != null && !o.isEmpty());
    Assertions.assertEquals(true, b[0]);
  }

  @Test
  void testReturnOnNotNullNorEmpty() {
    Boolean b = Run.runAndReturnIfCollectionNotNullNorEmpty((Set<?>) null, o -> true);
    Assertions.assertEquals(null, b);

    b = Run.runAndReturnIfMapNotNullNorEmpty((Map<?, ?>) null, o -> true);
    Assertions.assertEquals(null, b);

    b = Run.runAndReturnIfCollectionNotNullNorEmpty(Set.of(), o -> true);
    Assertions.assertEquals(null, b);

    b = Run.runAndReturnIfMapNotNullNorEmpty(Map.of(), o -> true);
    Assertions.assertEquals(null, b);

    b = Run.runAndReturnIfCollectionNotNullNorEmpty(Set.of("values"), o -> true);
    Assertions.assertEquals(true, b.booleanValue());

    b = Run.runAndReturnIfMapNotNullNorEmpty(Map.of("keys", "values"), o -> true);
    Assertions.assertEquals(true, b.booleanValue());
  }

  @Test
  void testRunOnAcquiringLock() throws InterruptedException {
    final Object lock = new Object();
    final AtomicLong lockDuration = new AtomicLong();

    new Thread(
            () -> {
              try {
                Run.runOnAcquiringLock(
                    lock,
                    () -> true,
                    () -> {
                      lockDuration.set(System.currentTimeMillis());
                      Thread.sleep(100);
                    });
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            })
        .start();
    Thread.sleep(50);
    new Thread(
            () -> {
              Run.runOnAcquiringLock(
                  lock,
                  () -> true,
                  () -> {
                    lockDuration.set(System.currentTimeMillis() - lockDuration.get());
                  });
            })
        .start();

    Thread.sleep(500);
    Assertions.assertTrue(lockDuration.get() > 100);
  }
}
