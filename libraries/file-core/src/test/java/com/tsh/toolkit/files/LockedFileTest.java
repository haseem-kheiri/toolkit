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

package com.tsh.toolkit.files;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LockedFileTest {

  private Path tempFile;

  @AfterEach
  void tearDown() throws IOException {
    if (tempFile != null) {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  void testWithLockedRegionExecutesBlockAndThrowsException() throws IOException {
    tempFile = Files.createTempFile("locked", ".dat");

    Assertions.assertThrows(
        IOException.class,
        () -> {
          try (FileChannel channel =
              FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            LockedFile.withLockedRegion(
                channel,
                0,
                10,
                () -> {
                  // verify we hold the lock
                  try (FileLock lock = channel.tryLock(0, 10, false)) {
                    Assertions.assertNull(lock, "Expected lock to be unavailable");
                  } catch (IOException ignored) {
                    // expected, already locked
                  }
                  return "success";
                });
          }
        });
  }

  @Test
  void testWithLockedFileDelegatesToLockedRegion() throws IOException {
    tempFile = Files.createTempFile("locked", ".dat");

    try (FileChannel channel =
        FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
      String result = LockedFile.withLockedFile(channel, () -> "whole-file");
      Assertions.assertEquals("whole-file", result);
    }
  }

  @Test
  void testNullArgumentsThrowExceptions() {
    Assertions.assertThrows(
        NullPointerException.class, () -> LockedFile.withLockedFile(null, () -> null));

    try (FileChannel channel =
        FileChannel.open(
            Files.createTempFile("locked", ".dat"),
            StandardOpenOption.READ,
            StandardOpenOption.WRITE)) {
      Assertions.assertThrows(
          NullPointerException.class, () -> LockedFile.withLockedRegion(channel, 0, 10, null));
    } catch (IOException e) {
      Assertions.fail(e);
    }
  }

  @Test
  void testNegativeSizeThrowsException() throws IOException {
    tempFile = Files.createTempFile("locked", ".dat");

    try (FileChannel channel =
        FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> LockedFile.withLockedRegion(channel, 0, -1, () -> null));
    }
  }

  @Test
  void testIOExceptionFromBlockPropagates() throws IOException {
    tempFile = Files.createTempFile("locked", ".dat");

    try (FileChannel channel =
        FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
      Assertions.assertThrows(
          IOException.class,
          () ->
              LockedFile.withLockedRegion(
                  channel,
                  0,
                  5,
                  () -> {
                    throw new IOException("simulated");
                  }));
    }
  }

  @Test
  void testLockIsReleasedAfterExecution() throws IOException {
    tempFile = Files.createTempFile("locked", ".dat");

    try (FileChannel channel =
        FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
      LockedFile.withLockedFile(channel, () -> "done");

      // Should be able to acquire the same lock again
      try (FileLock lock = channel.tryLock(0, Long.MAX_VALUE, false)) {
        Assertions.assertNotNull(lock, "Lock should be released after block execution");
      }
    }
  }
}
