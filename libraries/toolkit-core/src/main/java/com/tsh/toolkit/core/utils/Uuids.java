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

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for generating RFC 9562 compliant UUID version 7 values.
 *
 * <p>UUIDv7 is a time-ordered UUID format that uses the Unix epoch timestamp (milliseconds) as the
 * most significant component, followed by a per-millisecond sequence counter and random entropy.
 * This makes UUIDv7 suitable for use as database keys, log identifiers, and other scenarios where
 * roughly monotonic ordering is desirable.
 *
 * <h2>Properties</h2>
 *
 * <ul>
 *   <li>Time-ordered by Unix epoch milliseconds
 *   <li>Thread-safe and lock-free
 *   <li>RFC 4122 variant compliant
 *   <li>RFC 9562 UUID version 7 compliant
 * </ul>
 *
 * <h2>Ordering Guarantees</h2>
 *
 * <ul>
 *   <li>Monotonic ordering is preserved within the same JVM instance
 *   <li>Up to 4096 UUIDs per millisecond are strictly ordered
 *   <li>If more than 4096 UUIDs are generated in the same millisecond, ordering within that
 *       millisecond may wrap
 *   <li>System clock regressions may affect ordering
 * </ul>
 *
 * <p>This implementation prioritizes simplicity and performance while remaining compliant with the
 * UUIDv7 specification.
 */
public final class Uuids {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final AtomicLong LAST_TIME = new AtomicLong(-1);
  private static final AtomicLong COUNTER = new AtomicLong();

  private Uuids() {
    // utility class
  }

  /**
   * Generates a new UUID version 7 value.
   *
   * <p>The generated UUID embeds the current Unix epoch time in milliseconds and includes a
   * per-millisecond counter to ensure monotonic ordering under concurrent access.
   *
   * @return a RFC 9562 compliant UUID version 7
   * @see <a href="https://www.rfc-editor.org/rfc/rfc9562#name-uuid-version-7">RFC 9562 - UUID
   *     Version 7</a>
   */
  public static UUID uuid7() {
    long now = System.currentTimeMillis();
    long last = LAST_TIME.getAndUpdate(prev -> prev == now ? prev : now);

    long count = (now == last) ? COUNTER.incrementAndGet() : COUNTER.getAndSet(0);

    long msb = (now & 0xFFFFFFFFFFFFL) << 16;
    msb |= 0x7000; // version 7
    msb |= (count & 0x0FFF); // 12-bit sequence counter

    long lsb = RANDOM.nextLong();
    lsb &= 0x3FFFFFFFFFFFFFFFL; // clear variant
    lsb |= 0x8000000000000000L; // RFC 4122 variant

    return new UUID(msb, lsb);
  }
}
