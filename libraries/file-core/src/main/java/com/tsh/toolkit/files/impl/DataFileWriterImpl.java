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

import com.tsh.toolkit.codec.Codec;
import com.tsh.toolkit.files.DataFileWriter;
import com.tsh.toolkit.files.LockedFile;
import com.tsh.toolkit.files.NioChannels;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * A file writer that appends serialized objects to a data file safely, efficiently, and in
 * batch-atomic fashion.
 *
 * <p>Each record is written in the following binary format:
 *
 * <pre>
 *   [length: int][data: byte[length]][checksum: long]
 * </pre>
 *
 * <p>Atomicity and durability are enforced through a companion log file:
 *
 * <ul>
 *   <li>Rows are first staged to the log with an <em>in-progress</em> marker.
 *   <li>Once the full batch is written, the marker is flipped to <em>committed</em>.
 *   <li>On recovery, any committed log batch is replayed into the main data file.
 *   <li>This guarantees that either all rows of a batch are written, or none are.
 * </ul>
 *
 * <p><strong>Key features:</strong>
 *
 * <ul>
 *   <li>Uses a reusable {@link ByteBuffer} to minimize heap allocations during I/O.
 *   <li>Validates each record using CRC32 checksums for integrity verification.
 *   <li>Groups writes into batches under an exclusive {@link LockedFile} to improve throughput.
 *   <li>Automatically replays committed log batches after a crash or incomplete write.
 *   <li>Implements {@link Closeable} for safe resource cleanup.
 * </ul>
 *
 * <p><strong>Behavioral notes:</strong>
 *
 * <ul>
 *   <li>The {@code batchSize} must be between 100 (inclusive) and 500 (exclusive); otherwise an
 *       {@link IllegalArgumentException} is thrown.
 *   <li>{@link #append(List)} silently skips {@code null} elements.
 *   <li>Thread-safety is ensured via {@code synchronized} blocks for intra-process concurrency,
 *       while {@link LockedFile} enforces inter-process exclusivity.
 * </ul>
 *
 * @param <T> the type of object being serialized and written
 */
@Slf4j
public class DataFileWriterImpl<T> extends DataFileImpl<T> implements DataFileWriter<T> {
  private final Codec serializer;
  private final int batchSize;
  private final FileChannel logFileChannel;
  private final FileChannel fileChannel;
  private final ByteBuffer buffer;

  /**
   * Creates a new {@code DataFileWriterImpl} instance.
   *
   * @param homePath the base path of the main data file; a companion {@code .log} file is created
   *     automatically
   * @param serializer the {@link Codec} used to serialize objects to bytes
   * @param batchSize the maximum number of rows per batch; must be between 100 and 499
   * @throws IOException if the data or log file channels cannot be opened
   * @throws IllegalArgumentException if {@code batchSize} is outside the valid range
   */
  public DataFileWriterImpl(Path homePath, Codec serializer, int batchSize) throws IOException {
    this.serializer = serializer;
    this.batchSize = verify(batchSize);
    this.buffer = ByteBuffer.allocate(BUFFER_SIZE);

    this.logFileChannel = openLogFile(homePath);
    this.logFileChannel.force(true);

    this.fileChannel = openDataFile(homePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    this.fileChannel.force(true);
    this.fileChannel.position(this.fileChannel.size());
  }

  /** Closes both the main data file and the companion log file, suppressing any I/O exceptions. */
  @Override
  public void close() throws IOException {
    closeQuietly(fileChannel);
    closeQuietly(logFileChannel);
  }

  /**
   * Appends a list of objects to the data file, writing them in batches.
   *
   * <p>Each non-null object is serialized using the configured {@link Codec}. Once the number of
   * accumulated rows reaches {@code batchSize}, the batch is atomically written to disk under an
   * exclusive lock.
   *
   * @param list the list of objects to append; {@code null} or empty lists are ignored
   * @throws IOException if a write or file lock operation fails
   */
  @Override
  public synchronized void append(List<T> list) throws IOException {
    if (list == null || list.isEmpty()) {
      return;
    }

    List<byte[]> batch = new ArrayList<>(batchSize);

    for (T t : list) {
      if (t != null) {
        byte[] row = serializer.encode(t);
        batch.add(row);

        if (batch.size() >= batchSize) {
          writeBatch(batch);
          batch.clear();
        }
      }
    }

    if (!batch.isEmpty()) {
      writeBatch(batch);
      batch.clear();
    }
  }

  /**
   * Writes a serialized batch of rows to the main file under an exclusive file lock.
   *
   * <p>Process overview:
   *
   * <ol>
   *   <li>If the log contains a previously committed batch, it is first replayed into the main
   *       file.
   *   <li>The new batch is staged in the log file with an <em>in-progress</em> marker.
   *   <li>The marker is flipped to <em>committed</em> once all rows are written.
   *   <li>The log is replayed into the main file, then truncated.
   * </ol>
   *
   * @param rows the serialized rows to write
   * @throws IOException if writing to the log or replaying it fails
   */
  private void writeBatch(List<byte[]> rows) throws IOException {
    LockedFile.withLockedFile(
        logFileChannel,
        () -> {
          if (logFileChannel.size() > 0) {
            commitLog(logFileChannel, fileChannel, buffer);
          }

          long location = fileChannel.position();
          writeLog(logFileChannel, buffer, rows, location);

          try {
            commitLog(logFileChannel, fileChannel, buffer);
          } catch (Exception e) {
            log.warn("Error committing log file; preserving for later recovery.", e);
          }

          return null;
        });
  }

  /**
   * Writes a batch of rows to the log file with an initial <em>in-progress</em> marker, then flips
   * it to <em>committed</em> once the batch is fully staged.
   *
   * @param logFileChannel the file channel for the companion log
   * @param buffer a reusable {@link ByteBuffer} for temporary storage
   * @param rows the serialized rows to write
   * @param location the byte offset in the main file where the batch should be applied
   * @throws IOException if an I/O operation fails
   */
  private void writeLog(
      FileChannel logFileChannel, ByteBuffer buffer, List<byte[]> rows, long location)
      throws IOException {
    buffer.clear();
    NioChannels.writeByte(buffer, logFileChannel, LOG_IN_PROGRESS);
    NioChannels.writeLong(buffer, logFileChannel, location);
    NioChannels.writeInt(buffer, logFileChannel, rows.size());

    for (byte[] row : rows) {
      writeRow(logFileChannel, buffer, row);
    }

    // Flip first byte to "committed"
    logFileChannel.position(0);
    NioChannels.writeByte(buffer, logFileChannel, LOG_COMMITTED);
    logFileChannel.force(false);
  }
}
