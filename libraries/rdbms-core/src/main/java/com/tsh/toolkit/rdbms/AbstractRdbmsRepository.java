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

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

/**
 * An abstract RDBMS repository.
 *
 * @author Haseem Kheiri
 */
public class AbstractRdbmsRepository implements RdbmsRepository {

  private final DataSource dataSource;

  /** Constructs a RDBMS repository. */
  protected AbstractRdbmsRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Starts the database migration. All pending migrations will be applied in order. Calling migrate
   * on an up-to-date database has no effect.
   */
  public void migrate(String schema) {
    Flyway.configure()
        .dataSource(dataSource)
        .schemas(schema)
        .locations("classpath:db/migration/" + schema)
        .load()
        .migrate();
  }

  /** Executes the RDBMS code block and returns the result. */
  public <R> R executeAndReturn(RdbmsFunction<R> fn, boolean autocommit) {
    return executeAndReturn(dataSource, fn, autocommit);
  }

  /** Executes the RDBMS code block and returns the result. Auto commit is set to off. */
  public <R> R executeAndReturn(RdbmsFunction<R> fn) {
    return executeAndReturn(fn, false);
  }

  /** Executes the RDBMS code block and returns the result. */
  public void execute(RdbmsConsumer consumer, boolean autocommit) {
    executeAndReturn(
        (connection) -> {
          consumer.apply(connection);
          return null;
        },
        autocommit);
  }

  /** Executes the RDBMS code block and returns the result. Auto commit is set to off. */
  public void execute(RdbmsConsumer consumer) {
    execute(consumer, false);
  }
}
