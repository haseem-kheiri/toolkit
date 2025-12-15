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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsh.toolkit.codec.Codec;
import com.tsh.toolkit.codec.CodecException;
import com.tsh.toolkit.codec.ObjectType;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * JSON-based implementation of the {@link Codec} interface using Jackson.
 *
 * <p>This codec provides a reliable and straightforward mechanism for serializing and deserializing
 * arbitrary Java objects in JSON format. It leverages Jackson’s {@link ObjectMapper} for flexible
 * type handling, including full support for parameterized types such as {@code List<Map<String,
 * Integer>>}.
 *
 * <p>The {@code JsonCodec} is ideal for use cases where readability, portability, or
 * human-inspectable data formats are desirable—such as configuration storage, cluster state
 * persistence, or REST-like message exchange.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Codec codec = new JsonCodec();
 *
 * // Encode
 * byte[] json = codec.encode(Map.of("a", 1, "b", 2));
 *
 * // Decode to a parameterized type
 * Map<String, Integer> map = codec.decode(json, new ObjectType<Map<String, Integer>>() {});
 * }</pre>
 *
 * <p>For more compact or high-throughput binary serialization—e.g., for cluster gossip or
 * replication channels—consider using {@link com.platform.codec.MessagePackCodec}.
 */
public class JsonCodec implements Codec {

  /**
   * Functional interface for supplying a configured {@link ObjectMapper} instance.
   *
   * <p>This allows customization of serialization behavior (e.g., registering modules, adjusting
   * naming strategies, or changing date/time formats) without tightly coupling the codec to a
   * particular mapper configuration.
   */
  @FunctionalInterface
  public interface JsonMapperSupplier {
    /**
     * Returns a configured {@link ObjectMapper} instance.
     *
     * @return a non-null {@link ObjectMapper} (implementations may return {@code null}, in which
     *     case a default mapper will be used)
     */
    ObjectMapper get();
  }

  /** Jackson {@link ObjectMapper} used internally for JSON serialization and deserialization. */
  private final ObjectMapper objectMapper;

  /**
   * Constructs a new {@code JsonCodec} using a default {@link ObjectMapper}.
   *
   * <p>The mapper is configured with Jackson’s defaults, which provide general-purpose Java object
   * serialization and deserialization without additional modules. If you require specialized
   * configuration (e.g., {@code JavaTimeModule}, custom naming strategies, or mixins), consider
   * providing an {@link JsonMapperSupplier} via the alternate constructor.
   */
  public JsonCodec() {
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Constructs a new {@code JsonCodec} using a supplier for customized {@link ObjectMapper}
   * creation.
   *
   * <p>If the supplied {@code supplier} is {@code null} or returns {@code null}, a default {@link
   * ObjectMapper} will be created instead. This ensures the codec is always in a valid state, even
   * in misconfigured or testing environments.
   *
   * @param supplier a supplier that supplies a configured {@link ObjectMapper}, or {@code null} for
   *     default configuration
   */
  public JsonCodec(JsonMapperSupplier supplier) {
    ObjectMapper om = null;
    if (supplier != null) {
      om = supplier.get();
    }

    if (om == null) {
      om = new ObjectMapper();
    }

    this.objectMapper = om;
  }

  /**
   * Serializes the given Java object into its JSON byte array representation.
   *
   * @param obj the object to serialize; may be {@code null}
   * @return the JSON-encoded byte array
   * @throws CodecException if serialization fails
   */
  @Override
  public byte[] encode(Object obj) throws CodecException {
    try {
      return objectMapper.writeValueAsBytes(obj);
    } catch (JsonProcessingException e) {
      throw new CodecException("Failed to encode object to JSON", e);
    }
  }

  /**
   * Deserializes the given JSON byte array into an object of the specified type.
   *
   * <p>Supports both simple and parameterized types through {@link ObjectType}, enabling decoding
   * of complex generic structures (e.g., {@code List<Map<String, Integer>>}).
   *
   * <p>Both {@code encoded} and {@code type} must be non-null. A {@link CodecException} is thrown
   * if either is {@code null} or if the JSON cannot be parsed into the target type.
   *
   * @param encoded the JSON-encoded byte array; must not be {@code null}
   * @param type the {@link ObjectType} representing the target type to decode into; must not be
   *     {@code null}
   * @param <T> the generic type of the decoded object
   * @return the decoded object instance
   * @throws CodecException if deserialization fails or if the input cannot be parsed
   */
  @Override
  public <T> T decode(byte[] encoded, ObjectType<T> type) throws CodecException {
    if (encoded == null) {
      throw new CodecException(
          "Cannot decode from a null byte array", new IOException("Input byte array is null"));
    }
    if (type == null) {
      throw new CodecException(
          "Cannot decode without a target type", new IOException("ObjectType is null"));
    }

    try {
      return objectMapper.readValue(
          encoded,
          new TypeReference<T>() {
            @Override
            public Type getType() {
              return type.getType();
            }
          });
    } catch (IOException e) {
      throw new CodecException("Failed to decode JSON to object", e);
    }
  }
}
