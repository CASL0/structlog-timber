package io.github.casl0.structlog.extension.timber.logcat

import android.util.Log
import io.github.casl0.structlog.extension.timber.Sink
import io.github.casl0.structlog.extension.timber.StructuredLogEntry

/**
 * A [Sink] that writes structured logs to Android Logcat.
 *
 * Output format:
 * ```
 * D/Checkout: Purchase completed {item_id=SKU-123, price=1980, currency=JPY}
 * ```
 *
 * @param minPriority Minimum log priority to emit. Defaults to [Log.WARN].
 */
class LogcatSink(private val minPriority: Int = Log.WARN) : Sink {

  companion object {
    private const val DEFAULT_TAG = "StructuredLog"
  }

  override fun isLoggable(priority: Int): Boolean = priority >= minPriority

  /** Emit the structured log [entry] to Logcat with formatted key-value fields. */
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
