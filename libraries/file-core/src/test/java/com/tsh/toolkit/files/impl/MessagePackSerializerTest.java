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

package com.tsh.toolkit.files.impl;

import com.tsh.toolkit.codec.ObjectType;
import com.tsh.toolkit.codec.messagepack.impl.MessagePackCodec;
import com.tsh.toolkit.dataset.People;
import com.tsh.toolkit.dataset.People.Person;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MessagePackSerializerTest {

  @Getter
  @Setter
  static class TestClass {
    private byte byteValue = 0;
    private short shortValue = 1;
    private int intValue = 2;
    private long longValue = 3;

    private float floatValue = 4;
    private double doubleValue = 5;

    private boolean booleanValue = true;

    private char charValue = 'a';
    private String stringValue = "text";

    private byte[] byteArray = {0};
    private short[] shortArray = {1};
    private int[] intArray = {2};
    private long[] longArray = {3};

    private float[] floatArray = {4};
    private double[] doubleArray = {5};

    private boolean[] booleanArray = {true};

    private char[] charArray = {'a'};
    private String[] stringArray = {"text", "characters"};

    private List<String> listValue = List.of("string1", "string2");
    private Map<Integer, String> mapValue = Map.of(1, "string1");
  }

  private final MessagePackCodec serializer = new MessagePackCodec();

  @Test
  void test1() {
    final byte[] bin = serializer.encode(new TestClass());

    TestClass o = serializer.decode(bin, new ObjectType<TestClass>() {});
    Assertions.assertEquals(0, o.byteValue);
    Assertions.assertEquals(1, o.shortValue);
    Assertions.assertEquals(2, o.intValue);
    Assertions.assertEquals(3, o.longValue);
    Assertions.assertEquals(4, o.floatValue);
    Assertions.assertEquals(5, o.doubleValue);
    Assertions.assertEquals(true, o.booleanValue);
    Assertions.assertEquals('a', o.charValue);
    Assertions.assertEquals("text", o.stringValue);

    Assertions.assertEquals(0, o.byteArray[0]);
    Assertions.assertEquals(1, o.shortArray[0]);
    Assertions.assertEquals(2, o.intArray[0]);
    Assertions.assertEquals(3, o.longArray[0]);
    Assertions.assertEquals(4, o.floatArray[0]);
    Assertions.assertEquals(5, o.doubleArray[0]);
    Assertions.assertEquals(true, o.booleanArray[0]);
    Assertions.assertEquals('a', o.charArray[0]);
    Assertions.assertEquals("text", o.stringArray[0]);
    Assertions.assertEquals("characters", o.stringArray[1]);

    Assertions.assertEquals("string1", o.listValue.get(0));
    Assertions.assertEquals("string2", o.listValue.get(1));
    Assertions.assertEquals("string1", o.mapValue.get(1));
  }

  @Test
  void test2() {
    final byte[] bin = serializer.encode(People.randomPerson(UUID.randomUUID()));
    final Person person = serializer.decode(bin, new ObjectType<Person>() {});
    Assertions.assertNotNull(person);
  }
}
