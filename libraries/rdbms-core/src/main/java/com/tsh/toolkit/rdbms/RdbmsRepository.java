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

import com.tsh.toolkit.rdbms.AbstractRdbmsRepository.RdbmsFunction;
import com.tsh.toolkit.rdbms.AbstractRdbmsRepository.RdbmsInLiteralListConsumer;
import com.tsh.toolkit.rdbms.AbstractRdbmsRepository.RdbmsParamFunction;
import com.tsh.toolkit.rdbms.AbstractRdbmsRepository.RdbmsPreparedStatementParamInjector;
import com.tsh.toolkit.rdbms.AbstractRdbmsRepository.RdbmsResultListprocessor;
import com.tsh.toolkit.rdbms.AbstractRdbmsRepository.RdbmsSqlStatementSupplier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import javax.sql.DataSource;

/**
 * A RDBMS repository.
 *
 * @author Haseem Kheiri
 */
public interface RdbmsRepository {
  int PRE_QUERY_REJECTED = -256;

  /** Executes the RDBMS code block and returns the result. */
  default <R> R executeAndReturn(DataSource dataSource, RdbmsFunction<R> fn, boolean autocommit) {
    try (Connection connection = dataSource.getConnection()) {
      return executeAndReturn(connection, fn, autocommit);
    } catch (SQLException e) {
      throw new RdbmsRepositoryException(e);
    }
  }

  /** Executes the RDBMS code block and returns the result. */
  default <R> R executeAndReturn(Connection connection, RdbmsFunction<R> fn, boolean autocommit) {
    try {
      try {
        connection.setAutoCommit(autocommit);
        R apply = fn.apply(connection);
        if (!autocommit) {
          connection.commit();
        }
        return apply;
      } catch (Exception e) {
        if (!autocommit) {
          connection.rollback();
        }
        throw e;
      }
    } catch (SQLException e) {
      throw new RdbmsRepositoryException(e);
    }
  }

  /**
   * Helps execute DML batched statements.
   *
   * @param ps the prepared statement
   * @param batchSize of each batch execute
   * @param fn batching function to add parameters to a batch
   * @return an array of update counts containing one element for each command in the batch. The
   *     elements of the array are ordered according to the order in which commands were added to
   *     the batch.
   * @throws SQLException on SQL error
   */
  default <P> List<Integer> executeBatch(
      PreparedStatement ps, int batchSize, List<P> params, RdbmsParamFunction<P> fn)
      throws SQLException {
    List<Integer> results = new ArrayList<>();
    if (params != null) {
      int count = 0;
      int index = 0;
      for (P param : params) {
        if (fn.addToBatch(ps, param)) {
          results.add(0);
          ps.addBatch();
          count++;
        } else {
          results.add(PRE_QUERY_REJECTED);
        }

        if (count % batchSize == 0) {
          count = 0;
          int[] updateCounts = ps.executeBatch();
          for (Integer updateCount : updateCounts) {
            while (results.get(index) == PRE_QUERY_REJECTED) {
              index++;
            }
            results.set(index, updateCount);
            index++;
          }
          ps.clearBatch();
        }
      }

      if (count % batchSize != 0) {
        count = 0;
        int[] updateCounts = ps.executeBatch();
        for (Integer updateCount : updateCounts) {
          while (results.get(index) == PRE_QUERY_REJECTED) {
            index++;
          }
          results.set(index, updateCount);
          index++;
        }
        ps.clearBatch();
      }
    }
    return results;
  }

  /**
   * Generates a parameterized SQL IN clause for a PreparedStatement.
   *
   * @param inClauseLiteralSizeLimit max number of literals in a SQL IN clause
   * @return parameterized list (?,?,?) of SQL IN clause
   */
  default String generateInClause(int inClauseLiteralSizeLimit) {
    return " in ("
        + String.join(
            ",", IntStream.range(0, inClauseLiteralSizeLimit).boxed().map(i -> "?").toList())
        + ") ";
  }

  /** Create sub list based on maxSize. */
  default <T> List<List<T>> subList(List<T> list, int maxSize) {
    final List<List<T>> subLists = new ArrayList<>();
    if (list != null && !list.isEmpty()) {
      List<T> subList = null;
      for (int index = 0; index < list.size(); index++) {
        if (index % maxSize == 0) {
          subList = new ArrayList<>();
          subLists.add(subList);
        }
        subList.add(list.get(index));
      }
    }
    return subLists;
  }

  /**
   * Executes query with a SQL IN clause.
   *
   * @param <T> type of literal in a SQL IN clause
   * @param <R> type of result returned
   * @param maxSize is the max number of literals to be used in and SQL in clause
   * @param list of literals
   * @param supplier of SQL string
   * @param consumer to consume a literal by adding to PreparedStatement
   * @return results
   * @throws SQLException on SQL error
   */
  default <T, R> List<R> executeQueryWithInClause(
      int maxSize,
      List<T> list,
      RdbmsSqlStatementSupplier<T> supplier,
      RdbmsInLiteralListConsumer<T> consumer,
      RdbmsResultListprocessor<R> function)
      throws SQLException {
    final List<List<T>> subLists = subList(list, maxSize);
    final List<R> results = new ArrayList<>();
    for (List<T> subList : subLists) {
      try (final PreparedStatement ps = supplier.get(subList)) {
        consumer.addToInClause(ps, subList);
        final ResultSet rs = ps.executeQuery();
        results.addAll(function.processResults(rs));
      }
    }
    return results;
  }

  /** Injects parameters into a PreparedStatement. */
  default void injectParameter(PreparedStatement ps, RdbmsPreparedStatementParamInjector injector)
      throws SQLException {
    injector.inject(ps);
  }
}
