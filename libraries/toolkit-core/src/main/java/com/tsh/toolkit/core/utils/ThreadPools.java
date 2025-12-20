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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility methods for managing {@link ExecutorService} instances.
 *
 * <p>Provides convenient static methods to:
 *
 * <ul>
 *   <li>Shut down thread pools gracefully or forcefully
 *   <li>Execute tasks safely with retry behavior when a pool rejects submissions
 * </ul>
 *
 * <p>This utility centralizes best practices for working with thread pools — ensuring safe
 * shutdown, proper termination, and resilient task submission.
 */
@Slf4j
public final class ThreadPools {

  /** Functional interface representing an action to be performed on an {@link ExecutorService}. */
  @FunctionalInterface
  private interface PoolAction {
    /**
     * Performs an action on the given executor service.
     *
     * @param executor the executor service
     */
    void accept(ExecutorService executor);
  }

  private ThreadPools() {}

  private static void awaitTermination(ExecutorService executor, long timeout, TimeUnit unit) {
    try {
      boolean terminated = executor.awaitTermination(timeout, unit);
      if (!terminated) {
        log.warn("Executor did not terminate within {} {}", timeout, unit);
      }
    } catch (InterruptedException e) {
      log.warn("Executor shutdown interrupted", e);
      Thread.currentThread().interrupt(); // restore interrupted status
    }
  }

  /**
   * Creates a bounded, elastic {@link ThreadPoolExecutor} with core thread timeout enabled.
   *
   * <p>The executor scales between {@code corePoolSize} and {@code maxPoolSize}, uses a bounded
   * queue for backpressure, and allows all threads (including core threads) to terminate when idle.
   *
   * <p>Defaults:
   *
   * <ul>
   *   <li>Keep-alive time: 60 seconds
   *   <li>Queue capacity: 5
   * </ul>
   *
   * @param corePoolSize number of threads to keep under steady load
   * @param maxPoolSize maximum number of threads allowed
   * @return a configured {@link ThreadPoolExecutor}
   */
  public static ThreadPoolExecutor createBoundedElasticThreadPool(
      int corePoolSize, int maxPoolSize) {
    return createBoundedElasticThreadPool(corePoolSize, maxPoolSize, 60L, TimeUnit.SECONDS);
  }

  /**
   * Creates a bounded, elastic {@link ThreadPoolExecutor} with a custom keep-alive time.
   *
   * <p>Defaults:
   *
   * <ul>
   *   <li>Queue capacity: 5
   * </ul>
   *
   * @param corePoolSize number of threads to keep under steady load
   * @param maxPoolSize maximum number of threads allowed
   * @param keepAliveTime time idle threads wait before terminating
   * @param unit time unit for {@code keepAliveTime}
   * @return a configured {@link ThreadPoolExecutor}
   */
  public static ThreadPoolExecutor createBoundedElasticThreadPool(
      int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit unit) {
    return createBoundedElasticThreadPool(corePoolSize, maxPoolSize, keepAliveTime, unit, 5);
  }

  /**
   * Creates a fully configurable bounded, elastic {@link ThreadPoolExecutor}.
   *
   * <p>Tasks are rejected with {@link java.util.concurrent.RejectedExecutionException} when both
   * the thread limit and queue capacity are exhausted.
   *
   * @param corePoolSize number of threads to keep under steady load
   * @param maxPoolSize maximum number of threads allowed
   * @param keepAliveTime time idle threads wait before terminating
   * @param unit time unit for {@code keepAliveTime}
   * @param capacity maximum number of queued tasks
   * @return a configured {@link ThreadPoolExecutor}
   * @throws IllegalArgumentException if pool size configuration is invalid
   */
  public static ThreadPoolExecutor createBoundedElasticThreadPool(
      int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit unit, int capacity) {

    if (corePoolSize < 0 || maxPoolSize <= 0 || corePoolSize > maxPoolSize) {
      throw new IllegalArgumentException("Invalid pool size configuration");
    }

    ThreadPoolExecutor tpe =
        new ThreadPoolExecutor(
            corePoolSize, maxPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(capacity));

    tpe.allowCoreThreadTimeOut(true);
    return tpe;
  }

  /**
   * Attempts to stop all actively executing tasks and halts processing of waiting tasks.
   *
   * <p>After invocation, tasks that were waiting for execution may be returned by the executor.
   *
   * @param executor the executor service to terminate
   * @param timeout maximum time to wait for termination
   * @param unit time unit of the timeout argument
   */
  public static void terminateNow(ExecutorService executor, long timeout, TimeUnit unit) {
    Run.runIfNotNull(
        executor,
        e -> {
          e.shutdownNow();
          awaitTermination(e, timeout, unit);
        });
  }

  /**
   * Initiates an orderly shutdown of the executor service.
   *
   * <p>Previously submitted tasks are executed, but no new tasks will be accepted. If the executor
   * is already shut down, this method has no additional effect.
   *
   * @param executor the executor service to shut down
   * @param timeout maximum time to wait for termination
   * @param unit time unit of the timeout argument
   */
  public static void terminate(ExecutorService executor, long timeout, TimeUnit unit) {
    Run.runIfNotNull(
        executor,
        e -> {
          e.shutdown();
          awaitTermination(e, timeout, unit);
        });
  }

  /**
   * Executes a task using the given executor service while the condition is {@code true}.
   *
   * <p>If the executor rejects the task, this method retries until the condition becomes {@code
   * false}.
   *
   * @param task the task to execute
   * @param condition a supplier returning {@code true} to continue execution attempts
   * @param executor the executor service to run the task
   */
  public static void execute(Runnable task, Supplier<Boolean> condition, ExecutorService executor) {
    Check.requireNotNull(task, () -> "Cannot execute, task is null.");
    Check.requireNotNull(condition, () -> "Cannot execute, condition is null.");
    Check.requireNotNull(executor, () -> "Cannot execute, executor is null.");
    execute(task, condition, executor, ex -> ex.execute(task));
  }

  /**
   * Executes a task using a custom {@link PoolAction} while the condition is {@code true}.
   *
   * <p>Retries submission if the executor rejects it, sleeping briefly between attempts.
   *
   * @param task the task to execute
   * @param condition a supplier returning {@code true} to continue execution attempts
   * @param executor the executor service to run the task
   * @param action a custom action defining how to submit the task
   */
  public static void execute(
      Runnable task, Supplier<Boolean> condition, ExecutorService executor, PoolAction action) {
    boolean executed = false;
    while (!executed && Boolean.TRUE.equals(condition.get())) {
      try {
        action.accept(executor);
        executed = true;
      } catch (RejectedExecutionException e) {
        log.trace("Executor rejected task, retrying in 50ms", e);
        Threads.sleep(50);
      }
    }
  }
}
