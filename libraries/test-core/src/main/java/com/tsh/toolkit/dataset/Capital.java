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

/** Predefined dataset of world capitals and their countries. */
public interface Capital extends DataSet {

  /**
   * Represents a capital city and its corresponding country.
   *
   * @param city the name of the capital city
   * @param country the country the capital belongs to
   */
  record CapitalCity(String city, String country) {}

  /** Immutable list of capitals with their countries. */
  List<CapitalCity> CAPITALS =
      List.of(
          new CapitalCity("Kabul", "Afghanistan"),
          new CapitalCity("Tirana", "Albania"),
          new CapitalCity("Algiers", "Algeria"),
          new CapitalCity("Andorra la Vella", "Andorra"),
          new CapitalCity("Luanda", "Angola"),
          new CapitalCity("Buenos Aires", "Argentina"),
          new CapitalCity("Yerevan", "Armenia"),
          new CapitalCity("Canberra", "Australia"),
          new CapitalCity("Vienna", "Austria"),
          new CapitalCity("Baku", "Azerbaijan"),
          new CapitalCity("Nassau", "Bahamas"),
          new CapitalCity("Manama", "Bahrain"),
          new CapitalCity("Dhaka", "Bangladesh"),
          new CapitalCity("Bridgetown", "Barbados"),
          new CapitalCity("Minsk", "Belarus"),
          new CapitalCity("Brussels", "Belgium"),
          new CapitalCity("Belmopan", "Belize"),
          new CapitalCity("Porto-Novo", "Benin"),
          new CapitalCity("Thimphu", "Bhutan"),
          new CapitalCity("La Paz", "Bolivia"),
          new CapitalCity("Sarajevo", "Bosnia and Herzegovina"),
          new CapitalCity("Gaborone", "Botswana"),
          new CapitalCity("Brasília", "Brazil"),
          new CapitalCity("Bandar Seri Begawan", "Brunei"),
          new CapitalCity("Sofia", "Bulgaria"),
          new CapitalCity("Ouagadougou", "Burkina Faso"),
          new CapitalCity("Gitega", "Burundi"),
          new CapitalCity("Phnom Penh", "Cambodia"),
          new CapitalCity("Yaoundé", "Cameroon"),
          new CapitalCity("Ottawa", "Canada"),
          new CapitalCity("Santiago", "Chile"),
          new CapitalCity("Beijing", "China"),
          new CapitalCity("Bogotá", "Colombia"),
          new CapitalCity("San José", "Costa Rica"),
          new CapitalCity("Zagreb", "Croatia"),
          new CapitalCity("Havana", "Cuba"),
          new CapitalCity("Nicosia", "Cyprus"),
          new CapitalCity("Prague", "Czech Republic"),
          new CapitalCity("Copenhagen", "Denmark"),
          new CapitalCity("Cairo", "Egypt"),
          new CapitalCity("Paris", "France"),
          new CapitalCity("Berlin", "Germany"),
          new CapitalCity("Athens", "Greece"),
          new CapitalCity("New Delhi", "India"),
          new CapitalCity("Jakarta", "Indonesia"),
          new CapitalCity("Tehran", "Iran"),
          new CapitalCity("Baghdad", "Iraq"),
          new CapitalCity("Dublin", "Ireland"),
          new CapitalCity("Rome", "Italy"),
          new CapitalCity("Tokyo", "Japan"),
          new CapitalCity("Amman", "Jordan"),
          new CapitalCity("Nairobi", "Kenya"),
          new CapitalCity("Kuwait City", "Kuwait"),
          new CapitalCity("Beirut", "Lebanon"),
          new CapitalCity("Tripoli", "Libya"),
          new CapitalCity("Luxembourg", "Luxembourg"),
          new CapitalCity("Kuala Lumpur", "Malaysia"),
          new CapitalCity("Mexico City", "Mexico"),
          new CapitalCity("Rabat", "Morocco"),
          new CapitalCity("Kathmandu", "Nepal"),
          new CapitalCity("Amsterdam", "Netherlands"),
          new CapitalCity("Wellington", "New Zealand"),
          new CapitalCity("Abuja", "Nigeria"),
          new CapitalCity("Oslo", "Norway"),
          new CapitalCity("Islamabad", "Pakistan"),
          new CapitalCity("Panama City", "Panama"),
          new CapitalCity("Lima", "Peru"),
          new CapitalCity("Manila", "Philippines"),
          new CapitalCity("Warsaw", "Poland"),
          new CapitalCity("Lisbon", "Portugal"),
          new CapitalCity("Doha", "Qatar"),
          new CapitalCity("Moscow", "Russia"),
          new CapitalCity("Riyadh", "Saudi Arabia"),
          new CapitalCity("Belgrade", "Serbia"),
          new CapitalCity("Singapore", "Singapore"),
          new CapitalCity("Pretoria", "South Africa"),
          new CapitalCity("Seoul", "South Korea"),
          new CapitalCity("Madrid", "Spain"),
          new CapitalCity("Colombo", "Sri Lanka"),
          new CapitalCity("Khartoum", "Sudan"),
          new CapitalCity("Stockholm", "Sweden"),
          new CapitalCity("Bern", "Switzerland"),
          new CapitalCity("Damascus", "Syria"),
          new CapitalCity("Taipei", "Taiwan"),
          new CapitalCity("Bangkok", "Thailand"),
          new CapitalCity("Tunis", "Tunisia"),
          new CapitalCity("Ankara", "Turkey"),
          new CapitalCity("Kyiv", "Ukraine"),
          new CapitalCity("Abu Dhabi", "United Arab Emirates"),
          new CapitalCity("London", "United Kingdom"),
          new CapitalCity("Washington, D.C.", "United States"),
          new CapitalCity("Hanoi", "Vietnam"),
          new CapitalCity("Harare", "Zimbabwe"));

  /**
   * Selects a random capital city with its country.
   *
   * @return a randomly selected {@link CapitalCity}
   */
  static CapitalCity randomCapital() {
    return new Capital() {}.randomElement(CAPITALS);
  }
}
