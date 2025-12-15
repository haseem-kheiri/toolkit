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

package com.tsh.toolkit.codec.json.autoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsh.toolkit.codec.json.impl.JsonCodec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring {@link Configuration @Configuration} class that registers a {@link JsonCodec} bean.
 *
 * <p>This configuration makes the platform's JSON-based {@link JsonCodec} available as a
 * Spring-managed bean, enabling consistent serialization and deserialization of structured data
 * across the application.
 *
 * <p>The codec internally uses Jackson's {@link com.fasterxml.jackson.databind.ObjectMapper} to
 * convert between Java objects and JSON byte arrays. This allows for flexible and
 * standards-compliant data exchange suitable for configuration persistence, message encoding, or
 * cluster metadata synchronization.
 *
 * <p>By exposing the {@code JsonCodec} as a Spring bean, it can be injected wherever serialization
 * support is required, promoting reusability and consistency across modules.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Service
 * public class ExampleService {
 *   private final JsonCodec codec;
 *
 *   public ExampleService(JsonCodec codec) {
 *     this.codec = codec;
 *   }
 *
 *   public void example() {
 *     byte[] json = codec.encode(Map.of("key", "value"));
 *     Map<String, String> map =
 *         codec.decode(json, new ObjectType<Map<String, String>>() {});
 *   }
 * }
 * }</pre>
 *
 * <p>For high-performance or compact binary serialization—such as in cluster communication or
 * replicated state transfer—consider using {@link
 * com.platform.codec.messagepack.impl.MessagePackCodec} instead.
 */
@Configuration
public class JsonCodecConfiguration {

  /**
   * Defines a {@link JsonCodec} bean for JSON serialization and deserialization.
   *
   * <p>If no existing {@code JsonCodec} bean is present, this method creates a new instance backed
   * by a default {@link com.fasterxml.jackson.databind.ObjectMapper}. The mapper uses Jackson’s
   * standard configuration, which can be customized globally through Spring Boot’s <code>
   * spring.jackson.*</code> properties or by defining a user-provided {@link ObjectMapper} bean.
   *
   * @return a fully initialized {@link JsonCodec} instance
   */
  @ConditionalOnMissingBean
  @Bean
  JsonCodec jsonCodec() {
    return new JsonCodec();
  }
}
