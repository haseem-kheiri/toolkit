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

class MetadataTest {

  @Test
  void shouldCreateMetadataWithAllParameters() {
    Metadata metadata = new Metadata(500L, 100, 50, "stop");

    assertEquals(500L, metadata.getLatencyMs());
    assertEquals(100, metadata.getInputTokens());
    assertEquals(50, metadata.getOutputTokens());
    assertEquals("stop", metadata.getFinishReason());
  }

  @Test
  void shouldCreateMetadataWithNullValues() {
    Metadata metadata = new Metadata(null, null, null, null);

    assertNull(metadata.getLatencyMs());
    assertNull(metadata.getInputTokens());
    assertNull(metadata.getOutputTokens());
    assertNull(metadata.getFinishReason());
  }

  @Test
  void shouldCreateMetadataWithMixedNullValues() {
    Metadata metadata = new Metadata(300L, null, 25, "stop");

    assertEquals(300L, metadata.getLatencyMs());
    assertNull(metadata.getInputTokens());
    assertEquals(25, metadata.getOutputTokens());
    assertEquals("stop", metadata.getFinishReason());
  }

  @Test
  void shouldHandleZeroLatency() {
    Metadata metadata = new Metadata(0L, 10, 5, "stop");

    assertEquals(0L, metadata.getLatencyMs());
    assertEquals(10, metadata.getInputTokens());
    assertEquals(5, metadata.getOutputTokens());
  }

  @Test
  void shouldHandleHighLatency() {
    Metadata metadata = new Metadata(30000L, 1000, 500, "stop");

    assertEquals(30000L, metadata.getLatencyMs());
    assertEquals(1000, metadata.getInputTokens());
    assertEquals(500, metadata.getOutputTokens());
  }

  @Test
  void shouldHandleZeroTokenCounts() {
    Metadata metadata = new Metadata(200L, 0, 0, "error");

    assertEquals(200L, metadata.getLatencyMs());
    assertEquals(0, metadata.getInputTokens());
    assertEquals(0, metadata.getOutputTokens());
    assertEquals("error", metadata.getFinishReason());
  }

  @Test
  void shouldHandleLargeTokenCounts() {
    Metadata metadata = new Metadata(5000L, 100000, 50000, "length");

    assertEquals(5000L, metadata.getLatencyMs());
    assertEquals(100000, metadata.getInputTokens());
    assertEquals(50000, metadata.getOutputTokens());
    assertEquals("length", metadata.getFinishReason());
  }

  @Test
  void shouldSupportCommonFinishReasons() {
    Metadata stopMeta = new Metadata(300L, 30, 20, "stop");
    Metadata lengthMeta = new Metadata(500L, 50, 100, "length");
    Metadata errorMeta = new Metadata(100L, 20, 0, "error");
    Metadata timeoutMeta = new Metadata(10000L, 100, 10, "timeout");
    Metadata filterMeta = new Metadata(200L, 25, 0, "content_filter");

    assertEquals("stop", stopMeta.getFinishReason());
    assertEquals("length", lengthMeta.getFinishReason());
    assertEquals("error", errorMeta.getFinishReason());
    assertEquals("timeout", timeoutMeta.getFinishReason());
    assertEquals("content_filter", filterMeta.getFinishReason());
  }

  @Test
  void shouldSupportCustomFinishReasons() {
    Metadata customMeta = new Metadata(400L, 40, 30, "custom_reason");
    Metadata providerMeta = new Metadata(350L, 35, 25, "provider_specific_stop");

    assertEquals("custom_reason", customMeta.getFinishReason());
    assertEquals("provider_specific_stop", providerMeta.getFinishReason());
  }

  @Test
  void shouldHandleEmptyFinishReason() {
    Metadata metadata = new Metadata(250L, 25, 15, "");

    assertEquals("", metadata.getFinishReason());
  }

  @Test
  void shouldBeImmutableAfterConstruction() {
    Long originalLatency = 600L;
    Integer originalInput = 60;
    Integer originalOutput = 40;
    String originalReason = "stop";

    Metadata metadata = new Metadata(originalLatency, originalInput, originalOutput, originalReason);

    // Verify values remain the same
    assertEquals(originalLatency, metadata.getLatencyMs());
    assertEquals(originalInput, metadata.getInputTokens());
    assertEquals(originalOutput, metadata.getOutputTokens());
    assertEquals(originalReason, metadata.getFinishReason());
  }

