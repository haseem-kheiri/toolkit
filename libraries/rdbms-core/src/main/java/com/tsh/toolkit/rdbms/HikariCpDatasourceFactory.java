/*
 * Copyright (c) 2023-2025 Haseem Kheiri, Tahama Bin Haseem, and Shees Bin Haseem
 *
 * Licensed under the Apache License, Version 2.0.
 * See LICENSE in the project root for the full license text.
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
