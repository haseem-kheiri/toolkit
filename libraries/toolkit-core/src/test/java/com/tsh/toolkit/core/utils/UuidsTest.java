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

package com.tsh.toolkit.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class UuidsTest {

  @Test
  void shouldGenerateVersion7Uuid() {
    UUID uuid = Uuids.uuid7();
    assertEquals(7, uuid.version(), "UUID version must be 7");
  }

  @Test
  void shouldUseRfc4122Variant() {
    UUID uuid = Uuids.uuid7();
    assertEquals(2, uuid.variant(), "UUID variant must be RFC 4122");
  }

  @Test
  void shouldHaveNonDecreasingTimestamps() {
    UUID u1 = Uuids.uuid7();
    UUID u2 = Uuids.uuid7();
    UUID u3 = Uuids.uuid7();

    long t1 = extractTimestamp(u1);
    long t2 = extractTimestamp(u2);
    long t3 = extractTimestamp(u3);

    assertTrue(t1 <= t2);
    assertTrue(t2 <= t3);
  }

  private static long extractTimestamp(UUID uuid) {
    return (uuid.getMostSignificantBits() >>> 16) & 0xFFFFFFFFFFFFL;
  }

  @Test
  void shouldEmbedCurrentEpochMillis() {
    long before = System.currentTimeMillis();
    UUID uuid = Uuids.uuid7();
    long after = System.currentTimeMillis();

    long timestamp = (uuid.getMostSignificantBits() >>> 16) & 0xFFFFFFFFFFFFL;

    assertTrue(
        timestamp >= before && timestamp <= after,
        "UUID timestamp must be within generation window");
  }

  @Test
  void shouldHaveNonDecreasingTimestampsUnderConcurrency() throws Exception {
    int threads = 8;
    int perThread = 1_000;

    ExecutorService executor = Executors.newFixedThreadPool(threads);
    List<Future<List<UUID>>> futures = new ArrayList<>();

    for (int i = 0; i < threads; i++) {
      futures.add(
          executor.submit(
              () -> {
                List<UUID> uuids = new ArrayList<>(perThread);
                for (int j = 0; j < perThread; j++) {
                  uuids.add(Uuids.uuid7());
                }
                return uuids;
              }));
    }

    List<UUID> all = new ArrayList<>(threads * perThread);
    for (Future<List<UUID>> f : futures) {
      all.addAll(f.get());
    }

    executor.shutdown();

    // Sort by embedded timestamp (not UUID.compareTo)
    all.sort(Comparator.comparingLong(UuidsTest::extractTimestamp));

    long previous = -1;
    for (UUID uuid : all) {
      long ts = extractTimestamp(uuid);
      assertTrue(ts >= previous, "Timestamps must be non-decreasing");
      previous = ts;
    }
  }
}