  @Test
  void shouldHandleNegativeValues() {
    // While not semantically correct, test that negative values are stored
    Metadata metadata = new Metadata(-100L, -10, -5, "error");

    assertEquals(-100L, metadata.getLatencyMs());
    assertEquals(-10, metadata.getInputTokens());
    assertEquals(-5, metadata.getOutputTokens());
    assertEquals("error", metadata.getFinishReason());
  }

  @Test
  void shouldSupportRealisticScenarios() {
    // Fast response scenario
    Metadata fastResponse = new Metadata(150L, 20, 10, "stop");
    assertEquals(150L, fastResponse.getLatencyMs());
    assertEquals(20, fastResponse.getInputTokens());
    assertEquals(10, fastResponse.getOutputTokens());

    // Slow response scenario  
    Metadata slowResponse = new Metadata(8000L, 500, 1000, "stop");
    assertEquals(8000L, slowResponse.getLatencyMs());
    assertEquals(500, slowResponse.getInputTokens());
    assertEquals(1000, slowResponse.getOutputTokens());

    // Truncated response scenario
    Metadata truncatedResponse = new Metadata(3000L, 2000, 4096, "length");
    assertEquals("length", truncatedResponse.getFinishReason());
    assertTrue(truncatedResponse.getOutputTokens() > truncatedResponse.getInputTokens());
  }

  @Test
  void shouldSupportPerformanceMonitoring() {
    Metadata metadata = new Metadata(1234L, 567, 890, "stop");

    // Verify all metrics are accessible for monitoring
    assertNotNull(metadata.getLatencyMs());
    assertNotNull(metadata.getInputTokens());
    assertNotNull(metadata.getOutputTokens());
    assertNotNull(metadata.getFinishReason());

    // Verify specific values
    assertEquals(1234L, metadata.getLatencyMs());
    assertEquals(567, metadata.getInputTokens());
    assertEquals(890, metadata.getOutputTokens());
  }

  @Test
  void shouldSupportCostCalculation() {
    Metadata metadata = new Metadata(500L, 1000, 500, "stop");

    // Simulate cost calculation logic
    Integer inputTokens = metadata.getInputTokens();
    Integer outputTokens = metadata.getOutputTokens();

    assertNotNull(inputTokens);
    assertNotNull(outputTokens);

    int totalTokens = inputTokens + outputTokens;
    assertEquals(1500, totalTokens);
  }

  @Test
  void shouldHandleProviderSpecificCases() {
    // OpenAI style
    Metadata openaiMeta = new Metadata(750L, 100, 75, "stop");
    assertEquals("stop", openaiMeta.getFinishReason());

    // Claude style (hypothetical)
    Metadata claudeMeta = new Metadata(900L, 120, 80, "end_turn");
    assertEquals("end_turn", claudeMeta.getFinishReason());

    // Ollama style (hypothetical)
    Metadata ollamaMeta = new Metadata(2000L, 200, 150, "completed");
    assertEquals("completed", ollamaMeta.getFinishReason());
  }

  @Test
  void shouldSupportDifferentLatencyRanges() {
    // Very fast (cached/small)
    Metadata veryFast = new Metadata(50L, 5, 3, "stop");
    assertTrue(veryFast.getLatencyMs() < 100);

    // Typical
    Metadata typical = new Metadata(800L, 80, 60, "stop");
    assertTrue(typical.getLatencyMs() > 100 && typical.getLatencyMs() < 2000);

    // Slow (large content/complex)
    Metadata slow = new Metadata(15000L, 2000, 1500, "stop");
    assertTrue(slow.getLatencyMs() > 10000);
  }

  @Test
  void shouldRetainPrecisionForLongValues() {
    Long preciseLatency = 1234567890L;
    Metadata metadata = new Metadata(preciseLatency, 100, 50, "stop");

    assertEquals(preciseLatency, metadata.getLatencyMs());
    assertEquals(1234567890L, metadata.getLatencyMs().longValue());
  }
}