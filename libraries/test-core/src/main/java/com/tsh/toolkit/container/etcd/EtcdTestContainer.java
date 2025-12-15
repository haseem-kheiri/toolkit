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

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * A Testcontainers wrapper for running an etcd instance in integration tests.
 *
 * <p>This class extends {@link GenericContainer} to provide a lightweight etcd cluster for testing
 * purposes. It exposes the etcd client port (2379) and configures the container to start a
 * single-node etcd instance with a temporary data directory.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * EtcdTestContainer etcd =
 *     new EtcdTestContainer(DockerImageName.parse("quay.io/coreos/etcd:v3.5.13"));
 * etcd.start();
 *
 * String endpoint = etcd.getEndpoint();
 * Client client = Client.builder().endpoints(endpoint).build();
 * }</pre>
 *
 * <p>Additional utility methods are provided to get the client port, restart the container, and
 * customize the etcd data directory for tests.
 */
public class EtcdTestContainer extends GenericContainer<EtcdTestContainer> {

  /** Default client port for etcd. */
  private static final int CLIENT_PORT = 2379;

  /** Default data directory inside the container. */
  private static final String DEFAULT_DATA_DIR = "/etcd-data";

  /**
   * Creates a new etcd Testcontainer instance using the specified Docker image.
   *
   * <p>This constructor configures the container to:
   *
   * <ul>
   *   <li>Expose the client port {@code 2379} for connections from tests
   *   <li>Advertise and listen on client URLs {@code http://0.0.0.0:2379}
   *   <li>Use a temporary data directory inside the container ({@code /etcd-data})
   * </ul>
   *
   * @param dockerImageName the Docker image for etcd, e.g., {@code quay.io/coreos/etcd:v3.5.13}
   */
  public EtcdTestContainer(DockerImageName dockerImageName) {
    super(dockerImageName);
    withExposedPorts(CLIENT_PORT);
    withCommand(
        "etcd",
        "--advertise-client-urls",
        "http://0.0.0.0:" + CLIENT_PORT,
        "--listen-client-urls",
        "http://0.0.0.0:" + CLIENT_PORT,
        "--data-dir",
        DEFAULT_DATA_DIR);
  }

  /**
   * Returns the HTTP endpoint for connecting to the etcd client API.
   *
   * @return a string in the format {@code http://host:port}, where {@code host} and {@code port}
   *     are mapped to the container.
   */
  public String getEndpoint() {
    return String.format("http://%s:%d", getHost(), getMappedPort(CLIENT_PORT));
  }

  /**
   * Returns the mapped client port of the etcd container.
   *
   * @return the mapped client port
   */
  public int getClientPort() {
    return getMappedPort(CLIENT_PORT);
  }

  /** Restarts the etcd container. Useful for integration tests that need a fresh etcd instance. */
  public void restartContainer() {
    stop();
    start();
  }

  /**
   * Allows customizing the data directory inside the container.
   *
   * @param dataDir the directory path to use for etcd data
   * @return the container instance for fluent configuration
   */
  public EtcdTestContainer withDataDir(String dataDir) {
    withCommand(
        "etcd",
        "--advertise-client-urls",
        "http://0.0.0.0:" + CLIENT_PORT,
        "--listen-client-urls",
        "http://0.0.0.0:" + CLIENT_PORT,
        "--data-dir",
        dataDir);
    return this;
  }
}
