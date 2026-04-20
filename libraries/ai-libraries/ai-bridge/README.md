# AI Bridge

The `ai-bridge` module provides a **clean, extensible abstraction** for AI text generation services with concrete provider implementations.

It is designed for **infrastructure applications** where AI integration needs to be reliable, type-safe, and provider-agnostic.

---

## Modules

```text
ai-bridge
├── ai-bridge-core
└── ai-bridge-ollama-provider
```

Each module has a focused responsibility and can be consumed independently.

---

## ai-bridge-core

The `ai-bridge-core` module defines the **fundamental AI integration contract** shared by all provider implementations.

### Key Types

#### `AiBridgeProvider`

A simple interface for AI text generation:

```java
public interface AiBridgeProvider {
  AiRawResponse generate(AiRawRequest request);
}
```

#### `AbstractRoundRobinEndpointSelector`

Base class providing thread-safe round-robin endpoint selection:

```java
public abstract class AbstractRoundRobinEndpointSelector implements EndpointSelector {
  protected AbstractRoundRobinEndpointSelector(List<URI> endpoints);
  public final URI select(); // Thread-safe round-robin implementation
}
```

Features:
- **Thread-safe**: Uses synchronized blocks for concurrent access
- **Fair distribution**: Ensures all endpoints receive equal load over time  
- **Overflow handling**: Gracefully handles integer counter overflow
- **Reusable**: Can be extended by any provider needing round-robin selection

#### `AiRawRequest`

Structured request containing:
- Messages with roles (system, user, assistant)
- Generation configuration (temperature, max tokens, etc.)
- Model specification and metadata

#### `AiRawResponse`

Structured response containing:
- Generated text content
- Usage statistics and metadata
- Error information if applicable

---

## ai-bridge-ollama-provider

**Ollama backend implementation** that extends the round-robin endpoint selection base class.

### Current Implementation

- **OllamaEndpointSelector**: Extends `AbstractRoundRobinEndpointSelector` for fair load distribution
- **Provider stub**: Core provider interface defined but HTTP integration pending

### Architecture

```java
OllamaEndpointSelector extends AbstractRoundRobinEndpointSelector
                        implements EndpointSelector
```

The selector inherits thread-safe round-robin logic from the base class, allowing multiple Ollama instances to be used without custom load balancing code.

### Planned Features

- Integration with Ollama REST API
- Support for common open-source models  
- Configurable connection settings
- Spring Boot auto-configuration support

### Usage Example

```java
// Multiple Ollama instances for high availability
List<URI> ollamaEndpoints = List.of(
    URI.create("http://ollama1:11434"),
    URI.create("http://ollama2:11434"),
    URI.create("http://ollama3:11434")
);

OllamaEndpointSelector selector = new OllamaEndpointSelector(ollamaEndpoints);

// Each call rotates through endpoints fairly
URI endpoint1 = selector.select(); // http://ollama1:11434
URI endpoint2 = selector.select(); // http://ollama2:11434  
URI endpoint3 = selector.select(); // http://ollama3:11434
URI endpoint4 = selector.select(); // http://ollama1:11434 (wrap around)
```

---

## Design Principles

### Provider Abstraction

The core module defines interfaces only. Concrete providers implement the contract using different backends:

- **Ollama**: Local inference with open models
- **OpenAI**: (Future) Cloud-based inference
- **Anthropic**: (Future) Claude integration

### Type Safety

Request and response models use structured data instead of raw strings:

- Messages have explicit roles and metadata
- Configuration is type-safe with validation
- Responses include structured usage information

### Error Handling

Providers handle connection failures, rate limits, and model errors consistently:

- Timeouts and retries at the provider level
- Structured error responses
- Graceful degradation patterns

---

## Infrastructure Use Cases

This module is designed for:

- **Documentation generation**: Automated technical writing
- **Configuration templating**: AI-assisted config generation  
- **Gateway processing**: Request/response transformation
- **Control plane automation**: Decision support systems

It is **not intended for**:
- Interactive chat applications
- Real-time user-facing features
- High-throughput streaming scenarios

---

## Configuration

### Ollama Provider

```yaml
ai:
  bridge:
    ollama:
      baseUrl: "http://localhost:11434"
      timeout: "30s"
      defaultModel: "llama2"
```

---

## Dependencies

### Core Module

- **toolkit-core**: Foundation utilities
- **Jackson**: JSON serialization
- **SLF4J**: Logging abstraction

### Ollama Provider

- **Spring Boot**: Configuration and DI support
- **HTTP Client**: Ollama API communication

---

## Status

This module is in **early development**. The core abstractions (`AiBridgeProvider`, `AiRawRequest`, `AiRawResponse`) are defined and stable, but provider implementations are not yet complete.

The Ollama provider currently exists as a stub implementation. APIs may evolve as additional providers and use cases are added.

The core abstractions are stable and suitable for infrastructure usage.

---

## License

Licensed under the Apache License, Version 2.0.  
Copyright © 2025 Haseem Kheiri