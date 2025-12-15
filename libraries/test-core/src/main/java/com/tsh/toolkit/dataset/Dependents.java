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
 * Predefined dataset of dependent children counts.
 *
 * <p>Represents the number of dependents a person may have, ranging from 0 to 4 inclusive.
 */
public interface Dependents extends DataSet {

  /** Immutable list of possible dependent counts (0 to 4). */
  List<Byte> DEPENDENTS = List.of((byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4);

  /**
   * Selects a random number of dependents from the predefined dataset.
   *
   * @return a randomly chosen number of dependents
   */
  static Byte randomDependents() {
    return DataSet.get(DEPENDENTS);
  }
}
