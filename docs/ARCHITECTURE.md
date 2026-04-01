# Architecture

This document describes the architecture of the structlog-timber library,
following the principles outlined in the
[Guide to App Architecture](https://developer.android.com/topic/architecture) and
[Architecture Recommendations](https://developer.android.com/topic/architecture/recommendations)
from Android Developers.

---

## Module Structure

```
structlog-timber/
├── structlog-timber-core        # Core library (Sink, StructuredTree, StructuredLog)
└── structlog-timber-crashlytics # Crashlytics Sink implementation
```

### structlog-timber-core

The core module containing the public API and all framework-independent logic.
This module depends only on [Timber](https://github.com/JakeWharton/timber) and
the Android SDK.

### structlog-timber-crashlytics

An optional module providing a `CrashlyticsSink` implementation.
This module depends on `structlog-timber-core` and Firebase Crashlytics.

> *"Expose as little as possible from each module."*
>
> --- [Guide to App Architecture](https://developer.android.com/topic/architecture#best-practices)

Each module exposes only its public API surface. Internal implementation details
are kept `internal` to prevent unintended coupling.

---

## Architectural Principles

### Separation of Concerns

> *"The most important principle to follow is separation of concerns. It's a common mistake to write all your code in an Activity or a Fragment."*
>
> --- [Guide to App Architecture](https://developer.android.com/topic/architecture#separation-of-concerns)

The library separates responsibilities into distinct types:

| Type | Responsibility |
|------|---------------|
| `StructuredTimber` | Entry point — accepts log messages with key-value fields |
| `StructuredTree` | Timber `Tree` — merges fields and context, delegates to Sinks |
| `StructuredLog` | MDC-style thread-local context management |
| `StructuredLogEntry` | Immutable data model representing a single structured log entry |
| `Sink` | Interface — abstracts the log destination |

### Single Source of Truth (SSOT)

> *"When a new data type is defined in your app, you should assign a Single Source of Truth (SSOT) to it. The SSOT is the owner of that data, and only the SSOT can modify or mutate it. To achieve this, the SSOT exposes the data using an immutable type, and to modify the data, the SSOT exposes functions or receives events that other types can call."*
>
> --- [Guide to App Architecture](https://developer.android.com/topic/architecture#single-source-of-truth)

- `StructuredLog` is the SSOT for thread-local context data.
  Only `putContext()` and `removeContext()` can mutate the context.
  The `snapshot()` method exposes an immutable copy.
- `StructuredTimber` is the SSOT for per-log fields.
  Fields are set via `ThreadLocal` and consumed exactly once by `StructuredTree`.

### Unidirectional Data Flow (UDF)

> *"In UDF, state flows in only one direction. The events that modify the data flow in the opposite direction."*
>
> --- [Guide to App Architecture](https://developer.android.com/topic/architecture#unidirectional-data-flow)

The library follows a one-directional data flow:

```
Caller
  │
  ▼
StructuredTimber  ──(sets fields via ThreadLocal)──►  Timber.log()
                                                         │
                                                         ▼
                                                    StructuredTree.log()
                                                         │
                                            ┌────────────┼────────────┐
                                            ▼            ▼            ▼
                                       LogcatSink  CrashlyticsSink  CustomSink
```

Data flows from the caller through `StructuredTimber`, into `StructuredTree`,
and out to each `Sink`. There is no reverse flow from Sinks back to the caller.

### Drive UI from Data Models

> *"You should base your app architecture on data model classes. Data models are independent from the UI elements and other components in your app."*
>
> --- [Guide to App Architecture](https://developer.android.com/topic/architecture#drive-ui-from-data-models)

`StructuredLogEntry` is a pure data model that is independent of Android
framework classes. It carries all information a Sink needs to emit a log entry,
without any dependency on `Activity`, `Context`, or lifecycle.

---

## Layer Mapping

Although this is a library rather than an app, the design aligns with the
recommended layered architecture:

> *"Use a clearly defined data layer."*
> *"The data layer exposes application data to the rest of the app and contains the vast majority of business logic."*
>
> --- [Architecture Recommendations](https://developer.android.com/topic/architecture/recommendations)

| Android Layer | Library Equivalent | Description |
|---------------|-------------------|-------------|
| **UI Layer** | Consumer app | The app that calls `StructuredTimber` or `Timber` |
| **Domain Layer** | `StructuredTimber`, `StructuredLog` | Encapsulates structured logging use cases |
| **Data Layer** | `StructuredTree`, `Sink` implementations | Handles actual log emission to destinations |

---

## Dependency Management

> *"Use dependency injection best practices, mainly constructor injection when possible."*
>
> --- [Architecture Recommendations](https://developer.android.com/topic/architecture/recommendations)

`StructuredTree` uses constructor injection for its dependencies:

```kotlin
class StructuredTree(
    private val sinks: List<Sink>,                       // Required — log destinations
    private val globalFields: Map<String, Any?> = emptyMap(), // Optional — global attributes
) : Timber.Tree()
```

This makes the class easy to test and configure without a DI framework.
Consumers using Hilt or other DI frameworks can provide `StructuredTree` via
their existing dependency graph.

---

## Concurrency Policy

> *"Types that do long-running work should be responsible for moving the execution of that work to the right thread."*
>
> --- [Guide to App Architecture](https://developer.android.com/topic/architecture#best-practices)

- `StructuredTimber` and `StructuredTree` are **main-safe** — they perform no
  blocking I/O or heavy computation on the calling thread.
- Thread-local context (`StructuredLog`) is isolated per thread by design.
- Individual Sink implementations are responsible for their own concurrency
  policy. For example, a network-based Sink should offload I/O to a background
  thread or use coroutines.

---

## Testability

> *"Know what to test. Unless the project is roughly as simple as a hello world app, you should test it."*
> *"Prefer fakes to mocks."*
>
> --- [Architecture Recommendations](https://developer.android.com/topic/architecture/recommendations)

The `Sink` interface enables straightforward testing:

- Create a `FakeSink` that collects emitted entries in a list.
- Plant a `StructuredTree` with the fake Sink.
- Assert on the captured `StructuredLogEntry` instances.

No Android framework mocking is required to test the core logic.

---

## Naming Conventions

> *"Names for the implementations of interfaces should be meaningful."*
>
> --- [Architecture Recommendations](https://developer.android.com/topic/architecture/recommendations)

| Interface | Implementation | Rationale |
|-----------|---------------|-----------|
| `Sink` | `LogcatSink` | Outputs to Android Logcat |
| `Sink` | `CrashlyticsSink` | Outputs to Firebase Crashlytics |
| `Sink` | `FakeSink` (test) | Fake for unit testing |

Custom Sink implementations should follow the `{Destination}Sink` naming
pattern (e.g., `DatadogSink`, `CloudLoggingSink`).

---

## References

- [Guide to App Architecture](https://developer.android.com/topic/architecture)
- [Architecture Recommendations](https://developer.android.com/topic/architecture/recommendations)
- [Timber](https://github.com/JakeWharton/timber)
