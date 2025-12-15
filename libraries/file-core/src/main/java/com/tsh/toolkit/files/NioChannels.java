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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Utility class for efficient, low-level I/O operations using Java NIO {@link FileChannel} and
 * {@link ByteBuffer}.
 *
 * <p>This class provides optimized methods for reading and writing binary data, primitives, and
 * byte arrays directly to channels. It focuses on performance, safety, and explicit control over
 * buffering and persistence.
 *
 * <p>Key features include:
 *
 * <ul>
 *   <li>Reusable {@link ByteBuffer} instances to minimize memory churn and GC pressure.
 *   <li>Chunked I/O for handling large data efficiently.
 *   <li>Support for forced writes to disk for durability guarantees.
 *   <li>Exact-length reads that prevent silent truncation or partial data.
 * </ul>
 *
 * <p>This class is thread-safe only if the caller ensures exclusive access to the provided {@link
 * FileChannel} and {@link ByteBuffer} instances.
 *
 * <p><b>Usage example:</b>
 *
 * <pre>{@code
 * try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
 *     ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
 *     NioChannels.writeFully(channel, buffer, data, true);
 * }
 * }</pre>
 *
 * <p>Author: Haseem Kheiri
 */
public final class NioChannels {

  private NioChannels() {
    // Utility class; prevent instantiation
  }

  /**
   * Writes the full contents of a byte array to a {@link FileChannel} in chunks.
   *
   * <p>Chunks the array according to the buffer capacity, reusing the buffer to reduce allocations.
   * Optionally flushes data to disk without forcing metadata if {@code force} is true.
   *
   * @param file the target file channel; must not be null
   * @param buffer the buffer used as a staging area; must have capacity &gt; 0
   * @param data the byte array to write; must not be null
   * @param force if true, flushes the data to disk
   * @throws IOException if an IO error occurs
   * @throws NullPointerException if file, buffer, or data is null
   * @throws IllegalArgumentException if buffer capacity &lt; 1
   */
  public static void writeFully(FileChannel file, ByteBuffer buffer, byte[] data, boolean force)
      throws IOException {
    if (file == null || buffer == null || data == null) {
      throw new NullPointerException("file, buffer, and data must not be null");
    }
    if (buffer.capacity() < 1) {
      throw new IllegalArgumentException("buffer capacity must be >= 1");
    }

    int position = 0;
    final int size = data.length;
    final int capacity = buffer.capacity();

    while (position < size) {
      final int length = Math.min(size - position, capacity);
      buffer.clear();
      buffer.put(data, position, length);
      buffer.flip();
      file.write(buffer);
      position += length;
    }

    if (force) {
      file.force(false);
    }
  }

  /** Overload without force parameter (defaults to false). */
  public static void writeFully(FileChannel file, ByteBuffer buffer, byte[] data)
      throws IOException {
    writeFully(file, buffer, data, false);
  }

  /**
   * Writes a byte array to a specific position in a file channel.
   *
   * <p>Positions the channel at {@code position} and writes the array using {@link #writeFully}.
   *
   * @param channel the file channel to write to; must not be null
   * @param buffer the buffer used for chunked writes; must have capacity &gt; 0
   * @param position the position in the file to start writing
   * @param data the byte array to write; must not be null
   * @param force if true, flushes data to disk
   * @throws IOException if an IO error occurs
   */
  public static void writeToLocation(
      FileChannel channel, ByteBuffer buffer, long position, byte[] data, boolean force)
      throws IOException {
    if (channel == null) {
      throw new NullPointerException("channel must not be null");
    }
    channel.position(position);
    writeFully(channel, buffer, data, force);
  }

  /** Overload without force parameter (defaults to false). */
  public static void writeToLocation(
      FileChannel channel, ByteBuffer buffer, long position, byte[] data) throws IOException {
    writeToLocation(channel, buffer, position, data, false);
  }

  /**
   * Writes a 8-byte long to a file channel using a reusable buffer.
   *
   * @param buffer the staging buffer
   * @param channel the target file channel
   * @param value the long value to write
   * @throws IOException if writing fails
   */
  public static void writeLong(ByteBuffer buffer, FileChannel channel, long value)
      throws IOException {
    buffer.clear();
    buffer.putLong(value);
    buffer.flip();
    while (buffer.hasRemaining()) {
      channel.write(buffer);
    }
  }

  /**
   * Writes a 4-byte integer to a file channel using a reusable buffer.
   *
   * @param buffer the staging buffer
   * @param channel the target file channel
   * @param value the integer value to write
   * @throws IOException if writing fails
   */
  public static void writeInt(ByteBuffer buffer, FileChannel channel, int value)
      throws IOException {
    buffer.clear();
    buffer.putInt(value);
    buffer.flip();
    while (buffer.hasRemaining()) {
      channel.write(buffer);
    }
  }

  /**
   * Writes a single byte to a file channel using a reusable buffer.
   *
   * @param buffer the staging buffer
   * @param channel the target file channel
   * @param value the byte to write
   * @throws IOException if writing fails
   */
  public static void writeByte(ByteBuffer buffer, FileChannel channel, byte value)
      throws IOException {
    buffer.clear();
    buffer.put(value);
    buffer.flip();
    while (buffer.hasRemaining()) {
      channel.write(buffer);
    }
  }

  /**
   * Reads exactly {@code n} bytes from a file channel into a new byte array.
   *
   * <p>Chunks reads using the provided buffer, ensuring memory efficiency and preventing premature
   * EOF errors.
   *
   * @param file the file channel to read from; must not be null
   * @param buffer the buffer used as a staging area; must have capacity &gt; 0
   * @param n the number of bytes to read; must be &gt;= 0
   * @return a new byte array containing exactly {@code n} bytes
   * @throws IOException if reading fails or EOF is reached early
   * @throws NullPointerException if file or buffer is null
   * @throws IllegalArgumentException if n &lt; 0 or buffer capacity &lt; 1
   */
  public static byte[] readBytes(FileChannel file, ByteBuffer buffer, int n) throws IOException {
    if (file == null || buffer == null) {
      throw new NullPointerException("file and buffer must not be null");
    }
    if (n < 0) {
      throw new IllegalArgumentException("n must be >= 0");
    }
    if (buffer.capacity() < 1) {
      throw new IllegalArgumentException("buffer capacity must be >= 1");
    }

    byte[] bytes = new byte[n];
    int offset = 0;

    while (offset < n) {
      if (!buffer.hasRemaining()) {
        buffer.clear();
        int read = file.read(buffer);
        if (read == -1) {
          throw new IOException("Unexpected EOF while reading " + n + " bytes");
        }
        buffer.flip();
      }

      int toCopy = Math.min(buffer.remaining(), n - offset);
      buffer.get(bytes, offset, toCopy);
      offset += toCopy;
    }

    return bytes;
  }

  /** Reads a single byte from a file channel. */
  public static byte readByte(FileChannel file, ByteBuffer source, final ByteBuffer temp)
      throws IOException {
    temp.clear();
    temp.put(readBytes(file, source, 1));
    temp.flip();
    return temp.get();
  }

  /** Reads a 4-byte integer from a file channel. */
  public static int readInt(FileChannel file, ByteBuffer source, ByteBuffer temp)
      throws IOException {
    temp.clear();
    temp.put(readBytes(file, source, 4));
    temp.flip();
    return temp.getInt();
  }

  /** Reads an 8-byte long from a file channel. */
  public static long readLong(FileChannel file, ByteBuffer source, ByteBuffer temp)
      throws IOException {
    temp.clear();
    temp.put(readBytes(file, source, 8));
    temp.flip();
    return temp.getLong();
  }
}
