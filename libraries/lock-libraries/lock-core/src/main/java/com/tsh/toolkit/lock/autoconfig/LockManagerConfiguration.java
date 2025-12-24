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

package com.tsh.toolkit.lock.autoconfig;

import com.tsh.toolkit.lock.LockProvider;
import com.tsh.toolkit.lock.impl.LockManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring config. */
@Configuration
public class LockManagerConfiguration {

  @ConditionalOnMissingBean
  @Bean(destroyMethod = "stop")
  LockManager lockManager(LockProvider lockProvider) {
    final LockManager lockManager = new LockManager(lockProvider);
    lockManager.start();
    return lockManager;
  }
}
