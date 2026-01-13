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

package com.tsh.toolkit.cache.impl;

import com.tsh.toolkit.cache.Cache;
import com.tsh.toolkit.core.utils.Threads;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CacheTest {
  private CacheManager cacheManager = new CacheManager();

  @Test
  void testBuildNameNull() {
    Assertions.assertEquals(
        "name must not be blank.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                    cacheManager
                        .<String, String>create(null)
                        .ofTypes(String.class, String.class)
                        .config(cfg -> cfg.ttl(Duration.ofSeconds(2)).maxSize(1000))
                        .build())
            .getLocalizedMessage());
  }

  @Test
  void testBuildKeyTypeNull() {
    Assertions.assertEquals(
        "key type must not be null.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                    cacheManager
                        .<String, String>create("test-cache")
                        .ofTypes(null, String.class)
                        .config(cfg -> cfg.ttl(Duration.ofSeconds(2)).maxSize(1000))
                        .build())
            .getLocalizedMessage());
  }

  @Test
  void testBuildValueTypeNull() {
    Assertions.assertEquals(
        "value type must not be null.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                    cacheManager
                        .<String, String>create("test-cache")
                        .ofTypes(String.class, null)
                        .config(cfg -> cfg.ttl(Duration.ofSeconds(2)).maxSize(1000))
                        .build())
            .getLocalizedMessage());
  }

  @Test
  void testBuildConfigNull() {
    Assertions.assertEquals(
        "config must not be null.",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                    cacheManager
                        .<String, String>create("test-cache")
                        .ofTypes(String.class, String.class)
                        .config(null)
                        .build())
            .getLocalizedMessage());
  }

  @Test
  void testPut() {
    Cache<String, String> cache =
        cacheManager
            .<String, String>create("test-cache")
            .ofTypes(String.class, String.class)
            .config(cfg -> cfg.ttl(Duration.ofSeconds(2)).maxSize(1000))
            .build();

    Assertions.assertNull(cache.get("key"));
    cache.put("key", "value");
    Assertions.assertEquals("value", cache.get("key"));
    Threads.sleep(3, TimeUnit.SECONDS);
    Assertions.assertNull(cache.get("key"));
  }
}
