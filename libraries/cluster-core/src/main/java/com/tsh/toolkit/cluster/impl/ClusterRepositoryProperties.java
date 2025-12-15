/*
 * Copyright 2020–2025 Haseem Kheiri
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

package com.tsh.toolkit.cluster.impl;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties controlling lease behavior for the {@code ClusterRepository}.
 *
 * <p>These parameters adjust how often new leases are created and how much extra time is added to
 * each lease to account for timing variations across nodes.
 */
@Getter
@Setter
public class ClusterRepositoryProperties {

  /**
   * Multiplier that determines how long a single lease bucket remains valid before a new lease is
   * created.
   */
  private int leaseBucketSpanFactor = 5;

  /**
   * Multiplier applied to the lease TTL to pad its duration and tolerate clock drift or scheduling
   * delays.
   */
  private int leaseTtlPaddingFactor = 7;
}
