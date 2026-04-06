package io.github.casl0.structlog.extension.timber

import timber.log.Timber

/**
 * Entry point for structured logging.
 *
 * Wraps [Timber] to pass key-value fields alongside log messages. Fields are stored in a
 * [ThreadLocal] and consumed by [StructuredTree]. Each thread maintains its own pending fields, so
 * this object is safe to use from multiple threads concurrently without synchronization.
 *
 * Call [init] to set up structured logging before using any logging methods:
 * ```kotlin
 * StructuredTimber.init(
 *     LogcatSink(minPriority = Log.DEBUG),
 *     globalFields = mapOf("app_version" to BuildConfig.VERSION_NAME),
 * )
 * ```
 *
 * Then log messages with structured fields:
 * ```kotlin
 * StructuredTimber.d("Purchase completed",
 *     "item_id" to "SKU-123",
 *     "price" to 1980,
 *     "currency" to "JPY"
 * )
 * ```
 *
 * Alternatively, you can plant a [StructuredTree] manually via [Timber.plant] for advanced use
 * cases.
 *
 * @since 1.0.0
 */
object StructuredTimber {

  private val pendingFields = ThreadLocal<MutableMap<String, Any?>>()

  private var tree: StructuredTree? = null

  /**
   * Initialize structured logging by planting a [StructuredTree] with the given [sinks].
   *
   * This method is idempotent: calling it again uproots the previous [StructuredTree] before
   * planting a new one. Other [Timber.Tree] instances planted separately are not affected.
   *
   * This is a convenience method that replaces the manual `Timber.plant(StructuredTree(...))` call.
   *
   * ```kotlin
   * StructuredTimber.init(
   *     LogcatSink(minPriority = Log.DEBUG),
   *     CrashlyticsSink(minPriority = Log.WARN),
   *     globalFields = mapOf("app_version" to BuildConfig.VERSION_NAME),
   * )
   * ```
   *
   * @param sinks One or more [Sink] destinations for structured log entries.
   * @param globalFields Attributes automatically attached to every log entry. Defaults to an empty
   *   map.
   * @throws IllegalArgumentException if [sinks] is empty.
   * @since 2.1.0
   */
  fun init(vararg sinks: Sink, globalFields: Map<String, Any?> = emptyMap()) {
    require(sinks.isNotEmpty()) { "At least one Sink must be provided" }
    tree?.let {
      if (Timber.forest().contains(it)) {
        Timber.uproot(it)
      }
    }
    val newTree = StructuredTree(sinks = sinks.toList(), globalFields = globalFields)
    tree = newTree
    Timber.plant(newTree)
  }

  /**
   * Log a structured message at VERBOSE level.
   *
   * @param message The log message.
   * @param fields Key-value pairs to attach to this log entry.
   * @since 1.0.0
   */
  fun v(message: String, vararg fields: Pair<String, Any?>) = log(fields) { Timber.v(message) }

  /**
   * Log a structured message at DEBUG level.
   *
   * @param message The log message.
   * @param fields Key-value pairs to attach to this log entry.
   * @since 1.0.0
   */
  fun d(message: String, vararg fields: Pair<String, Any?>) = log(fields) { Timber.d(message) }

  /**
   * Log a structured message at INFO level.
   *
   * @param message The log message.
   * @param fields Key-value pairs to attach to this log entry.
   * @since 1.0.0
   */
  fun i(message: String, vararg fields: Pair<String, Any?>) = log(fields) { Timber.i(message) }

  /**
   * Log a structured message at WARN level.
   *
   * @param message The log message.
   * @param fields Key-value pairs to attach to this log entry.
   * @since 1.0.0
   */
  fun w(message: String, vararg fields: Pair<String, Any?>) = log(fields) { Timber.w(message) }

  /**
   * Log a structured message at ERROR level.
   *
   * @param message The log message.
   * @param fields Key-value pairs to attach to this log entry.
   * @since 1.0.0
   */
  fun e(message: String, vararg fields: Pair<String, Any?>) = log(fields) { Timber.e(message) }

  /**
   * Log a structured message at ERROR level with a [Throwable].
   *
   * @param t The throwable to log.
   * @param message The log message.
   * @param fields Key-value pairs to attach to this log entry.
   * @since 1.0.0
   */
  fun e(t: Throwable, message: String, vararg fields: Pair<String, Any?>) =
    log(fields) { Timber.e(t, message) }

  /**
   * Log a structured message at WTF level.
   *
   * @param message The log message.
   * @param fields Key-value pairs to attach to this log entry.
   * @since 1.0.0
   */
  fun wtf(message: String, vararg fields: Pair<String, Any?>) = log(fields) { Timber.wtf(message) }

  /**
   * Return a [Tagged] builder for the given [tag].
   *
   * ```kotlin
   * StructuredTimber.tag("Checkout").d("done", "item_id" to "SKU-123")
   * ```
   *
   * @param tag The log tag applied to all messages from the returned instance.
   * @return A [Tagged] instance that prefixes every log call with [tag].
   * @since 1.0.0
   */
  fun tag(tag: String): Tagged = Tagged(tag)

  /**
   * Consume and return pending fields set by the most recent log call on this thread.
   *
   * After this call, the thread-local pending fields are cleared.
   *
   * @return The pending fields, or an empty map if none were set.
   * @since 1.0.0
   */
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
   * Obtain an instance via [StructuredTimber.tag]. This class is safe to use from multiple threads
   * concurrently because it delegates field storage to a thread-local.
   *
   * @param tag The log tag applied to all messages from this instance.
   * @since 1.0.0
   */
  class Tagged internal constructor(private val tag: String) {

    /**
     * Log a structured message at VERBOSE level with the preset tag.
     *
     * @param message The log message.
     * @param fields Key-value pairs to attach to this log entry.
     * @since 1.0.0
     */
    fun v(message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).v(message) }

    /**
     * Log a structured message at DEBUG level with the preset tag.
     *
     * @param message The log message.
     * @param fields Key-value pairs to attach to this log entry.
     * @since 1.0.0
     */
    fun d(message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).d(message) }

    /**
     * Log a structured message at INFO level with the preset tag.
     *
     * @param message The log message.
     * @param fields Key-value pairs to attach to this log entry.
     * @since 1.0.0
     */
    fun i(message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).i(message) }

    /**
     * Log a structured message at WARN level with the preset tag.
     *
     * @param message The log message.
     * @param fields Key-value pairs to attach to this log entry.
     * @since 1.0.0
     */
    fun w(message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).w(message) }

    /**
     * Log a structured message at ERROR level with the preset tag.
     *
     * @param message The log message.
     * @param fields Key-value pairs to attach to this log entry.
     * @since 1.0.0
     */
    fun e(message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).e(message) }

    /**
     * Log a structured message at ERROR level with the preset tag and a [Throwable].
     *
     * @param t The throwable to log.
     * @param message The log message.
     * @param fields Key-value pairs to attach to this log entry.
     * @since 1.0.0
     */
    fun e(t: Throwable, message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).e(t, message) }

    /**
     * Log a structured message at WTF level with the preset tag.
     *
     * @param message The log message.
     * @param fields Key-value pairs to attach to this log entry.
     * @since 1.0.0
     */
    fun wtf(message: String, vararg fields: Pair<String, Any?>) =
      log(fields) { Timber.tag(tag).wtf(message) }
  }
}
