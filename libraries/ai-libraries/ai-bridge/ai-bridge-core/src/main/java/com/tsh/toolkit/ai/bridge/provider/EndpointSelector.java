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

import java.net.URI;

/**
 * An interface for selecting an endpoint URI from a list of available endpoints. This is used by AI
 * Bridge providers to determine which endpoint to send requests to, enabling load balancing and
 * failover strategies.
 */
public interface EndpointSelector {
  URI select();
}
