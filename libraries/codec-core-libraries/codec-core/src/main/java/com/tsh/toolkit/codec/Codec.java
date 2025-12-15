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

/**
 * Generic encoding and decoding abstraction.
 *
 * <p>A {@code Codec} provides a uniform interface for serializing and deserializing arbitrary
 * objects to and from a binary representation. It is typically used to convert in-memory data
 * structures into formats suitable for network transmission or persistent storage, and vice versa.
 *
 * <p>Implementations may use different serialization strategies—such as JSON, CBOR, Protobuf, Avro,
 * or custom binary encodings—but should always guarantee that data encoded by {@link
 * #encode(Object)} can be accurately reconstructed via {@link #decode(byte[], ObjectType)}.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Codec codec = new JsonCodec();
 *
 * // Encode
 * List<String> list = List.of("A", "B", "C");
 * byte[] encoded = codec.encode(list);
 *
 * // Decode using type capture
 * List<String> decoded = codec.decode(encoded, new ObjectType<List<String>>() {});
 * }</pre>
 */
public interface Codec {

  /**
   * Encodes the given object into a binary representation.
   *
   * <p>The returned byte array must fully represent the serialized form of the input object.
   * Implementations should ensure deterministic encoding when possible to support cache keys and
   * distributed data consistency.
   *
   * @param obj the object to encode; may be {@code null} if the implementation supports it
   * @return a non-null byte array representing the serialized form of the input object
   * @throws CodecException if encoding fails due to invalid input, I/O error, or serialization
   *     failure
   */
  byte[] encode(Object obj) throws CodecException;

  /**
   * Decodes the provided binary data into an object of the specified type.
   *
   * <p>The {@link ObjectType} parameter preserves full generic type information, allowing the codec
   * to reconstruct complex parameterized structures (e.g. {@code List<Foo>} or {@code Map<String,
   * Bar>}) that would otherwise be lost due to Java type erasure.
   *
   * @param encoded the serialized byte array to decode; must not be {@code null}
   * @param type the {@link ObjectType} representing the target type for deserialization; must not
   *     be {@code null}
   * @param <T> the expected result type
   * @return a deserialized instance of the requested type
   * @throws CodecException if decoding fails due to malformed input, type mismatch, or I/O error
   */
  <T> T decode(byte[] encoded, ObjectType<T> type) throws CodecException;
}
