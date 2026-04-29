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
  fun `emit sets custom keys and logs JSON breadcrumb`() {
    val sink = CrashlyticsSink(crashlytics = crashlytics)
    val entry =
      StructuredLogEntry(
        priority = Log.WARN,
        tag = "Test",
        message = "something happened",
        throwable = null,
        fields = linkedMapOf("user_id" to "u-1", "count" to 42),
      )

    sink.emit(entry)

    verify { crashlytics.setCustomKey("user_id", "u-1") }
    verify { crashlytics.setCustomKey("count", 42) }
    verify { crashlytics.log("""{"event":"something happened","user_id":"u-1","count":42}""") }
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

    verify { crashlytics.log("""{"event":"error occurred"}""") }
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
          linkedMapOf(
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
    verify {
      crashlytics.log(
        """{"event":"typed","bool":true,"int":1,"long":2,"float":3.0,"double":4.0,"string":"hello","other":[1,2,3]}"""
      )
    }
  }

  @Test
  fun `emit with empty fields only logs event in JSON`() {
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

    verify { crashlytics.log("""{"event":"no fields"}""") }
    verify(exactly = 0) { crashlytics.setCustomKey(any(), any<String>()) }
    verify(exactly = 0) { crashlytics.setCustomKey(any(), any<Boolean>()) }
    verify(exactly = 0) { crashlytics.setCustomKey(any(), any<Int>()) }
    verify(exactly = 0) { crashlytics.setCustomKey(any(), any<Long>()) }
    verify(exactly = 0) { crashlytics.setCustomKey(any(), any<Float>()) }
    verify(exactly = 0) { crashlytics.setCustomKey(any(), any<Double>()) }
  }

  @Test
  fun `field with key 'event' overrides the log message in breadcrumb`() {
    val sink = CrashlyticsSink(crashlytics = crashlytics)
    val entry =
      StructuredLogEntry(
        priority = Log.WARN,
        tag = null,
        message = "original",
        throwable = null,
        fields = mapOf("event" to "overridden"),
      )

    sink.emit(entry)

    verify { crashlytics.log("""{"event":"overridden"}""") }
  }

  @Test
  fun `null field value is serialized as JSON null`() {
    val sink = CrashlyticsSink(crashlytics = crashlytics)
    val entry =
      StructuredLogEntry(
        priority = Log.WARN,
        tag = null,
        message = "msg",
        throwable = null,
        fields = mapOf("missing" to null),
      )

    sink.emit(entry)

    verify { crashlytics.log("""{"event":"msg","missing":null}""") }
  }

  @Test
  fun `string with special characters is escaped in JSON`() {
    val sink = CrashlyticsSink(crashlytics = crashlytics)
    val entry =
      StructuredLogEntry(
        priority = Log.WARN,
        tag = null,
        message = "say \"hi\"",
        throwable = null,
        fields = mapOf("path" to "C:\\tmp\\file"),
      )

    sink.emit(entry)

    verify { crashlytics.log("""{"event":"say \"hi\"","path":"C:\\tmp\\file"}""") }
  }
}
