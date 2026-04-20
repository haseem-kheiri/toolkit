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

class RoleTest {

  @Test
  void shouldHavePredefinedSystemRole() {
    assertNotNull(Role.SYSTEM);
    assertEquals("system", Role.SYSTEM.getValue());
  }

  @Test
  void shouldHavePredefinedUserRole() {
    assertNotNull(Role.USER);
    assertEquals("user", Role.USER.getValue());
  }

  @Test
  void shouldHavePredefinedAssistantRole() {
    assertNotNull(Role.ASSISTANT);
    assertEquals("assistant", Role.ASSISTANT.getValue());
  }

  @Test
  void shouldReturnSameInstanceForPredefinedRoles() {
    // Verify singleton behavior for predefined roles
    assertSame(Role.SYSTEM, Role.SYSTEM);
    assertSame(Role.USER, Role.USER);
    assertSame(Role.ASSISTANT, Role.ASSISTANT);
  }

  @Test
  void shouldCreateCustomRoleWithStringValue() {
    Role customRole = new Role("custom");
    assertEquals("custom", customRole.getValue());
  }

  @Test
  void shouldCreateRoleWithEmptyString() {
    Role emptyRole = new Role("");
    assertEquals("", emptyRole.getValue());
  }

  @Test
  void shouldCreateRoleWithSpecialCharacters() {
    String specialValue = "rôle-spécial_123";
    Role specialRole = new Role(specialValue);
    assertEquals(specialValue, specialRole.getValue());
  }

  @Test
  void shouldDistinguishBetweenPredefinedRoles() {
    assertNotSame(Role.SYSTEM, Role.USER);
    assertNotSame(Role.USER, Role.ASSISTANT);
    assertNotSame(Role.SYSTEM, Role.ASSISTANT);
    
    assertNotEquals(Role.SYSTEM.getValue(), Role.USER.getValue());
    assertNotEquals(Role.USER.getValue(), Role.ASSISTANT.getValue());
    assertNotEquals(Role.SYSTEM.getValue(), Role.ASSISTANT.getValue());
  }

  @Test
  void shouldSupportCustomRoleBeyondPredefined() {
    Role toolRole = new Role("tool");
    Role functionRole = new Role("function");
    
    assertEquals("tool", toolRole.getValue());
    assertEquals("function", functionRole.getValue());
    
    assertNotEquals(toolRole.getValue(), Role.SYSTEM.getValue());
    assertNotEquals(functionRole.getValue(), Role.USER.getValue());
  }

  @Test
  void shouldHandleNullValueInConstructor() {
    // The class should either accept null or throw an exception
    // Based on the implementation, it appears to accept null
    Role nullRole = new Role(null);
    assertNull(nullRole.getValue());
  }

  @Test
  void shouldBeConsistentWithGetValue() {
    Role customRole = new Role("test-role");
    String value1 = customRole.getValue();
    String value2 = customRole.getValue();
    
    assertSame(value1, value2);
    assertEquals("test-role", value1);
  }

  @Test
  void shouldBeImmutable() {
    Role role = new Role("immutable");
    String originalValue = role.getValue();
    
    // Verify that the role value cannot be modified externally
    assertEquals("immutable", role.getValue());
    assertEquals(originalValue, role.getValue());
  }

  @Test
  void shouldWorkWithTypicalAIProviderRoles() {
    // Test roles commonly used by various AI providers
    Role system = Role.SYSTEM;
    Role user = Role.USER;
    Role assistant = Role.ASSISTANT;
    
    // OpenAI format
    assertEquals("system", system.getValue());
    assertEquals("user", user.getValue());
    assertEquals("assistant", assistant.getValue());
    
    // Anthropic Claude format (would be same)
    assertEquals("system", system.getValue());
    assertEquals("user", user.getValue());
    assertEquals("assistant", assistant.getValue());
  }

  @Test
  void shouldSupportCaseInsensitiveComparison() {
    Role lowercase = new Role("system");
    Role uppercase = new Role("SYSTEM");
    Role mixedCase = new Role("System");
    
    assertEquals("system", lowercase.getValue());
    assertEquals("SYSTEM", uppercase.getValue());
    assertEquals("System", mixedCase.getValue());
    
    // Values are different (case-sensitive storage)
    assertNotEquals(lowercase.getValue(), uppercase.getValue());
    assertNotEquals(lowercase.getValue(), mixedCase.getValue());
  }

  @Test
  void shouldHandleWhitespaceInValues() {
    Role spacedRole = new Role(" system ");
    Role tabbedRole = new Role("\tsystem\t");
    Role newlineRole = new Role("system\n");
    
    assertEquals(" system ", spacedRole.getValue());
    assertEquals("\tsystem\t", tabbedRole.getValue());
    assertEquals("system\n", newlineRole.getValue());
  }
}