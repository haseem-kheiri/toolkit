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
 * Predefined dataset of notable engineers, scientists, and historical figures.
 *
 * <p>This dataset includes modern and historical figures, including pioneers from the Islamic
 * Golden Age and other eras. Each name is represented using a {@link Name} record for easy access
 * to first and last name components.
 *
 * <p>Provides a convenient method {@link #randomName()} to select a random notable figure from the
 * dataset.
 */
public interface Name extends DataSet {

  /**
   * Represents a person’s name with first and last components.
   *
   * <p>If a figure does not have a conventional last name (e.g., historical Islamic scholars), the
   * last field may be empty.
   *
   * @param first the first name of the person
   * @param last the last name of the person, may be empty
   */
  record Fullname(String first, String last) {
    /**
     * Returns the full name as a single string.
     *
     * @return the concatenated first and last name
     */
    @Override
    public String toString() {
      return first + (last.isBlank() ? "" : " " + last);
    }
  }

  /**
   * Immutable list of notable names.
   *
   * <p>This list includes:
   *
   * <ul>
   *   <li>Historical Islamic Golden Age scholars
   *   <li>Modern scientists, engineers, and mathematicians
   *   <li>Female pioneers in STEM
   *   <li>Inventors and programmers
   * </ul>
   *
   * <p>Currently contains a subset; can be extended to reach ~500 names.
   */
  List<Fullname> NAMES =
      List.of(
          // Islamic Golden Age (sample)
          new Fullname("Al-Khwarizmi", ""),
          new Fullname("Ibn Sina", ""),
          new Fullname("Al-Razi", ""),
          new Fullname("Al-Farabi", ""),
          new Fullname("Ibn Al-Haytham", ""),
          new Fullname("Omar Khayyam", ""),
          new Fullname("Al-Biruni", ""),
          new Fullname("Ibn Khaldun", ""),
          new Fullname("Al-Zahrawi", ""),
          new Fullname("Jabir Ibn Hayyan", ""),

          // Modern scientists & engineers (sample)
          new Fullname("Nikola", "Tesla"),
          new Fullname("Albert", "Einstein"),
          new Fullname("Isaac", "Newton"),
          new Fullname("Marie", "Curie"),
          new Fullname("Ada", "Lovelace"),
          new Fullname("Charles", "Babbage"),
          new Fullname("Galileo", "Galilei"),
          new Fullname("James", "Watt"),
          new Fullname("Michael", "Faraday"),
          new Fullname("Niels", "Bohr"),
          new Fullname("Rosalind", "Franklin"),
          new Fullname("Richard", "Feynman"),
          new Fullname("Alan", "Turing"),
          new Fullname("Katherine", "Johnson"),
          new Fullname("Tim", "Berners-Lee"),
          new Fullname("Grace", "Hopper"),
          new Fullname("Carl", "Sagan"),
          new Fullname("Leonardo", "da Vinci"),
          new Fullname("Stephen", "Hawking"),
          new Fullname("Johannes", "Kepler"),

          // Female pioneers & programmers (sample)
          new Fullname("Margaret", "Hamilton"),
          new Fullname("Mary", "Anning"),
          new Fullname("Dorothy", "Vaughan"),
          new Fullname("Maryam", "Mirzakhani"),
          new Fullname("Elizabeth", "Blackwell"),
          new Fullname("Rachel", "Carson"),
          new Fullname("Jane", "Marcet"),
          new Fullname("Ada", "Byron"),
          new Fullname("Rosalind", "Elsie"),
          new Fullname("Lynn", "Margulis")
          // Add more names here to scale to 500
          );

  /**
   * Returns a randomly selected notable figure from the dataset.
   *
   * @return a {@link Name} chosen at random
   */
  static Fullname randomName() {
    return new Name() {}.randomElement(NAMES);
  }
}
