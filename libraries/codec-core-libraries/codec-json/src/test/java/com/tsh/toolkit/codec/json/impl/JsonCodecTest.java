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
import com.tsh.toolkit.codec.CodecException;
import com.tsh.toolkit.codec.ObjectType;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Unit tests for {@link JsonCodec}.
 *
 * <p>This suite verifies round-trip serialization, type handling, null safety, and {@link
 * CodecException} propagation. It also validates proper fallback to default mappers when custom
 * suppliers return {@code null}.
 */
@SpringBootTest(classes = JsonCodecTest.TestConfig.class)
@EnableAutoConfiguration
class JsonCodecTest {

  /** Minimal Spring configuration — no beans defined. */
  static class TestConfig {}

  @Autowired private JsonCodec codec;

  @Test
  void testEncodeAndDecodeSimpleType() {
    String original = "hello world";
    byte[] encoded = codec.encode(original);
    String decoded = codec.decode(encoded, new ObjectType<String>() {});
    Assertions.assertEquals(original, decoded);
  }

  @Test
  void testEncodeAndDecodeParameterizedType() {
    List<Map<String, Integer>> original = List.of(Map.of("a", 1, "b", 2), Map.of("x", 10, "y", 20));
    byte[] encoded = codec.encode(original);
    List<Map<String, Integer>> decoded =
        codec.decode(encoded, new ObjectType<List<Map<String, Integer>>>() {});
    Assertions.assertEquals(original, decoded);
  }

  @Test
  void testDecodeInvalidJsonThrowsException() {
    byte[] invalidJson = "{invalid}".getBytes();
    CodecException ex =
        Assertions.assertThrows(
            CodecException.class,
            () -> codec.decode(invalidJson, new ObjectType<Map<String, String>>() {}));
    Assertions.assertTrue(ex.getMessage().contains("decode"));
  }

  @Test
  void testEncodeNullValue() {
    byte[] encoded = codec.encode(null);
    Assertions.assertEquals("null", new String(encoded));
  }

  @Test
  void testDecodeEmptyJson() {
    byte[] emptyJson = "{}".getBytes();
    Map<String, Object> decoded = codec.decode(emptyJson, new ObjectType<Map<String, Object>>() {});
    Assertions.assertTrue(decoded.isEmpty());
  }

  @Test
  void testCustomObjectMapperFactoryFallback() {
    JsonCodec codecWithNullFactory = new JsonCodec((JsonCodec.JsonMapperSupplier) () -> null);
    String original = "test";
    byte[] encoded = codecWithNullFactory.encode(original);
    String decoded = codecWithNullFactory.decode(encoded, new ObjectType<String>() {});
    Assertions.assertEquals(original, decoded);
  }

  @Test
  void testCustomObjectMapperFactoryUsage() {
    ObjectMapper customMapper = new ObjectMapper();
    JsonCodec customCodec = new JsonCodec(() -> customMapper);
    Map<String, String> data = Map.of("key", "value");
    byte[] encoded = customCodec.encode(data);
    Map<String, String> decoded =
        customCodec.decode(encoded, new ObjectType<Map<String, String>>() {});
    Assertions.assertEquals(data, decoded);
  }

  /**
   * Verifies that {@link CodecException} contains an {@link IOException} cause when {@code encoded}
   * or {@code type} are {@code null}.
   */
  @Test
  void testDecodeNullInputsThrowCodecExceptionWithIoCause() {
    CodecException ex1 =
        Assertions.assertThrows(
            CodecException.class, () -> codec.decode(null, new ObjectType<>() {}));
    Assertions.assertInstanceOf(IOException.class, ex1.getCause());

    CodecException ex2 =
        Assertions.assertThrows(CodecException.class, () -> codec.decode("{}".getBytes(), null));
    Assertions.assertInstanceOf(IOException.class, ex2.getCause());
  }
}
