[日本語版はこちら](README_ja.md)

# structlog-timber

[![Maven Central](https://img.shields.io/maven-central/v/io.github.casl0/structlog-timber-core)](https://central.sonatype.com/search?q=g:io.github.casl0+structlog-timber)
[![codecov](https://codecov.io/github/CASL0/structlog-timber/graph/badge.svg?token=13QUMCR321)](https://codecov.io/github/CASL0/structlog-timber)
[![KDoc](https://img.shields.io/badge/KDoc-GitHub%20Pages-blueviolet)](https://casl0.github.io/structlog-timber/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

Structured logging for Android, built on [Timber](https://github.com/JakeWharton/timber). Attach key-value fields to every log call and route them to Logcat, Crashlytics, or any custom destination through a simple Sink interface.

```kotlin
StructuredTimber.i(
    "Purchase completed",
    "item_id" to "SKU-123",
    "price" to 1980,
    "currency" to "JPY",
)
// Logcat output:
// I/StructuredLog: Purchase completed {item_id=SKU-123, price=1980, currency=JPY}
```

## Features

- **Key-value fields on every log call** -- pass `Pair<String, Any?>` varargs alongside your message.
- **Thread-local context (MDC)** -- set once with `StructuredLog.putContext()`, automatically included in all subsequent logs on the same thread.
- **Global fields** -- attach `app_version`, `build_type`, or any constant to every entry via `StructuredTree`.
- **Pluggable Sink architecture** -- built-in sinks for Logcat and Firebase Crashlytics; implement the `Sink` interface to add your own.
- **Priority filtering** -- each Sink controls its own minimum log level via `isLoggable()`.
- **Zero overhead when unused** -- fields are stored in `ThreadLocal` and consumed only when a `StructuredTree` is planted.
- **Pure Kotlin core** -- the core module depends only on Timber, with no Android framework dependency.

## Setup

### Gradle

Add the modules you need to your `build.gradle.kts`:

```kotlin
dependencies {
    // Required -- core API
    implementation("io.github.casl0:structlog-timber-core:<version>")

    // Optional -- Logcat sink
    implementation("io.github.casl0:structlog-timber-logcat:<version>")

    // Optional -- Firebase Crashlytics sink
    implementation("io.github.casl0:structlog-timber-crashlytics:<version>")
}
```

### Module Overview

| Module | Artifact | Description |
|--------|----------|-------------|
| `structlog-timber-core` | `io.github.casl0:structlog-timber-core` | Core API: `StructuredTimber`, `StructuredTree`, `StructuredLog`, `Sink` |
| `structlog-timber-logcat` | `io.github.casl0:structlog-timber-logcat` | `LogcatSink` -- writes structured logs to Android Logcat |
| `structlog-timber-crashlytics` | `io.github.casl0:structlog-timber-crashlytics` | `CrashlyticsSink` -- sends fields as Crashlytics custom keys |

### Requirements

- Android API 26+
- Java 17+

## Quick Start

### 1. Plant the tree

Configure `StructuredTree` with the sinks you want and plant it in your `Application.onCreate()`:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(
            StructuredTree(
                sinks = listOf(LogcatSink(minPriority = Log.DEBUG)),
                globalFields = mapOf("app_version" to BuildConfig.VERSION_NAME),
            )
        )
    }
}
```

### 2. Log with fields

Pass key-value pairs as varargs:

```kotlin
StructuredTimber.d(
    "Activity created",
    "lifecycle" to "onCreate",
    "has_saved_state" to (savedInstanceState != null),
)
```

### 3. Add thread-local context

Set context values that automatically attach to all logs on the current thread:

```kotlin
StructuredLog.putContext("screen", "main")
StructuredLog.putContext("user_id", userId)

// Both logs below include screen and user_id
StructuredTimber.i("Item viewed", "item_id" to "SKU-456")
StructuredTimber.i("Item added to cart", "item_id" to "SKU-456")
```

Clear context when it is no longer needed:

```kotlin
StructuredLog.removeContext("user_id")  // remove a single key
StructuredLog.clearContext()            // remove all keys
```

### 4. Use tags

Apply a tag to a single log call:

```kotlin
StructuredTimber.tag("Checkout")
    .w("Slow payment response", "latency_ms" to 3200, "gateway" to "stripe")
```

### 5. Log exceptions

Pass a `Throwable` as the first argument to `e()`:

```kotlin
StructuredTimber.e(
    RuntimeException("Something went wrong"),
    "Unexpected error",
    "error_code" to "E001",
)
```

### 6. Write a custom Sink

Implement the `Sink` interface to route logs to any destination:

```kotlin
class DatadogSink : Sink {
    override fun isLoggable(priority: Int): Boolean = priority >= Log.INFO

    override fun emit(entry: StructuredLogEntry) {
        // Send entry.message and entry.fields to your backend
    }
}
```

Register it when planting the tree:

```kotlin
Timber.plant(
    StructuredTree(
        sinks = listOf(LogcatSink(), DatadogSink()),
    )
)
```

## Field Merge Order

`StructuredTree` merges fields from three layers. When keys conflict, higher-priority layers win:

| Priority | Source | Set via |
|----------|--------|---------|
| 1 (lowest) | Global fields | `StructuredTree(globalFields = ...)` |
| 2 | Thread-local context | `StructuredLog.putContext()` |
| 3 (highest) | Per-log fields | `StructuredTimber.d("msg", "key" to value)` |

## Documentation

- [Architecture](docs/ARCHITECTURE.md) -- module structure, data flow, and design principles
- [Coding Guidelines](docs/CODING_GUIDELINES.md) -- Kotlin style conventions
- [Security Policy](docs/SECURITY.md) -- OWASP MASVS-based security considerations
- [Git Workflow](docs/GIT_WORKFLOW.md) -- branching, commits, and release process
- [KDoc API Reference](https://casl0.github.io/structlog-timber/) -- generated API documentation

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, coding standards, and how to submit a pull request.

## License

```
Copyright 2026 CASL0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
