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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CodecExceptionTest {

  @Test
  void testConstructorWithCause() {
    IOException ioException = new IOException("disk error");
    CodecException exception = new CodecException(ioException);

    // Verify cause and message propagation
    Assertions.assertEquals(ioException, exception.getCause());
    Assertions.assertTrue(
        exception.getMessage().contains("disk error"),
        "Exception message should contain cause message");
  }

  @Test
  void testConstructorWithMessageAndCause() {
    IOException ioException = new IOException("network failure");
    CodecException exception = new CodecException("custom message", ioException);

    Assertions.assertEquals("custom message", exception.getMessage());
    Assertions.assertEquals(ioException, exception.getCause());
  }
}
