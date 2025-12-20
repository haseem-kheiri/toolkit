/*
 * Copyright (c) 2023-2025 Haseem Kheiri, Tahama Bin Haseem, and Shees Bin Haseem
 *
 * Licensed under the Apache License, Version 2.0.
 * See LICENSE in the project root for the full license text.
 */

package com.tsh.toolkit.rdbms.autoconfig;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Disables flyway execution by spring boot.
 *
 * @author Haseem Kheiri
 */
@Configuration
public class FlywayConfiguration {
  @Bean
  FlywayMigrationStrategy flywayMigrationStrategy() {
    return flyway -> {
      // skip migration
    };
  }
}
