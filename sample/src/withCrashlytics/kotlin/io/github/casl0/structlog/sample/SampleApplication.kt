package io.github.casl0.structlog.sample

import android.app.Application
import android.util.Log
import io.github.casl0.structlog.extension.timber.StructuredTimber
import io.github.casl0.structlog.extension.timber.crashlytics.CrashlyticsSink
import io.github.casl0.structlog.extension.timber.logcat.LogcatSink

class SampleApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    StructuredTimber.init(
      LogcatSink(minPriority = Log.DEBUG),
      CrashlyticsSink(minPriority = Log.WARN),
      globalFields = mapOf("app" to "structlog-sample"),
    )
  }
}
