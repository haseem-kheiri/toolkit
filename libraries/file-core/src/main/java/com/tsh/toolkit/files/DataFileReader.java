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

import java.io.Closeable;
import java.io.IOException;

/**
 * A reader for deserializing objects from a data file written with {@link DataFileWriter}.
 *
 * <p>The reader supports both sequential and random access to objects stored in the file. Each
 * object must have been written using a {@link Serializer} implementation, and can be retrieved by
 * its file position offset.
 *
 * <p>Typical usage:
 *
 * <pre>{@code
 * try (DataFileReader<MyType> reader = new DataFileReader<>(fileChannel, serializer)) {
 *     MyType obj1 = reader.readNext();
 *     MyType obj2 = reader.readAt(offset);
 * }
 * }</pre>
 *
 * @param <T> the type of objects being deserialized
 */
public interface DataFileReader<T> extends Closeable {

  /**
   * Reads the next object from the current file position, advancing the cursor.
   *
   * @return the next deserialized object, or {@code null} if end of file is reached
   * @throws IOException if an I/O error occurs during reading
   */
  T readNext() throws IOException;

  /**
   * Reads an object from the specified file position without altering the current cursor.
   *
   * @param position the byte offset in the file where the object begins
   * @return the deserialized object
   * @throws IOException if an I/O error occurs or the position is invalid
   */
  T readAt(long position) throws IOException;

  /**
   * Returns the current position of the reader's cursor in the file.
   *
   * @return the current file position
   * @throws IOException if an I/O error occurs
   */
  long position() throws IOException;

  /**
   * Moves the cursor to the specified position in the file.
   *
   * @param position the new file position
   * @throws IOException if an I/O error occurs or the position is invalid
   */
  void seek(long position) throws IOException;
}
