# AI Libraries

The `ai-libraries` module provides abstraction layers and implementations for integrating with AI/ML services in infrastructure applications.

This is designed for **infrastructure-grade** usage where type safety, pluggability, and failure handling matter.

---

## Philosophy

- **Provider-agnostic abstractions**  
  Core interfaces work with multiple AI service backends.

- **Infrastructure-first design**  
  Optimized for control planes, gateways, and internal platforms.

- **Minimal dependencies**  
  Framework-neutral core with optional integration layers.

---

## Modules

```text
ai-libraries
└── ai-bridge
    ├── ai-bridge-core
    └── ai-bridge-ollama-provider
```

---

## ai-bridge

The `ai-bridge` module provides a simple, extensible abstraction for AI text generation services.

### Design Goals

- **Pluggable providers**: Support multiple AI backends (Ollama, OpenAI, etc.)
- **Type-safe requests/responses**: Structured data models for reliable integration
- **Infrastructure focus**: Designed for automated systems, not interactive applications

### Core Components

- **ai-bridge-core**: Core abstractions and data models
- **ai-bridge-ollama-provider**: Ollama backend implementation

---

## Usage

The AI bridge is designed for programmatic text generation in infrastructure contexts:

- Content generation in control planes
- Automated documentation or configuration
- Template expansion with AI assistance
- Gateway request/response transformation

---

## Status

This module is in **early development**. Core abstractions are defined but provider implementations are still being developed.

Current implementation status:
- **ai-bridge-core**: ✅ Complete interfaces and data models
- **ai-bridge-ollama-provider**: 🚧 Stub implementation, integration pending

---

## License

Licensed under the Apache License, Version 2.0.  
Copyright © 2025 Haseem Kheiri