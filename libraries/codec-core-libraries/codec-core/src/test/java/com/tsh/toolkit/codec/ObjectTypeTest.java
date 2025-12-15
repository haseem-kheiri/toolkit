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

package com.tsh.toolkit.codec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ObjectType}, verifying correct capture of generic type information.
 *
 * <p>These tests ensure that {@code ObjectType} accurately reflects both parameterized and
 * non-parameterized types, and that it throws a meaningful exception when type information cannot
 * be inferred.
 */
class ObjectTypeTest {

  /**
   * Verifies that {@link ObjectType} correctly captures a parameterized type such as {@code
   * List<String>}.
   */
  @Test
  @DisplayName("Capture parameterized type (List<String>)")
  void testForList() {
    ObjectType<List<String>> type = new ObjectType<List<String>>() {};

    Type captured = type.getType();
    Assertions.assertTrue(captured instanceof ParameterizedType, "Expected parameterized type");

    ParameterizedType parameterizedType = (ParameterizedType) captured;
    Assertions.assertEquals(List.class, parameterizedType.getRawType());
    Assertions.assertEquals(String.class, parameterizedType.getActualTypeArguments()[0]);
    Assertions.assertTrue(type.isParameterized());
  }

  /**
   * Verifies that {@link ObjectType} correctly captures a non-parameterized type such as {@code
   * String}.
   */
  @Test
  @DisplayName("Capture non-parameterized type (String)")
  void testForNonParameterizedType() {
    ObjectType<String> type = new ObjectType<String>() {};
    Assertions.assertEquals(String.class, type.getType());
    Assertions.assertFalse(type.isParameterized());
  }

  /**
   * Verifies that nested parameterized types such as {@code Map<String, List<Integer>>} are
   * captured accurately.
   */
  @Test
  @DisplayName("Capture nested parameterized type (Map<String, List<Integer>>)")
  void testForNestedParameterizedType() {
    ObjectType<Map<String, List<Integer>>> type = new ObjectType<Map<String, List<Integer>>>() {};

    Type captured = type.getType();
    Assertions.assertTrue(captured instanceof ParameterizedType, "Expected parameterized type");

    ParameterizedType parameterizedType = (ParameterizedType) captured;
    Assertions.assertEquals(Map.class, parameterizedType.getRawType());

    Type keyType = parameterizedType.getActualTypeArguments()[0];
    Type valueType = parameterizedType.getActualTypeArguments()[1];

    Assertions.assertEquals(String.class, keyType);
    Assertions.assertTrue(valueType instanceof ParameterizedType);
    Assertions.assertEquals(List.class, ((ParameterizedType) valueType).getRawType());
    Assertions.assertEquals(
        Integer.class, ((ParameterizedType) valueType).getActualTypeArguments()[0]);
  }

  /**
   * Verifies that directly instantiating {@link ObjectType} without using an anonymous subclass
   * results in an {@link IllegalStateException}.
   */
  @Test
  @DisplayName("Throws when instantiated directly without type parameter")
  void testThrowsWhenNotSubclassed() {
    Assertions.assertThrows(
        IllegalStateException.class,
        ObjectType::new,
        "Expected IllegalStateException when no type parameter provided");
  }

  /** Verifies that {@link ObjectType#isParameterized()} correctly identifies nested types. */
  @Test
  @DisplayName("isParameterized returns true for multi-level generics")
  void testIsParameterizedForComplexType() {
    ObjectType<Map<String, List<Double>>> type = new ObjectType<Map<String, List<Double>>>() {};
    Assertions.assertTrue(type.isParameterized());
  }
}
