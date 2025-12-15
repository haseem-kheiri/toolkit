# test-core

`test-core` provides reusable testing utilities and fixtures intended to simplify integration, system, and data-driven testing across the toolkit and related projects.

The module focuses on **infrastructure containers**, **temporary filesystem helpers**, and **deterministic synthetic datasets** for repeatable tests.

## Scope

This module is intended **only for testing**.  
It is not required at runtime by production components.

Primary goals:
- Reduce boilerplate in integration tests
- Provide consistent container setup for common infrastructure
- Offer realistic, reusable datasets for simulations and test data generation

## Contents

### Testcontainers helpers

Fluent factories and wrappers around Testcontainers for commonly used infrastructure:

#### Supported containers
- **PostgreSQL**
- **Apache Kafka (Confluent)**
- **Apache ActiveMQ Artemis**
- **etcd**

Each container is provided via a small factory abstraction that:
- Encapsulates image selection and configuration
- Provides sensible defaults
- Allows fluent customization where needed

A central `Containers` utility class exposes entry points for common factories.

### Temporary filesystem utilities

Helpers for managing temporary files and directories during tests:
- System temp directory resolution
- Isolated, randomized temp directories for parallel test execution
- Recursive cleanup utilities
- `AutoCloseable` temp directory wrapper for safe use with try-with-resources

These utilities are designed to avoid test pollution and filesystem leaks.

### Synthetic datasets

Predefined, immutable datasets for generating realistic test data without external dependencies.

Included datasets:
- **Age** (18–100)
- **Date of birth** (1900–2025)
- **Gender**
- **Dependent counts**
- **Salary** (bell-curve distributed)
- **World capitals**
- **Notable historical and scientific names**
- **Composite people dataset**

Datasets expose:
- Immutable value collections
- Uniform random selection helpers
- Lightweight records for structured data

The `People` dataset combines multiple datasets to generate realistic synthetic person profiles suitable for simulations, load tests, and integration testing.

### Dataset abstraction

A minimal `DataSet` interface provides:
- Uniform random selection
- Reusable helper logic for dataset-backed generators
- No external dependencies

## Design intent

- Test-only dependency surface
- Deterministic, reusable test primitives
- Explicit container configuration
- No framework coupling beyond Testcontainers

This module is expected to evolve as new infrastructure and testing needs emerge, while remaining isolated from production code paths.
