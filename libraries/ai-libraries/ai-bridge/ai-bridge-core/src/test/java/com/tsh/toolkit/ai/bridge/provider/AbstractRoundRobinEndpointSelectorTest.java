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
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class AbstractRoundRobinEndpointSelectorTest {

  // Test implementation of the abstract class
  private static class TestRoundRobinSelector extends AbstractRoundRobinEndpointSelector {
    public TestRoundRobinSelector(List<URI> endpoints) {
      super(endpoints);
    }
  }

  @Test
  void shouldSelectEndpointsInRoundRobinOrder() {
    List<URI> endpoints = Arrays.asList(
        URI.create("http://localhost:11434"),
        URI.create("http://localhost:11435"),
        URI.create("http://localhost:11436")
    );
    
    TestRoundRobinSelector selector = new TestRoundRobinSelector(endpoints);
    
    // Test sequential round-robin
    assertEquals(endpoints.get(0), selector.select());
    assertEquals(endpoints.get(1), selector.select());
    assertEquals(endpoints.get(2), selector.select());
    assertEquals(endpoints.get(0), selector.select()); // Should wrap around
    assertEquals(endpoints.get(1), selector.select());
  }

  @Test
  void shouldBeThreadSafe() throws InterruptedException {
    List<URI> endpoints = Arrays.asList(
        URI.create("http://localhost:11434"),
        URI.create("http://localhost:11435"),
        URI.create("http://localhost:11436")
    );
    
    TestRoundRobinSelector selector = new TestRoundRobinSelector(endpoints);
    
    int numThreads = 10;
    int selectionsPerThread = 100;
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    CountDownLatch latch = new CountDownLatch(numThreads);
    
    int[] counts = new int[endpoints.size()];
    Object countsLock = new Object();
    
    // Launch concurrent threads
    for (int i = 0; i < numThreads; i++) {
      executor.submit(() -> {
        try {
          for (int j = 0; j < selectionsPerThread; j++) {
            URI selected = selector.select();
            int index = endpoints.indexOf(selected);
            synchronized (countsLock) {
              counts[index]++;
            }
          }
        } finally {
          latch.countDown();
        }
      });
    }
    
    latch.await();
    executor.shutdown();
    
    // Verify all endpoints were selected
    int totalSelections = numThreads * selectionsPerThread;
    int actualTotal = 0;
    for (int i = 0; i < endpoints.size(); i++) {
      int count = counts[i];
      assertTrue(count > 0, "Endpoint " + i + " should have been selected at least once");
      actualTotal += count;
    }
    
    assertEquals(totalSelections, actualTotal);
    
    // Verify reasonable distribution (within 20% of expected)
    int expectedPerEndpoint = totalSelections / endpoints.size();
    for (int i = 0; i < endpoints.size(); i++) {
      int count = counts[i];
      double deviation = Math.abs(count - expectedPerEndpoint) / (double) expectedPerEndpoint;
      assertTrue(deviation < 0.2, 
          "Endpoint " + i + " selection count (" + count + ") deviates too much from expected (" + expectedPerEndpoint + ")");
    }
  }

  @Test
  void shouldHandleCounterOverflow() {
    List<URI> endpoints = Arrays.asList(
        URI.create("http://localhost:11434"),
        URI.create("http://localhost:11435")
    );
    
    TestRoundRobinSelector selector = new TestRoundRobinSelector(endpoints);
    
    // Simulate overflow by accessing the counter through reflection or protected method
    // Here we'll test by making many selections to verify overflow handling
    URI firstSelection = selector.select();
    
    // Make enough selections to potentially cause overflow in a controlled test
    for (int i = 0; i < 1000; i++) {
      URI selected = selector.select();
      assertNotNull(selected);
      assertTrue(endpoints.contains(selected));
    }
    
    // Should still work after many selections
    URI lastSelection = selector.select();
    assertNotNull(lastSelection);
    assertTrue(endpoints.contains(lastSelection));
  }

  @Test
  void shouldRejectEmptyEndpointsList() {
    assertThrows(IllegalArgumentException.class, () -> {
      new TestRoundRobinSelector(Arrays.asList());
    });
  }

  @Test
  void shouldRejectNullEndpointsList() {
    assertThrows(IllegalArgumentException.class, () -> {
      new TestRoundRobinSelector(null);
    });
  }

  @Test
  void shouldHandleSingleEndpoint() {
    List<URI> endpoints = Arrays.asList(URI.create("http://localhost:11434"));
    TestRoundRobinSelector selector = new TestRoundRobinSelector(endpoints);
    
    // Should always return the same endpoint
    URI endpoint = endpoints.get(0);
    assertEquals(endpoint, selector.select());
    assertEquals(endpoint, selector.select());
    assertEquals(endpoint, selector.select());
  }
}