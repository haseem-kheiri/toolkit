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

package com.tsh.toolkit.cache;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class CacheConfigTest {

  @Test
  void shouldCreateConfigWithDefaultValues() {
    CacheConfig config = new CacheConfig();

    assertEquals(Duration.ofMinutes(5), config.getTtl());
    assertEquals(5000, config.getMaxSize());
  }

  @Test
  void shouldSetTtlFluentlyWithValidDuration() {
    CacheConfig config = new CacheConfig();
    Duration ttl = Duration.ofMinutes(10);

    CacheConfig result = config.ttl(ttl);

    assertEquals(ttl, config.getTtl());
    assertSame(config, result); // Fluent interface
  }

  @Test
  void shouldSetMaxSizeFluentlyWithValidValue() {
    CacheConfig config = new CacheConfig();
    int maxSize = 1000;

    CacheConfig result = config.maxSize(maxSize);

    assertEquals(maxSize, config.getMaxSize());
    assertSame(config, result); // Fluent interface
  }

  @Test
  void shouldSupportFluentChaining() {
    CacheConfig config = new CacheConfig()
        .ttl(Duration.ofHours(2))
        .maxSize(10000);

    assertEquals(Duration.ofHours(2), config.getTtl());
    assertEquals(10000, config.getMaxSize());
  }

  @Test
  void shouldThrowNullPointerExceptionForNullTtl() {
    CacheConfig config = new CacheConfig();

    assertThrows(IllegalArgumentException.class, () -> {
      config.ttl(null);
    });
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForZeroMaxSize() {
    CacheConfig config = new CacheConfig();

    assertThrows(IllegalArgumentException.class, () -> {
      config.maxSize(0);
    });
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForNegativeMaxSize() {
    CacheConfig config = new CacheConfig();

    assertThrows(IllegalArgumentException.class, () -> {
      config.maxSize(-1);
    });
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForMaxSizeAboveLimit() {
    CacheConfig config = new CacheConfig();

    assertThrows(IllegalArgumentException.class, () -> {
      config.maxSize(100001);
    });
  }

  @Test
  void shouldAcceptMinimumValidMaxSize() {
    CacheConfig config = new CacheConfig();

    CacheConfig result = config.maxSize(1);

    assertEquals(1, config.getMaxSize());
    assertSame(config, result);
  }

  @Test
  void shouldAcceptMaximumValidMaxSize() {
    CacheConfig config = new CacheConfig();

    CacheConfig result = config.maxSize(100000);

    assertEquals(100000, config.getMaxSize());
    assertSame(config, result);
  }

  @Test
  void shouldSupportVeryShortTtl() {
    CacheConfig config = new CacheConfig();
    Duration shortTtl = Duration.ofMillis(100);

    config.ttl(shortTtl);

    assertEquals(shortTtl, config.getTtl());
  }

  @Test
  void shouldSupportVeryLongTtl() {
    CacheConfig config = new CacheConfig();
    Duration longTtl = Duration.ofDays(365);

    config.ttl(longTtl);

    assertEquals(longTtl, config.getTtl());
  }

  @Test
  void shouldSupportZeroDurationTtl() {
    CacheConfig config = new CacheConfig();
    Duration zeroTtl = Duration.ZERO;

    config.ttl(zeroTtl);

    assertEquals(zeroTtl, config.getTtl());
  }

  @Test
  void shouldAllowMultipleConfigurationChanges() {
    CacheConfig config = new CacheConfig();

    // First configuration
    config.ttl(Duration.ofMinutes(1)).maxSize(100);
    assertEquals(Duration.ofMinutes(1), config.getTtl());
    assertEquals(100, config.getMaxSize());

    // Second configuration
    config.ttl(Duration.ofHours(1)).maxSize(1000);
    assertEquals(Duration.ofHours(1), config.getTtl());
    assertEquals(1000, config.getMaxSize());
  }

  @Test
  void shouldRetainPreviousValueOnException() {
    CacheConfig config = new CacheConfig()
        .ttl(Duration.ofMinutes(10))
        .maxSize(5000);

    Duration originalTtl = config.getTtl();
    Integer originalMaxSize = config.getMaxSize();

    // Attempt invalid operations
    assertThrows(IllegalArgumentException.class, () -> config.ttl(null));
    assertThrows(IllegalArgumentException.class, () -> config.maxSize(-1));

    // Original values should be preserved
    assertEquals(originalTtl, config.getTtl());
    assertEquals(originalMaxSize, config.getMaxSize());
  }

  @Test
  void shouldProvideGoodDefaultsForProductionUse() {
    CacheConfig defaults = new CacheConfig();

    // Default TTL should be reasonable for most use cases
    assertTrue(defaults.getTtl().toMinutes() > 0);
    assertTrue(defaults.getTtl().toMinutes() <= 60); // Not too long

    // Default max size should be reasonable
    assertTrue(defaults.getMaxSize() > 100); // Not too small
    assertTrue(defaults.getMaxSize() <= 10000); // Not too large
  }
}