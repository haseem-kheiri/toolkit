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
 * Represents a single message in a conversation with an AI language model (LLM).
 *
 * <p>This class encapsulates a message that forms part of a conversation thread with an AI model.
 * Each message has a role that defines the context and purpose of the message, along with the
 * actual content of the message.
 *
 * <p><strong>Role Types:</strong>
 *
 * <ul>
 *   <li>{@code Role.SYSTEM} - Provides system-level instructions or context to the AI model
 *   <li>{@code Role.USER} - Represents user input or questions
 *   <li>{@code Role.ASSISTANT} - Represents responses from the AI model
 * </ul>
 *
 * <p><strong>JSON Representation:</strong><br>
 * When serialized for LLM API calls, messages are typically structured as:
 *
 * <pre>{@code
 * {
 *   "messages": [
 *     {
 *       "role": {"value": "system"},
 *       "content": "You are a financial advisor. Output JSON only."
 *     },
 *     {
 *       "role": {"value": "user"},
 *       "content": "Suggest ETFs for $1000 investment."
 *     },
 *     {
 *       "role": {"value": "assistant"},
 *       "content": "[{\"etf_name\":\"VTI\",\"description\":\"...\",\"reason\":\"...\"}]"
 *     },
 *     {
 *       "role": {"value": "user"},
 *       "content": "Refine this for lower risk."
 *     }
 *   ]
 * }
 * }</pre>
 *
 * <p><strong>Usage Example:</strong>
 *
 * <pre>{@code
 * // Create a system message to set context
 * Message systemMsg = new Message(Role.SYSTEM, "You are a helpful assistant.");
 *
 * // Create a user message with a question
 * Message userMsg = new Message(Role.USER, "What is the capital of France?");
 *
 * // Create an assistant response
 * Message assistantMsg = new Message(Role.ASSISTANT, "The capital of France is Paris.");
 * }</pre>
 *
 * @since 0.0.1-SNAPSHOT
 * @author Haseem Kheiri
 * @see Role
 */
public final class Message {

  /** The role that defines the context and purpose of this message. */
  private final Role role;

  /** The actual text content of the message. */
  private final String content;

  /**
   * Constructs a new Message with the specified role and content.
   *
   * <p>Use the predefined role instances ({@link Role#SYSTEM}, {@link Role#USER}, 
   * {@link Role#ASSISTANT}) to ensure compatibility with standard AI APIs.
   *
   * @param role the role object that defines the context and purpose of this message
   * @param content the actual text content of the message
   * @throws NullPointerException if role or content is null
   */
  public Message(Role role, String content) {
    this.role = role;
    this.content = content;
  }

  /**
   * Returns the role object of this message.
   *
   * <p>The returned Role object will be one of the predefined instances
   * ({@link Role#SYSTEM}, {@link Role#USER}, {@link Role#ASSISTANT}) or a custom
   * Role instance if one was provided during construction.
   *
   * @return the Role object that defines the context and purpose of this message
   */
  public Role getRole() {
    return role;
  }

  /**
   * Returns the content of this message.
   *
   * @return the actual text content of this message
   */
  public String getContent() {
    return content;
  }
}
