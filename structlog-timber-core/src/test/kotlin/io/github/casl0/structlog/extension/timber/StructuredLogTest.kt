package io.github.casl0.structlog.extension.timber

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredLogTest {

  @After
  fun tearDown() {
    StructuredLog.clearLogContext()
  }

  @Test
  fun `putLogContext adds key-value to snapshot`() {
    StructuredLog.putLogContext("user_id", "u-123")

    val snapshot = StructuredLog.snapshot()

    assertEquals("u-123", snapshot["user_id"])
  }

  @Test
  fun `putLogContext overwrites existing key`() {
    StructuredLog.putLogContext("key", "old")
    StructuredLog.putLogContext("key", "new")

    assertEquals("new", StructuredLog.snapshot()["key"])
  }

  @Test
  fun `removeLogContext removes the specified key`() {
    StructuredLog.putLogContext("a", 1)
    StructuredLog.putLogContext("b", 2)

    StructuredLog.removeLogContext("a")

    val snapshot = StructuredLog.snapshot()
    assertTrue("a" !in snapshot)
    assertEquals(2, snapshot["b"])
  }

  @Test
  fun `removeLogContext on empty context does not throw`() {
    StructuredLog.removeLogContext("nonexistent")
  }

  @Test
  fun `clearLogContext removes all entries`() {
    StructuredLog.putLogContext("a", 1)
    StructuredLog.putLogContext("b", 2)

    StructuredLog.clearLogContext()

    assertTrue(StructuredLog.snapshot().isEmpty())
  }

  @Test
  fun `snapshot returns empty map when no context is set`() {
    assertTrue(StructuredLog.snapshot().isEmpty())
  }

  @Test
  fun `putLogContext supports null values`() {
    StructuredLog.putLogContext("nullable", null)

    val snapshot = StructuredLog.snapshot()
    assertTrue("nullable" in snapshot)
    assertNull(snapshot["nullable"])
  }

  @Test
  fun `withLogContext adds entries during block and removes them after`() {
    StructuredLog.withLogContext("request_id" to "r-1", "trace_id" to "t-1") {
      val snapshot = StructuredLog.snapshot()
      assertEquals("r-1", snapshot["request_id"])
      assertEquals("t-1", snapshot["trace_id"])
    }

    val after = StructuredLog.snapshot()
    assertTrue("request_id" !in after)
    assertTrue("trace_id" !in after)
  }

  @Test
  fun `withLogContext removes entries even when block throws`() {
    try {
      StructuredLog.withLogContext("key" to "value") { throw RuntimeException("boom") }
    } catch (_: RuntimeException) {}

    assertTrue("key" !in StructuredLog.snapshot())
  }

  @Test
  fun `withLogContext returns the block result`() {
    val result = StructuredLog.withLogContext("key" to "value") { 42 }

    assertEquals(42, result)
  }

  @Test
  fun `withLogContext restores previous value if key already existed`() {
    StructuredLog.putLogContext("key", "original")

    StructuredLog.withLogContext("key" to "temporary") {
      assertEquals("temporary", StructuredLog.snapshot()["key"])
    }

    assertEquals("original", StructuredLog.snapshot()["key"])
  }

  @Test
  fun `withLogContext preserves unrelated context entries`() {
    StructuredLog.putLogContext("existing", "keep")

    StructuredLog.withLogContext("scoped" to "value") {
      assertEquals("keep", StructuredLog.snapshot()["existing"])
    }

    assertEquals("keep", StructuredLog.snapshot()["existing"])
  }

  @Test
  fun `withLogContext with duplicate key uses last value and restores correctly`() {
    StructuredLog.withLogContext("key" to "first", "key" to "second") {
      assertEquals("second", StructuredLog.snapshot()["key"])
    }

    assertTrue("key" !in StructuredLog.snapshot())
  }

  @Test
  fun `withLogContext with duplicate key restores original value`() {
    StructuredLog.putLogContext("key", "original")

    StructuredLog.withLogContext("key" to "first", "key" to "second") {
      assertEquals("second", StructuredLog.snapshot()["key"])
    }

    assertEquals("original", StructuredLog.snapshot()["key"])
  }

  @Test
  fun `withLogContext with no entries does not mutate context`() {
    assertTrue(StructuredLog.snapshot().isEmpty())

    val result = StructuredLog.withLogContext { 99 }

    assertEquals(99, result)
    assertTrue(StructuredLog.snapshot().isEmpty())
  }
}
