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

**Ollama backend implementation** (currently in development).

### Planned Features

- Integration with Ollama REST API
- Support for common open-source models  
- Configurable connection settings
- Spring Boot auto-configuration support

### Current Status

The Ollama provider is currently a stub implementation. The core interface is defined but the actual Ollama integration is not yet complete.

```java
// Planned usage (not yet implemented)
@Autowired
private OllamaAiBridgeProvider provider;

AiRawRequest request = AiRawRequest.builder()
    .addMessage(Message.user("Explain distributed systems"))
    .withConfig(GenerationConfig.builder()
        .model("llama2")
        .maxTokens(500)
        .build())
    .build();

AiRawResponse response = provider.generate(request);
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