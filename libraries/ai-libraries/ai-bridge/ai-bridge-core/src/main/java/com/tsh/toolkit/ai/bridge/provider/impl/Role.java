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
 * Represents the different types of roles a message can have in an LLM conversation.
 *
 * <p>This class provides predefined role instances for the three standard message types
 * used in AI conversations. Each role instance encapsulates both a type-safe object
 * reference and its corresponding string value for serialization.
 *
 * <p>Each role serves a specific purpose in the conversation flow:
 *
 * <ul>
 *   <li>{@link #SYSTEM} - System messages establish context, behavior, and constraints for the AI
 *   <li>{@link #USER} - User messages contain human input, questions, or requests
 *   <li>{@link #ASSISTANT} - Assistant messages contain AI model responses and outputs
 * </ul>
 *
 * <p><strong>Usage:</strong> Use the static instances ({@code Role.SYSTEM}, 
 * {@code Role.USER}, {@code Role.ASSISTANT}) rather than creating new instances. 
 * These provide both type safety in Java code and the correct object structure
 * for JSON serialization with the value accessible via {@link #value()}.
 *
 * @since 0.0.1-SNAPSHOT
 * @author Haseem Kheiri
 */
public final class Role {

  /** Predefined role instance for system-level instructions that define the AI's behavior. */
  public static final Role SYSTEM = new Role("system");
  /** Predefined role instance for user input, questions, or requests to the AI model. */
  public static final Role USER = new Role("user");
  /** Predefined role instance for AI model responses and generated content. */
  public static final Role ASSISTANT = new Role("assistant");

  private final String value;

  /**
   * Constructs a new Role with the specified string value.
   *
   * <p><strong>Note:</strong> In most cases, you should use the predefined static 
   * instances ({@link #SYSTEM}, {@link #USER}, {@link #ASSISTANT}) rather than 
   * creating new Role objects. This constructor is provided for extensibility 
   * and custom role types if needed.
   *
   * @param value the string representation of the role (e.g., "system", "user", "assistant")
   */
  public Role(String value) {
    this.value = value;
  }

  /**
   * Returns the string value of this role for serialization purposes.
   *
   * <p>This method provides access to the underlying string representation of the role,
   * which is used when serializing messages to JSON for AI API calls. The standard
   * role values are "system", "user", and "assistant".
   *
   * @return the string representation of the role (e.g., "system", "user", "assistant")
   */
  public String value() {
    return value;
  }
}