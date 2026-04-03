package io.github.casl0.structlog.extension.timber

import timber.log.Timber

/**
 * Entry point for structured logging.
 *
 * Wraps [Timber] to pass key-value fields alongside log messages. Fields are stored in a
 * [ThreadLocal] and consumed by [StructuredTree].
 *
 * ```kotlin
 * StructuredTimber.d("Purchase completed",
 *     "item_id" to "SKU-123",
 *     "price" to 1980,
 *     "currency" to "JPY"
 * )
 * ```
 */
object StructuredTimber {

  private val pendingFields = ThreadLocal<MutableMap<String, Any?>>()

  /** Log a structured message at VERBOSE level. */
  fun v(message: String, vararg fields: Pair<String, Any?>) = log(fields) { Timber.v(message) }

  /** Log a structured message at DEBUG level. */
  fun d(message: String, vararg fields: Pair<String, Any?>) = log(fields) { Timber.d(message) }

  /** Log a structured message at INFO level. */
  fun i(message: String, vararg fields: Pair<String, Any?>) = log(fields) { Timber.i(message) }

  /** Log a structured message at WARN level. */
  fun w(message: String, vararg fields: Pair<String, Any?>) = log(fields) { Timber.w(message) }

  /** Log a structured message at ERROR level. */
  fun e(message: String, vararg fields: Pair<String, Any?>) = log(fields) { Timber.e(message) }

  /** Log a structured message at ERROR level with a [Throwable]. */
  fun e(t: Throwable, message: String, vararg fields: Pair<String, Any?>) =
    log(fields) { Timber.e(t, message) }

  /** Log a structured message at WTF level. */
  fun wtf(message: String, vararg fields: Pair<String, Any?>) = log(fields) { Timber.wtf(message) }

  /**
   * Return a [Tagged] builder for the given [tag].
   *
   * ```kotlin
   * StructuredTimber.tag("Checkout").d("done", "item_id" to "SKU-123")
   * ```
   */
  fun tag(tag: String): Tagged = Tagged(tag)

  /** Consume and return pending fields set by the most recent log call on this thread. */
  internal fun consumePendingFields(): Map<String, Any?> {
    val fields = pendingFields.get() ?: return emptyMap()
    pendingFields.remove()
    return fields
  }

  private inline fun log(fields: Array<out Pair<String, Any?>>, action: () -> Unit) {
    setPendingFields(fields)
    action()
  }

  private fun setPendingFields(fields: Array<out Pair<String, Any?>>) {
    if (fields.isNotEmpty()) {
      val map = HashMap<String, Any?>(fields.size * 2)
      for ((k, v) in fields) map[k] = v
      pendingFields.set(map)
    }
  }

  /**
   * A structured logger that prefixes every log call with a fixed [tag].
   *
   * Obtain an instance via [StructuredTimber.tag].
   *
   * @param tag The log tag applied to all messages from this instance.
   */
  class Tagged internal constructor(private val tag: String) {

    /** Log a structured message at VERBOSE level with the preset tag. */
    fun v(message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).v(message) }

    /** Log a structured message at DEBUG level with the preset tag. */
    fun d(message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).d(message) }

    /** Log a structured message at INFO level with the preset tag. */
    fun i(message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).i(message) }

    /** Log a structured message at WARN level with the preset tag. */
    fun w(message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).w(message) }

    /** Log a structured message at ERROR level with the preset tag. */
    fun e(message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).e(message) }

    /** Log a structured message at ERROR level with the preset tag and a [Throwable]. */
    fun e(t: Throwable, message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).e(t, message) }

    /** Log a structured message at WTF level with the preset tag. */
    fun wtf(message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).wtf(message) }
  }
}
