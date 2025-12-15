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
import java.util.List;

/**
 * A writer for appending serialized objects to a data file.
 *
 * <p>Implementations of this interface provide batched, append-only writes to a file, ensuring
 * durability and consistency of persisted data. Each object is serialized using a {@link
 * Serializer} supplied at construction time.
 *
 * <p><strong>Lifecycle:</strong> A {@code DataFileWriter} must be closed after use to ensure that
 * all buffered data is flushed and underlying resources are released.
 *
 * @param <T> the type of objects being serialized and written
 */
public interface DataFileWriter<T> extends Closeable {

  /**
   * Appends a list of objects to the data file.
   *
   * <p>Objects are serialized, grouped into a single batch, and written under an exclusive file
   * lock to guarantee that either all objects in the batch are persisted, or none are. This
   * provides atomicity at the batch level, not per-object granularity.
   *
   * <p>{@code null} elements in the list are ignored and will not be written. If the list itself is
   * {@code null} or empty, this method performs no operation.
   *
   * @param list the objects to append; may be {@code null} or empty
   * @throws IOException if an I/O error occurs while writing
   */
  void append(List<T> list) throws IOException;
}
