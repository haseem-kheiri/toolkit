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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

/** Predefined dataset of dates of birth (DOB) ranging from a start year to an end year. */
public interface DateOfBirth extends DataSet {

  /** Formatter for ISO-8601 style DOB strings (date only). */
  DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Represents a date of birth entry.
   *
   * @param date the date of birth as {@link LocalDate}
   */
  record Dob(LocalDate date) {
    @Override
    public String toString() {
      return date.format(FORMATTER);
    }
  }

  /** Immutable list of DOBs from 1900 to 2025 inclusive. */
  List<Dob> DOBS = generateDobs(1900, 2025);

  /**
   * Generates a list of date-of-birth records between the given years (inclusive).
   *
   * @param startYear the start year
   * @param endYear the end year
   * @return immutable list of {@link Dob}
   */
  static List<Dob> generateDobs(int startYear, int endYear) {
    return IntStream.rangeClosed(startYear, endYear)
        .boxed()
        .flatMap(
            year ->
                IntStream.rangeClosed(1, 12)
                    .boxed()
                    .flatMap(
                        month -> {
                          LocalDate first = LocalDate.of(year, month, 1);
                          int length = first.lengthOfMonth();
                          return IntStream.rangeClosed(1, length)
                              .mapToObj(day -> new Dob(LocalDate.of(year, month, day)));
                        }))
        .toList();
  }

  /**
   * Returns a randomly selected date of birth from the dataset.
   *
   * @return a {@link Dob} chosen at random
   */
  static Dob randomDob() {
    return DataSet.get(DOBS);
  }
}
