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

package com.platform.common.lock.provider.impl;

import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;

/** In memory lock provider properties. */
@Getter
@Setter
public class InMemoryLockProviderProperties {
  private long cleanupIntervalDuration = 10L;
  private TimeUnit cleanupIntervalDurationUnit = TimeUnit.SECONDS;
}
