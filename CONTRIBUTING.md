# Contributing

Thank you for your interest in contributing to structlog-timber!

## Getting Started

1. Fork the repository and clone it locally.
2. Create a branch from `main` for your change.
3. Run the build to make sure everything works:

```bash
./gradlew assembleDebug testDebugUnitTest spotlessCheck
```

## Development

### Prerequisites

- Android Studio (or IntelliJ IDEA with Android plugin)
- JDK 17+
- Android SDK with API 36

### Code Style

This project uses [ktfmt](https://github.com/facebook/ktfmt) (Google style) via [Spotless](https://github.com/diffplug/spotless).

```bash
# Check formatting
./gradlew spotlessCheck

# Auto-fix formatting
./gradlew spotlessApply
```

See [Coding Guidelines](docs/CODING_GUIDELINES.md) for detailed conventions.

### Running Tests

```bash
# Run all tests
./gradlew testDebugUnitTest

# Run tests with coverage
./gradlew testDebugUnitTest koverXmlReport
```

### Generating Documentation

```bash
./gradlew dokkaGeneratePublicationHtml
# Output: build/dokka/html/
```

## Submitting Changes

### Branch Naming

```
feature/add-datadog-sink
fix/thread-safety-in-structured-log
chore/update-dependencies
```

### Commit Messages

All commits must follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add minPriority parameter to LogcatSink
fix: prevent NPE when tag is null
docs: update Quick Start section in README
test: add tests for CrashlyticsSink type handling
```

See [Git Workflow](docs/GIT_WORKFLOW.md) for full details.

### Pull Requests

Create a PR using the `gh` CLI or GitHub UI:

```bash
gh pr create --title "feat: add DatadogSink" --body "$(cat <<'EOF'
## Summary
- Add DatadogSink implementation

## Test plan
- [ ] Unit tests pass
- [ ] Formatting is clean
EOF
)"
```

Before submitting, verify:

- [ ] `./gradlew assembleDebug` succeeds
- [ ] `./gradlew testDebugUnitTest` passes
- [ ] `./gradlew spotlessCheck` passes
- [ ] All public APIs have KDoc
- [ ] PR title follows Conventional Commits format

### Adding a New Sink

1. Create a new module (e.g., `structlog-timber-datadog`).
2. Implement the `Sink` interface.
3. Follow the `{Destination}Sink` naming pattern (e.g., `DatadogSink`).
4. Add unit tests.
5. Add the module to `settings.gradle.kts`.

See [Architecture](docs/ARCHITECTURE.md) for design guidelines.

## Reporting Issues

Use the [issue templates](https://github.com/CASL0/structlog-timber/issues/new/choose) to report bugs or request features.

## License

By contributing, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).
