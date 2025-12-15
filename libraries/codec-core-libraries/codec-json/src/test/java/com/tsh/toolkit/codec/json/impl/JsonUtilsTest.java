package com.tsh.toolkit.codec.json.impl;

import java.io.UncheckedIOException;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = JsonUtilsTest.class)
@EnableAutoConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JsonUtilsTest {

  @Order(1)
  @Test
  void testSetObjectMapper() {
    JsonUtils.instance().setObjectMapper(null);
  }

  @Order(2)
  @Test
  void testMap() {
    Assertions.assertEquals(
        Map.of("age", 0), JsonUtils.map(om -> om.readValue("{\"age\":0}", Map.class)));

    Assertions.assertThrows(
        UncheckedIOException.class,
        () -> {
          JsonUtils.map(om -> om.readValue("{age:0}", Map.class));
        });
  }

  @Order(2)
  @Test
  void testStringify() {
    Assertions.assertNull(JsonUtils.stringify(null));
    Assertions.assertEquals("{\"age\":0}", JsonUtils.stringify(Map.of("age", 0)));
  }

  @Order(3)
  @Test
  void testToBinary() {
    final byte[] bin = JsonUtils.toBinary(Map.of("age", 0));
    Assertions.assertEquals(Map.of("age", 0), JsonUtils.map(om -> om.readValue(bin, Map.class)));
  }
}
