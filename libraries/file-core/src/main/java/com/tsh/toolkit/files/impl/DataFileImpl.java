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

import com.tsh.toolkit.files.NioChannels;
import com.tsh.toolkit.files.NioFiles;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.CRC32C;
import lombok.extern.slf4j.Slf4j;

/** DataFileImpl. */
@Slf4j
public class DataFileImpl<T> {
  protected static final byte LOG_IN_PROGRESS = Byte.MIN_VALUE;
  protected static final byte LOG_COMMITTED = Byte.MAX_VALUE;
  protected static final int BUFFER_SIZE = 32 * 1024;

  private final CRC32C crc = new CRC32C();

  protected FileChannel openLogFile(Path homePath) throws IOException {
    NioFiles.createDirectory(homePath);
    return FileChannel.open(
        homePath.resolve("wal.log"),
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.READ);
  }

  protected FileChannel openDataFile(Path homePath, OpenOption... options) throws IOException {
    NioFiles.createDirectory(homePath);
    return FileChannel.open(homePath.resolve("data.ds"), options);
  }

  protected int verify(int batchSize) {
    if (batchSize < 100 || batchSize >= 500) {
      throw new IllegalArgumentException(
          "Batch size should be greater than or equal to 100 and less than 500.");
    }
    return batchSize;
  }

  /**
   * Replays a committed batch from the log file into the main file, then truncates the log.
   *
   * @param logFileChannel the log file channel
   * @param buffer scratch buffer
   * @throws IOException if replay fails or checksum is invalid
   */
  protected void commitLog(FileChannel logFileChannel, FileChannel fileChannel, ByteBuffer buffer)
      throws IOException {
    logFileChannel.position(0);

    buffer.clear();
    logFileChannel.read(buffer);
    buffer.flip();

    final ByteBuffer temp = ByteBuffer.allocate(BUFFER_SIZE);

    if (NioChannels.readByte(logFileChannel, buffer, temp) == LOG_COMMITTED) {
      long location = NioChannels.readLong(logFileChannel, buffer, temp);
      int rowCount = NioChannels.readInt(logFileChannel, buffer, temp);

      fileChannel.position(location);
      for (int i = 0; i < rowCount; i++) {
        writeRow(fileChannel, temp, readRow(logFileChannel, buffer, temp));
      }
      fileChannel.force(false);
    }
    logFileChannel.truncate(0);
    logFileChannel.force(true);
  }

  /**
   * Writes a row to the file: [length][data][checksum].
   *
   * <p>If the row is larger than the buffer, it is written in chunks.
   *
   * @param fileChannel target channel
   * @param buffer reusable buffer
   * @param row serialized row
   * @throws IOException if writing fails
   */
  protected void writeRow(FileChannel fileChannel, ByteBuffer buffer, byte[] row)
      throws IOException {
    NioChannels.writeInt(buffer, fileChannel, row.length);

    int offset = 0;
    while (offset < row.length) {
      buffer.clear();
      int chunkSize = Math.min(buffer.capacity(), row.length - offset);
      buffer.put(row, offset, chunkSize);
      buffer.flip();
      while (buffer.hasRemaining()) {
        fileChannel.write(buffer);
      }
      offset += chunkSize;
    }

    NioChannels.writeLong(buffer, fileChannel, computeChecksum(row));
  }

  /**
   * Reads a row ([length][data][checksum]) from a file channel.
   *
   * @param file the file channel
   * @param source buffer holding prefetched data
   * @param temp scratch buffer
   * @return the raw row data (excluding metadata)
   * @throws IOException if EOF occurs prematurely or checksum fails
   */
  protected byte[] readRow(FileChannel file, ByteBuffer source, ByteBuffer temp)
      throws IOException {
    int rowLength = NioChannels.readInt(file, source, temp);
    byte[] data = NioChannels.readBytes(file, source, rowLength);
    if (computeChecksum(data) != NioChannels.readLong(file, source, temp)) {
      throw new IOException("Checksum mismatch! Data corrupted.");
    }
    return data;
  }

  /**
   * Computes a CRC32 checksum of the given data.
   *
   * @param data data to checksum
   * @return checksum value
   */
  protected long computeChecksum(byte[] data) {
    crc.reset();
    crc.update(data);
    return crc.getValue();
  }

  /**
   * Closes the given {@link FileChannel}, logging any {@link IOException} instead of throwing it.
   *
   * @param channel the channel to close, ignored if {@code null}
   */
  protected void closeQuietly(FileChannel channel) {
    if (channel != null) {
      try {
        channel.close();
      } catch (IOException e) {
        log.warn("Error closing file: ", e);
      }
    }
  }
}
