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

package com.tsh.toolkit.dataset;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Provides utility methods for selecting random values from a dataset. */
public interface DataSet {

  /**
   * Selects a random element from the given list using a uniform distribution.
   *
   * <p>The method guarantees that every element in the list has an equal probability of being
   * selected.
   *
   * @param list the list to select from, must not be {@code null} or empty
   * @param <T> the type of element in the list
   * @return a randomly selected element from the list
   * @throws IllegalArgumentException if the list is {@code null} or empty
   */
  static <T> T get(List<T> list) {
    if (list == null || list.isEmpty()) {
      throw new IllegalArgumentException("List must not be null or empty");
    }
    int index = ThreadLocalRandom.current().nextInt(list.size());
    return list.get(index);
  }

  /**
   * Selects a random element from the dataset represented by the given list.
   *
   * <p>This is a convenience method for dataset interfaces to provide direct random access without
   * repeating boilerplate code.
   *
   * @param list the dataset list
   * @param <T> the type of element in the dataset
   * @return a randomly selected element
   */
  default <T> T randomElement(List<T> list) {
    return get(list);
  }
}
