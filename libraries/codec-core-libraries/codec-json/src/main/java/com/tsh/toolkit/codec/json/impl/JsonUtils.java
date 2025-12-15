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

package com.tsh.toolkit.codec.json.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsh.toolkit.core.utils.Run;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Utility class for JSON serialization and deserialization using a configurable {@link
 * ObjectMapper}.
 *
 * <p>This class provides convenient static methods to convert objects to JSON strings or byte
 * arrays and to map JSON content back to objects. It uses a singleton instance internally and wraps
 * checked {@link IOException} in {@link UncheckedIOException}.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * JsonUtils.instance().setObjectMapper(new ObjectMapper());
 * String json = JsonUtils.stringify(myObject);
 * MyClass obj = JsonUtils.map(om -> om.readValue(json, MyClass.class));
 * }</pre>
 *
 * <p>The class is thread-safe for read operations after the ObjectMapper has been set.
 *
 * @author Haseem Kheiri
 */
public class JsonUtils {

  /**
   * Functional interface for mapping JSON using a provided {@link ObjectMapper}.
   *
   * @param <T> the type of the result object
   */
  @FunctionalInterface
  public interface JsonMapper<T> {

    /**
     * Performs a JSON mapping operation using the given {@link ObjectMapper}.
     *
     * @param objectMapper the Jackson ObjectMapper to use
     * @return the mapped object
     * @throws IOException if a mapping error occurs
     */
    T map(ObjectMapper objectMapper) throws IOException;
  }

  /** Singleton instance of {@code JsonUtils}. */
  private static final JsonUtils INSTANCE = new JsonUtils();

  /**
   * Returns the singleton instance of {@code JsonUtils}.
   *
   * @return the singleton instance
   */
  public static JsonUtils instance() {
    return INSTANCE;
  }

  /** The {@link ObjectMapper} used for JSON mapping; must be set before use. */
  private ObjectMapper objectMapper;

  /** Private constructor for singleton pattern. */
  private JsonUtils() {}

  /**
   * Sets the {@link ObjectMapper} if it has not already been set.
   *
   * @param objectMapper the ObjectMapper to set
   */
  public void setObjectMapper(ObjectMapper objectMapper) {
    Run.runIfNull(
        this.objectMapper,
        () -> {
          this.objectMapper = objectMapper;
        });
  }

  /**
   * Executes a JSON mapping operation using the singleton {@link ObjectMapper}. Wraps any {@link
   * IOException} in an {@link UncheckedIOException}.
   *
   * @param mapper the JSON mapping function
   * @param <T> the type of the result
   * @return the result of the mapping function
   * @throws UncheckedIOException if the mapping throws an IOException
   */
  public static <T> T map(JsonMapper<T> mapper) {
    try {
      return mapper.map(INSTANCE.objectMapper);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Converts an object to a JSON string.
   *
   * @param o the object to serialize, may be null
   * @return the JSON string representation of the object, or null if {@code o} is null
   * @throws UncheckedIOException if serialization fails
   */
  public static String stringify(Object o) {
    return map(om -> o == null ? null : om.writeValueAsString(o));
  }

  /**
   * Converts an object to a JSON byte array.
   *
   * @param o the object to serialize, may be null
   * @return the JSON byte array representation of the object, or null if {@code o} is null
   * @throws UncheckedIOException if serialization fails
   */
  public static byte[] toBinary(Object o) {
    return map(om -> o == null ? null : om.writeValueAsBytes(o));
  }
}
