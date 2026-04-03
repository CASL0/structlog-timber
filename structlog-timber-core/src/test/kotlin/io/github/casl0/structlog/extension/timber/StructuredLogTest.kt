package io.github.casl0.structlog.extension.timber

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredLogTest {

  @After
  fun tearDown() {
    StructuredLog.clearContext()
  }

  @Test
  fun `putContext adds key-value to snapshot`() {
    StructuredLog.putContext("user_id", "u-123")

    val snapshot = StructuredLog.snapshot()

    assertEquals("u-123", snapshot["user_id"])
  }

  @Test
  fun `putContext overwrites existing key`() {
    StructuredLog.putContext("key", "old")
    StructuredLog.putContext("key", "new")

    assertEquals("new", StructuredLog.snapshot()["key"])
  }

  @Test
  fun `removeContext removes the specified key`() {
    StructuredLog.putContext("a", 1)
    StructuredLog.putContext("b", 2)

    StructuredLog.removeContext("a")

    val snapshot = StructuredLog.snapshot()
    assertTrue("a" !in snapshot)
    assertEquals(2, snapshot["b"])
  }

  @Test
  fun `removeContext on empty context does not throw`() {
    StructuredLog.removeContext("nonexistent")
  }

  @Test
  fun `clearContext removes all entries`() {
    StructuredLog.putContext("a", 1)
    StructuredLog.putContext("b", 2)

    StructuredLog.clearContext()

    assertTrue(StructuredLog.snapshot().isEmpty())
  }

  @Test
  fun `snapshot returns empty map when no context is set`() {
    assertTrue(StructuredLog.snapshot().isEmpty())
  }

  @Test
  fun `putContext supports null values`() {
    StructuredLog.putContext("nullable", null)

    val snapshot = StructuredLog.snapshot()
    assertTrue("nullable" in snapshot)
    assertNull(snapshot["nullable"])
  }

  @Test
  fun `withContext adds entries during block and removes them after`() {
    StructuredLog.withContext("request_id" to "r-1", "trace_id" to "t-1") {
      val snapshot = StructuredLog.snapshot()
      assertEquals("r-1", snapshot["request_id"])
      assertEquals("t-1", snapshot["trace_id"])
    }

    val after = StructuredLog.snapshot()
    assertTrue("request_id" !in after)
    assertTrue("trace_id" !in after)
  }

  @Test
  fun `withContext removes entries even when block throws`() {
    try {
      StructuredLog.withContext("key" to "value") { throw RuntimeException("boom") }
    } catch (_: RuntimeException) {}

    assertTrue("key" !in StructuredLog.snapshot())
  }

  @Test
  fun `withContext returns the block result`() {
    val result = StructuredLog.withContext("key" to "value") { 42 }

    assertEquals(42, result)
  }

  @Test
  fun `withContext restores previous value if key already existed`() {
    StructuredLog.putContext("key", "original")

    StructuredLog.withContext("key" to "temporary") {
      assertEquals("temporary", StructuredLog.snapshot()["key"])
    }

    assertEquals("original", StructuredLog.snapshot()["key"])
  }

  @Test
  fun `withContext preserves unrelated context entries`() {
    StructuredLog.putContext("existing", "keep")

    StructuredLog.withContext("scoped" to "value") {
      assertEquals("keep", StructuredLog.snapshot()["existing"])
    }

    assertEquals("keep", StructuredLog.snapshot()["existing"])
  }
}
