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

package com.tsh.toolkit.codec;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Exception indicating a failure during encoding or decoding operations.
 *
 * <p>This unchecked I/O exception is thrown by {@link Codec} implementations when a serialization
 * or deserialization operation fails due to an underlying {@link IOException} or data format
 * problem.
 *
 * <p>By extending {@link UncheckedIOException}, this class allows codec operations to propagate
 * I/O-related failures without requiring callers to handle checked exceptions explicitly.
 *
 * <p>Example:
 *
 * <pre>{@code
 * try {
 *     byte[] data = codec.encode(object);
 * } catch (CodecException e) {
 *     log.error("Codec error: {}", e.getMessage(), e);
 * }
 * }</pre>
 */
@SuppressWarnings("serial")
public class CodecException extends UncheckedIOException {

  /** Constructs a new {@code CodecException} with the specified {@link IOException} cause. */
  public CodecException(IOException cause) {
    super(cause);
  }

  /**
   * Constructs a new {@code CodecException} with the specified detail message and {@link
   * IOException} cause.
   *
   * @param message the detail message describing the cause of the failure
   * @param cause the underlying I/O exception that triggered this error
   */
  public CodecException(String message, IOException cause) {
    super(message, cause);
  }
}
