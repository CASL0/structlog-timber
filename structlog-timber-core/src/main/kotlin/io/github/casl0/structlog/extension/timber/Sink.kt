package io.github.casl0.structlog.extension.timber

/**
 * Abstraction for structured log destinations.
 *
 * [StructuredTree] delegates each log entry to registered Sinks. Implement this interface to send
 * structured logs to Crashlytics, Logcat, Datadog, or any other backend.
 *
 * Implementations must be safe to call from any thread, as [StructuredTree] may invoke [emit] from
 * whichever thread the log call originates on.
 *
 * @since 1.0.0
 */
interface Sink {

  /**
   * Emit a structured log [entry] to this destination.
   *
   * @param entry The structured log entry to emit.
   * @since 1.0.0
   */
  fun emit(entry: StructuredLogEntry)

  /**
   * Whether this Sink should handle the given log [priority].
   *
   * Defaults to `true` for all priorities. Override to restrict (e.g., only WARN and above).
   *
   * @param priority Log priority ([android.util.Log] constants).
   * @return `true` if this Sink should handle the given priority.
   * @since 1.0.0
   */
  fun isLoggable(priority: Int): Boolean = true
}
