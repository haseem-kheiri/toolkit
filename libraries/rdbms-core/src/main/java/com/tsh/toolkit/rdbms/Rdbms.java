/*
 * Copyright (c) 2023-2025 Haseem Kheiri, Tahama Bin Haseem, and Shees Bin Haseem
 *
 * Licensed under the Apache License, Version 2.0.
 * See LICENSE in the project root for the full license text.
 */

package com.tsh.toolkit.rdbms;

/**
 * RDBMS utilities.
 *
 * @author Haseem Kheiri
 */
public class Rdbms {
  /** Create a Hikary CP data source factory. */
  public static HikariCpDatasourceFactory hikariCpDatasourceFactory() {
    return new HikariCpDatasourceFactory();
  }
}
