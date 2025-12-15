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

package com.tsh.toolkit.core;

import com.tsh.toolkit.core.utils.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test for Maps utility. */
class MapsTest {

  @Test
  void test() {
    final Map<UUID, Object> source = new HashMap<>();
    final UUID key = UUID.randomUUID();
    Assertions.assertNull(source.get(key));
    Assertions.assertEquals("Hello", Maps.get(source, key, () -> "Hello"));
    Assertions.assertEquals("Hello", Maps.get(source, key, () -> "Hello again"));
    Assertions.assertEquals("Hello", source.get(key));
  }
}
