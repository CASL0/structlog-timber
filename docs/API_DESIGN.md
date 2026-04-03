# API Design Guidelines

This document defines the API design guidelines for the structlog-timber library.
It covers API quality attributes, Kotlin-specific idioms, and compatibility
strategies, serving as the decision criteria when adding or changing public APIs.

---

## 1. API Quality Attributes

### 1.1 Minimality

Keep the public API surface to the necessary minimum.

- Do not provide multiple ways to accomplish the same goal.
- Do not expose an API just because it "might be useful." Add it only when a
  real use case from consumers is confirmed.
- Do not expose methods that exist solely for internal implementation purposes.
- Mark unstable APIs with `@RequiresOptIn` and exclude them from the stable
  public API until they are ready.

```kotlin
@RequiresOptIn(message = "This API is experimental and may change.")
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalStructlogApi
```

### 1.2 Completeness

The API must have no gaps for its intended use cases.

- If consumers need reflection or casts to access internals, the API has a hole.
- Always provide both sides of paired operations (e.g., set/unset, add/remove).

### 1.3 Ease of Use

- Make the correct usage natural (easy to use correctly).
- Make incorrect usage difficult (hard to use incorrectly).
- Provide sensible default values so that the API works with minimal arguments.
- Prefer default parameter values over overload proliferation.

### 1.4 Consistency

- Use the same naming patterns for similar operations.
- Keep parameter ordering uniform across the entire API.
- Use return types consistently (e.g., how success/failure is represented).
- Follow Kotlin standard library conventions (`forEach`, `map`, `filter`, etc.).

### 1.5 Orthogonality

- Each API element should have a single, independent responsibility.
- Enable complex use cases through composition of features rather than
  creating dedicated methods for every combination.

---

## 2. Information Hiding

### 2.1 Minimize Visibility

- Make everything `internal` or `private` unless it needs to be public.
- Once an API is `public`, it carries a compatibility obligation. Be deliberate
  about what you expose.
- Do not expose implementation details (e.g., ThreadLocal management, field
  merge logic).
- Do not leak platform types (Java types without nullability information) in
  the public API.

### 2.2 Separate Interface from Implementation

- Separate public interfaces from their implementation classes.
- Consumers should depend only on interfaces so that implementation changes
  do not affect them.

### 2.3 Physical Isolation

- Use module boundaries to physically isolate implementations.
- Place implementations with optional dependencies (e.g., Firebase) in
  separate modules to keep the core module's dependency graph minimal.

### 2.4 Collection Exposure

- Never expose internal mutable collections directly. Return read-only
  `List` / `Map` / `Set` instead.
- Document whether a returned collection is a snapshot or a live view.

```kotlin
// Good: returns a read-only snapshot
fun fields(): Map<String, Any?> = _fields.toMap()

// Bad: leaks a mutable reference to internal state
fun fields(): MutableMap<String, Any?> = _fields
```

---

## 3. Naming

### 3.1 Principles

- **Intent-revealing names**: Name things after what they do, not how they
  are implemented.
- **Avoid abbreviations**: Do not abbreviate except for universally recognized
  terms (API, HTTP, JSON, etc.).
- **Symmetry**: Keep paired operation names consistent
  (`put`/`remove`, `add`/`delete`, `open`/`close`).
- **Avoid Manager / Util / Helper**: These names suggest insufficient
  separation of responsibilities.

### 3.2 Function Naming

- **Functions with side effects**: Use verb phrases (`clear()`, `add()`,
  `remove()`).
- **Functions returning values**: Use noun phrases or adjectives describing
  what is returned (`size()`, `isEmpty()`).
- **Builder functions**: Follow the `buildX { }` pattern
  (`buildList { }`, `buildString { }`).

### 3.3 Project Naming Conventions

| Category | Pattern | Example |
|----------|---------|---------|
| Sink implementation | `{Destination}Sink` | `LogcatSink`, `CrashlyticsSink` |
| Module | `structlog-timber-{destination}` | `structlog-timber-logcat` |
| Data class | Noun | `StructuredLogEntry` |
| Entry point | `Structured` + domain name | `StructuredTimber`, `StructuredLog` |

