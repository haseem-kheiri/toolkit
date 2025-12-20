package com.tsh.toolkit.rdbms;

import com.tsh.toolkit.containers.Containers;
import com.tsh.toolkit.core.utils.Run;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Test for RDBMS utility.
 *
 * @author Haseem Kheiri
 */
class RdbmsTest {
  private static PostgreSQLContainer<?> postgreSql;
  private static HikariDataSource datasource;

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
  }

  @AfterAll
  static void teardown() {
    Run.runIfNotNull(datasource, ds -> ds.close());
    Run.runIfNotNull(postgreSql, ps -> ps.stop());
  }

  @Test
  void test() {
    Assertions.assertNotNull(datasource);
  }
}
