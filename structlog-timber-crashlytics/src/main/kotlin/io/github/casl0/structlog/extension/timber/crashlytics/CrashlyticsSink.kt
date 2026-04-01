package io.github.casl0.structlog.extension.timber.crashlytics

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.github.casl0.structlog.extension.timber.Sink
import io.github.casl0.structlog.extension.timber.StructuredLogEntry

/**
 * A [Sink] that sends structured logs to Firebase Crashlytics.
 * - Key-value fields are set as Crashlytics custom keys.
 * - The log message is recorded via [FirebaseCrashlytics.log].
 * - If a [Throwable] is present, it is recorded as a non-fatal exception.
 *
 * By default, only logs at WARN level and above are sent. Override [minPriority] to change this.
 *
 * @param crashlytics The [FirebaseCrashlytics] instance. Defaults to
 *   [FirebaseCrashlytics.getInstance].
 * @param minPriority Minimum log priority to send. Defaults to [Log.WARN].
 */
class CrashlyticsSink(
  private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance(),
  private val minPriority: Int = Log.WARN,
) : Sink {

  override fun isLoggable(priority: Int): Boolean = priority >= minPriority

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

    crashlytics.log(entry.message)

    entry.throwable?.let { crashlytics.recordException(it) }
  }
}
