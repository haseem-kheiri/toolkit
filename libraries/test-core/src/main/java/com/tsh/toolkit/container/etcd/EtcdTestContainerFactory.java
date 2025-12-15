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

package com.tsh.toolkit.container.etcd;

import java.util.HashMap;
import java.util.Map;
import org.testcontainers.utility.DockerImageName;

/**
 * Factory class to build {@link EtcdTestContainer} instances for integration tests.
 *
 * <p>This factory provides a fluent API for configuring and creating etcd containers. By default,
 * it uses the official etcd Docker image version {@code quay.io/coreos/etcd:v3.5.13}.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * EtcdTestContainer etcdContainer = new EtcdTestContainerFactory()
 *     .setDockerImageName("quay.io/coreos/etcd:v3.5.13")
 *     .setClientPort(2379)
 *     .setDataDir("/tmp/etcd-data")
 *     .withEnv("ETCD_ENABLE_V2", "true")
 *     .enableReuse(true)
 *     .build();
 * etcdContainer.start();
 * }</pre>
 *
 * <p>This factory allows configuring the etcd container for integration tests in a flexible way.
 *
 * @author Haseem
 */
public class EtcdTestContainerFactory {

  private String dockerImageName = "quay.io/coreos/etcd:v3.5.13";
  private int clientPort = 2379;
  private String dataDir = "/etcd-data";
  private final Map<String, String> envVars = new HashMap<>();
  private boolean reuse = false;

  /**
   * Sets the Docker image name to use for the etcd container.
   *
   * @param dockerImageName Docker image name, e.g., "quay.io/coreos/etcd:v3.5.13"
   * @return this factory instance for fluent chaining
   */
  public EtcdTestContainerFactory setDockerImageName(String dockerImageName) {
    this.dockerImageName = dockerImageName;
    return this;
  }

  /**
   * Overrides the default client port for the etcd container.
   *
   * @param port client port number
   * @return this factory instance for fluent chaining
   */
  public EtcdTestContainerFactory setClientPort(int port) {
    this.clientPort = port;
    return this;
  }

  /**
   * Sets the data directory inside the container.
   *
   * @param dataDir path to store etcd data inside the container
   * @return this factory instance for fluent chaining
   */
  public EtcdTestContainerFactory setDataDir(String dataDir) {
    this.dataDir = dataDir;
    return this;
  }

  /**
   * Adds an environment variable to pass into the container.
   *
   * @param key environment variable name
   * @param value environment variable value
   * @return this factory instance for fluent chaining
   */
  public EtcdTestContainerFactory withEnv(String key, String value) {
    envVars.put(key, value);
    return this;
  }

  /**
   * Enables or disables Testcontainers container reuse across test runs.
   *
   * @param reuse true to enable reuse
   * @return this factory instance for fluent chaining
   */
  public EtcdTestContainerFactory enableReuse(boolean reuse) {
    this.reuse = reuse;
    return this;
  }

  /**
   * Builds a new {@link EtcdTestContainer} instance with the configured settings.
   *
   * @return a configured etcd Testcontainer instance
   */
  @SuppressWarnings("resource")
  public EtcdTestContainer build() {
    EtcdTestContainer container =
        new EtcdTestContainer(DockerImageName.parse(dockerImageName))
            .withExposedPorts(clientPort)
            .withCommand(
                "etcd",
                "--advertise-client-urls",
                "http://0.0.0.0:" + clientPort,
                "--listen-client-urls",
                "http://0.0.0.0:" + clientPort,
                "--data-dir",
                dataDir);

    envVars.forEach(container::withEnv);

    if (reuse) {
      container.withReuse(true);
    }

    return container;
  }
}
