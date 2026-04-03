package io.github.casl0.structlog.extension.timber.logcat

import android.util.Log
import io.github.casl0.structlog.extension.timber.Sink
import io.github.casl0.structlog.extension.timber.StructuredLogEntry

/**
 * A [Sink] that writes structured logs to Android Logcat.
 *
 * This class is thread-safe; it performs no mutable state changes and delegates to [Log.println]
 * which is itself thread-safe.
 *
 * Output format:
 * ```
 * D/Checkout: Purchase completed {item_id=SKU-123, price=1980, currency=JPY}
 * ```
 *
 * @param minPriority Minimum log priority to emit. Defaults to [Log.WARN].
 * @since 1.0.0
 */
class LogcatSink(private val minPriority: Int = Log.WARN) : Sink {

  companion object {
    private const val DEFAULT_TAG = "StructuredLog"
  }

  /**
   * Return `true` if [priority] is at or above [minPriority].
   *
   * @param priority Log priority to check.
   * @return `true` if [priority] >= [minPriority].
   * @since 1.0.0
   */
  override fun isLoggable(priority: Int): Boolean = priority >= minPriority

  /**
   * Emit the structured log [entry] to Logcat with formatted key-value fields.
   *
   * @param entry The structured log entry to emit.
   * @since 1.0.0
   */
  override fun emit(entry: StructuredLogEntry) {
    val tag = entry.tag ?: DEFAULT_TAG
    val fieldsStr =
      if (entry.fields.isNotEmpty()) {
        " " + entry.fields.entries.joinToString(prefix = "{", postfix = "}") { (k, v) -> "$k=$v" }
      } else {
        ""
      }

    val fullMessage = "${entry.message}$fieldsStr"

    if (entry.throwable != null) {
      Log.println(entry.priority, tag, "$fullMessage\n${Log.getStackTraceString(entry.throwable)}")
    } else {
      Log.println(entry.priority, tag, fullMessage)
    }
  }
}
