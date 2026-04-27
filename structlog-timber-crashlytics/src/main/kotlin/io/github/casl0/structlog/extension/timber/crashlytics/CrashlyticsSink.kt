package io.github.casl0.structlog.extension.timber.crashlytics

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.github.casl0.structlog.extension.timber.Sink
import io.github.casl0.structlog.extension.timber.StructuredLogEntry
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

/**
 * A [Sink] that sends structured logs to Firebase Crashlytics.
 * - Key-value fields are set as Crashlytics custom keys.
 * - The log message and fields are recorded as a single flat JSON breadcrumb via
 *   [FirebaseCrashlytics.log], with the message stored under the `event` key.
 * - If a [Throwable] is present, it is recorded as a non-fatal exception.
 *
 * By default, only logs at WARN level and above are sent. Override [minPriority] to change this.
 *
 * This class is thread-safe; all operations delegate to [FirebaseCrashlytics] which is itself
 * thread-safe.
 *
 * @param crashlytics The [FirebaseCrashlytics] instance. Defaults to
 *   [FirebaseCrashlytics.getInstance].
 * @param minPriority Minimum log priority to send. Defaults to [Log.WARN].
 * @since 1.0.0
 */
class CrashlyticsSink(
  private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance(),
  private val minPriority: Int = Log.WARN,
) : Sink {

  /**
   * Return `true` if [priority] is at or above [minPriority].
   *
   * @param priority Log priority to check.
   * @return `true` if [priority] >= [minPriority].
   * @since 1.0.0
   */
  override fun isLoggable(priority: Int): Boolean = priority >= minPriority

  /**
   * Emit the structured log [entry] to Firebase Crashlytics.
   *
   * Fields are set as custom keys with type-aware conversion for [Boolean], [Int], [Long], [Float],
   * [Double], and [String]. All other types are converted via [Any.toString].
   *
   * The breadcrumb log records a single JSON object containing the message under the `event` key
   * plus all fields at the top level. If a field key collides with `event`, the field wins.
   *
   * @param entry The structured log entry to emit.
   * @since 1.0.0
   */
  override fun emit(entry: StructuredLogEntry) {
    for ((key, value) in entry.fields) {
      when (value) {
        is Boolean -> crashlytics.setCustomKey(key, value)
        is Int -> crashlytics.setCustomKey(key, value)
        is Long -> crashlytics.setCustomKey(key, value)
        is Float -> crashlytics.setCustomKey(key, value)
        is Double -> crashlytics.setCustomKey(key, value)
        is String -> crashlytics.setCustomKey(key, value)
        else -> crashlytics.setCustomKey(key, value.toString())
      }
    }

    crashlytics.log(buildBreadcrumbJson(entry))

    entry.throwable?.let { crashlytics.recordException(it) }
  }

  /**
   * Build a flat JSON breadcrumb with the message under the `event` key and fields merged at the
   * top level.
   *
   * @param entry The structured log entry to render.
   * @return Compact JSON string.
   */
  private fun buildBreadcrumbJson(entry: StructuredLogEntry): String =
    buildJsonObject {
        put(EVENT_KEY, JsonPrimitive(entry.message))
        for ((key, value) in entry.fields) {
          put(key, value.toJsonElement())
        }
      }
      .toString()

  private companion object {
    /** Key under which the log message is stored in the breadcrumb JSON. */
    private const val EVENT_KEY = "event"
  }
}

/** Convert an arbitrary value into a [JsonElement], falling back to [Any.toString] for unknowns. */
private fun Any?.toJsonElement(): JsonElement =
  when (this) {
    null -> JsonNull
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Map<*, *> ->
      buildJsonObject {
        for ((k, v) in this@toJsonElement) {
          put(k.toString(), v.toJsonElement())
        }
      }
    is Iterable<*> -> buildJsonArray { for (v in this@toJsonElement) add(v.toJsonElement()) }
    else -> JsonPrimitive(toString())
  }
