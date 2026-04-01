package io.github.casl0.structlog.extension.timber

/**
 * Abstraction for structured log destinations.
 *
 * [StructuredTree] delegates each log entry to registered Sinks. Implement this interface to send
 * structured logs to Crashlytics, Logcat, Datadog, or any other backend.
 */
interface Sink {

  /** Emit a structured log [entry] to this destination. */
  fun emit(entry: StructuredLogEntry)

  /**
   * Whether this Sink should handle the given log [priority].
   *
   * Defaults to `true` for all priorities. Override to restrict (e.g., only WARN and above).
   *
   * @param priority Log priority ([android.util.Log] constants).
   */
  fun isLoggable(priority: Int): Boolean = true
}
