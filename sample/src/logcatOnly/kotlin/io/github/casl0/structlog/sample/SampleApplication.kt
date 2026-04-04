package io.github.casl0.structlog.sample

import android.app.Application
import android.util.Log
import io.github.casl0.structlog.extension.timber.StructuredTree
import io.github.casl0.structlog.extension.timber.logcat.LogcatSink
import timber.log.Timber

class SampleApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    Timber.plant(
      StructuredTree(
        sinks = listOf(LogcatSink(minPriority = Log.DEBUG)),
        globalFields = mapOf("app" to "structlog-sample"),
      )
    )
  }
}
