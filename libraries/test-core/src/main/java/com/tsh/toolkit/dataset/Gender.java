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

/**
 * Predefined dataset of gender options using single-character codes.
 *
 * <p>Codes:
 *
 * <ul>
 *   <li>'M' – Male
 *   <li>'F' – Female
 *   <li>'O' – Other
 * </ul>
 *
 * <p>This dataset can be used for simulations, testing, or data generation purposes.
 */
public interface Gender extends DataSet {

  /** Immutable list of gender codes. */
  List<Character> GENDERS = List.of('M', 'F', 'O');

  /**
   * Returns a randomly selected gender code from the predefined dataset.
   *
   * @return a randomly selected gender code ('M', 'F', or 'O')
   */
  static Character randomGender() {
    return DataSet.get(GENDERS);
  }
}
