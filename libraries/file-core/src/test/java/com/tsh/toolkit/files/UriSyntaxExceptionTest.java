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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class UriSyntaxExceptionTest {

  @Test
  void defaultConstructor_hasNoMessageOrCause() {
    UriSyntaxException ex = new UriSyntaxException();
    assertNull(ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  void messageConstructor_setsMessage() {
    UriSyntaxException ex = new UriSyntaxException("Invalid URI");
    assertEquals("Invalid URI", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  void causeConstructor_setsCause() {
    Throwable cause = new IllegalArgumentException("Bad URI");
    UriSyntaxException ex = new UriSyntaxException(cause);
    assertEquals(cause, ex.getCause());
  }

  @Test
  void messageAndCauseConstructor_setsBoth() {
    Throwable cause = new IllegalArgumentException("Bad URI");
    UriSyntaxException ex = new UriSyntaxException("Invalid URI", cause);
    assertEquals("Invalid URI", ex.getMessage());
    assertEquals(cause, ex.getCause());
  }

  @Test
  void fullConstructor_respectsSuppressionAndWritableStackTrace() {
    Throwable cause = new IllegalArgumentException("Bad URI");
    UriSyntaxException ex = new UriSyntaxException("Invalid URI", cause, false, false);

    assertEquals("Invalid URI", ex.getMessage());
    assertEquals(cause, ex.getCause());
  }
}
