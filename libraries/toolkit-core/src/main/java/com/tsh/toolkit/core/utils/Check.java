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

package com.tsh.toolkit.core.utils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Utility class for runtime checks and validations.
 *
 * <p>This class provides methods to assert conditions on values, strings, collections, and
 * patterns. If a condition is violated, the corresponding method throws an appropriate {@link
 * IllegalArgumentException} or a user-supplied exception.
 *
 * <p>Typical usage:
 *
 * <pre>{@code
 * Check.requireTrue(x > 0, () -> "Value must be positive");
 * String name = Check.requireNotBlank(input, () -> "Name cannot be blank");
 * Check.requireFalse(flag, () -> "Flag must be false");
 * }</pre>
 *
 * <p>This class is backward-compatible; all existing method signatures remain unchanged. New helper
 * methods are added as optional utilities.
 *
 * @author Haseem
 */
public class Check {

  /**
   * Throws the supplied exception if the condition is false.
   *
   * @param condition the condition to check
   * @param e supplier of the exception to throw if the condition is false
   * @param <E> type of exception
   * @throws E if {@code condition} is false
   */
  public static <E extends Exception> void requireTrueOrThrow(boolean condition, Supplier<E> e)
      throws E {
    if (!condition) {
      throw e.get();
    }
  }

  /**
   * Throws an {@link IllegalArgumentException} with the supplied message if the condition is false.
   *
   * @param condition the condition to check
   * @param errorMessageSupplier supplier of the exception message if the condition is false
   * @throws IllegalArgumentException if {@code condition} is false
   */
  public static void requireTrue(boolean condition, Supplier<String> errorMessageSupplier) {
    requireTrueOrThrow(condition, () -> new IllegalArgumentException(errorMessageSupplier.get()));
  }

  /**
   * Evaluates a value from a supplier, checks it against a predicate, and returns it if valid.
   * Otherwise, throws an {@link IllegalArgumentException} with the supplied error message.
   *
   * @param supplier supplier of the value to check
   * @param predicate predicate that must return {@code true} for a valid value
   * @param errorMessageSupplier supplier of the exception message if the predicate fails
   * @param <T> type of the value
   * @return the supplied value if it passes the predicate
   * @throws IllegalArgumentException if the predicate returns {@code false}
   */
  public static <T> T requireTrue(
      Supplier<T> supplier, Function<T, Boolean> predicate, Supplier<String> errorMessageSupplier) {
    T t = supplier.get();
    requireTrue(predicate.apply(t), errorMessageSupplier);
    return t;
  }

  /**
   * Throws an {@link IllegalArgumentException} if the string is not null and not blank.
   *
   * @param s the string to check
   * @param fn function that generates the exception message
   * @return the original string if it is null or blank
   * @throws IllegalArgumentException if the string is not null and not blank
   */
  public static String requireBlank(String s, Function<String, String> fn) {
    requireTrue(s == null || s.isBlank(), () -> fn.apply(s));
    return s;
  }

  /**
   * Throws an {@link IllegalArgumentException} if the string is null or blank.
   *
   * @param s the string to check
   * @param errorMessageSupplier supplier of the exception message if the string is null/empty
   * @return the original string if it is non-null and non-blank
   * @throws IllegalArgumentException if the string is null or blank
   */
  public static String requireNotBlank(String s, Supplier<String> errorMessageSupplier) {
    requireTrue(s != null && !s.isBlank(), errorMessageSupplier);
    return s;
  }

  /**
   * Checks if the given string matches the supplied pattern. Returns the string if it matches;
   * otherwise throws an {@link IllegalArgumentException} with the supplied message.
   *
   * @param pattern the regex pattern to match
   * @param s the string to check
   * @param fn function to generate the exception message if the string does not match
   * @return the original string if it matches the pattern
   * @throws IllegalArgumentException if the string is null or does not match the pattern
   */
  public static String requireMatches(Pattern pattern, String s, Function<String, String> fn) {
    requireTrue(s != null && pattern.matcher(s).matches(), () -> fn.apply(s));
    return s;
  }

  /**
   * Throws an {@link IllegalArgumentException} if the object is not null.
   *
   * @param t the object to check
   * @param fn function to generate the exception message if the object is not null
   * @param <T> type of the object
   * @throws IllegalArgumentException if the object is not null
   */
  public static <T> void requireNull(T t, Function<T, String> fn) {
    requireTrue(t == null, () -> fn.apply(t));
  }

  /**
   * Throws an {@link IllegalArgumentException} if the object is null; otherwise returns it.
   *
   * @param t the object to check
   * @param errorMessageSupplier supplier of the exception message if the object is null
   * @param <T> type of the object
   * @return the original object if it is not null
   * @throws IllegalArgumentException if the object is null
   */
  public static <T> T requireNotNull(T t, Supplier<String> errorMessageSupplier) {
    requireTrue(t != null, errorMessageSupplier);
    return t;
  }

  /**
   * Throws an {@link IllegalArgumentException} if the collection is null or empty; otherwise
   * returns it.
   *
   * @param collection the collection to check
   * @param errorMessageSupplier supplier of the exception message if the collection is null/empty
   * @param <T> type of the collection
   * @return the original collection if it is not null and not empty
   * @throws IllegalArgumentException if the collection is null or empty
   */
  public static <T extends Collection<?>> T requireNotEmpty(
      T collection, Supplier<String> errorMessageSupplier) {
    requireTrue(collection != null && !collection.isEmpty(), () -> errorMessageSupplier.get());
    return collection;
  }

  /**
   * Throws an {@link IllegalArgumentException} if the condition is true.
   *
   * @param condition the condition to check
   * @param errorMessageSupplier supplier of the exception message if the condition is true
   * @throws IllegalArgumentException if the condition is true
   */
  public static void requireFalse(boolean condition, Supplier<String> errorMessageSupplier) {
    requireTrue(!condition, errorMessageSupplier);
  }

  /**
   * Throws an {@link IllegalArgumentException} if two objects are not equal.
   *
   * @param a first object
   * @param b second object
   * @param errorMessageSupplier supplier of the exception message if objects are not equal
   * @param <T> type of objects
   * @throws IllegalArgumentException if a and b are not equal
   */
  public static <T> void requireEqual(T a, T b, Supplier<String> errorMessageSupplier) {
    requireTrue(Objects.equals(a, b), errorMessageSupplier);
  }

  /**
   * Throws an {@link IllegalArgumentException} if the object is not an instance of the given class.
   *
   * @param obj the object to check
   * @param clazz the expected class
   * @param errorMessageSupplier supplier of the exception message if the object is not an instance
   * @param <T> type of the class
   * @throws IllegalArgumentException if obj is not an instance of clazz
   */
  public static <T> void requireInstanceOf(
      Object obj, Class<T> clazz, Supplier<String> errorMessageSupplier) {
    requireTrue(clazz.isInstance(obj), errorMessageSupplier);
  }
}
