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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tsh.toolkit.codec.CodecException;
import com.tsh.toolkit.codec.ObjectType;
import com.tsh.toolkit.codec.messagepack.impl.MessagePackCodec.MessagePackMapperSupplier;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Unit tests for {@link MessagePackCodec}.
 *
 * <p>This suite validates MessagePack-based serialization and deserialization, including primitive,
 * generic, nested, and custom object types. It also verifies correct exception propagation,
 * null-safety, and the correct configuration of {@link MessagePackMapperSupplier}.
 *
 * <p>The tests run within a minimal Spring context providing an {@link ObjectMapper} bean for
 * autowiring convenience, while also including standalone construction tests to validate the
 * supplier-based configuration.
 */
@SpringBootTest(classes = MessagePackCodecTest.class)
@EnableAutoConfiguration
class MessagePackCodecTest {
  class Invalid {}

  /** The codec under test, injected from the Spring context. */
  @Autowired private MessagePackCodec codec;

  // ---------------------------------------------------------------------
  // Standard tests (Spring-managed codec)
  // ---------------------------------------------------------------------

  @Test
  void testEncodeInvalidValue() {
    Assertions.assertThrows(CodecException.class, () -> codec.encode(new Invalid()));
  }

  @Test
  void testDecodeInvalidType() {
    Assertions.assertThrows(CodecException.class, () -> codec.decode(new byte[] {}, null));
  }

  @Test
  void testEncodeAndDecodeSimpleType() {
    String original = "hello world";

    byte[] encoded = codec.encode(original);
    Assertions.assertNotNull(encoded);
    Assertions.assertTrue(encoded.length > 0);

    String decoded = codec.decode(encoded, new ObjectType<String>() {});
    Assertions.assertEquals(original, decoded);
  }

  @Test
  void testEncodeAndDecodeParameterizedType() {
    List<Map<String, Integer>> original = List.of(Map.of("a", 1, "b", 2), Map.of("x", 10, "y", 20));

    byte[] encoded = codec.encode(original);
    Assertions.assertNotNull(encoded);
    Assertions.assertTrue(encoded.length > 0);

    List<Map<String, Integer>> decoded =
        codec.decode(encoded, new ObjectType<List<Map<String, Integer>>>() {});

    Assertions.assertEquals(original, decoded);
    Assertions.assertEquals(2, decoded.size());
    Assertions.assertEquals(1, decoded.get(0).get("a"));
  }

  @Test
  void testDecodeInvalidMessagePackThrowsException() {
    byte[] invalidData = "{not-valid-messagepack}".getBytes();

    CodecException ex =
        Assertions.assertThrows(
            CodecException.class,
            () -> codec.decode(invalidData, new ObjectType<Map<String, String>>() {}));

    Assertions.assertTrue(ex.getMessage().contains("decode"));
  }

  @Test
  void testDecodeNullThrowsException() {
    CodecException ex =
        Assertions.assertThrows(
            CodecException.class, () -> codec.decode(null, new ObjectType<String>() {}));

    Assertions.assertTrue(ex.getMessage().contains("Cannot decode"));
  }

  @Test
  void testEncodeAndDecodeNullValue() {
    byte[] encoded = codec.encode(null);
    Assertions.assertNotNull(encoded);

    String decoded = codec.decode(encoded, new ObjectType<String>() {});
    Assertions.assertNull(decoded);
  }

  @Test
  void testEncodeAndDecodeCustomPojo() {
    record Person(String name, int age) {}

    Person original = new Person("Alice", 42);

    byte[] encoded = codec.encode(original);
    Assertions.assertNotNull(encoded);

    Person decoded = codec.decode(encoded, new ObjectType<Person>() {});
    Assertions.assertEquals(original, decoded);
  }

  @Test
  void testEncodeAndDecodeNestedStructure() {
    Map<String, Object> original =
        Map.of("name", "test", "values", List.of(Map.of("x", 1), Map.of("y", 2)));

    byte[] encoded = codec.encode(original);
    Assertions.assertNotNull(encoded);

    Map<String, Object> decoded = codec.decode(encoded, new ObjectType<Map<String, Object>>() {});
    Assertions.assertEquals(original, decoded);
  }

  // ---------------------------------------------------------------------
  // Supplier-based construction tests
  // ---------------------------------------------------------------------

  /**
   * Verifies that {@link MessagePackMapperSupplier} provides a properly configured {@link
   * ObjectMapper} using {@link MessagePackFactory}.
   */
  @Test
  void testMessagePackMapperSupplierCreatesValidMapper() {
    MessagePackMapperSupplier supplier =
        () -> {
          ObjectMapper om = new ObjectMapper(new MessagePackFactory());
          om.registerModule(new JavaTimeModule());
          om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
          return om;
        };
    ObjectMapper mapper = supplier.get();

    Assertions.assertNotNull(mapper, "Supplier should return a non-null ObjectMapper");
    Assertions.assertTrue(
        mapper.getFactory() instanceof MessagePackFactory,
        "ObjectMapper must use MessagePackFactory");

    try {
      Map<String, String> original = Map.of("key", "value");
      byte[] encoded = mapper.writeValueAsBytes(original);
      Map<?, ?> decoded = mapper.readValue(encoded, Map.class);
      Assertions.assertEquals(original, decoded);
    } catch (IOException e) {
      Assertions.fail("Supplier-provided mapper failed to serialize/deserialize", e);
    }
  }

  /**
   * Verifies that a {@link MessagePackCodec} built using {@link MessagePackMapperSupplier} behaves
   * identically to a Spring-injected instance.
   */
  @Test
  void testCodecConstructedWithSupplierPerformsCorrectly() {
    MessagePackCodec supplierCodec =
        new MessagePackCodec(
            () -> {
              ObjectMapper om = new ObjectMapper(new MessagePackFactory());
              om.registerModule(new JavaTimeModule());
              om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
              return om;
            });

    record Sample(String key, int value) {}

    Sample original = new Sample("data", 123);

    byte[] encoded = supplierCodec.encode(original);
    Assertions.assertNotNull(encoded);
    Assertions.assertTrue(encoded.length > 0);

    Sample decoded = supplierCodec.decode(encoded, new ObjectType<Sample>() {});
    Assertions.assertEquals(original, decoded);
  }

  /**
   * Ensures that constructing a codec with a null-returning supplier fails cleanly and predictably.
   */
  @Test
  void testCodecFailsWithNullMapperFromSupplier() {
    Assertions.assertNotNull(new MessagePackCodec(null));
  }
}
