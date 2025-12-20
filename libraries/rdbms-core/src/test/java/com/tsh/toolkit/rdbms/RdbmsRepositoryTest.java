package com.tsh.toolkit.rdbms;

import com.tsh.toolkit.containers.Containers;
import com.tsh.toolkit.core.utils.Run;
import com.tsh.toolkit.rdbms.helper.TestRepository;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Test for RDBMS repository.
 *
 * @author Haseem Kheiri
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class RdbmsRepositoryTest {
  public record Person(long id, String firstname, String lastname, int age) {}

  private static HikariDataSource datasource;
  private static TestRepository repository;
  private static PostgreSQLContainer<?> postgreSql;

  @BeforeAll
  static void setup() {
    final String userName = "testuser";
    final String password = "testpassword";
    postgreSql =
        Containers.postgreSqlFactory()
            .setDockeImageName("postgres:16")
            .setDatabaseName("platform")
            .setUserName(userName)
            .setPassword(password)
            .build();
    postgreSql.start();

    datasource =
        Rdbms.hikariCpDatasourceFactory()
            .setJdbcUrl(postgreSql.getJdbcUrl())
            .setUsername(userName)
            .setPassword(password)
            .setMaximumPoolSize(3)
            .build();

    repository = new TestRepository(datasource);
    repository.migrate("test");
  }

  @AfterAll
  static void teardown() {
    Run.runIfNotNull(datasource, ds -> ds.close());
    Run.runIfNotNull(postgreSql, ps -> ps.stop());
  }

  private Long insertPerson(Person person, Connection c) throws SQLException {
    try (final PreparedStatement ps =
        c.prepareStatement(
            "insert into test.persons(first_name, last_name, age) values (?,?,?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, person.firstname);
      ps.setString(2, person.lastname);
      ps.setInt(3, person.age);

      ps.executeUpdate();

      final ResultSet rs = ps.getGeneratedKeys();
      return rs.next() ? rs.getLong(1) : null;
    }
  }

  private Long addPerson(Person person) {
    return repository.executeAndReturn((c) -> insertPerson(person, c));
  }

  private Long addPersonAndAutoCommit(Person person) {
    return repository.executeAndReturn((c) -> insertPerson(person, c), true);
  }

  @Test
  @Order(1)
  void testExecuteAndReturn() {
    Assertions.assertEquals(1, addPerson(new Person(0, "Haseem", "Kheiri", 55)));
    Assertions.assertEquals(2, addPersonAndAutoCommit(new Person(0, "Ravjot", "Singh", 35)));
  }

  @Test
  @Order(2)
  void testExecuteAndReturnFailure() {
    Assertions.assertThrows(
        RdbmsRepositoryException.class,
        () -> {
          addPerson(new Person(0, "Haseem", "Kheiri", 55));
        });

    Assertions.assertThrows(
        RdbmsRepositoryException.class,
        () -> {
          addPersonAndAutoCommit(new Person(0, "Ravjot", "Singh", 35));
        });
  }

  @Test
  @Order(3)
  void testExecute() {
    final Map<String, Object> rec = new TreeMap<>();
    repository.execute(
        (c) -> {
          try (final PreparedStatement ps =
              c.prepareStatement(
                  "select first_name, last_name, age from test.persons where id = ?")) {
            ps.setInt(1, 1);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
              rec.put("firstname", rs.getString(1));
              rec.put("lastname", rs.getString(2));
              rec.put("age", rs.getInt(3));
            }
          }
        });

    Assertions.assertEquals("Haseem", rec.get("firstname"));
    Assertions.assertEquals("Kheiri", rec.get("lastname"));
    Assertions.assertEquals(55, rec.get("age"));
  }

  @Test
  @Order(4)
  void testExecuteBatch() {
    List<Person> list =
        List.of(
            new Person(0, "Grish", "Sharma", 46),
            new Person(0, "Future", "Dev", -10),
            new Person(0, "Tahama", "Haseem", 23),
            new Person(0, "Shees", "Haseen", 20));

    List<Integer> updateCounts =
        repository.executeAndReturn(
            (connection) -> {
              try (final PreparedStatement ps =
                  connection.prepareStatement(
                      "insert into test.persons(first_name, last_name, age) values (?,?,?)")) {
                return repository.executeBatch(
                    ps,
                    2,
                    list,
                    (stmt, param) -> {
                      if (param.age > 0) {
                        stmt.setString(1, param.firstname);
                        stmt.setString(2, param.lastname);
                        stmt.setInt(3, param.age);
                        return true;
                      } else {
                        return false;
                      }
                    });
              }
            });

    log.info("Batch update counts -> {}.", updateCounts);
    Assertions.assertEquals(4, updateCounts.size());
    Assertions.assertEquals(1, updateCounts.get(0));
    Assertions.assertEquals(RdbmsRepository.PRE_QUERY_REJECTED, updateCounts.get(1));
    Assertions.assertEquals(1, updateCounts.get(2));
    Assertions.assertEquals(1, updateCounts.get(3));
  }

  @Test
  @Order(5)
  void testExecuteQueryWithInClause() {
    List<Integer> ids = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    List<Person> results =
        repository.executeAndReturn(
            (connection) -> {
              return repository.executeQueryWithInClause(
                  2,
                  ids,
                  (params) -> {
                    final String sql =
                        "select id, first_name, last_name, age from test.persons "
                            + "where age > ? and id"
                            + repository.generateInClause(params.size());
                    log.info(sql);
                    return connection.prepareStatement(sql);
                  },
                  (ps, params) -> {
                    int index = 0;
                    ps.setInt(++index, 40);
                    for (Integer param : params) {
                      ps.setLong(++index, param);
                    }
                  },
                  (rs) -> {
                    final List<Person> list = new ArrayList<>();
                    while (rs.next()) {
                      list.add(
                          new Person(
                              rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(4)));
                    }
                    return list;
                  });
            });

    Assertions.assertEquals(2, results.size());
  }
}
