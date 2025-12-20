/*
 * Copyright (c) 2023-2025 Haseem Kheiri, Tahama Bin Haseem, and Shees Bin Haseem
 *
 * Licensed under the Apache License, Version 2.0.
 * See LICENSE in the project root for the full license text.
 */

package com.tsh.toolkit.rdbms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

/**
 * An abstract RDBMS repository.
 *
 * @author Haseem Kheiri
 */
public class AbstractRdbmsRepository implements RdbmsRepository {

  /** RDBMS SQL parameter injector. */
  @FunctionalInterface
  public interface RdbmsPreparedStatementParamInjector {
    /** Injects parameters in a prepared statement. */
    void inject(PreparedStatement ps) throws SQLException;
  }

  /** RDBMS SQL result set processor. */
  public interface RdbmsResultListprocessor<T> {
    /** Process SQL results. */
    List<T> processResults(ResultSet rs) throws SQLException;
  }

  /** SQL statement supplier. */
  @FunctionalInterface
  public interface RdbmsSqlStatementSupplier<T> {
    /** Supplies a PreparedStatement. */
    PreparedStatement get(List<T> subList) throws SQLException;
  }

  /** RDBMS consumer to add parameters to a PreparedStatment. */
  @FunctionalInterface
  public interface RdbmsInLiteralListConsumer<P> {
    /** Add parameters to a PreparedStatement. */
    void addToInClause(PreparedStatement ps, List<P> params) throws SQLException;
  }

  /** RDBMS function to add parameters to a PreparedStatment. */
  @FunctionalInterface
  public interface RdbmsParamFunction<P> {
    /**
     * Add parameters to a PreparedStatement.
     *
     * @param ps the PreparedStatement
     * @param param to add
     * @return true is parameter is added or else false.
     */
    boolean addToBatch(PreparedStatement ps, P param) throws SQLException;
  }

  /** RDBMS consumer. */
  public interface RdbmsConsumer {
    /** Accepts the connection and run code block on it. */
    void apply(Connection con) throws SQLException;
  }

  /** RDBMS function. */
  @FunctionalInterface
  public interface RdbmsFunction<R> {
    /** Applies the code block on the RDBMS connection provided. */
    R apply(Connection con) throws SQLException;
  }

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
    Flyway.configure().dataSource(dataSource).schemas(schema).load().migrate();
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
