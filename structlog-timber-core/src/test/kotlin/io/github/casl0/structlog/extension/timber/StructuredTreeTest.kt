package io.github.casl0.structlog.extension.timber

import android.util.Log
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import timber.log.Timber

class StructuredTreeTest {

  @After
  fun tearDown() {
    Timber.uprootAll()
    StructuredLog.clearLogContext()
  }

  private fun createSink(): Sink {
    val sink = mockk<Sink>(relaxed = true)
    every { sink.isLoggable(any()) } returns true
    return sink
  }

  @Test
  fun `log delegates to sink with correct entry`() {
    val sink = createSink()
    val entrySlot = slot<StructuredLogEntry>()
    every { sink.emit(capture(entrySlot)) } returns Unit
    Timber.plant(StructuredTree(sinks = listOf(sink)))

    StructuredTimber.tag("TestTag").d("hello")

    val entry = entrySlot.captured
    assertEquals(Log.DEBUG, entry.priority)
    assertEquals("TestTag", entry.tag)
    assertEquals("hello", entry.message)
    assertNull(entry.throwable)
  }

  @Test
  fun `log merges global fields into entry`() {
    val sink = createSink()
    val entrySlot = slot<StructuredLogEntry>()
    every { sink.emit(capture(entrySlot)) } returns Unit
    Timber.plant(StructuredTree(sinks = listOf(sink), globalFields = mapOf("app" to "test-app")))

    StructuredTimber.i("msg")

    assertEquals("test-app", entrySlot.captured.fields["app"])
  }

  @Test
  fun `log merges context fields into entry`() {
    val sink = createSink()
    val entrySlot = slot<StructuredLogEntry>()
    every { sink.emit(capture(entrySlot)) } returns Unit
    Timber.plant(StructuredTree(sinks = listOf(sink)))

    StructuredLog.putLogContext("user_id", "u-1")
    StructuredTimber.d("msg")

    assertEquals("u-1", entrySlot.captured.fields["user_id"])
  }

  @Test
  fun `per-log fields override context and global fields`() {
    val sink = createSink()
    val entrySlot = slot<StructuredLogEntry>()
    every { sink.emit(capture(entrySlot)) } returns Unit
    Timber.plant(StructuredTree(sinks = listOf(sink), globalFields = mapOf("key" to "global")))

    StructuredLog.putLogContext("key", "context")
    StructuredTimber.d("msg", "key" to "per-log")

    assertEquals("per-log", entrySlot.captured.fields["key"])
  }

  @Test
  fun `sink with isLoggable false is skipped`() {
    val sink = mockk<Sink>()
    every { sink.isLoggable(any()) } returns false
    Timber.plant(StructuredTree(sinks = listOf(sink)))

    StructuredTimber.v("msg")

    verify { sink.isLoggable(Log.VERBOSE) }
    verify(exactly = 0) { sink.emit(any()) }
    confirmVerified(sink)
  }

  @Test
  fun `log dispatches to multiple sinks`() {
    val sink1 = createSink()
    val sink2 = createSink()
    Timber.plant(StructuredTree(sinks = listOf(sink1, sink2)))

    StructuredTimber.w("msg")

    verify(exactly = 1) { sink1.emit(any()) }
    verify(exactly = 1) { sink2.emit(any()) }
  }

  @Test
  fun `failing sink does not block subsequent sinks`() {
    val sink1 = createSink()
    val sink2 = createSink()
    every { sink1.emit(any()) } throws RuntimeException("sink1 failed")
    Timber.plant(StructuredTree(sinks = listOf(sink1, sink2)))

    try {
      StructuredTimber.w("msg")
    } catch (_: RuntimeException) {}

    verify(exactly = 1) { sink2.emit(any()) }
  }

  @Test
  fun `exception from sink is rethrown after all sinks are dispatched`() {
    val sink = createSink()
    val error = RuntimeException("boom")
    every { sink.emit(any()) } throws error
    Timber.plant(StructuredTree(sinks = listOf(sink)))

    val thrown =
      try {
        StructuredTimber.w("msg")
        null
      } catch (e: RuntimeException) {
        e
      }

    assertEquals(error, thrown)
  }

  @Test
  fun `multiple sink failures are collected as suppressed exceptions`() {
    val sink1 = createSink()
    val sink2 = createSink()
    val error1 = RuntimeException("sink1")
    val error2 = RuntimeException("sink2")
    every { sink1.emit(any()) } throws error1
    every { sink2.emit(any()) } throws error2
    Timber.plant(StructuredTree(sinks = listOf(sink1, sink2)))

    val thrown =
      try {
        StructuredTimber.w("msg")
        null
      } catch (e: RuntimeException) {
        e
      }

    assertEquals(error1, thrown)
    val suppressed = thrown?.suppressed ?: emptyArray()
    assertEquals(1, suppressed.size)
    assertEquals(error2, suppressed[0])
  }

  @Test
  fun `throwable is passed through to entry`() {
    val sink = createSink()
    val entrySlot = slot<StructuredLogEntry>()
    every { sink.emit(capture(entrySlot)) } returns Unit
    Timber.plant(StructuredTree(sinks = listOf(sink)))

    val exception = RuntimeException("boom")
    StructuredTimber.e(exception, "error")

    assertEquals(exception, entrySlot.captured.throwable)
  }
}
