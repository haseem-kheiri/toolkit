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

package com.tsh.toolkit.container.postgres;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Factory to build PostgreSql container.
 *
 * @author Haseem Kheiri
 */
public class PostgreSqlContainerFactory {
  private String dockeImageName;
  private String databaseName;
  private String userName;
  private String password;

  /** sets PostgreSql dockerImageName. */
  public PostgreSqlContainerFactory setDockeImageName(String dockeImageName) {
    this.dockeImageName = dockeImageName;
    return this;
  }

  /** sets PostgreSql databaseName. */
  public PostgreSqlContainerFactory setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
    return this;
  }

  /** sets PostgreSql userName. */
  public PostgreSqlContainerFactory setUserName(String userName) {
    this.userName = userName;
    return this;
  }

  /** sets PostgreSql password. */
  public PostgreSqlContainerFactory setPassword(String password) {
    this.password = password;
    return this;
  }

  /** Builds a PostgreSql container. */
  @SuppressWarnings("resource")
  public PostgreSQLContainer<?> build() {
    return new PostgreSQLContainer<>(dockeImageName)
        .withDatabaseName(databaseName)
        .withUsername(userName)
        .withPassword(password);
  }
}
