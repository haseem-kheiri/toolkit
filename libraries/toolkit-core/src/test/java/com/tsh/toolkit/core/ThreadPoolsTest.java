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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.tsh.toolkit.core.utils.ThreadPools;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class ThreadPoolsTest {

  @Test
  void terminateShouldInvokeShutdownAndAwaitTermination() throws Exception {
    ExecutorService executor = mock(ExecutorService.class);
    when(executor.awaitTermination(anyLong(), any())).thenReturn(true);

    ThreadPools.terminate(executor, 1, TimeUnit.SECONDS);

    verify(executor).shutdown();
    verify(executor).awaitTermination(1, TimeUnit.SECONDS);
  }

  @Test
  void terminateNowShouldInvokeShutdownNowAndAwaitTermination() throws Exception {
    ExecutorService executor = mock(ExecutorService.class);
    when(executor.awaitTermination(anyLong(), any())).thenReturn(true);

    ThreadPools.terminateNow(executor, 500, TimeUnit.MILLISECONDS);

    verify(executor).shutdownNow();
    verify(executor).awaitTermination(500, TimeUnit.MILLISECONDS);
  }

  @Test
  void executeShouldRunTaskSuccessfully() {
    ExecutorService executor = mock(ExecutorService.class);

    AtomicBoolean ran = new AtomicBoolean(false);
    Runnable task = () -> ran.set(true);

    ThreadPools.execute(task, () -> true, executor);

    verify(executor).execute(task);
  }

  @Test
  void executeShouldRetryOnRejectedExecution() {
    ExecutorService executor = mock(ExecutorService.class);
    AtomicBoolean executed = new AtomicBoolean(false);

    doThrow(new RejectedExecutionException())
        .doAnswer(
            invocation -> {
              ((Runnable) invocation.getArgument(0)).run();
              executed.set(true);
              return null;
            })
        .when(executor)
        .execute(any(Runnable.class));

    Runnable task = () -> {};
    ThreadPools.execute(task, () -> true, executor);

    assertTrue(executed.get(), "Task should eventually execute after retry");
    verify(executor, atLeast(2)).execute(any(Runnable.class));
  }

  @Test
  void executeShouldStopRetryingWhenConditionBecomesFalse() {
    ExecutorService executor = mock(ExecutorService.class);

    // Always reject, but condition stops after one check
    AtomicBoolean conditionChecked = new AtomicBoolean(false);
    Supplier<Boolean> condition =
        () -> {
          if (conditionChecked.get()) return false;
          conditionChecked.set(true);
          return true;
        };

    doThrow(new RejectedExecutionException()).when(executor).execute(any(Runnable.class));

    Runnable task = () -> {};
    ThreadPools.execute(task, condition, executor);

    verify(executor, times(1)).execute(any(Runnable.class));
  }

  @Test
  void terminateShouldHandleInterruptedAwaitTermination() throws Exception {
    ExecutorService executor = mock(ExecutorService.class);
    when(executor.awaitTermination(anyLong(), any())).thenThrow(new InterruptedException());

    Thread.currentThread().interrupt(); // simulate interruption
    ThreadPools.terminate(executor, 100, TimeUnit.MILLISECONDS);

    verify(executor).shutdown();
    assertTrue(Thread.currentThread().isInterrupted(), "Interrupt status should be preserved");
  }

  @Test
  void executeShouldThrowWhenTaskOrExecutorOrConditionIsNull() {
    ExecutorService executor = mock(ExecutorService.class);
    Runnable task = () -> {};

    assertThrows(
        IllegalArgumentException.class, () -> ThreadPools.execute(null, () -> true, executor));
    assertThrows(IllegalArgumentException.class, () -> ThreadPools.execute(task, null, executor));
    assertThrows(IllegalArgumentException.class, () -> ThreadPools.execute(task, () -> true, null));
  }

  @Test
  void shouldRejectInvalidPoolSizes() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ThreadPools.createBoundedElasticThreadPool(-1, 1, 1, TimeUnit.SECONDS, 10));

    assertThrows(
        IllegalArgumentException.class,
        () -> ThreadPools.createBoundedElasticThreadPool(2, 1, 1, TimeUnit.SECONDS, 10));

    assertThrows(
        IllegalArgumentException.class,
        () -> ThreadPools.createBoundedElasticThreadPool(1, 0, 1, TimeUnit.SECONDS, 10));
  }

  @Test
  void shouldCreateExecutorWithBoundedQueue() {
    ThreadPoolExecutor executor =
        ThreadPools.createBoundedElasticThreadPool(1, 2, 10, TimeUnit.SECONDS, 5);

    BlockingQueue<Runnable> queue = executor.getQueue();

    assertTrue(queue instanceof LinkedBlockingQueue);
    assertEquals(5, queue.remainingCapacity());

    executor.shutdownNow();
  }

  @Test
  void shouldConfigureCoreAndMaxPoolSizes() {
    ThreadPoolExecutor executor =
        ThreadPools.createBoundedElasticThreadPool(2, 4, 30, TimeUnit.SECONDS, 10);

    assertEquals(2, executor.getCorePoolSize());
    assertEquals(4, executor.getMaximumPoolSize());

    executor.shutdownNow();
  }

  @Test
  void shouldAllowCoreThreadTimeout() {
    ThreadPoolExecutor executor =
        ThreadPools.createBoundedElasticThreadPool(1, 1, 1, TimeUnit.SECONDS, 10);

    assertTrue(executor.allowsCoreThreadTimeOut());

    executor.shutdownNow();
  }

  @Test
  void shouldExpandUpToMaxPoolSizeWhenQueueIsFull() throws Exception {
    ThreadPoolExecutor executor =
        ThreadPools.createBoundedElasticThreadPool(1, 2, 60, TimeUnit.SECONDS, 1);

    CountDownLatch latch = new CountDownLatch(1);

    // First task occupies core thread
    executor.execute(() -> await(latch));

    // Second task fills queue
    executor.execute(() -> await(latch));

    // Third task forces expansion to maxPoolSize
    executor.execute(() -> await(latch));

    // Allow threads to start
    Thread.sleep(100);

    assertEquals(2, executor.getPoolSize());

    latch.countDown();
    executor.shutdownNow();
  }

  @Test
  void shouldRejectTasksWhenPoolAndQueueAreSaturated() throws Exception {
    ThreadPoolExecutor executor =
        ThreadPools.createBoundedElasticThreadPool(1, 1, 60, TimeUnit.SECONDS, 1);

    CountDownLatch latch = new CountDownLatch(1);

    // Occupy core thread
    executor.execute(() -> await(latch));

    // Fill queue
    executor.execute(() -> await(latch));

    // Saturated: next task must be rejected
    assertThrows(RejectedExecutionException.class, () -> executor.execute(() -> {}));

    latch.countDown();
    executor.shutdownNow();
  }

  private static void await(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
