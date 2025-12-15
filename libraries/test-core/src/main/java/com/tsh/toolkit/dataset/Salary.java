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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Predefined dataset of salaries.
 *
 * <p>This dataset generates 500 salaries following a bell curve (normal distribution) with most
 * salaries clustered around the median, ranging from $25,000 to $1,000,000.
 */
public interface Salary extends DataSet {

  /** Minimum salary value. */
  long MIN_SALARY = 25_000;

  /** Maximum salary value. */
  long MAX_SALARY = 1_000_000;

  /** Number of salaries to generate. */
  int SIZE = 500;

  /** Immutable list of salaries in USD. */
  List<Long> SALARIES = generateSalaries(SIZE, MIN_SALARY, MAX_SALARY);

  /**
   * Generates a list of salaries following a bell curve (normal distribution).
   *
   * @param size the number of salaries to generate
   * @param min minimum salary
   * @param max maximum salary
   * @return immutable list of salaries
   */
  static List<Long> generateSalaries(int size, long min, long max) {
    double mean = (min + max) / 2.0; // center of the bell
    double stdDev = (max - min) / 6.0; // ~99.7% within min-max range

    return IntStream.range(0, size)
        .mapToObj(
            i -> {
              double value;
              do {
                // Generate value using Box-Muller transform (normal distribution)
                double u = ThreadLocalRandom.current().nextDouble();
                double v = ThreadLocalRandom.current().nextDouble();
                double z = Math.sqrt(-2 * Math.log(u)) * Math.cos(2 * Math.PI * v);
                value = mean + z * stdDev;
              } while (value < min || value > max); // clamp to min-max
              return Math.round(value);
            })
        .collect(Collectors.toUnmodifiableList());
  }

  /**
   * Returns a randomly selected salary from the dataset.
   *
   * @return a salary value in USD
   */
  static Long randomSalary() {
    return DataSet.get(SALARIES);
  }
}
