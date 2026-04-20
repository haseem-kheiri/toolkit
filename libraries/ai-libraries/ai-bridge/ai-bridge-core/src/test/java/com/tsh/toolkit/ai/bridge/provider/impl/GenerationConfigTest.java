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

class GenerationConfigTest {

  @Test
  void shouldCreateConfigWithAllParameters() {
    GenerationConfig config = new GenerationConfig(0.7, 0.9, 500);
    
    assertEquals(0.7, config.getTemperature());
    assertEquals(0.9, config.getTopP());
    assertEquals(500, config.getMaxTokens());
  }

  @Test
  void shouldCreateConfigWithNullParameters() {
    GenerationConfig config = new GenerationConfig(null, null, null);
    
    assertNull(config.getTemperature());
    assertNull(config.getTopP());
    assertNull(config.getMaxTokens());
  }

  @Test
  void shouldCreateConfigWithMixedParameters() {
    GenerationConfig config = new GenerationConfig(0.5, null, 1000);
    
    assertEquals(0.5, config.getTemperature());
    assertNull(config.getTopP());
    assertEquals(1000, config.getMaxTokens());
  }

  @Test
  void shouldSupportMinimumTemperature() {
    GenerationConfig config = new GenerationConfig(0.0, 1.0, 100);
    assertEquals(0.0, config.getTemperature());
  }

  @Test
  void shouldSupportMaximumTemperature() {
    GenerationConfig config = new GenerationConfig(2.0, 1.0, 100);
    assertEquals(2.0, config.getTemperature());
  }

  @Test
  void shouldSupportTypicalTemperatureValues() {
    // Test common temperature ranges
    GenerationConfig deterministic = new GenerationConfig(0.1, null, null);
    GenerationConfig balanced = new GenerationConfig(0.7, null, null);
    GenerationConfig creative = new GenerationConfig(1.2, null, null);
    
    assertEquals(0.1, deterministic.getTemperature());
    assertEquals(0.7, balanced.getTemperature());
    assertEquals(1.2, creative.getTemperature());
  }

  @Test
  void shouldSupportMinimumTopP() {
    GenerationConfig config = new GenerationConfig(null, 0.0, null);
    assertEquals(0.0, config.getTopP());
  }

  @Test
  void shouldSupportMaximumTopP() {
    GenerationConfig config = new GenerationConfig(null, 1.0, null);
    assertEquals(1.0, config.getTopP());
  }

  @Test
  void shouldSupportTypicalTopPValues() {
    GenerationConfig focused = new GenerationConfig(null, 0.1, null);
    GenerationConfig moderate = new GenerationConfig(null, 0.9, null);
    GenerationConfig full = new GenerationConfig(null, 1.0, null);
    
    assertEquals(0.1, focused.getTopP());
    assertEquals(0.9, moderate.getTopP());
    assertEquals(1.0, full.getTopP());
  }

  @Test
  void shouldSupportVariousMaxTokenValues() {
    GenerationConfig shortResponse = new GenerationConfig(null, null, 50);
    GenerationConfig mediumResponse = new GenerationConfig(null, null, 500);
    GenerationConfig longResponse = new GenerationConfig(null, null, 2000);
    
    assertEquals(50, shortResponse.getMaxTokens());
    assertEquals(500, mediumResponse.getMaxTokens());
    assertEquals(2000, longResponse.getMaxTokens());
  }

  @Test
  void shouldSupportMinimumMaxTokens() {
    GenerationConfig config = new GenerationConfig(null, null, 1);
    assertEquals(1, config.getMaxTokens());
  }

  @Test
  void shouldHandleLargeMaxTokens() {
    GenerationConfig config = new GenerationConfig(null, null, 100000);
    assertEquals(100000, config.getMaxTokens());
  }

  @Test
  void shouldRetainPrecisionForFloatingPointValues() {
    double preciseTemp = 0.123456789;
    double preciseTopP = 0.987654321;
    
    GenerationConfig config = new GenerationConfig(preciseTemp, preciseTopP, null);
    
    assertEquals(preciseTemp, config.getTemperature(), 0.0);
    assertEquals(preciseTopP, config.getTopP(), 0.0);
  }

  @Test
  void shouldSupportNegativeTemperature() {
    // Some providers might accept negative temperatures
    GenerationConfig config = new GenerationConfig(-0.5, null, null);
    assertEquals(-0.5, config.getTemperature());
  }

  @Test
  void shouldSupportTemperatureAboveTwo() {
    // Some providers might accept temperatures above 2.0
    GenerationConfig config = new GenerationConfig(3.0, null, null);
    assertEquals(3.0, config.getTemperature());
  }

  @Test
  void shouldSupportTopPAboveOne() {
    // Edge case: some implementations might accept values above 1.0
    GenerationConfig config = new GenerationConfig(null, 1.5, null);
    assertEquals(1.5, config.getTopP());
  }

  @Test
  void shouldSupportNegativeTopP() {
    // Edge case: negative topP (invalid but should be stored)
    GenerationConfig config = new GenerationConfig(null, -0.1, null);
    assertEquals(-0.1, config.getTopP());
  }

  @Test
  void shouldSupportZeroMaxTokens() {
    GenerationConfig config = new GenerationConfig(null, null, 0);
    assertEquals(0, config.getMaxTokens());
  }

  @Test
  void shouldSupportNegativeMaxTokens() {
    // Invalid but should be stored as-is
    GenerationConfig config = new GenerationConfig(null, null, -100);
    assertEquals(-100, config.getMaxTokens());
  }

  @Test
  void shouldBeImmutableAfterConstruction() {
    GenerationConfig config = new GenerationConfig(0.8, 0.95, 750);
    
    Double originalTemp = config.getTemperature();
    Double originalTopP = config.getTopP();
    Integer originalMaxTokens = config.getMaxTokens();
    
    // Verify values don't change
    assertEquals(originalTemp, config.getTemperature());
    assertEquals(originalTopP, config.getTopP());
    assertEquals(originalMaxTokens, config.getMaxTokens());
  }

  @Test
  void shouldCreateConfigForDeterministicGeneration() {
    GenerationConfig deterministicConfig = new GenerationConfig(0.0, 0.1, 200);
    
    assertEquals(0.0, deterministicConfig.getTemperature());
    assertEquals(0.1, deterministicConfig.getTopP());
    assertEquals(200, deterministicConfig.getMaxTokens());
  }

  @Test
  void shouldCreateConfigForCreativeGeneration() {
    GenerationConfig creativeConfig = new GenerationConfig(1.5, 1.0, 1000);
    
    assertEquals(1.5, creativeConfig.getTemperature());
    assertEquals(1.0, creativeConfig.getTopP());
    assertEquals(1000, creativeConfig.getMaxTokens());
  }

  @Test
  void shouldCreateConfigForProviderDefaults() {
    GenerationConfig defaultConfig = new GenerationConfig(null, null, null);
    
    assertNull(defaultConfig.getTemperature());
    assertNull(defaultConfig.getTopP());
    assertNull(defaultConfig.getMaxTokens());
  }

  @Test
  void shouldSupportBoxedIntegerAndDoubleTypes() {
    Double temp = Double.valueOf(0.6);
    Double topP = Double.valueOf(0.8);
    Integer maxTokens = Integer.valueOf(300);
    
    GenerationConfig config = new GenerationConfig(temp, topP, maxTokens);
    
    assertEquals(temp, config.getTemperature());
    assertEquals(topP, config.getTopP());
    assertEquals(maxTokens, config.getMaxTokens());
  }
}