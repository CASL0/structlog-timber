package io.github.casl0.structlog.extension.timber

import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import timber.log.Timber

class StructuredTimberInitTest {

  @After
  fun tearDown() {
    Timber.uprootAll()
  }

  private fun createSink(): Sink {
    val sink = mockk<Sink>(relaxed = true)
    every { sink.isLoggable(any()) } returns true
    return sink
  }

  @Test
  fun `init plants a StructuredTree with the given sinks`() {
    val sink = createSink()
    val entrySlot = slot<StructuredLogEntry>()
    every { sink.emit(capture(entrySlot)) } returns Unit

    StructuredTimber.init(sink)

    StructuredTimber.d("hello")

    val entry = entrySlot.captured
    assertEquals(Log.DEBUG, entry.priority)
    assertEquals("hello", entry.message)
  }

  @Test
  fun `init plants a StructuredTree with multiple sinks`() {
    val sink1 = createSink()
    val sink2 = createSink()
    val entrySlot1 = slot<StructuredLogEntry>()
    val entrySlot2 = slot<StructuredLogEntry>()
    every { sink1.emit(capture(entrySlot1)) } returns Unit
    every { sink2.emit(capture(entrySlot2)) } returns Unit

    StructuredTimber.init(sink1, sink2)

    StructuredTimber.d("hello")

    assertEquals("hello", entrySlot1.captured.message)
    assertEquals("hello", entrySlot2.captured.message)
  }

  @Test
  fun `init passes globalFields to StructuredTree`() {
    val sink = createSink()
    val entrySlot = slot<StructuredLogEntry>()
    every { sink.emit(capture(entrySlot)) } returns Unit

    StructuredTimber.init(sink, globalFields = mapOf("app" to "test-app"))

    StructuredTimber.d("hello")

    assertEquals("test-app", entrySlot.captured.fields["app"])
  }

  @Test
  fun `init with no globalFields results in empty fields`() {
    val sink = createSink()
    val entrySlot = slot<StructuredLogEntry>()
    every { sink.emit(capture(entrySlot)) } returns Unit

    StructuredTimber.init(sink)

    StructuredTimber.d("hello")

    assertTrue(entrySlot.captured.fields.isEmpty())
  }

  @Test(expected = IllegalArgumentException::class)
  fun `init with no sinks throws`() {
    StructuredTimber.init()
  }

  @Test
  fun `init called twice uproots previous tree and emits only once`() {
    val oldSink = createSink()
    val newSink = createSink()

    StructuredTimber.init(oldSink)
    StructuredTimber.init(newSink)

    StructuredTimber.d("hello")

    verify(exactly = 0) { oldSink.emit(any()) }
    verify(exactly = 1) { newSink.emit(any()) }
  }
}
