package io.github.casl0.structlog.extension.timber.logcat

import android.util.Log
import io.github.casl0.structlog.extension.timber.StructuredLogEntry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LogcatSinkTest {

  @Test
  fun `isLoggable returns false below minPriority`() {
    val sink = LogcatSink()

    assertFalse(sink.isLoggable(Log.DEBUG))
    assertFalse(sink.isLoggable(Log.INFO))
    assertTrue(sink.isLoggable(Log.WARN))
    assertTrue(sink.isLoggable(Log.ERROR))
  }

  @Test
  fun `emit writes message to logcat`() {
    val sink = LogcatSink()
    val entry =
      StructuredLogEntry(
        priority = Log.WARN,
        tag = "TestTag",
        message = "hello",
        throwable = null,
        fields = emptyMap(),
      )

    sink.emit(entry)

    val logs = ShadowLog.getLogs()
    assertTrue(logs.any { it.tag == "TestTag" && it.msg == "hello" })
  }

  @Test
  fun `emit formats fields as key=value`() {
    val sink = LogcatSink()
    val entry =
      StructuredLogEntry(
        priority = Log.WARN,
        tag = "Tag",
        message = "msg",
        throwable = null,
        fields = mapOf("key1" to "val1", "key2" to 42),
      )

    sink.emit(entry)

    val logs = ShadowLog.getLogs()
    val logMsg = logs.first { it.tag == "Tag" }.msg
    assertTrue(logMsg.contains("key1=val1"))
    assertTrue(logMsg.contains("key2=42"))
    assertTrue(logMsg.startsWith("msg {"))
  }

  @Test
  fun `emit uses default tag when tag is null`() {
    val sink = LogcatSink()
    val entry =
      StructuredLogEntry(
        priority = Log.WARN,
        tag = null,
        message = "no tag",
        throwable = null,
        fields = emptyMap(),
      )

    sink.emit(entry)

    val logs = ShadowLog.getLogs()
    assertTrue(logs.any { it.tag == "StructuredLog" && it.msg == "no tag" })
  }

  @Test
  fun `emit includes stack trace when throwable is present`() {
    val sink = LogcatSink()
    val exception = RuntimeException("boom")
    val entry =
      StructuredLogEntry(
        priority = Log.ERROR,
        tag = "Err",
        message = "error",
        throwable = exception,
        fields = emptyMap(),
      )

    sink.emit(entry)

    val logs = ShadowLog.getLogs()
    val logMsg = logs.first { it.tag == "Err" }.msg
    assertTrue(logMsg.contains("error"))
    assertTrue(logMsg.contains("RuntimeException"))
  }
}
