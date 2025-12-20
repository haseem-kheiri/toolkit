package com.tsh.toolkit.rdbms;

import com.tsh.toolkit.containers.Containers;
import com.tsh.toolkit.rdbms.FlywayConfigurationTest.FlywayConfigurationTestConfig;
import com.tsh.toolkit.rdbms.autoconfig.FlywayConfiguration;
import com.tsh.toolkit.rdbms.helper.TestRepository;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Testing FlywayConfiguration.
 *
 * @author Haseem Kheiri
 */
@SpringBootTest(classes = FlywayConfigurationTestConfig.class)
@EnableAutoConfiguration()
@Slf4j
class FlywayConfigurationTest {

  public static class FlywayConfigurationTestConfig {
    final String userName = "testuser";
    final String password = "testpassword";

    @Bean(destroyMethod = "stop")
    PostgreSQLContainer<?> postgreSqlContainer() {
      PostgreSQLContainer<?> postgreSql =
          Containers.postgreSqlFactory()
              .setDockeImageName("postgres:16")
              .setDatabaseName("platform")
              .setUserName(userName)
              .setPassword(password)
              .build();
      postgreSql.start();
      return postgreSql;
    }

    @Bean
    HikariDataSource hikariDataSource(PostgreSQLContainer<?> postgreSql) {
      return Rdbms.hikariCpDatasourceFactory()
          .setJdbcUrl(postgreSql.getJdbcUrl())
          .setUsername(userName)
          .setPassword(password)
          .setMaximumPoolSize(3)
          .build();
    }

    @Bean
    TestRepository testRepository(HikariDataSource hikariDataSource) {
      final TestRepository testRepository = new TestRepository(hikariDataSource);
      testRepository.migrate("test");
      return testRepository;
    }
  }

  @Autowired private FlywayConfiguration config;
  @Autowired private TestRepository testRepository;

  @Test
  void test() throws SQLException {
    Assertions.assertNotNull(config);

    final Object o =
        testRepository.executeAndReturn(
            (connection) -> {
              try (final PreparedStatement ps =
                  connection.prepareStatement("SELECT CURRENT_DATE")) {
                final ResultSet rs = ps.executeQuery();
                return rs.next() ? rs.getObject(1) : null;
              }
            });

    log.info("SELECT CURRENT_DATE => {}", o);
    Assertions.assertNotNull(o);
  }
}
