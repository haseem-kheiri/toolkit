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

import java.util.UUID;

/**
 * Predefined dataset for generating random persons.
 *
 * <p>This dataset combines multiple datasets (Names, Age, Gender, DOB, Dependents, Salary, Capital,
 * Deceased) to generate realistic synthetic individual profiles.
 */
public interface People extends DataSet {

  /** Represents a synthetic person with multiple attributes. */
  record Person(
      UUID id,
      Name.Fullname name,
      Integer age,
      Character gender,
      DateOfBirth.Dob dob,
      Byte dependents,
      Long salary,
      Capital.CapitalCity capital) {

    @Override
    public String toString() {
      return String.format(
          "Id: %s, Name: %s, Age: %d, Gender: %s, DOB: %s, Dependents: %d, "
              + "Salary: $%,d, Address: %s",
          id,
          name,
          age,
          gender,
          dob,
          dependents,
          salary,
          capital.city() + ", " + capital.country());
    }
  }

  /**
   * Generates a random synthetic person.
   *
   * @return a {@link Person} instance with random attributes
   */
  static Person randomPerson(UUID id) {
    Name.Fullname name = Name.randomName();
    Integer age = Age.randomAge();
    Character gender = Gender.randomGender();
    DateOfBirth.Dob dob = DateOfBirth.randomDob();
    Byte dependents = Dependents.randomDependents();
    Long salary = Salary.randomSalary();
    Capital.CapitalCity capital = Capital.randomCapital();

    return new Person(id, name, age, gender, dob, dependents, salary, capital);
  }
}
