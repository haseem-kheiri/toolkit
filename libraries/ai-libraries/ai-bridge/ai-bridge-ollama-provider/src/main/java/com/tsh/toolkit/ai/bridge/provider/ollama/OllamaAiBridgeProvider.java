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

import com.tsh.toolkit.ai.bridge.provider.AiBridgeProvider;
import com.tsh.toolkit.ai.bridge.provider.EndpointSelector;
import com.tsh.toolkit.ai.bridge.provider.autoconfig.OllamaAiBridgeProviderProperties;
import com.tsh.toolkit.ai.bridge.provider.impl.AiRawRequest;
import com.tsh.toolkit.ai.bridge.provider.impl.AiRawResponse;
import com.tsh.toolkit.core.utils.Check;
import java.net.URI;
import org.springframework.web.reactive.function.client.WebClient;

/** AI Bridge provider for Ollama. */
public class OllamaAiBridgeProvider implements AiBridgeProvider {
  private final WebClient webClient = WebClient.builder().build();
  private final EndpointSelector endpointSelector;

  public OllamaAiBridgeProvider(OllamaAiBridgeProviderProperties properties) {
    Check.requireNotNull(properties, () -> "OllamaAiBridgeProviderProperties must not be null");
    this.endpointSelector = new OllamaEndpointSelector(properties.getEndpoints());
  }

  @Override
  public AiRawResponse generate(AiRawRequest request) {
    URI endpoint = endpointSelector.select();
    return null;
  }
}
