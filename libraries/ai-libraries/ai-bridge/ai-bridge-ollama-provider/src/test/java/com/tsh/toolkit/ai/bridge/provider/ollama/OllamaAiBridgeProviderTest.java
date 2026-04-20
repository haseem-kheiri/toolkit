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

package com.tsh.toolkit.ai.bridge.provider.ollama;

import com.tsh.toolkit.ai.bridge.provider.autoconfig.OllamaBridgeProviderConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {OllamaBridgeProviderConfiguration.class},
    properties = {
      "ai.bridge.ollama.model=ollama-model",
      "ai.bridge.ollama.endpoints[0]=http://localhost:11434"
    })
@EnableAutoConfiguration
class OllamaAiBridgeProviderTest {
  @Autowired private OllamaAiBridgeProvider provider;

  @Test
  void testProviderIsNotNull() {
    Assertions.assertNotNull(provider, "OllamaAiBridgeProvider should be injected by Spring");
    Assertions.assertNull(provider.generate(null));
  }
}
