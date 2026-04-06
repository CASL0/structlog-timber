# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Before Starting Work

Review the relevant docs under `docs/` before making changes:

- `docs/ARCHITECTURE.md` — module structure and design decisions
- `docs/CODING_GUIDELINES.md` — code style and conventions
- `docs/GIT_WORKFLOW.md` — branching, commits, and release process
- `docs/API_DESIGN.md` — public API design principles
- `docs/SECURITY.md` — security policies

## Build & Test Commands

```bash
# Build
./gradlew assembleDebug

# Run all tests
./gradlew testDebugUnitTest

# Run a single test class
./gradlew testDebugUnitTest --tests "StructuredTreeTest"

# Run a single test method (backtick-quoted names require wildcard)
./gradlew testDebugUnitTest --tests "StructuredTreeTest.*log delegates to sink*"

# Tests with coverage
./gradlew testDebugUnitTest koverXmlReport

# Check formatting (ktfmt, Google style)
./gradlew spotlessCheck

# Auto-fix formatting
./gradlew spotlessApply

# Generate KDoc
./gradlew dokkaGeneratePublicationHtml
```

## Architecture

Multi-module Android library providing structured logging on top of Timber.

```
structlog-timber-core         Pure Kotlin. StructuredTimber, StructuredTree, StructuredLog, Sink, StructuredLogEntry
structlog-timber-logcat       LogcatSink (depends on core + android.util.Log)
structlog-timber-crashlytics  CrashlyticsSink (depends on core + Firebase Crashlytics)
sample                        Demo app (excluded from coverage and publishing)
```

### Data Flow

`StructuredTimber.d("msg", "k" to "v")` stores fields in a `ThreadLocal`, then calls `Timber.d()`. `StructuredTree` (a `Timber.Tree`) consumes pending fields, snapshots thread-local context from `StructuredLog`, merges with `globalFields`, builds an immutable `StructuredLogEntry`, and dispatches to each `Sink` whose `isLoggable()` returns true.

Field merge priority (highest wins): per-log fields > context fields > global fields.

### Adding a New Sink

1. Create a module following `structlog-timber-{destination}` naming.
2. Implement `Sink` interface (`emit` + `isLoggable`).
3. Class name must follow `{Destination}Sink` pattern (e.g., `DatadogSink`).
4. Accept `minPriority` as constructor parameter (default `Log.WARN`), matching `CrashlyticsSink` / `LogcatSink`.
5. Register in `settings.gradle.kts`.

## Code Conventions

- **Formatter**: ktfmt (Google style) via Spotless. 2-space indent, 100-char line limit.
- **KDoc**: Required on every public and internal member, even self-explanatory ones.
- **Nullability**: Prefer `?.` and `?:`. Avoid `!!`.
- **Testing**: JUnit 4 + MockK. Backtick-quoted test names. AAA pattern. Always `@After` cleanup (`Timber.uprootAll()`, `StructuredLog.clearLogContext()`).
- **Robolectric**: Used only in `structlog-timber-logcat` tests (for `ShadowLog`).

## Git Workflow

- **Branching**: GitHub Flow. Feature branches from `main`, squash-merge PRs.
- **Commits**: Conventional Commits required (`feat:`, `fix:`, `docs:`, `ci:`, `build:`, `refactor:`, `test:`, `chore:`).
- **Releases**: Fully automated via Release Please. Do not manually edit `VERSION_NAME` in `gradle.properties` or create tags.
- **Publishing**: Maven Central via vanniktech plugin. Triggered automatically on GitHub Release.

## Module Coordinates

```
io.github.casl0:structlog-timber-core
io.github.casl0:structlog-timber-logcat
io.github.casl0:structlog-timber-crashlytics
```
