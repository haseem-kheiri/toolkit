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

package com.tsh.toolkit.containers;

import com.tsh.toolkit.container.artemis.ArtemisContainerFactory;
import com.tsh.toolkit.container.kafka.ConfluentKafkaContainerFactory;
import com.tsh.toolkit.container.postgres.PostgreSqlContainerFactory;

/**
 * Utility class for building docker containers.
 *
 * @author Haseem Kheiri
 */
public class Containers {

  /** Creates a PostgreSql container factory. */
  public static PostgreSqlContainerFactory postgreSqlFactory() {
    return new PostgreSqlContainerFactory();
  }

  /** Create a Kafka container factory. */
  public static ConfluentKafkaContainerFactory kafkaFactory() {
    return new ConfluentKafkaContainerFactory();
  }

  /** Create a Artemis container factory. */
  public static ArtemisContainerFactory artemisContainerFactory() {
    return new ArtemisContainerFactory();
  }
}
