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

package com.tsh.toolkit.container.artemis;

import java.time.Duration;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Factory to build Artemis container.
 *
 * @author Haseem Kheiri
 */
public class ArtemisContainerFactory {
  private String dockeImageName;
  private String userName;
  private String password;

  /** sets Artemis dockerImageName. */
  public ArtemisContainerFactory setDockeImageName(String dockeImageName) {
    this.dockeImageName = dockeImageName;
    return this;
  }

  /** sets Artemis userName. */
  public ArtemisContainerFactory setUserName(String userName) {
    this.userName = userName;
    return this;
  }

  /** sets Artemis password. */
  public ArtemisContainerFactory setPassword(String password) {
    this.password = password;
    return this;
  }

  /** Builds a Artemis container. */
  public ArtemisContainer build() {
    return new ArtemisContainer(dockeImageName) {
      @Override
      protected void configure() {
        setWaitStrategy(
            Wait.forLogMessage(".*AMQ221007.*ArtemisConsole.*started.*", 1)
                .withStartupTimeout(Duration.ofSeconds(120)));
      }
    }.withUser(userName).withPassword(password);
  }
}
