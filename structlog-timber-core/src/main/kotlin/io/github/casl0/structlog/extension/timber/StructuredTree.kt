package io.github.casl0.structlog.extension.timber

import timber.log.Timber

/**
 * A [Timber.Tree] that supports structured logging.
 *
 * Merges per-log fields from [StructuredTimber], thread-local context from [StructuredLog], and
 * [globalFields], then delegates to each registered [Sink]. This class is safe to use from multiple
 * threads concurrently; field merging is performed on the calling thread using thread-local state.
 *
 * ```kotlin
 * Timber.plant(
 *     StructuredTree(
 *         sinks = listOf(LogcatSink(), CrashlyticsSink()),
 *         globalFields = mapOf("app_version" to BuildConfig.VERSION_NAME)
 *     )
 * )
 * ```
 *
 * @param sinks Log destinations. Must not be empty.
 * @param globalFields Attributes automatically attached to every log entry. Defaults to an empty
 *   map.
 * @since 1.0.0
 */
class StructuredTree(
  private val sinks: List<Sink>,
  private val globalFields: Map<String, Any?> = emptyMap(),
) : Timber.Tree() {

  /**
   * Merge per-log, context, and global fields into a [StructuredLogEntry] and dispatch to each
   * registered [Sink] whose [Sink.isLoggable] returns `true`.
   *
   * Field merge priority (highest wins): per-log fields > context fields > global fields.
   *
   * @param priority Log priority ([android.util.Log] constants).
   * @param tag Log tag, or `null` if not set.
   * @param message The formatted log message.
   * @param t Optional throwable.
   * @since 1.0.0
   */
  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    val perLogFields = StructuredTimber.consumePendingFields()
    val contextFields = StructuredLog.snapshot()

    val mergedFields =
      when {
        globalFields.isEmpty() && contextFields.isEmpty() -> perLogFields
        globalFields.isEmpty() && perLogFields.isEmpty() -> contextFields
        else ->
          buildMap(globalFields.size + contextFields.size + perLogFields.size) {
            putAll(globalFields)
            putAll(contextFields)
            putAll(perLogFields)
          }
      }

    val entry =
      StructuredLogEntry(
        priority = priority,
        tag = tag,
        message = message,
        throwable = t,
        fields = mergedFields,
      )

    for (sink in sinks) {
      if (sink.isLoggable(priority)) {
        sink.emit(entry)
      }
    }
  }
}
