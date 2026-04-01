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
}
