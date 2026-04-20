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

package com.tsh.toolkit.ai.bridge.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiRawRequestTest {

  @Test
  void shouldCreateRequestWithAllParameters() {
    List<Message> messages =
        Arrays.asList(
            new Message(Role.SYSTEM, "You are a helpful assistant."),
            new Message(Role.USER, "Hello!"));
    GenerationConfig config = new GenerationConfig(0.7, 0.9, 500);
    String model = "gpt-4";

    AiRawRequest request = new AiRawRequest(messages, config, model);

    assertEquals(messages, request.getMessages());
    assertEquals(config, request.getConfig());
    assertEquals(model, request.getModel());
  }

  @Test
  void shouldCreateRequestWithNullModel() {
    List<Message> messages = Arrays.asList(new Message(Role.USER, "Test message"));
    GenerationConfig config = new GenerationConfig(0.5, null, 100);

    AiRawRequest request = new AiRawRequest(messages, config, null);

    assertEquals(messages, request.getMessages());
    assertEquals(config, request.getConfig());
    assertNull(request.getModel());
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForEmptyMessagesList() {
    List<Message> emptyMessages = Collections.emptyList();
    GenerationConfig config = new GenerationConfig(1.0, 1.0, 200);
    String model = "claude-3";

    assertThrows(IllegalArgumentException.class, () -> {
      new AiRawRequest(emptyMessages, config, model);
    });
  }

  @Test
  void shouldCreateRequestWithSingleMessage() {
    List<Message> singleMessage =
        Arrays.asList(new Message(Role.USER, "What is the weather today?"));
    GenerationConfig config = new GenerationConfig(null, null, null);
    String model = "mistral:7b";

    AiRawRequest request = new AiRawRequest(singleMessage, config, model);

    assertEquals(1, request.getMessages().size());
    assertEquals("What is the weather today?", request.getMessages().get(0).getContent());
    assertEquals(Role.USER, request.getMessages().get(0).getRole());
  }

  @Test
  void shouldCreateRequestWithMultipleMessages() {
    List<Message> conversation =
        Arrays.asList(
            new Message(Role.SYSTEM, "You are a coding assistant."),
            new Message(Role.USER, "How do I create a list in Python?"),
            new Message(Role.ASSISTANT, "You can create a list using square brackets: [1, 2, 3]"),
            new Message(Role.USER, "How do I add items to it?"));
    GenerationConfig config = new GenerationConfig(0.3, 0.8, 150);
    String model = "codex";

    AiRawRequest request = new AiRawRequest(conversation, config, model);

    assertEquals(4, request.getMessages().size());
    assertEquals(Role.SYSTEM, request.getMessages().get(0).getRole());
    assertEquals(Role.USER, request.getMessages().get(1).getRole());
    assertEquals(Role.ASSISTANT, request.getMessages().get(2).getRole());
    assertEquals(Role.USER, request.getMessages().get(3).getRole());
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForNullMessages() {
    GenerationConfig config = new GenerationConfig(0.7, 0.9, 500);

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new AiRawRequest(null, config, "gpt-4");
        });
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForNullConfig() {
    List<Message> messages = Arrays.asList(new Message(Role.USER, "Test message"));

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new AiRawRequest(messages, null, "gpt-4");
        });
  }

  @Test
  void shouldPreserveMessageOrder() {
    List<Message> orderedMessages =
        Arrays.asList(
            new Message(Role.SYSTEM, "First message"),
            new Message(Role.USER, "Second message"),
            new Message(Role.ASSISTANT, "Third message"));
    GenerationConfig config = new GenerationConfig(0.5, 0.5, 300);

    AiRawRequest request = new AiRawRequest(orderedMessages, config, "test-model");

    List<Message> retrievedMessages = request.getMessages();
    assertEquals("First message", retrievedMessages.get(0).getContent());
    assertEquals("Second message", retrievedMessages.get(1).getContent());
    assertEquals("Third message", retrievedMessages.get(2).getContent());
  }

  @Test
  void shouldSupportVariousModelIdentifiers() {
    List<Message> messages = Arrays.asList(new Message(Role.USER, "Test"));
    GenerationConfig config = new GenerationConfig(0.7, null, 100);

    // Test different model naming conventions
    AiRawRequest gpt4Request = new AiRawRequest(messages, config, "gpt-4");
    AiRawRequest claudeRequest = new AiRawRequest(messages, config, "claude-3-sonnet-20240229");
    AiRawRequest mistralRequest = new AiRawRequest(messages, config, "mistral:7b-instruct");
    AiRawRequest ollamaRequest = new AiRawRequest(messages, config, "llama2:13b");

    assertEquals("gpt-4", gpt4Request.getModel());
    assertEquals("claude-3-sonnet-20240229", claudeRequest.getModel());
    assertEquals("mistral:7b-instruct", mistralRequest.getModel());
    assertEquals("llama2:13b", ollamaRequest.getModel());
  }

  @Test
  void shouldHandleSpecialCharactersInModel() {
    List<Message> messages = Arrays.asList(new Message(Role.USER, "Test"));
    GenerationConfig config = new GenerationConfig(0.7, null, 100);
    String specialModel = "model-name_v2.1:latest@provider.com";

    AiRawRequest request = new AiRawRequest(messages, config, specialModel);

    assertEquals(specialModel, request.getModel());
  }

  @Test
  void shouldRetainConfigurationPrecisely() {
    List<Message> messages = Arrays.asList(new Message(Role.USER, "Test"));
    GenerationConfig preciseConfig = new GenerationConfig(0.123456789, 0.987654321, 42);

    AiRawRequest request = new AiRawRequest(messages, preciseConfig, "test");

    assertEquals(0.123456789, request.getConfig().getTemperature());
    assertEquals(0.987654321, request.getConfig().getTopP());
    assertEquals(42, request.getConfig().getMaxTokens());
  }

  @Test
  void shouldBeImmutableAfterConstruction() {
    List<Message> originalMessages = Arrays.asList(new Message(Role.USER, "Original message"));
    GenerationConfig originalConfig = new GenerationConfig(0.8, 0.9, 400);
    String originalModel = "original-model";

    AiRawRequest request = new AiRawRequest(originalMessages, originalConfig, originalModel);

    // Verify that the request retains its values
    assertEquals(originalMessages.size(), request.getMessages().size());
    assertEquals("Original message", request.getMessages().get(0).getContent());
    assertEquals(0.8, request.getConfig().getTemperature());
    assertEquals("original-model", request.getModel());
  }

  @Test
  void shouldSupportComplexConversationFlow() {
    // Simulate a realistic conversation flow
    List<Message> complexConversation =
        Arrays.asList(
            new Message(Role.SYSTEM, "You are a financial advisor. Provide JSON responses only."),
            new Message(Role.USER, "I have $10,000 to invest. What do you recommend?"),
            new Message(
                Role.ASSISTANT,
                "{\"recommendation\": \"diversified portfolio\", \"allocation\": {\"stocks\": 60, \"bonds\": 30, \"cash\": 10}}"),
            new Message(Role.USER, "What about cryptocurrency?"),
            new Message(
                Role.ASSISTANT,
                "{\"crypto_advice\": \"limit to 5% of portfolio\", \"risk_level\": \"high\"}"),
            new Message(Role.USER, "Show me specific ETF recommendations."));

    GenerationConfig financialConfig = new GenerationConfig(0.1, 0.9, 500);
    String financialModel = "gpt-4-financial";

    AiRawRequest request = new AiRawRequest(complexConversation, financialConfig, financialModel);

    assertEquals(6, request.getMessages().size());
    assertEquals(Role.SYSTEM, request.getMessages().get(0).getRole());
    assertTrue(request.getMessages().get(0).getContent().contains("financial advisor"));
    assertEquals(0.1, request.getConfig().getTemperature()); // Low temp for financial advice
    assertEquals("gpt-4-financial", request.getModel());
  }

  @Test
  void shouldHandleEmptyStringModel() {
    List<Message> messages = Arrays.asList(new Message(Role.USER, "Test"));
    GenerationConfig config = new GenerationConfig(0.7, null, 100);

    AiRawRequest request = new AiRawRequest(messages, config, "");

    assertEquals("", request.getModel());
  }

  @Test
  void shouldSupportAllRoleTypes() {
    List<Message> allRoleTypes =
        Arrays.asList(
            new Message(Role.SYSTEM, "System instruction"),
            new Message(Role.USER, "User input"),
            new Message(Role.ASSISTANT, "Assistant response"),
            new Message(new Role("tool"), "Tool output"), // Custom role
            new Message(new Role("function"), "Function result") // Custom role
            );

    GenerationConfig config = new GenerationConfig(0.6, 0.8, 300);
    AiRawRequest request = new AiRawRequest(allRoleTypes, config, "multi-role-model");

    assertEquals(5, request.getMessages().size());
    assertEquals("system", request.getMessages().get(0).getRole().getValue());
    assertEquals("user", request.getMessages().get(1).getRole().getValue());
    assertEquals("assistant", request.getMessages().get(2).getRole().getValue());
    assertEquals("tool", request.getMessages().get(3).getRole().getValue());
    assertEquals("function", request.getMessages().get(4).getRole().getValue());
  }
}
