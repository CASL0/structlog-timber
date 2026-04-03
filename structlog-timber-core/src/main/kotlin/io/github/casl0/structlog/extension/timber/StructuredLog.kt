package io.github.casl0.structlog.extension.timber

/**
 * MDC-style thread-local context manager for structured logging.
 *
 * Values set via [putContext] are automatically included in all subsequent structured log entries
 * on the same thread. Each thread maintains its own independent context, so this object is safe to
 * use from multiple threads concurrently without synchronization.
 *
 * ```kotlin
 * StructuredLog.putContext("user_id", userId)
 * StructuredLog.putContext("session_id", sessionId)
 *
 * // All logs below will include user_id and session_id.
 * StructuredTimber.d("Purchase completed", "item_id" to "SKU-123")
 * ```
 *
 * @since 1.0.0
 */
object StructuredLog {

  private val contextHolder = ThreadLocal<MutableMap<String, Any?>>()

  /**
   * Add a key-value pair to the current thread's context.
   *
   * If [key] already exists, its value is overwritten. The entry persists until explicitly removed
   * via [removeContext] or [clearContext].
   *
   * @param key The context key.
   * @param value The context value. May be `null`.
   * @since 1.0.0
   */
  fun putContext(key: String, value: Any?) {
    getOrCreateContext()[key] = value
  }

  /**
   * Remove a key from the current thread's context.
   *
   * No-op if [key] does not exist.
   *
   * @param key The context key to remove.
   * @since 1.0.0
   */
  fun removeContext(key: String) {
    contextHolder.get()?.remove(key)
  }

  /**
   * Clear all entries from the current thread's context.
   *
   * @since 1.0.0
   */
  fun clearContext() {
    contextHolder.remove()
  }

  /**
   * Return the current context as a read-only view.
   *
   * No defensive copy is made. Because the context is thread-local, the returned map is safe to
   * read on the calling thread but must not be stored for later use on a different thread.
   *
   * @return The current context map, or an empty map if no context has been set.
   * @since 1.0.0
   */
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
