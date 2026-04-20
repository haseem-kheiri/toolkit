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

import lombok.Getter;

/**
 * Metadata class encapsulates additional information about the AI response, such as latency, token
 * usage, and finish reason.
 */
@Getter
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
}
