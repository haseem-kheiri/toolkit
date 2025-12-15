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

import java.util.Map;

/** Utility Maps. */
public class Maps {

  /** Supplies a default key value. */
  @FunctionalInterface
  public interface MapDefaultValueSupplier<T, E extends Exception> {
    /** Gets a default key value. */
    T getDefault() throws E;
  }

  /** Gets a value form a map if found else put a default value for the key and returns it. */
  public static <K, V, E extends Exception> V get(
      Map<K, V> source, K key, MapDefaultValueSupplier<V, E> s) throws E {
    V value = source.get(key);
    if (value == null) {
      value = s.getDefault();
      source.put(key, value);
    }
    return value;
  }
}
