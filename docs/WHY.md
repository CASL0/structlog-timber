# Why structlog-timber?

Is `Log.d(TAG, "something happened")` enough for your Android app?
This document explains why structured logging matters and what problems structlog-timber solves.

---

## 1. Logs in the AI Agent Era: Firebase MCP Integration

Firebase provides an MCP (Model Context Protocol) server that lets AI agents
read and analyze Crashlytics crash reports and events directly.

```
AI Agent ──(MCP)──► Firebase Crashlytics
    │
    └──► Analyze structured fields
```

When logs are unstructured text, AI agents can only work with raw strings.
Discrete fields make the difference:

| Log format | What an AI agent can extract |
|------------|------------------------------|
| `"Purchase failed for user"` | A message string only |
| `"Purchase failed" {user_id=U-42, item_id=SKU-123, error_code=E001}` | User, item, and error code as queryable fields |

With structured logs, agents can parse individual fields and automatically
surface patterns — "payment errors are concentrated on a specific item" or
"crashes spike on a particular OS version" — without any manual string parsing.

**Unstructured logs are for humans. Structured logs serve humans and machines alike.**

---

## 2. Faster Crash Triage with Crashlytics Custom Keys

Crashlytics custom keys are the single most effective tool for crash triage.
A stack trace tells you *where* the crash happened; custom keys tell you *why* —
which user, which state, which input triggered it.

Without custom keys, a `NullPointerException` in `PaymentProcessor.process()` is
one of thousands. With keys like `gateway=stripe`, `user_tier=premium`, and
`retry_count=3`, the same crash becomes an actionable report: "premium users
hitting Stripe's rate limit on the third retry."

### The problem with manual `setCustomKey()`

Firebase's API requires you to call `setCustomKey()` one key at a time, and
each call is a global side effect that persists until overwritten:

```kotlin
// Scattered throughout the codebase — easy to forget, hard to maintain
FirebaseCrashlytics.getInstance().setCustomKey("gateway", "stripe")
FirebaseCrashlytics.getInstance().setCustomKey("retry_count", 3)
FirebaseCrashlytics.getInstance().setCustomKey("user_tier", "premium")
```

Keys set in one screen leak into crashes on another screen. Keys added by
one developer are overwritten silently by another. There is no scoping, no
cleanup, and no connection between the keys and the log message they describe.

### How structlog-timber solves this

`CrashlyticsSink` sends structured fields as custom keys automatically,
scoped to the log entry they belong to:

```kotlin
StructuredTimber.e(
    RuntimeException("Payment declined"),
    "Payment failed",
    "gateway" to "stripe",
    "retry_count" to 3,
    "user_tier" to "premium",
)
```

- Fields are set just before the exception is recorded — no stale keys from
  a previous screen or flow.
- Type-aware: `Boolean`, `Int`, `Long`, `Float`, `Double`, and `String` are
  sent with their native Crashlytics types, preserving dashboard filtering and
  sorting.
- Combined with global fields and thread-local context, every crash report
  automatically includes `app_version`, `session_id`, and whatever context
  you have set — without a single manual `setCustomKey()` call.

---

## 3. Consistent Logs: Global Fields and Thread-Local Context

In real apps, you almost always want common metadata on every log entry.

```kotlin
// Set once at app startup
StructuredTimber.init(
    LogcatSink(),
    globalFields = mapOf(
        "app_version" to BuildConfig.VERSION_NAME,
        "build_type" to BuildConfig.BUILD_TYPE,
    ),
)

// Set context on screen transition
StructuredLog.putLogContext("screen", "checkout")
StructuredLog.putLogContext("session_id", sessionId)
```

Every subsequent log on that thread automatically includes `app_version`, `build_type`, `screen`,
and `session_id`. No need to pass them manually on every call — no more forgotten fields.

For scoped context that cleans itself up automatically, use `withLogContext`:

```kotlin
StructuredLog.withLogContext("request_id" to requestId, "user_id" to userId) {
    // All logs within this block include request_id and user_id.
    StructuredTimber.d("Processing request", "action" to "checkout")
}
// request_id and user_id are removed automatically, even if the block throws.
```

---

## 4. Full Timber Compatibility

structlog-timber is a Timber `Tree`. It slots into any existing Timber setup without code changes.

- Existing `Timber.d()` / `Timber.e()` calls continue to work as-is.
- Plain logging without structured fields is fully supported.
- Migrate incrementally — introduce structured fields file by file, not all at once.

```kotlin
// Existing code: works unchanged
Timber.d("Simple log")

// New code: add structured fields where they matter
StructuredTimber.d("Structured log", "key" to "value")
```

---

## 5. Destination Abstraction via Sinks

Log destinations change over an app's lifetime — Logcat during development,
Crashlytics in production, and potentially Datadog or Cloud Logging next year.

Implement the `Sink` interface to add or swap destinations without touching call-site code.

```kotlin
// Development
StructuredTimber.init(LogcatSink())

// Production — add sinks by changing the init() call
StructuredTimber.init(
    CrashlyticsSink(minPriority = Log.WARN),
    DatadogSink(),   // your custom Sink implementation
)
```

---

## 6. Testability

The `Sink` interface makes log output easy to verify in tests.
Implement a simple `FakeSink` that collects entries in a list:

```kotlin
class FakeSink : Sink {
    val entries = mutableListOf<StructuredLogEntry>()
    override fun emit(entry: StructuredLogEntry) { entries += entry }
}

val fakeSink = FakeSink()
StructuredTimber.init(fakeSink)

StructuredTimber.i("test", "key" to "value")

assertEquals("test", fakeSink.entries.first().message)
assertEquals("value", fakeSink.entries.first().fields["key"])
```

Assert on log content without mocking any Android framework classes.

---

## 7. Thread-Safe by Design

Both `StructuredTimber` and `StructuredLog` store state in `ThreadLocal`.
Each thread manages its own pending fields and context independently —
no explicit locking or synchronization is required from the caller.

`StructuredTree` is also main-safe: it performs no blocking I/O or heavy
computation on the calling thread. Sinks that need to perform network I/O
should offload that work to a background thread themselves.

---

## 8. A First Step Toward Mobile Observability

Observability is no longer just for backend services.
Structured logging is one of the three pillars of observability, alongside metrics and traces.

By adopting structlog-timber, you get:

- **Searchable logs** — filter crash reports and events by specific field values
- **Aggregatable logs** — compute statistics per field (error rates by gateway, crashes by OS version)
- **Correlatable logs** — attach a `session_id` or `request_id` as a context field to tie related log entries together

Bring the structured logging practices that backend teams take for granted to Android.

---

## Summary

| Problem | How structlog-timber solves it |
|---------|-------------------------------|
| AI agents cannot parse unstructured logs | Discrete key-value fields are machine-readable |
| Difficult to filter crashes in Crashlytics | Structured fields sent as custom keys automatically |
| Common metadata missing or forgotten in logs | Global fields and thread-local context auto-attached to every entry |
| Context cleanup requires manual bookkeeping | `withLogContext` scopes context to a block and cleans up automatically |
| Changing log destinations requires code changes | Sink abstraction decouples destinations from call sites |
| Hard to test log output | Implement `FakeSink` to assert on entries without mocking the Android framework |
| Existing Timber codebase requires a full rewrite | `StructuredTree` is a Timber `Tree` — migrate incrementally |
