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

import com.tsh.toolkit.core.utils.Threads;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/**
 * Utility class for safe file operations with {@link FileChannel}, providing exclusive access via
 * {@link FileLock}.
 *
 * <p>This class offers helper methods for:
 *
 * <ul>
 *   <li>Executing tasks while holding an exclusive file or region lock
 *   <li>Writing byte arrays to a file in full, using a reusable {@link ByteBuffer}
 *   <li>Reading an exact number of bytes from a file
 * </ul>
 *
 * <p>All locks acquired are <b>exclusive</b> (no concurrent readers/writers allowed). The class is
 * final and not instantiable.
 */
public final class LockedFile {

  private LockedFile() {
    // Utility class; prevent instantiation
  }

  /**
   * A functional interface similar to {@link java.util.concurrent.Callable}, but restricted to
   * throwing {@link IOException}.
   *
   * @param <T> the type of the result produced by the task
   */
  @FunctionalInterface
  public interface LockedTask<T> {
    T call() throws IOException;
  }

  /**
   * Executes a block of code while holding an <b>exclusive lock</b> on a specific region of a file.
   *
   * <p>This method attempts to acquire a lock on the specified file region, retrying a limited
   * number of times if an {@link java.nio.channels.OverlappingFileLockException} occurs (which
   * typically indicates that the same JVM already holds a conflicting lock on the region). Between
   * retries, it pauses briefly to allow any in-flight operations to complete.
   *
   * <p>Unlike {@link FileChannel#tryLock(long, long, boolean)}, this method blocks until a lock is
   * acquired (or retries are exhausted) and ensures proper cleanup through try-with-resources
   * semantics. The lock is automatically released when the block completes or an exception is
   * thrown.
   *
   * @param <T> the type of result returned by the block
   * @param file the {@link FileChannel} to lock; must not be {@code null}
   * @param from the starting position of the lock (inclusive)
   * @param size the number of bytes to lock; must be {@code >= 0}
   * @param block the block of code to execute while the lock is held; must not be {@code null}
   * @return the result produced by the block
   * @throws IOException if the lock cannot be acquired after all retries, or if {@code
   *     block.call()} throws an {@link IOException}
   * @throws NullPointerException if {@code file} or {@code block} is {@code null}
   * @throws IllegalArgumentException if {@code size} is negative
   * @implNote This method retries up to twenty five times when an {@link
   *     OverlappingFileLockException} is thrown, waiting 10 milliseconds between attempts. The
   *     retry loop is primarily designed to handle short-lived reentrancy scenarios within the same
   *     JVM and should not be used to coordinate high-contention locking across multiple threads.
   */
  public static <T> T withLockedRegion(FileChannel file, long from, long size, LockedTask<T> block)
      throws IOException {
    if (file == null || block == null) {
      throw new NullPointerException("file and block must not be null");
    }

    if (size < 0) {
      throw new IllegalArgumentException("size must be >= 0");
    }

    int retries = 25;
    while (true) {
      try (FileLock lock = file.lock(from, size, false)) {
        return block.call();
      } catch (OverlappingFileLockException e) {
        if (--retries == 0) {
          throw new IOException("Failed to acquire lock after retries", e);
        }
        Threads.sleep(10);
      }
    }
  }

  /**
   * Executes a block of code while holding an <b>exclusive lock</b> on the entire file.
   *
   * <p>This method locks the full file (from position {@code 0} to {@link Long#MAX_VALUE}) and
   * executes the provided block while the lock is held. It ensures mutual exclusion across all
   * threads and processes attempting to acquire a lock on the same file.
   *
   * <p>If an {@link java.nio.channels.OverlappingFileLockException} occurs (indicating that the
   * current JVM already holds an overlapping lock on the file), this method retries a limited
   * number of times with short pauses between attempts.
   *
   * <p>The lock is automatically released when the block completes or an exception occurs. This
   * method guarantees proper cleanup even in the presence of exceptions.
   *
   * @param <T> the type of result returned by the block
   * @param file the {@link FileChannel} to lock; must not be {@code null}
   * @param block the block of code to execute while the lock is held; must not be {@code null}
   * @return the result produced by the block
   * @throws IOException if the lock cannot be acquired after all retries, or if {@code
   *     block.call()} throws an {@link IOException}
   * @throws NullPointerException if {@code file} or {@code block} is {@code null}
   * @implNote This method delegates to {@link #withLockedRegion(FileChannel, long, long,
   *     LockedTask)} with parameters {@code from = 0} and {@code size = Long.MAX_VALUE}. It retries
   *     up to twenty five times with 10-millisecond intervals when an {@link
   *     OverlappingFileLockException} is encountered. The retry mechanism is intended only for
   *     transient, intra-process lock contention and should not be used as a general-purpose
   *     inter-process synchronization mechanism.
   */
  public static <T> T withLockedFile(FileChannel file, LockedTask<T> block) throws IOException {
    return withLockedRegion(file, 0, Long.MAX_VALUE, block);
  }
}