### 3.4 Boolean Methods / Properties

- Use `is`, `has`, `can`, or `should` as a prefix.
- Use affirmative names (`isEnabled`, not `isDisabled`).

---

## 4. Predictability and Debuggability

### 4.1 Principle of Least Surprise

- APIs should behave exactly as their names imply.
- Avoid behavior that depends on hidden global or mutable state.
- If a function has side effects, document them or make them explicit in the
  function name.

### 4.2 Null Handling

- Give `null` a clear, intentional meaning.
- When returning collections, represent "no results" with an empty collection,
  not `null`.
- Distinguish between "not found" (`null`) and "error" (exception).

### 4.3 Data Classes and toString

- Use `data class` for public value types to auto-generate `toString()`,
  `equals()`, and `hashCode()`.
- If not using `data class`, override `toString()` with a meaningful
  representation.
- Include the offending value in precondition violation messages.

```kotlin
require(age >= 0) { "Age must be non-negative, but was $age" }
```

### 4.4 sealed class / sealed interface

Use `sealed` for type hierarchies that require exhaustive branching, leveraging
`when` expression exhaustiveness checks.

```kotlin
sealed interface LogLevel {
    data object Verbose : LogLevel
    data object Debug : LogLevel
    // ...
}
```

---

## 5. Error Handling

### 5.1 Strategy

- Maintain a consistent error handling strategy across the API. Do not mix
  exceptions and return-value-based error signaling within the same library.
- Distinguish between recoverable errors and programming errors:
  - **Recoverable errors**: Express via return values, `Result` types, or
    domain-specific sealed classes.
  - **Programming errors**: Detect early with `require` / `check`.
- Never silently swallow exceptions. Either log them or rethrow.

### 5.2 Exception Usage

- Document all exceptions a public API can throw using `@throws` in KDoc.
- Do not leak internal implementation exceptions to consumers. Wrap them
  as needed.

---

## 6. Compatibility and Versioning

### 6.1 Semantic Versioning

