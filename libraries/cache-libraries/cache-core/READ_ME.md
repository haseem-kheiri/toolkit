# Distributed Cache Invalidation System

This document describes the architecture and usage of the Cache, CacheManager, and CacheInvalidationBus.

## Components

### Cache
A minimal key–value store with explicit eviction semantics.

### CacheManager
Coordinates multiple named caches and propagates evictions through a distributed bus.

### CacheInvalidationBus
Abstract transport for eviction events with a global clock.

## Architecture

Local evictions are written to an event log and published. Remote nodes poll and replay those events to converge.

## Guarantees

- At-least-once delivery of eviction events
- Monotonic convergence based on timestamps
- Loop-free propagation

## Usage

Create a CacheManager, register caches, and call evict() to propagate deletions.
