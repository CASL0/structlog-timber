package io.github.casl0.structlog.extension.timber.crashlytics

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.github.casl0.structlog.extension.timber.StructuredLogEntry
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashlyticsSinkTest {

  private val crashlytics = mockk<FirebaseCrashlytics>(relaxed = true)

  @Test
  fun `isLoggable returns false below minPriority`() {
    val sink = CrashlyticsSink(crashlytics = crashlytics, minPriority = Log.WARN)

    assertFalse(sink.isLoggable(Log.DEBUG))
    assertFalse(sink.isLoggable(Log.INFO))
    assertTrue(sink.isLoggable(Log.WARN))
    assertTrue(sink.isLoggable(Log.ERROR))
  }

  @Test
  fun `emit sets custom keys and logs message`() {
    val sink = CrashlyticsSink(crashlytics = crashlytics)
    val entry =
      StructuredLogEntry(
        priority = Log.WARN,
        tag = "Test",
        message = "something happened",
        throwable = null,
        fields = mapOf("user_id" to "u-1", "count" to 42),
      )

    sink.emit(entry)

    verify { crashlytics.setCustomKey("user_id", "u-1") }
    verify { crashlytics.setCustomKey("count", 42) }
    verify { crashlytics.log("something happened") }
    verify(exactly = 0) { crashlytics.recordException(any()) }
  }

  @Test
  fun `emit records exception when throwable is present`() {
    val sink = CrashlyticsSink(crashlytics = crashlytics)
    val exception = RuntimeException("boom")
    val entry =
      StructuredLogEntry(
        priority = Log.ERROR,
        tag = null,
        message = "error occurred",
        throwable = exception,
        fields = emptyMap(),
      )

    sink.emit(entry)

    verify { crashlytics.log("error occurred") }
    verify { crashlytics.recordException(exception) }
  }

  @Test
  fun `emit handles typed values correctly`() {
    val sink = CrashlyticsSink(crashlytics = crashlytics)
    val entry =
      StructuredLogEntry(
        priority = Log.WARN,
        tag = null,
        message = "typed",
        throwable = null,
        fields =
          mapOf(
            "bool" to true,
            "int" to 1,
            "long" to 2L,
            "float" to 3.0f,
            "double" to 4.0,
            "string" to "hello",
            "other" to listOf(1, 2, 3),
          ),
      )

    sink.emit(entry)

    verify { crashlytics.setCustomKey("bool", true) }
    verify { crashlytics.setCustomKey("int", 1) }
    verify { crashlytics.setCustomKey("long", 2L) }
    verify { crashlytics.setCustomKey("float", 3.0f) }
    verify { crashlytics.setCustomKey("double", 4.0) }
    verify { crashlytics.setCustomKey("string", "hello") }
    verify { crashlytics.setCustomKey("other", "[1, 2, 3]") }
  }

  @Test
  fun `emit with empty fields only logs message`() {
    val sink = CrashlyticsSink(crashlytics = crashlytics)
    val entry =
      StructuredLogEntry(
        priority = Log.WARN,
        tag = null,
        message = "no fields",
        throwable = null,
        fields = emptyMap(),
      )

    sink.emit(entry)

    verify { crashlytics.log("no fields") }
    verify(exactly = 0) { crashlytics.setCustomKey(any(), any<String>()) }
    verify(exactly = 0) { crashlytics.setCustomKey(any(), any<Boolean>()) }
    verify(exactly = 0) { crashlytics.setCustomKey(any(), any<Int>()) }
    verify(exactly = 0) { crashlytics.setCustomKey(any(), any<Long>()) }
    verify(exactly = 0) { crashlytics.setCustomKey(any(), any<Float>()) }
    verify(exactly = 0) { crashlytics.setCustomKey(any(), any<Double>()) }
  }
}
