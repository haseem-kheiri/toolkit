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

package com.tsh.toolkit.cluster.repository.impl;

import com.tsh.toolkit.cluster.impl.Cluster.ClusterNode;
import com.tsh.toolkit.cluster.impl.Cluster.NodeHeartbeat;
import com.tsh.toolkit.cluster.impl.ClusterNodeState;
import com.tsh.toolkit.cluster.impl.ClusterProperties;
import com.tsh.toolkit.cluster.repository.ClusterRepository;
import com.tsh.toolkit.codec.Codec;
import com.tsh.toolkit.codec.ObjectType;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;

/**
 * {@code EtcdClusterRepository} implements {@link ClusterRepository} using <a
 * href="https://etcd.io/">etcd</a> as the backend for distributed coordination.
 *
 * <p>This repository maintains:
 *
 * <ul>
 *   <li><b>Ephemeral heartbeats</b> — periodically refreshed entries that mark active nodes.
 *   <li><b>Ephemeral node states</b> — transient per-node data automatically removed when leases
 *       expire.
 * </ul>
 *
 * <p>Each stored key is associated with an etcd lease to enable automatic expiry when a node fails
 * or becomes unresponsive. To minimize lease churn, leases are reused across extended time buckets
 * defined by configurable span and padding factors.
 *
 * @param <T> the type of per-node state object stored in etcd
 */
@Slf4j
public class EtcdClusterRepository<T extends ClusterNodeState> implements ClusterRepository<T> {

  private static final String HEARTBEAT_PREFIX = "heartbeat";
  private static final String STATE_PREFIX = "state";

  private final int leaseBucketSpanFactor;
  private final int leaseTtlPaddingFactor;
  private final Client client;
  private final KV kvClient;
  private final Lease leaseClient;
  private final Codec codec;

  /** Active leases grouped by their time bucket to avoid excessive lease creation. */
  private final Map<Long, Long> currentKeys = new ConcurrentHashMap<>();

  private final long stateRefreshIntervalMs;

  /**
   * Constructs a new {@code EtcdClusterRepository} instance.
   *
   * <p>Initializes the etcd client and its key–value and lease sub-clients. The repository uses
   * configuration values from {@link ClusterProperties} to determine lease bucketing and TTL
   * padding behavior, as well as the expected state refresh interval that drives internal timing
   * for persistence and expiry.
   *
   * @param properties the cluster configuration providing repository and timing parameters
   * @param codec the {@link Codec} used to serialize and deserialize stored state objects
   * @param endpoints one or more etcd endpoints (for example, {@code "http://localhost:2379"})
   */
  public EtcdClusterRepository(ClusterProperties properties, Codec codec, String... endpoints) {
    this.leaseBucketSpanFactor = properties.getRepository().getLeaseBucketSpanFactor();
    this.leaseTtlPaddingFactor = properties.getRepository().getLeaseTtlPaddingFactor();
    this.codec = codec;
    this.stateRefreshIntervalMs =
        properties.getStateChangeIntervalUnit().toMillis(properties.getStateChangeInterval());
    this.client = Client.builder().endpoints(endpoints).build();
    this.kvClient = client.getKVClient();
    this.leaseClient = client.getLeaseClient();
  }

  /**
   * Pushes the local node's heartbeat and retrieves all active heartbeats for the cluster.
   *
   * <p>The local heartbeat is stored under a key with a lease so that it expires automatically if
   * the node fails to refresh it within the TTL.
   *
   * @param clusterName the cluster name
   * @param localHeartbeat the local node heartbeat to push
   * @param ttlMs the heartbeat TTL in milliseconds
   * @return the list of all active node heartbeats in the cluster
   */
  @Override
  public List<NodeHeartbeat> pushHeartbeats(
      String clusterName, NodeHeartbeat localHeartbeat, long ttlMs) {

    try {
      if (localHeartbeat != null) {
        ByteSequence nodeKey = nodeHeartbeatKey(localHeartbeat);
        ByteSequence nodeValue = ByteSequence.from(codec.encode(localHeartbeat));
        putWithTtl(lease(ttlMs), nodeKey, nodeValue);
      }

      // Fetch all heartbeats in the cluster
      ByteSequence prefixKey = clusterHeartbeatPrefix(clusterName);
      GetOption getOption = GetOption.builder().withPrefix(prefixKey).build();
      GetResponse response = kvClient.get(prefixKey, getOption).get();

      if (response.getKvs().isEmpty()) {
        log.info("No heartbeats found for cluster: {}", clusterName);
        return List.of();
      }

      return response.getKvs().stream()
          .map(kv -> codec.decode(kv.getValue().getBytes(), new ObjectType<NodeHeartbeat>() {}))
          .toList();

    } catch (Exception e) {
      log.warn("Error pushing heartbeats", e);
      return List.of();
    }
  }

  // --- Helper methods ---

  /** Builds the etcd key for a node heartbeat. */
  private ByteSequence nodeHeartbeatKey(NodeHeartbeat heartbeat) {
    return ByteSequence.from(
        String.format("%s/%s/%s", HEARTBEAT_PREFIX, heartbeat.clusterName(), heartbeat.nodeId())
            .getBytes(StandardCharsets.UTF_8));
  }

