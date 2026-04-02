package io.github.casl0.structlog.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.casl0.structlog.extension.timber.StructuredLog
import io.github.casl0.structlog.extension.timber.StructuredTimber

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    StructuredLog.putContext("screen", "main")

    StructuredTimber.d(
      "Activity created",
      "lifecycle" to "onCreate",
      "has_saved_state" to (savedInstanceState != null),
    )

    StructuredTimber.i(
      "Purchase completed",
      "item_id" to "SKU-123",
      "price" to 1980,
      "currency" to "JPY",
    )

    StructuredTimber.tag("Checkout")
      .w("Slow payment response", "latency_ms" to 3200, "gateway" to "stripe")

    StructuredTimber.e(
      RuntimeException("Something went wrong"),
      "Unexpected error",
      "error_code" to "E001",
    )
  }

  override fun onDestroy() {
    super.onDestroy()
    StructuredLog.clearContext()
  }
}
