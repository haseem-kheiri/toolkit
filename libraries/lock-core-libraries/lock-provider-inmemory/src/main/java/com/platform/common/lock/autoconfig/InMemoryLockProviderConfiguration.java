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

package com.platform.common.lock.autoconfig;

import com.platform.common.lock.provider.impl.InMemoryLockProvider;
import com.platform.common.lock.provider.impl.InMemoryLockProviderProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring configuration for in-memory lock provider. */
@Configuration
public class InMemoryLockProviderConfiguration {
  @ConditionalOnMissingBean
  @ConfigurationProperties("com.platform.lock.provider.inmemory")
  @Bean
  InMemoryLockProviderProperties inMemoryLockProviderProperties() {
    return new InMemoryLockProviderProperties();
  }

  @ConditionalOnMissingBean
  @Bean(destroyMethod = "stop")
  InMemoryLockProvider inMemoryLockProvider(InMemoryLockProviderProperties properties) {
    final InMemoryLockProvider provider = new InMemoryLockProvider(properties);
    provider.start();
    return provider;
  }
}