  /** Builds the etcd key prefix for all heartbeats in a cluster. */
  private ByteSequence clusterHeartbeatPrefix(String clusterName) {
    return ByteSequence.from(
        (HEARTBEAT_PREFIX + "/" + clusterName + "/").getBytes(StandardCharsets.UTF_8));
  }

  /** Builds the etcd key for a node's state entry. */
  private ByteSequence stateKey(ClusterNode localNode) {
    String keyStr =
        String.format(
            "%s/%s/%s",
            STATE_PREFIX,
            localNode.getIdentity().getClusterName(),
            localNode.getIdentity().getNodeId());
    System.err.println(keyStr);
    return ByteSequence.from(keyStr.getBytes(StandardCharsets.UTF_8));
  }

  /** Builds the etcd key prefix for all state entries in a cluster. */
  private ByteSequence clusterStatePrefix(String clusterName) {
    return ByteSequence.from(
        (STATE_PREFIX + "/" + clusterName + "/").getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Puts a key with the given TTL lease.
   *
   * @param leaseId the lease identifier
   * @param key the etcd key
   * @param value the value to store
   */
  private void putWithTtl(long leaseId, ByteSequence key, ByteSequence value) throws Exception {
    kvClient.put(key, value, PutOption.builder().withLeaseId(leaseId).build()).get();
  }

  /**
   * Creates a new lease with the given TTL.
   *
   * @param ttlMs TTL in milliseconds
   * @return the created lease ID
   */
  private long lease(long ttlMs) throws InterruptedException, ExecutionException {
    long ttlSeconds = Math.max(1, ttlMs / 1000);
    return leaseClient.grant(ttlSeconds).get().getID();
  }

  /**
   * Closes the underlying etcd client and releases resources.
   *
   * <p>Suppresses harmless {@code ClosedClientException} errors that occur when the client is
   * already closed.
   */
  @Override
  public void close() {
    if (client == null) {
      return;
    }

    try {
      client.close();
    } catch (Exception e) {
      if (!(e instanceof io.etcd.jetcd.common.exception.ClosedClientException)) {
        log.warn("Error while closing etcd client", e);
      } else {
        log.debug("Ignoring ClosedClientException during etcd shutdown");
      }
    }
  }

  /**
   * Persists the given node's state with a lease-managed TTL.
   *
   * <p>The lease is reused within a computed time bucket to minimize lease churn and etcd load.
   * Each persisted entry automatically expires if the node stops updating it.
   *
   * @param localNode the cluster node whose state is being persisted
   * @param state the current node state to store (ignored if {@code null})
   */
  @Override
  public void persist(ClusterNode localNode, T state) {
    if (state == null) {
      return;
    }

    try {
      ByteSequence nodeKey = stateKey(localNode);
      ByteSequence nodeValue = ByteSequence.from(codec.encode(state));
      putWithTtl(currentLeaseId(stateRefreshIntervalMs), nodeKey, nodeValue);
    } catch (Exception e) {
      log.warn("Error persisting node state", e);
    }
  }

  /**
   * Returns a reusable lease ID for the current time bucket.
   *
   * <p>Leases are grouped into extended time buckets (5× the base interval) so that multiple
   * consecutive persist cycles reuse the same lease. This significantly reduces lease churn.
   *
   * <p>The TTL is padded to approximately 7× the base interval to accommodate:
   *
   * <ul>
   *   <li>Minor clock drift between nodes
   *   <li>Scheduling or GC delays
   *   <li>Network latency when refreshing state
   * </ul>
   *
   * @param ttlMs the base persistence interval in milliseconds
   * @return an active, reusable lease ID
   */
  private long currentLeaseId(long ttlMs) {
    long bucket = System.currentTimeMillis() / (ttlMs * leaseBucketSpanFactor);
    return currentKeys.compute(
        bucket,
        (k, v) -> {
          if (v == null) {
            currentKeys.clear();
            try {
              return lease(ttlMs * leaseTtlPaddingFactor);
            } catch (Exception e) {
              throw new IllegalStateException("Failed to create lease", e);
            }
          }
          return v;
        });
  }

  /**
   * Retrieves all persisted node states for the cluster.
   *
   * <p>Each state entry is decoded from its serialized form and returned as an instance of {@code
   * T}. Expired entries are automatically cleaned up by etcd through their associated leases.
   *
   * @param localNode the local node whose cluster defines the query scope
   */
  @Override
  public void getState(ClusterNode localNode) {
    final String clusterName = localNode.getIdentity().getClusterName();
    try {
      ByteSequence prefixKey = clusterStatePrefix(clusterName);
      GetOption getOption = GetOption.builder().withPrefix(prefixKey).build();
      GetResponse response = kvClient.get(prefixKey, getOption).get();

      if (response.getKvs().isEmpty()) {
        log.info("No state found for cluster: {}", clusterName);
      }

      List<T> list =
          response.getKvs().stream()
              .map(kv -> codec.decode(kv.getValue().getBytes(), new ObjectType<T>() {}))
              .toList();
      System.err.println(list);
    } catch (Exception e) {
      log.warn("Error fetching cluster state", e);
    }
  }
}
