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

package com.tsh.toolkit.ai.bridge.provider.ollama;

import com.tsh.toolkit.ai.bridge.provider.AbstractRoundRobinEndpointSelector;
import java.net.URI;
import java.util.List;

/**
 * Ollama-specific implementation of endpoint selector using round-robin strategy.
 *
 * <p>This selector distributes AI requests across multiple Ollama instances
 * in a fair, round-robin manner. It extends the common round-robin functionality
 * provided by {@link AbstractRoundRobinEndpointSelector}.
 *
 * <p>Example usage:
 * <pre>{@code
 * List<URI> endpoints = List.of(
 *     URI.create("http://ollama1:11434"),
 *     URI.create("http://ollama2:11434"),
 *     URI.create("http://ollama3:11434")
 * );
 * 
 * OllamaEndpointSelector selector = new OllamaEndpointSelector(endpoints);
 * URI endpoint = selector.select(); // Returns endpoints in rotation
 * }</pre>
 *
 * @since 0.0.1-SNAPSHOT
 * @author Haseem Kheiri
 */
public class OllamaEndpointSelector extends AbstractRoundRobinEndpointSelector {

  /**
   * Creates a new Ollama endpoint selector with round-robin strategy.
   *
   * @param endpoints the list of Ollama endpoints to select from; must not be null or empty
   * @throws IllegalArgumentException if endpoints is null or empty
   */
  public OllamaEndpointSelector(List<URI> endpoints) {
    super(endpoints);
  }
}
