package com.tsh.toolkit.rdbms.helper;

import com.tsh.toolkit.rdbms.AbstractRdbmsRepository;
import javax.sql.DataSource;

/**
 * A test repository created to test a RDBMS repository.
 *
 * @author Haseem Kheiri
 */
public class TestRepository extends AbstractRdbmsRepository {

  /** Construct a test repository. */
  public TestRepository(DataSource dataSource) {
    super(dataSource);
  }
}
