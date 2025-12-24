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

import com.tsh.toolkit.core.utils.Run;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Hikari connection pool data source factory.
 *
 * @author Haseem Kheiri
 */
public class HikariCpDatasourceFactory {

  private String jdbcUrl;
  private String username;
  private String password;
  private int maximumPoolSize = 1;

  /** JDBC URL setter. */
  public HikariCpDatasourceFactory setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
    return this;
  }

  /** User name setter. */
  public HikariCpDatasourceFactory setUsername(String username) {
    this.username = username;
    return this;
  }

  /** Password setter. */
  public HikariCpDatasourceFactory setPassword(String password) {

    this.password = password;
    return this;
  }

  /** Maximum pool size setter. */
  public HikariCpDatasourceFactory setMaximumPoolSize(int maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
    return this;
  }

  /** Build the data source. */
  public HikariDataSource build() {
    final HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcUrl);
    Run.runIfNotNull(username, un -> config.setUsername(un));
    Run.runIfNotNull(password, pw -> config.setPassword(pw));
    config.setMaximumPoolSize(maximumPoolSize);
    return new HikariDataSource(config);
  }
}
