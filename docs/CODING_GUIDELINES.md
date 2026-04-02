# Coding Guidelines

This document defines the coding conventions for the structlog-timber project.
Rules are based on the official
[Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide) and
[Common Kotlin Patterns for Android](https://developer.android.com/kotlin/common-patterns).

---

## Source Files

### Naming

> *A source file containing a single top-level class is named after that class
> with the `.kt` extension.*
>
> --- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide#source_file_naming)

- One top-level class per file: `CrashlyticsSink.kt`
- Multiple related declarations: descriptive PascalCase name: `StructuredLogEntry.kt`

### Structure

Every source file is ordered as follows:

1. Copyright / license header (if applicable)
2. File-level annotations
3. `package` statement
4. `import` statements --- ASCII-sorted, **no wildcard imports**
5. Top-level declarations

Each section is separated by exactly **one blank line**.

---

## Formatting

### Indentation

- **2 spaces** per indent level (project convention, aligned with existing code).
- Never use tabs.

### Column Limit

**100 characters.** Exceptions:

- Long URLs in KDoc
- `package` and `import` statements
- Command lines in comments

> *Each line of text in your code has a column limit of 100 characters.*
>
> --- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide#line_wrapping)

### Braces

Follow **K&R style** (Egyptian brackets):

```kotlin
// Good
if (condition) {
  doSomething()
} else {
  doOtherThing()
}

// Single-line exception
if (string.isEmpty()) return
```

> *Braces follow the Kernighan and Ritchie style ("Egyptian brackets").*
>
> --- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide#braces)

### Line Wrapping

When a statement exceeds the column limit:

- Break **after** operators and infix function names
- Break **before** `.` and `?.`
- Keep function/constructor name attached to opening `(`
- Comma stays attached to preceding token

```kotlin
// Good
val entry =
  StructuredLogEntry(
    priority = priority,
    tag = tag,
    message = message,
    throwable = t,
    fields = mergedFields,
  )
```

### Trailing Commas

Use trailing commas in parameter lists, constructor arguments, and `when` entries.

```kotlin
data class StructuredLogEntry(
  val priority: Int,
  val tag: String?,
  val message: String,
  val throwable: Throwable?,
  val fields: Map<String, Any?>,  // trailing comma
)
```

### Whitespace

- One blank line between class members (optional between consecutive properties)
- Space after `if`, `for`, `when`, `catch` before `(`
- Space around binary operators and `->`
- **No space** around `::` or `.`

---

## Naming Conventions

### Packages

All lowercase, no underscores:

```kotlin
package io.github.casl0.structlog.extension.timber  // Good
package io.github.casl0.structlog.Extension.timber   // Bad
```

> *Package names are all lowercase, with consecutive words simply concatenated
> together (no underscores).*
>
> --- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide#package_names)

### Types

PascalCase nouns:

```kotlin
class StructuredTree     // Good
interface Sink           // Good
data class StructuredLogEntry  // Good
```

### Functions

camelCase verbs:

```kotlin
fun emit(entry: StructuredLogEntry)  // Good
fun isLoggable(priority: Int)        // Good
fun consumePendingFields()           // Good
```

> *Function names are written in camelCase and are typically verbs or verb
> phrases.*
>
> --- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide#function_names)

Underscores are allowed only in **test method names**:

```kotlin
@Test
fun `log delegates to sink with correct entry`() { }
```

### Constants

`UPPER_SNAKE_CASE` for compile-time constants and deeply immutable top-level / `object` vals:

```kotlin
const val DEFAULT_TAG = "StructLog"
val EMPTY_FIELDS = emptyMap<String, Any?>()
```

> *Constant names use UPPER_SNAKE_CASE.*
>
> --- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide#constant_names)

### Backing Properties

Prefix with underscore:

```kotlin
private var _table: Map<String, Int>? = null
val table: Map<String, Int>
  get() { /* ... */ }
```

### Camel Case Conversion

| Prose Form | Correct | Incorrect |
|---|---|---|
| "XML Http Request" | `XmlHttpRequest` | `XMLHTTPRequest` |
| "new customer ID" | `newCustomerId` | `newCustomerID` |

> *Sometimes there is more than one reasonable way to convert an English phrase
> into camel case ... to improve the determinism and readability of names,
> use the following scheme.*
>
> --- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide#camel_case)

---

## Documentation (KDoc)

### Required KDoc

**Every `public` and `internal` function, property, and type must have KDoc,**
even when the name is self-explanatory.

```kotlin
/** Abstraction for structured log destinations. */
interface Sink {

  /** Emit a structured log [entry] to this destination. */
  fun emit(entry: StructuredLogEntry)

  /**
   * Whether this Sink should handle the given log [priority].
   *
   * Defaults to `true` for all priorities.
   *
   * @param priority Log priority ([android.util.Log] constants).
   */
  fun isLoggable(priority: Int): Boolean = true
}
```

### Format

- Start with a brief summary fragment (noun or verb phrase)
- Separate paragraphs with blank lines
- Block tags in order: `@param`, `@property`, `@return`, `@throws`, `@see`
- Include code examples with triple backtick fenced blocks

> *At a minimum, KDoc is present for every public type, and every public or
> protected member of such a type.*
>
> --- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide#kdoc)

---

## Language Features

### Immutability

Prefer `val` over `var`. Use immutable collections by default.

```kotlin
// Good
val mergedFields = buildMap { /* ... */ }

// Avoid
var mergedFields = mutableMapOf<String, Any?>()
```

### Data Classes

Use `data class` for types that are pure data holders:

```kotlin
data class StructuredLogEntry(
  val priority: Int,
  val tag: String?,
  val message: String,
  val throwable: Throwable?,
  val fields: Map<String, Any?>,
)
```

### Singleton Objects

Use `object` declarations for singletons:

```kotlin
object StructuredTimber {
  // ...
}
```

> *Companion objects are similar to Java's static keyword. Use a companion
> object to define class-level constants.*
>
> --- [Common Kotlin Patterns](https://developer.android.com/kotlin/common-patterns#companion-objects)

### Expression Functions

Use single-expression syntax when the body is a single expression:

```kotlin
// Good
override fun isLoggable(priority: Int): Boolean = priority >= minPriority

// Avoid (unnecessary block body for simple return)
override fun isLoggable(priority: Int): Boolean {
  return priority >= minPriority
}
```

> *When a function contains only a single expression it can be represented as
> an expression function.*
>
> --- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide#expression_functions)

### String Templates

Use string templates instead of concatenation:

```kotlin
// Good
val message = "User $userId performed action $action"

// Avoid
val message = "User " + userId + " performed action " + action
```

### `when` Expressions

Prefer `when` over `if`-`else` chains for multiple conditions:

```kotlin
when (value) {
  is Boolean -> crashlytics.setCustomKey(key, value)
  is Int -> crashlytics.setCustomKey(key, value)
  is String -> crashlytics.setCustomKey(key, value)
  else -> crashlytics.setCustomKey(key, value.toString())
}
```

### Scope Functions

- `let` --- null-safe operations: `entry.throwable?.let { recordException(it) }`
- `apply` --- object configuration
- `also` --- side effects
- `run` / `with` --- grouping operations on an object

### Type Inference

Omit explicit types when the type is obvious from context. **Library public API
should retain explicit return types.**

```kotlin
// Internal --- type inference is fine
private val pendingFields = ThreadLocal<MutableMap<String, Any?>>()

// Public API --- explicit return type required
fun isLoggable(priority: Int): Boolean = true
```

> *If the body of a function is a single expression ... the return type can be
> omitted when it can be clearly inferred by the reader.*
>
> --- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide#implicit_return_property_types)

---

## Nullability

### Prefer Safe-call and Elvis Operators

> *Prefer safe-call (`?.`) and Elvis (`?:`) operators over not-null assertion
> (`!!`).*
>
> --- [Common Kotlin Patterns](https://developer.android.com/kotlin/common-patterns#nullability)

```kotlin
// Good
entry.throwable?.let { crashlytics.recordException(it) }

// Good --- early return with Elvis
val tag = entry.tag ?: return

// Avoid
entry.throwable!!.let { crashlytics.recordException(it) }
```

### lateinit

Use `lateinit` for properties that cannot be initialized at construction time
but are guaranteed to be set before first access:

```kotlin
private lateinit var statusTextView: TextView
```

> *Use `lateinit` to avoid null checks when a property is referenced in the
> body of a class.*
>
> --- [Common Kotlin Patterns](https://developer.android.com/kotlin/common-patterns#initialization)

### `!!` Operator

Avoid `!!`. If unavoidable, add a comment explaining **why** it is safe.

---

## Constructor Injection

Prefer constructor injection for dependencies:

```kotlin
class CrashlyticsSink(
  private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance(),
  private val minPriority: Int = Log.WARN,
) : Sink
```

Use default parameter values to provide sensible defaults while keeping the
class testable.

---

## Testing

### Naming

Use backtick-quoted descriptive names:

```kotlin
@Test
fun `log delegates to sink with correct entry`() { }

@Test
fun `sink with isLoggable false is skipped`() { }
```

### Structure

Follow **Arrange-Act-Assert (AAA)** pattern:

```kotlin
@Test
fun `per-log fields override context and global fields`() {
  // Arrange
  val sink = createSink()
  val entrySlot = slot<StructuredLogEntry>()
  every { sink.emit(capture(entrySlot)) } returns Unit
  Timber.plant(StructuredTree(sinks = listOf(sink), globalFields = mapOf("key" to "global")))

  // Act
  StructuredLog.putContext("key", "context")
  StructuredTimber.d("msg", "key" to "per-log")

  // Assert
  assertEquals("per-log", entrySlot.captured.fields["key"])
}
```

### Cleanup

Always clean up shared state in `@After`:

```kotlin
@After
fun tearDown() {
  Timber.uprootAll()
  StructuredLog.clearContext()
}
```

### Mocking

Use [MockK](https://mockk.io/) for mocking. Prefer fakes over mocks when
practical.

---

## Annotations

- Place annotations on separate lines above the declaration
- Multiple annotations targeting the same site may share a line

```kotlin
@Retention(SOURCE)
@Target(FUNCTION, PROPERTY_SETTER, FIELD)
annotation class Global
```

> *Annotations without arguments can be placed on a single line.*
>
> --- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide#annotations)

---

## Enum Classes

Simple enums may be single-line. Enums with members use one-entry-per-line:

```kotlin
enum class LogLevel { VERBOSE, DEBUG, INFO, WARN, ERROR }

enum class LogLevel {
  VERBOSE,
  DEBUG,
  INFO,
  WARN,
  ERROR,
}
```

---

## SAM Conversion

Use lambda syntax for single-abstract-method interfaces:

```kotlin
// Good
button.setOnClickListener { doSomething() }

// Avoid
button.setOnClickListener(object : View.OnClickListener {
  override fun onClick(v: View) {
    doSomething()
  }
})
```

> *You can replace the implementation of a Java single-method interface using
> a lambda expression.*
>
> --- [Common Kotlin Patterns](https://developer.android.com/kotlin/common-patterns#sam)

---

## References

- [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- [Common Kotlin Patterns for Android](https://developer.android.com/kotlin/common-patterns)
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
