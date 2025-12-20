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

package com.tsh.toolkit.rdbms;

import java.sql.SQLException;

/**
 * Runtime exception used to wrap {@link java.sql.SQLException} instances originating from RDBMS
 * repository operations.
 *
 * <p>This exception provides a consistent, unchecked abstraction for database access failures,
 * allowing higher layers to avoid direct coupling to {@code SQLException} while still preserving
 * the original cause for diagnostics and logging.
 */
@SuppressWarnings("serial")
public class RdbmsRepositoryException extends RuntimeException {

  /**
   * Creates a new {@code RdbmsRepositoryException} wrapping the given {@link
   * java.sql.SQLException}.
   *
   * @param e the underlying SQL exception that caused the failure; must not be {@code null}
   */
  public RdbmsRepositoryException(SQLException e) {
    super(e);
  }
}
