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

package com.tsh.toolkit.codec.messagepack.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tsh.toolkit.codec.Codec;
import com.tsh.toolkit.codec.CodecException;
import com.tsh.toolkit.codec.ObjectType;
import java.io.IOException;
import java.lang.reflect.Type;
import org.msgpack.jackson.dataformat.MessagePackFactory;

/**
 * MessagePack-based implementation of the {@link Codec} interface using Jackson.
 *
 * <p>This codec provides a compact, high-performance binary serialization mechanism based on the
 * MessagePack format. It serializes Java objects into binary form and deserializes them back to
 * Java types using Jackson’s {@link ObjectMapper} configured with a {@link MessagePackFactory}.
 *
 * <p>The codec supports both simple and parameterized types (e.g., {@code List<Map<String,
 * Integer>>}) and automatically registers the {@link JavaTimeModule} for Java 8 date/time support.
 * The {@link SerializationFeature#WRITE_DATES_AS_TIMESTAMPS} setting is disabled to ensure
 * date/time values are written in ISO-8601 format.
 *
 * <p>This codec is suitable for scenarios where binary efficiency, compactness, or low-latency data
 * exchange is important — such as cluster gossip, replicated state transfer, or inter-node
 * messaging.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Codec codec = new MessagePackCodec();
 *
 * // Encode
 * byte[] encoded = codec.encode(Map.of("a", 1, "b", 2));
 *
 * // Decode
 * Map<String, Integer> decoded =
 *     codec.decode(encoded, new ObjectType<Map<String, Integer>>() {});
 * }</pre>
 *
 * <p>For human-readable or configuration-oriented serialization, prefer {@link
 * com.platform.codec.json.impl.JsonCodec} instead.
 */
public class MessagePackCodec implements Codec {

  /**
   * Functional interface for supplying a configured {@link ObjectMapper} instance.
   *
   * <p>This allows external customization of serialization behavior (e.g., registering modules,
   * configuring visibility, naming strategies, or date/time formatting) without directly modifying
   * the codec.
   */
  @FunctionalInterface
  public interface MessagePackMapperSupplier {
    /**
     * Returns a configured {@link ObjectMapper} instance.
     *
     * @return a non-null {@link ObjectMapper}; implementations may return {@code null}, in which
     *     case a default MessagePack mapper will be created
     */
    ObjectMapper get();
  }

  /** Jackson {@link ObjectMapper} configured with a {@link MessagePackFactory}. */
  private final ObjectMapper objectMapper;

  /**
   * Constructs a new {@code MessagePackCodec} with a default {@link ObjectMapper} configured for
   * MessagePack serialization.
   *
   * <p>The mapper includes:
   *
   * <ul>
   *   <li>{@link MessagePackFactory} for binary encoding and decoding
   *   <li>{@link JavaTimeModule} for Java 8 date/time type support
   *   <li>{@link SerializationFeature#WRITE_DATES_AS_TIMESTAMPS} disabled for ISO-8601 output
   * </ul>
   */
  public MessagePackCodec() {
    objectMapper = new ObjectMapper(new MessagePackFactory());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  /**
   * Constructs a {@code MessagePackCodec} using a custom mapper supplier.
   *
   * <p>If the provided {@code supplier} is {@code null}, or if it returns {@code null}, a default
   * {@link ObjectMapper} with {@link MessagePackFactory} is used. This allows dependency injection
   * frameworks (e.g., Spring) or testing utilities to provide specialized configurations while
   * preserving default behavior otherwise.
   *
   * @param supplier a supplier of preconfigured {@link ObjectMapper} instances
   */
  public MessagePackCodec(MessagePackMapperSupplier supplier) {
    ObjectMapper om = null;
    if (supplier != null) {
      om = supplier.get();
    }

    if (om == null) {
      om = new ObjectMapper(new MessagePackFactory());
      om.registerModule(new JavaTimeModule());
      om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    this.objectMapper = om;
  }

  /**
   * Serializes the given Java object into its MessagePack-encoded byte representation.
   *
   * @param obj the object to serialize; may be {@code null}
   * @return the MessagePack-encoded byte array
   * @throws CodecException if serialization fails
   */
  @Override
  public byte[] encode(Object obj) throws CodecException {
    try {
      return objectMapper.writeValueAsBytes(obj);
    } catch (JsonProcessingException e) {
      throw new CodecException("Failed to encode object to MessagePack", e);
    }
  }

  /**
   * Deserializes the given MessagePack byte array into an object of the specified type.
   *
   * <p>Supports both simple and parameterized types via {@link ObjectType}, allowing accurate
   * reconstruction of complex generic structures such as {@code List<Map<String, Integer>>}.
   *
   * <p>Both {@code encoded} and {@code type} must be non-null. A {@link CodecException} is thrown
   * if either argument is {@code null} or if the MessagePack payload cannot be deserialized into
   * the requested type.
   *
   * <p>This method leverages a Jackson {@link com.fasterxml.jackson.databind.ObjectMapper}
   * configured with the MessagePack data format to handle serialization details transparently.
   *
   * @param encoded the MessagePack-encoded byte array; must not be {@code null}
   * @param type the {@link ObjectType} describing the target type to decode into; must not be
   *     {@code null}
   * @param <T> the generic type of the decoded object
   * @return the decoded object instance
   * @throws CodecException if deserialization fails, the input is invalid, or the byte array is
   *     {@code null}
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
      throw new CodecException("Failed to decode MessagePack to object", e);
    }
  }
}