Follow [Semantic Versioning 2.0.0](https://semver.org/).

| Version Element | When to Increment |
|-----------------|-------------------|
| MAJOR | Backward-incompatible API changes |
| MINOR | Backward-compatible feature additions |
| PATCH | Backward-compatible bug fixes |

### 6.2 Breaking Changes

The following are **breaking changes** that require a MAJOR version bump:

- Removing or renaming a public class, interface, or method
- Changing a method signature (parameter types, order, or required/optional)
- Changing the behavior of an existing method (contract violation)
- Changing the inheritance hierarchy of a public type
- Converting between `class` and `data class`
  (affects `copy`, `equals`, `hashCode`, `componentN`)
- Changing `open` to `final`
- Narrowing visibility (`public` to `internal`)

### 6.3 Safe Changes (Non-Breaking)

The following can be done in a MINOR version:

- Adding new functions, classes, or properties
- Adding parameters **with default values** to existing functions
- Widening visibility (`internal` to `public`)
- Adding methods with default implementations to interfaces
  (use caution in mixed Java/Kotlin projects)

### 6.4 Deprecation

Follow a graduated deprecation cycle:

1. `DeprecationLevel.WARNING` — warns but compiles successfully
2. `DeprecationLevel.ERROR` — causes a compilation error
3. Remove in the next MAJOR version

Always provide both `message` (reason) and `replaceWith` (migration target)
in the `@Deprecated` annotation.

```kotlin
@Deprecated(
    message = "Use StructuredTimber.d() instead.",
    replaceWith = ReplaceWith("StructuredTimber.d(message, *fields)"),
    level = DeprecationLevel.WARNING,
)
fun oldMethod(message: String, vararg fields: Pair<String, Any?>) { ... }
```

---

## 7. Documentation

### 7.1 KDoc Requirements

- Add KDoc to every `public` and `internal` member.
- Include the following:
  - **Summary**: A one-line description of what the member does. Describe
    not only "what" but also "why" and "when" to use it.
  - **@param**: The meaning and constraints of each parameter.
  - **@return**: The meaning of the return value (unless `Unit`).
  - **@throws**: Exceptions that can be thrown.
  - **@sample**: Usage examples (for complex APIs).
  - **@since**: The version in which the API was introduced.
- Use `[ClassName]` / `[functionName]` link syntax for references to other
  symbols.

### 7.2 Documenting Contracts

- State preconditions and postconditions explicitly.
- State thread-safety guarantees explicitly.
- State null handling when it cannot be fully expressed by Kotlin's type system.

---

## 8. Testability

### 8.1 Design for Testability

- Prefer constructor injection so that mocks or fakes can be substituted
  during testing.
- Minimize dependency on singletons and global state. When unavoidable,
  provide a reset mechanism.
- Abstract non-deterministic elements (time, randomness, I/O) so they can
  be controlled in tests.

---

## 9. Performance

### 9.1 Performance-Aware API Design

- Design APIs to avoid unnecessary object allocation.
- Aim for allocation-free hot paths.
- However, do not sacrifice usability for performance. Optimize based on
  measurement.

---

## 10. Extensibility

### 10.1 Extension Point Design

- Clearly identify the points where consumers are expected to extend.
- Define extension points as interfaces. Use abstract classes only when
  shared implementation is necessary.
- Document the lifecycle and responsibilities of each extension point.

### 10.2 Open-Closed Principle

- Allow new functionality to be added without modifying existing code.
- Guarantee that adding a new Sink does not require changes to the core module.

### 10.3 Extension Function Usage

- Prefer extension functions over utility classes.
- Do not use extension functions to "fix" a poorly designed class — redesign
  the class instead.
- Avoid extension functions on overly broad types (`Any`, `String`) as they
  pollute autocomplete suggestions.

---

## 11. Java Interoperability

As an Android library, consider usage from Java.

- Use `@JvmOverloads` to generate Java-friendly overloads for functions with
  default parameters.
- Annotate constants in companion objects with `@JvmField` to avoid the
  `Companion.CONST` access pattern from Java.
- Annotate methods in companion objects with `@JvmStatic`.
- Apply nullability annotations (`@NonNull` / `@Nullable`) to convey accurate
  null information to Java callers.

```kotlin
companion object {
    @JvmField
    val DEFAULT_TAG = "StructuredLog"

    @JvmStatic
    fun create(): StructuredTree { ... }
}

@JvmOverloads
fun connect(host: String, port: Int = 443, timeout: Duration = 30.seconds) { ... }
```

---

## 12. API Review Checklist

Verify the following when adding or changing a public API:

- [ ] **Minimality**: Does this API truly need to be public? Should it be
      marked with `@RequiresOptIn` as experimental instead?
- [ ] **Completeness**: Are paired operations missing?
- [ ] **Consistency**: Are naming, parameter ordering, and error handling
      consistent with the existing API and Kotlin standard library conventions?
- [ ] **Orthogonality**: Does this overlap in responsibility with an existing
      API?
- [ ] **Naming**: Does the name convey intent? Are abbreviations avoided?
      Do side-effecting functions use verb phrases?
- [ ] **Default values**: Does the API work sensibly with minimal arguments?
- [ ] **Information hiding**: Are implementation details leaking? Are internal
      mutable collections exposed?
- [ ] **Compatibility**: Does this break existing consumers? Do `data class`
      changes affect `copy`/`componentN`?
- [ ] **KDoc**: Are all parameters, return values, exceptions, and
      thread-safety guarantees documented?
- [ ] **Testability**: Can this be substituted with a mock/fake in tests?
- [ ] **Performance**: Are unnecessary allocations avoided on hot paths?
- [ ] **Java interop**: Are `@JvmOverloads` / `@JvmStatic` / `@JvmField`
      applied where needed?

---

## References

- [Kotlin API Guidelines](https://kotlinlang.org/docs/api-guidelines-introduction.html)
- [Semantic Versioning 2.0.0](https://semver.org/)
