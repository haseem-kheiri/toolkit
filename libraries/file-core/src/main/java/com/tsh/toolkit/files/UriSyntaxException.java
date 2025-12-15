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

/**
 * Unchecked exception thrown when a string cannot be parsed into a valid {@link java.net.URI}.
 *
 * <p>This exception wraps {@link java.net.URISyntaxException} and is used to simplify internal code
 * paths where checked exceptions would otherwise complicate control flow. It indicates an invalid
 * or malformed URI syntax encountered during URI-to-path conversion.
 */
@SuppressWarnings("serial")
public class UriSyntaxException extends RuntimeException {

  public UriSyntaxException() {
    super();
  }

  public UriSyntaxException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public UriSyntaxException(String message, Throwable cause) {
    super(message, cause);
  }

  public UriSyntaxException(String message) {
    super(message);
  }

  public UriSyntaxException(Throwable cause) {
    super(cause);
  }
}
