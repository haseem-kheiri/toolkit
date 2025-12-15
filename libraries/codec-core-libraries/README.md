# Codec Core Libraries

`codec-core-libraries` provides a **clean, extensible serialization abstraction** with concrete implementations for JSON and MessagePack.  
It is designed for infrastructure code where **type safety, determinism, and pluggable encodings** matter.

The library is intentionally minimal, framework-neutral at the core, and integration-friendly at the edges.

---

## Modules

This repository is organized as a small set of focused submodules:

```text
codec-core-libraries
├── codec-core
├── codec-json
└── codec-message-pack
```

Each module has a single responsibility and can be consumed independently.

---

## codec-core

The `codec-core` module defines the **fundamental serialization contract** shared by all codec implementations.

### Key Types

#### `Codec`

A generic abstraction for encoding and decoding arbitrary objects to and from binary form.

```java
public interface Codec {
  byte[] encode(Object obj);
  <T> T decode(byte[] encoded, ObjectType<T> type);
}
```

## Design Characteristics

- **Binary-first API**  
  No assumptions about string-based encodings.

- **Supports complex generic types**  
  Handles deeply parameterized structures safely.

- **Deterministic encoding encouraged**  
  Suitable for caching, hashing, and distributed consistency.

- **Unchecked failure model**  
  Errors propagate via `CodecException`.

---

## ObjectType\<T\>

A runtime type-capture utility that preserves full generic type information despite Java type erasure.

```java
ObjectType<List<String>> type =
    new ObjectType<List<String>>() {};
```
Used to safely deserialize parameterized structures such as:

- `List<Foo>`
- `Map<String, Bar>`
- Nested generics

Instantiation without an anonymous subclass is explicitly rejected.

---

## CodecException

An unchecked exception used to signal encoding or decoding failures.

- Wraps underlying `IOException`
- Avoids forcing checked-exception handling
- Makes codec usage suitable for infrastructure paths

---

## codec-json

The `codec-json` module provides a JSON-based `Codec` implementation backed by Jackson.

### JsonCodec

- Uses Jackson `ObjectMapper`
- Supports full generic deserialization
- Suitable for:
  - Configuration persistence
  - Human-readable storage
  - Debuggable payloads
  - REST-adjacent data exchange

Example:

```java
Codec codec = new JsonCodec();

byte[] json = codec.encode(Map.of("a", 1, "b", 2));

Map<String, Integer> decoded =
    codec.decode(json, new ObjectType<Map<String, Integer>>() {});
```

## Spring Boot Integration (JSON)

Auto-configuration is provided via:

- `JsonCodecConfiguration`
- `JsonConfiguration`

### Features

- Registers `JsonCodec` as a Spring bean
- Honors application-wide `ObjectMapper` customization
- Does not interfere with existing Jackson setup

---

## JsonUtils

A lightweight utility for JSON operations using a singleton `ObjectMapper`.

### Capabilities

- Object → JSON string
- Object → JSON bytes
- Functional mapping with automatic exception wrapping

Intended for utility-level usage, not as a replacement for the `Codec` abstraction.

---

## codec-message-pack

The `codec-message-pack` module provides a compact, high-performance binary codec using MessagePack.

### MessagePackCodec

- Uses Jackson with `MessagePackFactory`
- Registers `JavaTimeModule`
- Disables timestamp-based date serialization
- Fully supports parameterized types

### Example

```java
Codec codec = new MessagePackCodec();

byte[] encoded = codec.encode(Map.of("a", 1, "b", 2));

Map<String, Integer> decoded =
    codec.decode(encoded, new ObjectType<Map<String, Integer>>() {});
```
## When to Use MessagePack

Prefer `codec-message-pack` when you need:

- Compact binary payloads
- Low-latency serialization
- Inter-node or cluster communication
- Replicated state transfer

---

## Spring Boot Integration (MessagePack)

`MessagePackCodecConfiguration` provides:

- Automatic bean registration
- Isolation from JSON `ObjectMapper`
- Safe coexistence with standard Jackson configuration

---

## Design Principles

- **Single abstraction, multiple encodings**  
  All codecs conform to the same contract.

- **Explicit type safety**  
  Generic type capture is mandatory and enforced.

- **No framework coupling at the core**  
  Spring support is additive, not required.

- **Infrastructure-first design**  
  Suitable for storage engines, messaging systems, and cluster coordination.

- **Deterministic behavior**  
  Predictable serialization and failure semantics.

---

## When to Use

Use this toolkit when you need:

- A stable serialization SPI
- Swappable encoding formats
- Generic-safe deserialization
- Infrastructure-grade correctness
- Clean separation between abstraction and implementation

---

## License

Licensed under the Apache License, Version 2.0.
