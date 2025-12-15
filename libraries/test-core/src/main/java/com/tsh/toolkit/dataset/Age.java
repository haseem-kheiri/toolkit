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
import java.util.stream.IntStream;

/**
 * Predefined dataset of ages ranging from 18 to 100 inclusive.
 *
 * <p>This dataset can be used in scenarios where random age values are needed, such as testing,
 * simulations, or data generation.
 */
public interface Age extends DataSet {

  /** Immutable list of ages from 18 to 100 inclusive. */
  List<Integer> AGES = IntStream.rangeClosed(18, 100).boxed().toList();

  /**
   * Selects a random age between 18 and 100 inclusive.
   *
   * <p>The selection is uniformly distributed across all values in {@link #AGES}.
   *
   * @return a randomly selected age
   */
  static Integer randomAge() {
    return new Age() {}.randomElement(AGES);
  }
}
