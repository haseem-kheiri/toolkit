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
 * Configuration parameters that control AI language model text generation behavior.
 *
 * <p>This class encapsulates the key parameters that influence how AI models generate responses,
 * providing fine-grained control over creativity, diversity, and output length. These parameters
 * are commonly used across different AI providers with consistent semantics.
 *
 * <p><strong>Key Parameters:</strong>
 *
 * <ul>
 *   <li><strong>Temperature (0.0-2.0):</strong> Controls randomness and creativity in responses
 *   <li><strong>Top-P (0.0-1.0):</strong> Controls diversity through nucleus sampling
 *   <li><strong>Max Tokens:</strong> Limits the maximum length of generated responses
 * </ul>
 *
 * <p><strong>Parameter Guidelines:</strong>
 *
 * <ul>
 *   <li><strong>Low Temperature (0.0-0.3):</strong> Deterministic, focused, factual responses
 *   <li><strong>Medium Temperature (0.4-0.7):</strong> Balanced creativity and coherence
 *   <li><strong>High Temperature (0.8-2.0):</strong> Creative, varied, but potentially inconsistent
 *   <li><strong>Top-P 1.0:</strong> Consider all tokens (full vocabulary)
 *   <li><strong>Top-P 0.1:</strong> Consider only top 10% most likely tokens
 * </ul>
 *
 * <p><strong>JSON Representation:</strong><br>
 * When serialized as part of an AI request, the configuration appears as:
 *
 * <pre>{@code
 * {
 *   "config": {
 *     "temperature": 0.0,
 *     "topP": 1.0,
 *     "maxTokens": 300
 *   }
 * }
 * }</pre>
 *
 * <p><strong>Usage Examples:</strong>
 *
 * <pre>{@code
 * // Conservative configuration for factual responses
 * GenerationConfig factual = new GenerationConfig(0.1, 0.9, 150);
 *
 * // Balanced configuration for general conversation
 * GenerationConfig balanced = new GenerationConfig(0.7, 1.0, 500);
 *
 * // Creative configuration for storytelling
 * GenerationConfig creative = new GenerationConfig(1.2, 0.95, 1000);
 *
 * // Deterministic configuration for structured data
 * GenerationConfig structured = new GenerationConfig(0.0, 1.0, 300);
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong> This class is immutable and thread-safe. All fields are final
 * and primitive wrapper types are used to allow null values when specific parameters should use
 * provider defaults.
 *
 * @see AiRawRequest
 */
public final class GenerationConfig {

  /**
   * Controls the randomness and creativity of the AI model's responses.
   *
   * <p>Lower values (0.0-0.3) produce more deterministic and focused outputs, while higher values
   * (0.8-2.0) increase creativity but may reduce coherence. A value of 0.0 makes the model
   * deterministic, always choosing the most likely token.
   *
   * <p><strong>Range:</strong> 0.0 to 2.0 (some providers may allow higher values) <br>
   * <strong>Default:</strong> Provider-specific (typically 0.7-1.0) <br>
   * <strong>Null handling:</strong> Uses provider default when null
   */
  private final Double temperature;

  /**
   * Controls diversity through nucleus sampling, limiting token selection to the top percentile.
   *
   * <p>This parameter implements nucleus sampling where only the most probable tokens that comprise
   * the top P% of probability mass are considered. Lower values increase focus and consistency,
   * while higher values allow more diverse outputs.
   *
   * <p><strong>Range:</strong> 0.0 to 1.0 <br>
   * <strong>Common values:</strong> 0.9 (focused), 1.0 (full vocabulary) <br>
   * <strong>Default:</strong> Provider-specific (typically 1.0) <br>
   * <strong>Null handling:</strong> Uses provider default when null
   */
  private final Double topP;

  /**
   * Maximum number of tokens the AI model should generate in its response.
   *
   * <p>This parameter limits the length of the generated text to prevent overly verbose responses
   * and control API costs. The actual response may be shorter if the model naturally concludes its
   * response before reaching this limit.
   *
   * <p><strong>Range:</strong> 1 to provider maximum (varies by model and provider) <br>
   * <strong>Common values:</strong> 150-500 for brief responses, 1000+ for detailed content <br>
   * <strong>Note:</strong> Includes both input and output tokens for some providers <br>
   * <strong>Null handling:</strong> Uses provider default when null
   */
  private final Integer maxTokens;

  /**
   * Constructs a new GenerationConfig with the specified parameters for controlling AI text
   * generation.
   *
   * <p>This constructor accepts null values for any parameter, which will cause the AI provider to
   * use its default value for that parameter. This allows for flexible configuration where only
   * specific parameters need to be customized.
   *
   * <p><strong>Parameter Combinations:</strong>
   *
   * <ul>
   *   <li>All parameters specified: Full control over generation behavior
   *   <li>Some parameters null: Uses provider defaults for null parameters
   *   <li>All parameters null: Uses all provider defaults (equivalent to no configuration)
   * </ul>
   *
   * @param temperature controls randomness (0.0-2.0); null uses provider default
   * @param topP controls diversity via nucleus sampling (0.0-1.0); null uses provider default
   * @param maxTokens limits response length (1+); null uses provider default
   * @see #getTemperature()
   * @see #getTopP()
   * @see #getMaxTokens()
   */
  public GenerationConfig(Double temperature, Double topP, Integer maxTokens) {
    this.temperature = temperature;
    this.topP = topP;
    this.maxTokens = maxTokens;
  }

  /**
   * Returns the temperature parameter that controls randomness and creativity.
   *
   * <p>The temperature parameter influences the model's token selection process:
   *
   * <ul>
   *   <li><strong>0.0:</strong> Deterministic output (always selects most probable token)
   *   <li><strong>0.1-0.3:</strong> Very focused, factual responses
   *   <li><strong>0.4-0.7:</strong> Balanced creativity and coherence
   *   <li><strong>0.8-1.0:</strong> More creative and varied responses
   *   <li><strong>1.0+:</strong> Highly creative but potentially inconsistent
   * </ul>
   *
   * @return the temperature value (0.0-2.0), or null if provider default should be used
   */
  public Double getTemperature() {
    return temperature;
  }

  /**
   * Returns the top-P parameter that controls diversity through nucleus sampling.
   *
   * <p>The top-P (nucleus sampling) parameter limits token selection to the most probable
   * candidates that comprise the specified percentage of the cumulative probability mass:
   *
   * <ul>
   *   <li><strong>0.1:</strong> Very focused (top 10% of probability mass)
   *   <li><strong>0.5:</strong> Moderately focused (top 50% of probability mass)
   *   <li><strong>0.9:</strong> Diverse but coherent (top 90% of probability mass)
   *   <li><strong>1.0:</strong> Full vocabulary available (no filtering)
   * </ul>
   *
   * @return the top-P value (0.0-1.0), or null if provider default should be used
   */
  public Double getTopP() {
    return topP;
  }

  /**
   * Returns the maximum number of tokens the model should generate.
   *
   * <p>This parameter controls the maximum length of the AI response and can help:
   *
   * <ul>
   *   <li>Prevent overly verbose responses
   *   <li>Control API costs (many providers charge per token)
   *   <li>Ensure responses fit within application constraints
   *   <li>Maintain consistent response lengths across requests
   * </ul>
   *
   * <p><strong>Note:</strong> The actual response may be shorter if the model naturally concludes
   * its response before reaching this limit.
   *
   * @return the maximum token count (1+), or null if provider default should be used
   */
  public Integer getMaxTokens() {
    return maxTokens;
  }
}
