package io.github.casl0.structlog.extension.timber

import timber.log.Timber

/**
 * A [Timber.Tree] that supports structured logging.
 *
 * Merges per-log fields from [StructuredTimber], thread-local context from [StructuredLog], and
 * [globalFields], then delegates to each registered [Sink].
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
 * @param sinks Log destinations.
 * @param globalFields Attributes automatically attached to every log entry.
 */
class StructuredTree(
  private val sinks: List<Sink>,
  private val globalFields: Map<String, Any?> = emptyMap(),
) : Timber.Tree() {

  /**
   * Merge per-log, context, and global fields into a [StructuredLogEntry] and dispatch to each
   * registered [Sink] whose [Sink.isLoggable] returns `true`.
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
