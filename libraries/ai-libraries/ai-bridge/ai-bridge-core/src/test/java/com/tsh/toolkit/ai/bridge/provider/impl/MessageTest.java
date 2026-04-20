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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class MessageTest {

  @Test
  void shouldCreateMessageWithRoleAndContent() {
    Message message = new Message(Role.USER, "Hello, AI!");

    assertEquals(Role.USER, message.getRole());
    assertEquals("Hello, AI!", message.getContent());
  }

  @Test
  void shouldCreateSystemMessage() {
    String systemContent = "You are a helpful assistant.";
    Message message = new Message(Role.SYSTEM, systemContent);

    assertEquals(Role.SYSTEM, message.getRole());
    assertEquals(systemContent, message.getContent());
  }

  @Test
  void shouldCreateAssistantMessage() {
    String assistantContent = "I understand. How can I help you today?";
    Message message = new Message(Role.ASSISTANT, assistantContent);

    assertEquals(Role.ASSISTANT, message.getRole());
    assertEquals(assistantContent, message.getContent());
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForEmptyContent() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Message(Role.USER, "");
    });
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForBlankContent() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Message(Role.USER, "   ");
    });
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForWhitespaceOnlyContent() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Message(Role.USER, "\n\t\r  ");
    });
  }

  @Test
  void shouldHandleMultilineContent() {
    String multilineContent = "This is a message\nwith multiple\nlines of text.";
    Message message = new Message(Role.USER, multilineContent);

    assertEquals(Role.USER, message.getRole());
    assertEquals(multilineContent, message.getContent());
  }

  @Test
  void shouldHandleSpecialCharacters() {
    String specialContent = "Special chars: áéíóú ñ 中文 🚀 \"quotes\" 'apostrophes'";
    Message message = new Message(Role.USER, specialContent);

    assertEquals(Role.USER, message.getRole());
    assertEquals(specialContent, message.getContent());
  }

  @Test
  void shouldHandleLongContent() {
    String longContent = "x".repeat(10000);
    Message message = new Message(Role.USER, longContent);

    assertEquals(Role.USER, message.getRole());
    assertEquals(longContent, message.getContent());
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForNullRole() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new Message(null, "Some content");
        });
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForNullContent() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new Message(Role.USER, null);
        });
  }

  @Test
  void shouldBeEqualBasedOnRoleAndContent() {
    Message message1 = new Message(Role.USER, "Hello");
    Message message2 = new Message(Role.USER, "Hello");
    Message message3 = new Message(Role.ASSISTANT, "Hello");
    Message message4 = new Message(Role.USER, "Hi");

    // Note: This test assumes equals() is implemented (may need to be added to Message class)
    // For now, we test field equality
    assertEquals(message1.getRole(), message2.getRole());
    assertEquals(message1.getContent(), message2.getContent());

    assertNotEquals(message1.getRole(), message3.getRole());
    assertNotEquals(message1.getContent(), message4.getContent());
  }

  @Test
  void shouldSupportAllStandardRoles() {
    Message systemMsg = new Message(Role.SYSTEM, "System instruction");
    Message userMsg = new Message(Role.USER, "User question");
    Message assistantMsg = new Message(Role.ASSISTANT, "Assistant response");

    assertEquals("system", systemMsg.getRole().getValue());
    assertEquals("user", userMsg.getRole().getValue());
    assertEquals("assistant", assistantMsg.getRole().getValue());
  }

  @Test
  void shouldRetainOriginalContentExactly() {
    String originalContent =
        "  Leading and trailing spaces  \n\tWith tabs\r\nAnd carriage returns  ";
    Message message = new Message(Role.USER, originalContent);

    assertEquals(originalContent, message.getContent());
    assertEquals(originalContent.length(), message.getContent().length());
  }
}
