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

package com.tsh.toolkit.ai.bridge.provider.autoconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class OllamaAiBridgeProviderPropertiesTest {

  @Test
  void shouldCreatePropertiesWithDefaultValues() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();

    assertNull(properties.getModel());
    assertEquals(properties.getEndpoints().size(), 0);
  }

  @Test
  void shouldSetAndGetModel() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();
    String model = "llama2:7b";

    properties.setModel(model);

    assertEquals(model, properties.getModel());
  }

  @Test
  void shouldSetAndGetEndpoints() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();
    List<URI> endpoints =
        Arrays.asList(URI.create("http://localhost:11434"), URI.create("http://ollama2:11434"));

    properties.setEndpoints(endpoints);

    assertEquals(endpoints, properties.getEndpoints());
    assertEquals(2, properties.getEndpoints().size());
  }

  @Test
  void shouldHandleEmptyEndpointsList() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();
    List<URI> emptyEndpoints = Collections.emptyList();

    properties.setEndpoints(emptyEndpoints);

    assertEquals(emptyEndpoints, properties.getEndpoints());
    assertTrue(properties.getEndpoints().isEmpty());
  }

  @Test
  void shouldHandleNullModel() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();

    properties.setModel(null);

    assertNull(properties.getModel());
  }

  @Test
  void shouldHandleNullEndpoints() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();

    properties.setEndpoints(null);

    assertNull(properties.getEndpoints());
  }

  @Test
  void shouldSupportVariousModelFormats() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();

    // Test different model naming conventions
    properties.setModel("llama2");
    assertEquals("llama2", properties.getModel());

    properties.setModel("llama2:7b");
    assertEquals("llama2:7b", properties.getModel());

    properties.setModel("mistral:7b-instruct");
    assertEquals("mistral:7b-instruct", properties.getModel());

    properties.setModel("codellama:34b");
    assertEquals("codellama:34b", properties.getModel());
  }

  @Test
  void shouldSupportDifferentEndpointFormats() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();

    List<URI> variousEndpoints =
        Arrays.asList(
            URI.create("http://localhost:11434"),
            URI.create("https://ollama.example.com:11434"),
            URI.create("http://192.168.1.100:11434"),
            URI.create("https://secure-ollama.company.com"));

    properties.setEndpoints(variousEndpoints);

    assertEquals(4, properties.getEndpoints().size());
    assertEquals("localhost", properties.getEndpoints().get(0).getHost());
    assertEquals(11434, properties.getEndpoints().get(0).getPort());
    assertEquals("https", properties.getEndpoints().get(1).getScheme());
  }

  @Test
  void shouldHandleSingleEndpoint() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();
    List<URI> singleEndpoint = Arrays.asList(URI.create("http://localhost:11434"));

    properties.setEndpoints(singleEndpoint);

    assertEquals(1, properties.getEndpoints().size());
    assertEquals(URI.create("http://localhost:11434"), properties.getEndpoints().get(0));
  }

  @Test
  void shouldSupportMethodChaining() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();

    // Test that setters allow method chaining (if implemented)
    properties.setModel("llama2:13b");
    properties.setEndpoints(Arrays.asList(URI.create("http://localhost:11434")));

    assertEquals("llama2:13b", properties.getModel());
    assertEquals(1, properties.getEndpoints().size());
  }

  @Test
  void shouldRetainEndpointOrder() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();
    List<URI> orderedEndpoints =
        Arrays.asList(
            URI.create("http://primary:11434"),
            URI.create("http://secondary:11434"),
            URI.create("http://tertiary:11434"));

    properties.setEndpoints(orderedEndpoints);

    List<URI> retrievedEndpoints = properties.getEndpoints();
    assertEquals("primary", retrievedEndpoints.get(0).getHost());
    assertEquals("secondary", retrievedEndpoints.get(1).getHost());
    assertEquals("tertiary", retrievedEndpoints.get(2).getHost());
  }

  @Test
  void shouldHandleEmptyStringModel() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();

    properties.setModel("");

    assertEquals("", properties.getModel());
  }

  @Test
  void shouldSupportComplexModelNames() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();
    String complexModel = "custom-model:v2.1-fine-tuned_2024";

    properties.setModel(complexModel);

    assertEquals(complexModel, properties.getModel());
  }

  @Test
  void shouldAllowMutableEndpointsList() {
    OllamaAiBridgeProviderProperties properties = new OllamaAiBridgeProviderProperties();
    List<URI> mutableEndpoints =
        Arrays.asList(URI.create("http://endpoint1:11434"), URI.create("http://endpoint2:11434"));

    properties.setEndpoints(mutableEndpoints);

    assertEquals(mutableEndpoints, properties.getEndpoints());
    assertEquals(2, properties.getEndpoints().size());
  }

  @Test
  void shouldSupportDevelopmentAndProductionScenarios() {
    // Development scenario - single local endpoint
    OllamaAiBridgeProviderProperties devProps = new OllamaAiBridgeProviderProperties();
    devProps.setModel("llama2:7b");
    devProps.setEndpoints(Arrays.asList(URI.create("http://localhost:11434")));

    assertEquals("llama2:7b", devProps.getModel());
    assertEquals(1, devProps.getEndpoints().size());

    // Production scenario - multiple endpoints
    OllamaAiBridgeProviderProperties prodProps = new OllamaAiBridgeProviderProperties();
    prodProps.setModel("llama2:70b");
    prodProps.setEndpoints(
        Arrays.asList(
            URI.create("https://ollama1.prod.company.com"),
            URI.create("https://ollama2.prod.company.com"),
            URI.create("https://ollama3.prod.company.com")));

    assertEquals("llama2:70b", prodProps.getModel());
    assertEquals(3, prodProps.getEndpoints().size());
  }
}
