package io.github.casl0.structlog.extension.timber

/**
 * Immutable data model representing a single structured log entry.
 *
 * @param priority Log priority ([android.util.Log] constants).
 * @param tag Log tag.
 * @param message Log message.
 * @param throwable Optional throwable.
 * @param fields Merged key-value attributes (global + context + per-log).
 */
data class StructuredLogEntry(
  val priority: Int,
  val tag: String?,
  val message: String,
  val throwable: Throwable?,
  val fields: Map<String, Any?>,
)
