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

import static org.junit.jupiter.api.Assertions.*;

import com.tsh.toolkit.ai.bridge.provider.impl.AiRawRequest;
import com.tsh.toolkit.ai.bridge.provider.impl.AiRawResponse;
import com.tsh.toolkit.ai.bridge.provider.impl.GenerationConfig;
import com.tsh.toolkit.ai.bridge.provider.impl.Message;
import com.tsh.toolkit.ai.bridge.provider.impl.Role;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiBridgeRouterTest {

  @Test
  void shouldResolveCorrectProviderByRouteKey() {
    TestAiBridgeProvider provider1 = new TestAiBridgeProvider("Ollama Response");
    TestAiBridgeProvider provider2 = new TestAiBridgeProvider("OpenAI Response");

    // Create a simple implementation for testing
    TestAiBridgeRouter router = new TestAiBridgeRouter();
    router.addProvider("ollama", provider1);
    router.addProvider("openai", provider2);

    AiBridgeProvider resolvedProvider1 = router.resolve("ollama");
    AiBridgeProvider resolvedProvider2 = router.resolve("openai");

    assertEquals(provider1, resolvedProvider1);
    assertEquals(provider2, resolvedProvider2);
  }

  @Test
  void shouldReturnNullForUnknownRouteKey() {
    TestAiBridgeProvider provider1 = new TestAiBridgeProvider("Test Response");
    TestAiBridgeRouter router = new TestAiBridgeRouter();
    router.addProvider("ollama", provider1);

    AiBridgeProvider unknownProvider = router.resolve("unknown-provider");

    assertNull(unknownProvider);
  }

  @Test
  void shouldHandleNullRouteKey() {
    TestAiBridgeProvider provider1 = new TestAiBridgeProvider("Test Response");
    TestAiBridgeRouter router = new TestAiBridgeRouter();
    router.addProvider("ollama", provider1);

    AiBridgeProvider provider = router.resolve(null);

    assertNull(provider);
  }

  @Test
  void shouldHandleEmptyRouteKey() {
    TestAiBridgeProvider provider1 = new TestAiBridgeProvider("Test Response");
    TestAiBridgeRouter router = new TestAiBridgeRouter();
    router.addProvider("ollama", provider1);

    AiBridgeProvider provider = router.resolve("");

    assertNull(provider);
  }

  @Test
  void shouldSupportCaseSensitiveRouting() {
    TestAiBridgeProvider provider1 = new TestAiBridgeProvider("Lowercase Response");
    TestAiBridgeProvider provider2 = new TestAiBridgeProvider("Uppercase Response");
    TestAiBridgeRouter router = new TestAiBridgeRouter();
    router.addProvider("ollama", provider1);
    router.addProvider("OLLAMA", provider2);

    AiBridgeProvider lowercaseProvider = router.resolve("ollama");
    AiBridgeProvider uppercaseProvider = router.resolve("OLLAMA");

    assertEquals(provider1, lowercaseProvider);
    assertEquals(provider2, uppercaseProvider);
    assertNotEquals(lowercaseProvider, uppercaseProvider);
  }

  @Test
  void shouldAllowProviderOverride() {
    TestAiBridgeProvider provider1 = new TestAiBridgeProvider("Original Response");
    TestAiBridgeProvider provider2 = new TestAiBridgeProvider("Override Response");
    TestAiBridgeRouter router = new TestAiBridgeRouter();
    router.addProvider("ollama", provider1);

    // Override with new provider
    router.addProvider("ollama", provider2);

    AiBridgeProvider provider = router.resolve("ollama");

    assertEquals(provider2, provider);
  }

  @Test
  void shouldSupportMultipleProviders() {
    TestAiBridgeProvider provider1 = new TestAiBridgeProvider("Provider 1");
    TestAiBridgeProvider provider2 = new TestAiBridgeProvider("Provider 2");
    TestAiBridgeRouter router = new TestAiBridgeRouter();
    router.addProvider("ollama", provider1);
    router.addProvider("openai", provider2);

    assertNotNull(router.resolve("ollama"));
    assertNotNull(router.resolve("openai"));
    assertNotSame(router.resolve("ollama"), router.resolve("openai"));
  }

  @Test
  void shouldWorkWithComplexRouteKeys() {
    TestAiBridgeProvider provider1 = new TestAiBridgeProvider("Llama Response");
    TestAiBridgeProvider provider2 = new TestAiBridgeProvider("GPT Response");
    TestAiBridgeRouter router = new TestAiBridgeRouter();
    router.addProvider("ollama:llama2:7b", provider1);
    router.addProvider("openai:gpt-4", provider2);

    assertEquals(provider1, router.resolve("ollama:llama2:7b"));
    assertEquals(provider2, router.resolve("openai:gpt-4"));
  }

  @Test
  void shouldHandleSpecialCharactersInRouteKey() {
    TestAiBridgeProvider provider1 = new TestAiBridgeProvider("Provider 1");
    TestAiBridgeProvider provider2 = new TestAiBridgeProvider("Provider 2");
    TestAiBridgeRouter router = new TestAiBridgeRouter();
    router.addProvider("provider-1_v2.0", provider1);
    router.addProvider("provider@company.com", provider2);

    assertEquals(provider1, router.resolve("provider-1_v2.0"));
    assertEquals(provider2, router.resolve("provider@company.com"));
  }

  @Test
  void shouldWorkInRoutingScenario() {
    TestAiBridgeProvider ollamaProvider = new TestAiBridgeProvider("Response from Ollama");
    TestAiBridgeProvider openaiProvider = new TestAiBridgeProvider("Response from OpenAI");

    TestAiBridgeRouter router = new TestAiBridgeRouter();
    router.addProvider("ollama", ollamaProvider);
    router.addProvider("openai", openaiProvider);

    // Create a test request
    AiRawRequest request = new AiRawRequest(
        Arrays.asList(new Message(Role.USER, "Hello")),
        new GenerationConfig(0.7, null, 100),
        null
    );

    // Route to different providers
    AiBridgeProvider resolvedOllamaProvider = router.resolve("ollama");
    AiBridgeProvider resolvedOpenaiProvider = router.resolve("openai");

    AiRawResponse ollamaResponse = resolvedOllamaProvider.generate(request);
    AiRawResponse openaiResponse = resolvedOpenaiProvider.generate(request);

    assertEquals("Response from Ollama", ollamaResponse.getContent());
    assertEquals("Response from OpenAI", openaiResponse.getContent());
  }

  // Simple test implementation of AiBridgeRouter for testing
  private static class TestAiBridgeRouter implements AiBridgeRouter {
    private final Map<String, AiBridgeProvider> providers = new HashMap<>();

    public void addProvider(String routeKey, AiBridgeProvider provider) {
      providers.put(routeKey, provider);
    }

    @Override
    public AiBridgeProvider resolve(String routeKey) {
      if (routeKey == null || routeKey.isEmpty()) {
        return null;
      }
      return providers.get(routeKey);
    }
  }

  // Simple test implementation of AiBridgeProvider for testing
  private static class TestAiBridgeProvider implements AiBridgeProvider {
    private final String responseContent;

    public TestAiBridgeProvider(String responseContent) {
      this.responseContent = responseContent;
    }

    @Override
    public AiRawResponse generate(AiRawRequest request) {
      return new AiRawResponse(responseContent, null);
    }
  }
}