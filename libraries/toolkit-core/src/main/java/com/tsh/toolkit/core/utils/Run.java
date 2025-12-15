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

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Run utility.
 *
 * @author Haseem Kheiri
 */
public class Run {

  /** Run runnable interface. */
  public interface RunRunable<E extends Exception> {
    /** run method to override. */
    void run() throws E;
  }

  /** Run consumer interface. */
  @FunctionalInterface
  public interface RunConsumer<T, E extends Exception> {
    /** accept method to override. */
    void accept(T t) throws E;
  }

  /** Run function interface. */
  @FunctionalInterface
  public interface RunFunction<T, R, E extends Exception> {
    /** apply method to override. */
    R apply(T t) throws E;
  }

  /** Run supplier interface. */
  @FunctionalInterface
  public interface RunSupplier<T, E extends Exception> {
    /** get method to override. */
    T get() throws E;
  }

  /** If condition is true executes the consumer. */
  public static <T, E extends Exception> void runIfTrue(
      boolean condition, T t, RunConsumer<T, E> con) throws E {
    if (condition) {
      con.accept(t);
    }
  }

  /** If supplied value is not null executes the consumer. */
  public static <T, E1 extends Exception, E2 extends Exception> void runIfSuppliedIsNotNull(
      RunSupplier<T, E1> s, RunConsumer<T, E2> con) throws E1, E2 {
    final T t = s.get();
    runIfTrue(t != null, t, con);
  }

  /** If supplied value is not null executes the consumer. */
  public static <T, E extends Exception> void runIfNull(T t, RunRunable<E> runner) throws E {
    runIfTrue(t == null, t, (o) -> runner.run());
  }

  /** Runs consumer if parameter o is not null. */
  public static <T, E extends Exception> void runIfNotNull(T o, RunConsumer<T, E> con) throws E {
    runIfSuppliedIsNotNull(() -> o, con);
  }

  /** Runs consumer if string is blank. */
  public static <E extends Exception> void runIfBlank(String s, RunRunable<E> runner) throws E {
    runIfTrue(s == null || s.isBlank(), null, (o) -> runner.run());
  }

  /** Runs consumer if string is not blank. */
  public static <E extends Exception> void runIfNotBlank(String s, RunConsumer<String, E> con)
      throws E {
    runIfTrue(s != null && !s.isBlank(), null, con);
  }

  /** If condition is true executes the function and returns the result. */
  public static <T, R, E extends Exception> R runAndReturnIfTrue(
      boolean condition, T t, RunFunction<T, R, E> fn) throws E {
    if (condition) {
      return fn.apply(t);
    }
    return null;
  }

  /** If supplied value is not null executes the function and returns the result. */
  public static <T, R, E1 extends Exception, E2 extends Exception> R runAndReturnIfSuppliedNotNull(
      RunSupplier<T, E1> s, RunFunction<T, R, E2> fn) throws E1, E2 {
    final T t = s.get();
    return runAndReturnIfTrue(t != null, t, fn);
  }

  /** Runs consumer if collection is not null not empty. */
  public static <T extends Collection<?>, E extends Exception> void runIfCollectionNotNullNorEmpty(
      T t, RunConsumer<T, E> con) throws E {
    runIfTrue(t != null && !t.isEmpty(), t, con);
  }

  /** Runs consumer if map is not null not empty. */
  public static <K, V, E extends Exception> void runIfMapNotNullNorEmpty(
      Map<K, V> t, RunConsumer<Map<K, V>, E> con) throws E {
    runIfTrue(t != null && !t.isEmpty(), t, con);
  }

  /** Runs function if parameter o is not null. */
  public static <T, R, E extends Exception> R runAndReturnIfNotNull(T o, RunFunction<T, R, E> fn)
      throws E {
    return runAndReturnIfSuppliedNotNull(() -> o, fn);
  }

  /** If supplied value is null executes the supplier and returns the result. */
  public static <T, R, E extends Exception> R runAndReturnIfNull(T t, RunSupplier<R, E> supplier)
      throws E {
    return runAndReturnIfTrue(t == null, t, (o) -> supplier.get());
  }

  /** Runs function if string is not blank. */
  public static <R, E extends Exception> R runAndReturnIfNotBlank(
      String s, RunFunction<String, R, E> fn) throws E {
    return runAndReturnIfTrue(s != null && !s.isBlank(), s, fn);
  }

  /** Runs function if string is not blank. */
  public static <R, E extends Exception> R runAndReturnIfBlank(String s, RunSupplier<R, E> supplier)
      throws E {
    return runAndReturnIfTrue(s == null || s.isBlank(), null, (o) -> supplier.get());
  }

  /** Runs function if collection is not null not empty. */
  public static <T extends Collection<?>, R, E extends Exception>
      R runAndReturnIfCollectionNotNullNorEmpty(T t, RunFunction<T, R, E> fn) throws E {
    return runAndReturnIfTrue(t != null && !t.isEmpty(), t, fn);
  }

  /** Runs function if map is not null not empty. */
  public static <K, V, R, E extends Exception> R runAndReturnIfMapNotNullNorEmpty(
      Map<K, V> t, RunFunction<Map<K, V>, R, E> fn) throws E {
    return runAndReturnIfTrue(t != null && !t.isEmpty(), t, fn);
  }

  /** Runs the runnable is lock is acquired. */
  public static <E extends Exception> void runOnAcquiringLock(
      Object mutex, Supplier<Boolean> condition, RunRunable<E> runnable) throws E {
    if (condition.get()) {
      synchronized (mutex) {
        if (condition.get()) {
          runnable.run();
        }
      }
    }
  }
}
