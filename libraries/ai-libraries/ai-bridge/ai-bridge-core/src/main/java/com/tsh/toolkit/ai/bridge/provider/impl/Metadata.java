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

/**
 * Metadata class encapsulates additional information about the AI response, such as latency, token
 * usage, and finish reason.
 */
public final class Metadata {

  private final Long latencyMs;
  private final Integer inputTokens;
  private final Integer outputTokens;
  private final String finishReason;

  /**
   * Constructs a Metadata instance with the provided values.
   *
   * @param latencyMs Total response latency in milliseconds, including network and processing time.
   * @param inputTokens Number of input tokens processed, including system, user, and assistant
   *     messages.
   * @param outputTokens Number of output tokens generated in the response, including text and
   *     formatting tokens.
   * @param finishReason Reason why the AI model stopped generating text, such as "stop", "length",
   *     "content_filter", "error", or "timeout". Different providers may have additional or
   *     different finish reasons.
   */
  public Metadata(Long latencyMs, Integer inputTokens, Integer outputTokens, String finishReason) {
    this.latencyMs = latencyMs;
    this.inputTokens = inputTokens;
    this.outputTokens = outputTokens;
    this.finishReason = finishReason;
  }

  /**
   * Returns the total response latency in milliseconds.
   *
   * <p>This metric includes the complete round-trip time from when the request was initiated to
   * when the response was fully received. It encompasses:
   *
   * <ul>
   *   <li>Network latency to and from the AI provider
   *   <li>Queue time waiting for processing
   *   <li>Actual text generation time
   *   <li>Response serialization and transmission time
   * </ul>
   *
   * @return the latency in milliseconds, or null if not provided by the AI provider
   */
  public Long getLatencyMs() {
    return latencyMs;
  }

  /**
   * Returns the number of input tokens that were processed.
   *
   * <p>Input tokens typically include:
   *
   * <ul>
   *   <li>System message tokens (instructions, context)
   *   <li>User message tokens (current and historical)
   *   <li>Assistant message tokens (conversation history)
   *   <li>Special formatting tokens (depending on provider)
   * </ul>
   *
   * <p>This metric is crucial for cost tracking as most AI providers charge for input tokens.
   *
   * @return the number of input tokens processed, or null if not provided by the AI provider
   */
  public Integer getInputTokens() {
    return inputTokens;
  }

  /**
   * Returns the number of output tokens that were generated.
   *
   * <p>Output tokens represent the length of the AI-generated response content. This count
   * typically includes:
   *
   * <ul>
   *   <li>Response text tokens
   *   <li>Formatting tokens in the response
   *   <li>Special end-of-sequence tokens
   * </ul>
   *
   * <p>Combined with input tokens, this provides the total token usage for billing purposes.
   *
   * @return the number of output tokens generated, or null if not provided by the AI provider
   */
  public Integer getOutputTokens() {
    return outputTokens;
  }

  /**
   * Returns the reason why the AI model stopped generating text.
   *
   * <p>The finish reason provides insight into how and why the generation process ended:
   *
   * <ul>
   *   <li><strong>"stop":</strong> Natural completion - the model finished its response
   *   <li><strong>"length":</strong> Hit maximum token limit before natural completion
   *   <li><strong>"content_filter":</strong> Response blocked by content safety systems
   *   <li><strong>"error":</strong> Generation failed due to an internal error
   *   <li><strong>"timeout":</strong> Request exceeded time limits
   * </ul>
   *
   * <p>Different providers may use different values or additional provider-specific reasons.
   * Applications should handle unknown finish reasons gracefully.
   *
   * @return the finish reason string, or null if not provided by the AI provider
   */
  public String getFinishReason() {
    return finishReason;
  }
}
