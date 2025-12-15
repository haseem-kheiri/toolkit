# File Core

`file-core` provides low-level, high-performance file primitives and durable data-file abstractions built on Java NIO.  
It is designed for correctness, crash safety, and explicit control over I/O behavior.

This module is intentionally framework-free and suitable for use in storage engines, write-ahead logs, audit systems, and embedded persistence layers.

## Overview

The file core module offers:

- Append-only, batch-atomic data file writing
- Sequential and random-access data file reading
- Durable write-ahead logging with crash recovery
- CRC-based data integrity verification
- Safe file locking utilities
- Optimized NIO helpers for channels, buffers, paths, and files

The design favors **explicitness over convenience** and exposes predictable behavior under failure scenarios.

## Key Components

### Data File Abstractions

#### `DataFileWriter<T>`

An append-only writer for persisting serialized objects to disk in **batch-atomic** fashion.

Characteristics:
- Objects are written in batches
- Each batch is either fully committed or not written at all
- Uses a companion write-ahead log for durability
- Skips `null` entries safely
- Requires explicit `close()` to flush and release resources

Binary layout per record:`[length:int][data:byte][checksum:long]`


#### `DataFileReader<T>`

A reader for deserializing objects written by `DataFileWriter`.

Capabilities:
- Sequential reads (`readNext`)
- Random access reads (`readAt`)
- Explicit cursor control (`position`, `seek`)
- Automatic replay of committed log batches before reading
- End-of-file indicated by `null`

### Write-Ahead Log and Recovery

The writer implementation guarantees durability via a companion log file:

1. Rows are first written to a log file with an *in-progress* marker
2. Once fully written, the marker is flipped to *committed*
3. On startup or read access, committed log entries are replayed
4. The log is truncated only after successful replay

This ensures **crash consistency** without relying on external transaction managers.

### File Locking

#### `LockedFile`

Utility for executing code under **exclusive file or region locks** using `FileChannel` and `FileLock`.

Features:
- Exclusive, blocking locks
- Retry handling for overlapping JVM locks
- Automatic release via try-with-resources semantics
- Intended for low-contention coordination

### NIO Utilities

#### `NioChannels`

Low-level helpers for:
- Chunked reads and writes
- Exact-length reads (no silent truncation)
- Primitive I/O (`int`, `long`, `byte`)
- Forced disk writes for durability guarantees

#### `NioFiles`

Safe, high-level helpers for:
- File and directory creation
- Recursive deletion
- File copy and move operations
- Recursive directory listing

#### `NioPaths`

Utility methods for:
- URI ↔ Path conversion
- Path normalization and resolution
- Validity checks
- Parent and filename extraction

### Exceptions

- `UriSyntaxException`  
  An unchecked wrapper for `URISyntaxException` used to simplify internal path handling.

## Usage Example

Minimal example showing how to append and read structured data:

```java
Path home = Paths.get("/var/data/example");
Codec codec = /* codec implementation */;

// Write data
try (DataFileWriter<MyType> writer =
         new DataFileWriterImpl<>(home, codec, 200)) {
    writer.append(List.of(new MyType("a"), new MyType("b")));
}

// Read data
try (DataFileReader<MyType> reader =
         new DataFileReaderImpl<>(MyType.class, home, codec)) {

    MyType first = reader.readNext();
    MyType second = reader.readNext();
}
```

## Design Principles

- **Crash safety by design**  
  All writes are recoverable after process or system failure.

- **Explicit I/O semantics**  
  No hidden buffering or implicit flushing.

- **Minimal abstractions**  
  Small, stable interfaces that do one thing well.

- **No framework dependencies**  
  Suitable for embedded systems and infrastructure code.

## When to Use

Use `file-core` when you need:

- Durable, append-only persistence
- Deterministic recovery behavior
- High-throughput batch writes
- Low-level control over file I/O
- A foundation for logs, audits, or embedded storage

## License

Licensed under the Apache License, Version 2.0.
