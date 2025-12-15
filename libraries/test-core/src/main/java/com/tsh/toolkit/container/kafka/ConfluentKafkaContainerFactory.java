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

package com.tsh.toolkit.container.kafka;

import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Factory to build a Kafka container.
 *
 * @author Haseem Kheiri
 */
public class ConfluentKafkaContainerFactory {

  private String dockeImageName = "confluentinc/cp-kafka:7.6.0";

  /** sets Kafka dockerImageName. */
  public ConfluentKafkaContainerFactory setDockeImageName(String dockeImageName) {
    this.dockeImageName = dockeImageName;
    return this;
  }

  /** Builds a kafka container. */
  public ConfluentKafkaContainer build() {
    return new ConfluentKafkaContainer(DockerImageName.parse(dockeImageName));
  }
}
