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

import com.tsh.toolkit.core.utils.Check;
import java.util.List;
import lombok.Getter;

/**
 * Represents a raw request to an AI language model, encapsulating the complete payload needed to
 * interact with various AI providers.
 *
 * <p>This class serves as a standardized container for AI requests across different providers,
 * containing the conversation messages, generation configuration parameters, and model
 * specification. It abstracts the common structure used by most LLM APIs while allowing
 * provider-specific customizations.
 *
 * <p><strong>Request Components:</strong>
 *
 * <ul>
 *   <li><strong>Messages:</strong> The conversation history including system, user, and assistant
 *       messages
 *   <li><strong>Config:</strong> Generation parameters like temperature, top-p, and token limits
 *   <li><strong>Model:</strong> Provider-specific model identifier (e.g., "mistral:7b", "gpt-4",
 *       "claude-3")
 * </ul>
 *
 * <p><strong>JSON Representation:</strong><br>
 * When serialized for API calls, the request structure typically looks like:
 *
 * <pre>{@code
 * {
 *   "messages": [
 *     {
 *       "role": {"value": "system"},
 *       "content": "You are a financial advisor. Respond strictly in valid JSON."
 *     },
 *     {
 *       "role": {"value": "user"},
 *       "content": "Based on current market trends, which ETFs should I invest $1000 in?"
 *     }
 *   ],
 *   "config": {
 *     "temperature": 0.0,
 *     "topP": 1.0,
 *     "maxTokens": 300
 *   },
 *   "model": "mistral:7b"
 * }
 * }</pre>
 *
 * <p><strong>Usage Example:</strong>
 *
 * <pre>{@code
 * // Create messages for the conversation
 * List<Message> messages = List.of(
 *     new Message(Role.SYSTEM, "You are a helpful assistant."),
 *     new Message(Role.USER, "Explain quantum computing in simple terms.")
 * );
 *
 * // Configure generation parameters
 * GenerationConfig config = new GenerationConfig(0.7, 1.0, 150);
 *
 * // Create the request
 * AiRawRequest request = new AiRawRequest(messages, config, "gpt-4");
 *
 * // Send to AI provider
 * AiResponse response = aiProvider.process(request);
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong> This class is immutable and thread-safe. All fields are final
 * and the messages list should be treated as read-only.
 *
 * @since 0.0.1-SNAPSHOT
 * @author Haseem Kheiri
 * @see Message
 * @see Role
 * @see GenerationConfig
 */
@Getter
public final class AiRawRequest {

  /**
   * The structured conversation messages containing system instructions, user inputs, and AI
   * responses. This list maintains the conversation history and context for the AI model.
   */
  private final List<Message> messages;

  /**
   * Generation configuration parameters that control AI behavior such as creativity (temperature),
   * diversity (top-p), and response length (max tokens).
   */
  private final GenerationConfig config;

  /**
   * Optional provider-specific model identifier (e.g., "mistral:7b", "gpt-4", "claude-3-sonnet").
   * This allows targeting specific models within a provider's catalog.
   */
  private final String model;

  /**
   * Constructs a new AI raw request with the specified conversation messages, configuration, and
   * model.
   *
   * <p>This constructor creates an immutable request object that contains all the necessary
   * components for making an AI API call. The messages represent the conversation history, the
   * config controls generation parameters, and the model specifies which AI model to use.
   *
   * @param messages the structured conversation messages (system, user, assistant); cannot be null
   * @param config the generation configuration parameters controlling AI behavior; cannot be null
   * @param model the provider-specific model identifier (e.g., "mistral:7b", "gpt-4"); may be null
   * @throws IllegalArgumentException if messages or config is null
   */
  public AiRawRequest(List<Message> messages, GenerationConfig config, String model) {
    this.messages = Check.requireNotEmpty(messages, () -> "Messages list cannot be null or empty");
    this.config = Check.requireNotNull(config, () -> "GenerationConfig cannot be null");
    this.model = model;
  }
}
