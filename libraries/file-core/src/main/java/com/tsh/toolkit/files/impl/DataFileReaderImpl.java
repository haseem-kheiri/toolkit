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
import com.tsh.toolkit.codec.ObjectType;
import com.tsh.toolkit.files.DataFileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A robust file reader that sequentially or randomly reads serialized objects from a structured
 * data file, automatically committing any pending log batches before access.
 *
 * <p>This reader complements {@link com.platform.files.DataFileWriterImpl} by ensuring that
 * previously committed but unreplayed log batches are integrated into the main data file prior to
 * deserialization.
 *
 * <p><strong>Features:</strong>
 *
 * <ul>
 *   <li>Automatically commits any uncommitted log batches before reading.
 *   <li>Supports both sequential reads via {@link #readNext()} and random-access reads via {@link
 *       #readAt(long)}.
 *   <li>Allows seeking to any valid byte offset within the file using {@link #seek(long)}.
 *   <li>Validates row integrity using CRC32 checksums through {@link
 *       DataFileImpl#readRow(FileChannel, ByteBuffer, ByteBuffer)}.
 *   <li>Implements {@link AutoCloseable} for reliable resource cleanup.
 * </ul>
 *
 * <p><strong>Usage notes:</strong>
 *
 * <ul>
 *   <li>This class is not thread-safe; each thread should use its own instance.
 *   <li>End-of-file is indicated by returning {@code null} from {@link #readNext()} or {@link
 *       #readAt(long)}.
 *   <li>Although the superclass validates batch size for consistency, it is not directly used in
 *       this reader implementation.
 * </ul>
 *
 * @param <T> the type of object being deserialized from the data file
 */
public class DataFileReaderImpl<T> extends DataFileImpl<T> implements DataFileReader<T> {

  private final ObjectType<T> type;
  private final Codec serializer;
  private final FileChannel logFileChannel;
  private final FileChannel fileChannel;
  private final ByteBuffer buffer;
  private final ByteBuffer temp;
  private long position = 0;

  /**
   * Constructs a new {@code DataFileReaderImpl}.
   *
   * <p>Opens the main data file in read-only mode and its companion log file in read/write mode to
   * permit committing any pending batches before reading.
   *
   * @param type the class type of the objects to read from the file
   * @param homePath the base path of the data file (a companion {@code .log} file is assumed)
   * @param serializer the {@link Codec} responsible for decoding byte arrays into objects
   * @throws IOException if either the log or data file channels cannot be opened
   */
  public DataFileReaderImpl(Class<T> type, Path homePath, Codec serializer) throws IOException {
    this.type =
        new ObjectType<>() {
          @Override
          public Type getType() {
            return type;
          }
        };
    this.serializer = serializer;
    this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
    this.temp = ByteBuffer.allocate(BUFFER_SIZE);

    // Open log file in read/write mode to allow committing pending batches
    this.logFileChannel = openLogFile(homePath);

    // Open main data file in read-only mode
    this.fileChannel = openDataFile(homePath, StandardOpenOption.READ);
    seek(0);
  }

  /**
   * Reads the next object in sequence from the current file position.
   *
   * <p>If a log batch is pending, it is first committed to the main data file before reading.
   * Returns {@code null} when end-of-file (EOF) is reached.
   *
   * @return the next deserialized object, or {@code null} if EOF is reached
   * @throws IOException if a read or commit operation fails
   */
  @Override
  public T readNext() throws IOException {
    return readAt(position());
  }

  /**
   * Reads an object located at the specified byte offset within the file.
   *
   * <p>This method automatically commits any uncommitted log batches before reading. The provided
   * {@code position} corresponds to a raw byte offset in the file, accounting for metadata fields
   * such as record length and checksum. The reader’s internal position is advanced past the
   * deserialized record after reading.
   *
   * @param position the byte offset in the file from which to read
   * @return the deserialized object at that position, or {@code null} if position is at or beyond
   *     EOF
   * @throws IOException if an I/O error occurs or the file is corrupt
   */
  @Override
  public T readAt(long position) throws IOException {
    if (logFileChannel.size() > 0) {
      commitLog(logFileChannel, fileChannel, buffer);
    }

    if (position < fileChannel.size()) {
      byte[] row = readRow(fileChannel, buffer, temp);
      this.position += 4 + row.length + 8; // [int length][data][long checksum]
      return serializer.decode(row, type);
    }

    return null;
  }

  /**
   * Returns the current read offset in the data file.
   *
   * <p>The offset represents the next byte that will be read by {@link #readNext()}.
   *
   * @return the current byte position in the data file
   * @throws IOException if the underlying file channel fails
   */
  @Override
  public long position() throws IOException {
    return position;
  }

  /**
   * Moves the read cursor to the specified byte position within the file.
   *
   * <p>Subsequent calls to {@link #readNext()} or {@link #readAt(long)} will begin reading from
   * this new position. If the position exceeds the file size, an exception is thrown.
   *
   * @param position the byte offset to move the read cursor to
   * @throws IOException if {@code position} is greater than the file size
   */
  @Override
  public void seek(long position) throws IOException {
    if (position < fileChannel.size()) {
      this.position = position;
      fileChannel.position(position);
      buffer.clear();
      buffer.flip();
    } else {
      throw new IOException(
          "Seek position " + position + " exceeds file size " + fileChannel.size());
    }
  }

  /**
   * Closes the main data file and its companion log file, suppressing any {@link IOException}.
   *
   * <p>Once closed, this reader cannot be reused.
   *
   * @throws IOException if closing either file channel fails
   */
  @Override
  public void close() throws IOException {
    closeQuietly(fileChannel);
    closeQuietly(logFileChannel);
  }
}
