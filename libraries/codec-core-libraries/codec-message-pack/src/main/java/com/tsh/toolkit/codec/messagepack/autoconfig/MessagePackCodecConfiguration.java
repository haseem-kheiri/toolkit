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

package com.tsh.toolkit.codec.messagepack.autoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsh.toolkit.codec.messagepack.impl.MessagePackCodec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring {@link Configuration @Configuration} class that registers a {@link MessagePackCodec} bean.
 *
 * <p>This configuration integrates the platform’s {@link MessagePackCodec} implementation into the
 * Spring application context, providing high-performance binary serialization and deserialization
 * using the MessagePack format.
 *
 * <p>Once registered, other Spring-managed components can inject the {@link MessagePackCodec}
 * wherever compact, efficient, and schema-flexible serialization is needed — for example, in
 * distributed communication, persistent caching, or cluster metadata replication.
 *
 * <p>Unlike {@link com.platform.codec.json.impl.JsonCodec}, which prioritizes human readability,
 * the {@link MessagePackCodec} focuses on minimizing payload size and serialization overhead while
 * retaining full structural fidelity.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Service
 * public class ExampleService {
 *   private final MessagePackCodec codec;
 *
 *   public ExampleService(MessagePackCodec codec) {
 *     this.codec = codec;
 *   }
 *
 *   public void example() {
 *     byte[] encoded = codec.encode(Map.of("key", "value"));
 *     Map<String, String> decoded =
 *         codec.decode(encoded, new ObjectType<Map<String, String>>() {});
 *   }
 * }
 * }</pre>
 *
 * <p>For interoperability or debugging scenarios where binary formats are less convenient, consider
 * using {@link com.platform.codec.json.impl.JsonCodec} instead.
 */
@Configuration
public class MessagePackCodecConfiguration {

  /**
   * Defines and registers a {@link MessagePackCodec} bean for use across the Spring context.
   *
   * <p>The {@link MessagePackCodec} internally configures a dedicated Jackson {@link ObjectMapper}
   * backed by a {@link org.msgpack.jackson.dataformat.MessagePackFactory}, ensuring MessagePack
   * serialization behavior remains isolated from other JSON-based mappers in the system.
   *
   * <p>This setup allows consistent and efficient binary encoding without affecting any
   * application-wide JSON configuration or Spring Boot’s default Jackson settings.
   *
   * @return a fully initialized {@link MessagePackCodec} instance
   */
  @ConditionalOnMissingBean
  @Bean
  MessagePackCodec messagePackCodec() {
    return new MessagePackCodec();
  }
}
