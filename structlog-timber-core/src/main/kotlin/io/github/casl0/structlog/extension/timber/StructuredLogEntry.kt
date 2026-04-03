package io.github.casl0.structlog.extension.timber

/**
 * Immutable data model representing a single structured log entry.
 *
 * Instances are created by [StructuredTree] and passed to each [Sink]. This class is a
 * [data class], so [equals], [hashCode], [toString], and [copy] are generated automatically.
 *
 * @param priority Log priority ([android.util.Log] constants).
 * @param tag Log tag, or `null` if not set by the caller.
 * @param message Log message.
 * @param throwable Optional throwable, or `null` if none was provided.
 * @param fields Merged key-value attributes (global + context + per-log).
 * @since 1.0.0
 */
data class StructuredLogEntry(
  val priority: Int,
  val tag: String?,
  val message: String,
  val throwable: Throwable?,
  val fields: Map<String, Any?>,
)
