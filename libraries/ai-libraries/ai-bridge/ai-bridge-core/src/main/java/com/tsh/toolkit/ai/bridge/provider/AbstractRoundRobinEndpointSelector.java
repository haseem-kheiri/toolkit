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

package com.tsh.toolkit.ai.bridge.provider;

import com.tsh.toolkit.core.utils.Check;
import java.net.URI;
import java.util.List;

/**
 * Abstract base class for endpoint selectors that use round-robin selection strategy.
 *
 * <p>This class provides thread-safe round-robin selection logic that can be reused by different AI
 * provider implementations. It ensures fair distribution of requests across all configured
 * endpoints.
 *
 * <p>The round-robin implementation handles integer overflow gracefully and maintains thread safety
 * using synchronized blocks.
 *
 * @since 0.0.1-SNAPSHOT
 * @author Haseem Kheiri
 */
public abstract class AbstractRoundRobinEndpointSelector implements EndpointSelector {

  private final List<URI> endpoints;
  private int counter;

  /**
   * Creates a new round-robin endpoint selector.
   *
   * @param endpoints the list of endpoints to select from; must not be null or empty
   * @throws IllegalArgumentException if endpoints is null or empty
   */
  protected AbstractRoundRobinEndpointSelector(List<URI> endpoints) {
    this.endpoints = Check.requireNotEmpty(endpoints, () -> "Endpoints list must not be empty");
    this.counter = 0;
  }

  /**
   * Selects the next endpoint using round-robin strategy.
   *
   * <p>This method is thread-safe and ensures that all endpoints are selected in a fair, rotating
   * manner. When the counter overflows, it is reset to 0 to continue the rotation.
   *
   * @return the selected endpoint URI
   */
  @Override
  public final URI select() {
    int index;
    synchronized (this) {
      index = counter % endpoints.size();
      counter++;
      // Handle overflow by resetting to 0
      if (counter < 0) {
        counter = 0;
      }
    }
    return endpoints.get(index);
  }
}
