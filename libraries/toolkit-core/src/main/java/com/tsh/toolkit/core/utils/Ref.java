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

/**
 * Mutable reference holder intended primarily for use with Java lambdas and anonymous classes where
 * captured variables must be effectively final.
 *
 * <p>This class allows a value to be read and updated indirectly through a stable reference,
 * enabling mutation within lambda expressions.
 *
 * <p>This type is deliberately minimal and carries no concurrency guarantees. If thread-safety is
 * required, consider {@link java.util.concurrent.atomic.AtomicReference} instead.
 *
 * @param <T> the type of the referenced value
 */
public final class Ref<T> {

  /** The referenced value. May be {@code null}. */
  private T value;

  private Ref(T value) {
    this.value = value;
  }

  /**
   * Creates a new mutable reference holding the given value.
   *
   * @param value the initial value; may be {@code null}
   * @param <T> the type of the referenced value
   * @return a new {@code Ref} instance
   */
  public static <T> Ref<T> of(T value) {
    return new Ref<>(value);
  }

  /**
   * Returns the current value held by this reference.
   *
   * @return the current value, which may be {@code null}
   */
  public T get() {
    return value;
  }

  /**
   * Updates the value held by this reference.
   *
   * @param value the new value; may be {@code null}
   */
  public void set(T value) {
    this.value = value;
  }
}
