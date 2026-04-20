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

import static org.junit.jupiter.api.Assertions.*;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class OllamaEndpointSelectorTest {

  @Test
  void shouldExtendAbstractRoundRobinEndpointSelector() {
    List<URI> endpoints = Arrays.asList(
        URI.create("http://ollama1:11434"),
        URI.create("http://ollama2:11434"),
        URI.create("http://ollama3:11434")
    );
    
    OllamaEndpointSelector selector = new OllamaEndpointSelector(endpoints);
    
    // Verify it implements the expected interface
    assertTrue(selector instanceof com.tsh.toolkit.ai.bridge.provider.EndpointSelector);
    assertTrue(selector instanceof com.tsh.toolkit.ai.bridge.provider.AbstractRoundRobinEndpointSelector);
  }

  @Test
  void shouldSelectOllamaEndpointsInRoundRobinOrder() {
    List<URI> endpoints = Arrays.asList(
        URI.create("http://ollama1:11434"),
        URI.create("http://ollama2:11434"),
        URI.create("http://ollama3:11434")
    );
    
    OllamaEndpointSelector selector = new OllamaEndpointSelector(endpoints);
    
    // Test round-robin behavior specific to Ollama endpoints
    assertEquals(URI.create("http://ollama1:11434"), selector.select());
    assertEquals(URI.create("http://ollama2:11434"), selector.select());
    assertEquals(URI.create("http://ollama3:11434"), selector.select());
    assertEquals(URI.create("http://ollama1:11434"), selector.select()); // Wrap around
  }

  @Test
  void shouldValidateOllamaEndpoints() {
    // Test with typical Ollama endpoint format
    List<URI> endpoints = Arrays.asList(
        URI.create("http://localhost:11434"),
        URI.create("https://ollama.example.com:11434"),
        URI.create("http://10.0.0.1:11434")
    );
    
    OllamaEndpointSelector selector = new OllamaEndpointSelector(endpoints);
    
    for (int i = 0; i < 10; i++) {
      URI selected = selector.select();
      assertNotNull(selected);
      assertTrue(endpoints.contains(selected));
    }
  }

  @Test
  void shouldRejectInvalidInputs() {
    assertThrows(IllegalArgumentException.class, () -> {
      new OllamaEndpointSelector(null);
    });
    
    assertThrows(IllegalArgumentException.class, () -> {
      new OllamaEndpointSelector(Arrays.asList());
    });
  }
}