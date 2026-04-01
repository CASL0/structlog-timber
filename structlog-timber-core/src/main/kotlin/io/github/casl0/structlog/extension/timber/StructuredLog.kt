package io.github.casl0.structlog.extension.timber

/**
 * MDC-style thread-local context manager.
 *
 * Values set via [putContext] are automatically included in all subsequent structured log entries
 * on the same thread.
 *
 * ```kotlin
 * StructuredLog.putContext("user_id", userId)
 * StructuredLog.putContext("session_id", sessionId)
 *
 * // All logs below will include user_id and session_id.
 * StructuredTimber.d("Purchase completed", "item_id" to "SKU-123")
 * ```
 */
object StructuredLog {

  private val contextHolder = ThreadLocal<MutableMap<String, Any?>>()

  /** Add a key-value pair to the current thread's context. */
  fun putContext(key: String, value: Any?) {
    getOrCreateContext()[key] = value
  }

  /** Remove a [key] from the current thread's context. */
  fun removeContext(key: String) {
    contextHolder.get()?.remove(key)
  }

  /** Clear all entries from the current thread's context. */
  fun clearContext() {
    contextHolder.remove()
  }

  /** Return the current context as a read-only view. No defensive copy is made. */
  internal fun snapshot(): Map<String, Any?> {
    return contextHolder.get().orEmpty()
  }

  private fun getOrCreateContext(): MutableMap<String, Any?> {
    var map = contextHolder.get()
    if (map == null) {
      map = mutableMapOf()
      contextHolder.set(map)
    }
    return map
  }
}
