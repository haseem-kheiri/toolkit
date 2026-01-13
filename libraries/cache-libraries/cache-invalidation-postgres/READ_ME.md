# Cache Module

## Overview
This module provides a distributed cache with PostgreSQL-backed invalidation.

## Components
- Cache
- CacheManager
- CacheInvalidationBus
- PostgresCacheInvalidationBus

## How it works
Local evictions are written to a durable event log. Other nodes poll and replay.

## Test
The provided Spring Boot test spins up PostgreSQL via Testcontainers and validates eviction propagation.

## Usage
Create CacheManager, build caches, call put/get/evict.
