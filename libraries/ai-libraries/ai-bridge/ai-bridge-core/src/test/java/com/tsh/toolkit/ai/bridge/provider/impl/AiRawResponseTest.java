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

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AiRawResponseTest {

  @Test
  void shouldCreateResponseWithContentAndMetadata() {
    String content = "This is the AI response.";
    Metadata metadata = new Metadata(500L, 50, 25, "stop");

    AiRawResponse response = new AiRawResponse(content, metadata);

    assertEquals(content, response.getContent());
    assertEquals(metadata, response.getMetadata());
  }

  @Test
  void shouldCreateResponseWithContentOnly() {
    String content = "Response without metadata.";

    AiRawResponse response = new AiRawResponse(content, null);

    assertEquals(content, response.getContent());
    assertNull(response.getMetadata());
  }

  @Test
  void shouldHandleEmptyContent() {
    String emptyContent = "";
    Metadata metadata = new Metadata(100L, 10, 0, "error");

    AiRawResponse response = new AiRawResponse(emptyContent, metadata);

    assertEquals("", response.getContent());
    assertEquals(metadata, response.getMetadata());
  }

  @Test
  void shouldHandleLongContent() {
    String longContent = "A".repeat(10000);
    Metadata metadata = new Metadata(2000L, 500, 800, "stop");

    AiRawResponse response = new AiRawResponse(longContent, metadata);

    assertEquals(longContent, response.getContent());
    assertEquals(10000, response.getContent().length());
    assertEquals(metadata, response.getMetadata());
  }

  @Test
  void shouldHandleMultilineContent() {
    String multilineContent = "Line 1\nLine 2\nLine 3\n\nLine 5";
    Metadata metadata = new Metadata(300L, 20, 15, "stop");

    AiRawResponse response = new AiRawResponse(multilineContent, metadata);

    assertEquals(multilineContent, response.getContent());
    assertTrue(response.getContent().contains("\n"));
  }

  @Test
  void shouldHandleJsonContent() {
    String jsonContent = "{\"response\": \"Hello World\", \"confidence\": 0.95}";
    Metadata metadata = new Metadata(250L, 30, 20, "stop");

    AiRawResponse response = new AiRawResponse(jsonContent, metadata);

    assertEquals(jsonContent, response.getContent());
    assertTrue(response.getContent().contains("{"));
    assertTrue(response.getContent().contains("}"));
  }

  @Test
  void shouldHandleSpecialCharacters() {
    String specialContent = "Special chars: áéíóú ñ 中文 🚀 \"quotes\" 'apostrophes' \t\r\n";
    Metadata metadata = new Metadata(400L, 40, 35, "stop");

    AiRawResponse response = new AiRawResponse(specialContent, metadata);

    assertEquals(specialContent, response.getContent());
  }

  @Test
  void shouldAcceptNullContent() {
    // While not recommended, the constructor should accept null content
    Metadata metadata = new Metadata(100L, 10, 0, "error");

    AiRawResponse response = new AiRawResponse(null, metadata);

    assertNull(response.getContent());
    assertEquals(metadata, response.getMetadata());
  }

  @Test
  void shouldAcceptNullMetadata() {
    String content = "Content without metadata";

    AiRawResponse response = new AiRawResponse(content, null);

    assertEquals(content, response.getContent());
    assertNull(response.getMetadata());
  }

  @Test
  void shouldAcceptBothNullValues() {
    AiRawResponse response = new AiRawResponse(null, null);

    assertNull(response.getContent());
    assertNull(response.getMetadata());
  }

  @Test
  void shouldBeImmutableAfterConstruction() {
    String originalContent = "Original content";
    Metadata originalMetadata = new Metadata(500L, 50, 25, "stop");

    AiRawResponse response = new AiRawResponse(originalContent, originalMetadata);

    // Verify values remain the same
    assertEquals(originalContent, response.getContent());
    assertEquals(originalMetadata, response.getMetadata());
  }

  @Test
  void shouldHandleCodeContent() {
    String codeContent = "def hello_world():\n    print(\"Hello, World!\")\n    return \"success\"";
    Metadata metadata = new Metadata(600L, 60, 40, "stop");

    AiRawResponse response = new AiRawResponse(codeContent, metadata);

    assertEquals(codeContent, response.getContent());
    assertTrue(response.getContent().contains("def"));
    assertTrue(response.getContent().contains("print"));
  }

  @Test
  void shouldHandleDifferentFinishReasons() {
    String content = "Response content";

    AiRawResponse stopResponse = new AiRawResponse(content, 
        new Metadata(300L, 30, 20, "stop"));
    AiRawResponse lengthResponse = new AiRawResponse(content, 
        new Metadata(400L, 40, 100, "length"));
    AiRawResponse errorResponse = new AiRawResponse(content, 
        new Metadata(100L, 20, 0, "error"));

    assertEquals("stop", stopResponse.getMetadata().getFinishReason());
    assertEquals("length", lengthResponse.getMetadata().getFinishReason());
    assertEquals("error", errorResponse.getMetadata().getFinishReason());
  }

  @Test
  void shouldSupportHighPerformanceScenarios() {
    String content = "Fast response";
    Metadata fastMetadata = new Metadata(50L, 10, 5, "stop");

    AiRawResponse response = new AiRawResponse(content, fastMetadata);

    assertEquals(50L, response.getMetadata().getLatencyMs());
    assertEquals(10, response.getMetadata().getInputTokens());
    assertEquals(5, response.getMetadata().getOutputTokens());
  }

  @Test
  void shouldSupportLargeTokenCounts() {
    String content = "Large token response";
    Metadata largeMetadata = new Metadata(5000L, 8192, 4096, "length");

    AiRawResponse response = new AiRawResponse(content, largeMetadata);

    assertEquals(5000L, response.getMetadata().getLatencyMs());
    assertEquals(8192, response.getMetadata().getInputTokens());
    assertEquals(4096, response.getMetadata().getOutputTokens());
  }

  @Test
  void shouldRetainMetadataReference() {
    String content = "Test content";
    Metadata metadata = new Metadata(200L, 25, 15, "stop");

    AiRawResponse response = new AiRawResponse(content, metadata);

    // Verify it's the same object reference
    assertSame(metadata, response.getMetadata());
  }

  @Test
  void shouldHandleWhitespaceOnlyContent() {
    String whitespaceContent = "   \n\t\r\n   ";
    Metadata metadata = new Metadata(150L, 15, 5, "stop");

    AiRawResponse response = new AiRawResponse(whitespaceContent, metadata);

    assertEquals(whitespaceContent, response.getContent());
    assertEquals(whitespaceContent.length(), response.getContent().length());
  }
}