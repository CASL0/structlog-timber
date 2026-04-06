package io.github.casl0.structlog.extension.timber

/**
 * MDC-style thread-local context manager for structured logging.
 *
 * Values set via [putLogContext] are automatically included in all subsequent structured log
 * entries on the same thread. Each thread maintains its own independent context, so this object is
 * safe to use from multiple threads concurrently without synchronization.
 *
 * ```kotlin
 * StructuredLog.putLogContext("user_id", userId)
 * StructuredLog.putLogContext("session_id", sessionId)
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
   * via [removeLogContext] or [clearLogContext].
   *
   * @param key The context key.
   * @param value The context value. May be `null`.
   * @since 2.0.0
   */
  fun putLogContext(key: String, value: Any?) {
    getOrCreateContext()[key] = value
  }

  /**
   * Remove a key from the current thread's context.
   *
   * No-op if [key] does not exist.
   *
   * @param key The context key to remove.
   * @since 2.0.0
   */
  fun removeLogContext(key: String) {
    contextHolder.get()?.remove(key)
  }

  /**
   * Clear all entries from the current thread's context.
   *
   * @since 2.0.0
   */
  fun clearLogContext() {
    contextHolder.remove()
  }

  /**
   * Execute [block] with the given [entries] added to the current thread's context.
   *
   * The entries are added before [block] runs and removed (or restored to their previous values)
   * after [block] completes, even if it throws an exception. This prevents context leaks compared
   * to manual [putLogContext] / [removeLogContext] pairs.
   *
   * **Thread constraint:** Because the context is backed by a [ThreadLocal], [block] must complete
   * entirely on the calling thread. Do not switch coroutine dispatchers (e.g.,
   * `kotlinx.coroutines.withContext(Dispatchers.IO)`) inside [block]; context entries will not be
   * visible on the new thread.
   *
   * ```kotlin
   * StructuredLog.withLogContext("request_id" to requestId, "user_id" to userId) {
   *     // All logs within this block include request_id and user_id.
   *     StructuredTimber.d("Processing request", "action" to "checkout")
   * }
   * // request_id and user_id are automatically removed here.
   * ```
   *
   * @param entries Key-value pairs to add to the context for the duration of [block].
   * @param block The block to execute. Scoped entries are available to all [StructuredTimber] calls
   *   made on this thread within [block]; no argument is passed.
   * @return The result of [block].
   * @since 2.0.0
   */
  fun <R> withLogContext(vararg entries: Pair<String, Any?>, block: () -> R): R {
    // Snapshot pre-mutation state so duplicate keys in entries don't corrupt restoration.
    val snapshot = contextHolder.get()?.toMap().orEmpty()

    for ((key, value) in entries) {
      putLogContext(key, value)
    }

    try {
      return block()
    } finally {
      for ((key, _) in entries) {
        if (key in snapshot) {
          putLogContext(key, snapshot[key])
        } else {
          removeLogContext(key)
        }
      }
    }
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
