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

package com.tsh.toolkit.core;

import com.tsh.toolkit.core.utils.Check;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive test for {@link Check}.
 *
 * @author Haseem Kheiri
 */
class CheckTest {

  @Test
  void testRequireTrueOrThrow() {
    Assertions.assertDoesNotThrow(
        () -> Check.requireTrueOrThrow(true, () -> new RuntimeException("No error")));
    Exception e =
        Assertions.assertThrows(
            RuntimeException.class,
            () -> Check.requireTrueOrThrow(false, () -> new RuntimeException("Forced error")));
    Assertions.assertEquals("Forced error", e.getMessage());
  }

  @Test
  void testRequireTrue() {
    Check.requireTrue(true, () -> "No error.");
    Exception e =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> Check.requireTrue(false, () -> "Forced error."));
    Assertions.assertEquals("Forced error.", e.getLocalizedMessage());
  }

  @Test
  void testRequireTrueWithSupplierAndPredicate() {
    String value = Check.requireTrue(() -> "abc", s -> s.length() == 3, () -> "Length mismatch");
    Assertions.assertEquals("abc", value);

    Exception e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Check.requireTrue(() -> "abcd", s -> s.length() == 3, () -> "Forced error"));
    Assertions.assertEquals("Forced error", e.getLocalizedMessage());
  }

  @Test
  void testRequireBlank() {
    Check.requireBlank("", s -> "No error.");
    Check.requireBlank(null, s -> "No error.");
    Exception e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Check.requireBlank("Not blank", s -> "Forced error."));
    Assertions.assertEquals("Forced error.", e.getLocalizedMessage());
  }

  @Test
  void testRequireNotBlank() {
    Check.requireNotBlank("text", () -> "No error.");
    Exception e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Check.requireNotBlank(null, () -> "Forced error."));
    Assertions.assertEquals("Forced error.", e.getLocalizedMessage());

    e =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> Check.requireNotBlank("", () -> "Forced error."));
    Assertions.assertEquals("Forced error.", e.getLocalizedMessage());
  }

  @Test
  void testRequireMatches() {
    final Pattern p = Pattern.compile("^[a-z][a-z_]{3,17}$");
    Check.requireMatches(p, "word", (o) -> "No error.");
    Exception e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () ->
                Check.requireMatches(
                    p, "missing underscore", (o) -> String.format("No match as %s.", o)));
    Assertions.assertEquals("No match as missing underscore.", e.getLocalizedMessage());
  }

  @Test
  void testRequireNull() {
    Check.requireNull(null, o -> "No error.");
    Exception e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Check.requireNull("text", o -> String.format("Found %s should be null.", o)));
    Assertions.assertEquals("Found text should be null.", e.getLocalizedMessage());
  }

  @Test
  void testRequireNotNull() {
    Assertions.assertEquals("not null", Check.requireNotNull("not null", () -> "No error."));
    Exception e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Check.requireNotNull(null, () -> "Forced error."));
    Assertions.assertEquals("Forced error.", e.getLocalizedMessage());
  }

  @Test
  void testRequireNotEmpty() {
    final Set<String> set = Set.of("name");
    Assertions.assertEquals(set, Check.requireNotEmpty(set, () -> "No error."));
    Exception e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Check.requireNotEmpty(null, () -> "Forced error."));
    Assertions.assertEquals("Forced error.", e.getLocalizedMessage());

    e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Check.requireNotEmpty(List.of(), () -> "Forced error."));
    Assertions.assertEquals("Forced error.", e.getLocalizedMessage());
  }

  @Test
  void testRequireFalse() {
    Check.requireFalse(false, () -> "No error.");
    Exception e =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> Check.requireFalse(true, () -> "Forced error."));
    Assertions.assertEquals("Forced error.", e.getLocalizedMessage());
  }

  @Test
  void testRequireEqual() {
    Check.requireEqual("a", "a", () -> "No error.");
    Exception e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Check.requireEqual("a", "b", () -> "Forced error."));
    Assertions.assertEquals("Forced error.", e.getLocalizedMessage());
  }

  @Test
  void testRequireInstanceOf() {
    Check.requireInstanceOf("string", String.class, () -> "No error.");
    Exception e =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Check.requireInstanceOf(123, String.class, () -> "Forced error."));
    Assertions.assertEquals("Forced error.", e.getLocalizedMessage());
  }
}
