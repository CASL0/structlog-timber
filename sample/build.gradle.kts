plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.google.services)
  alias(libs.plugins.firebase.crashlytics.plugin)
}

android {
  namespace = "io.github.casl0.structlog.sample"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "io.github.casl0.structlog.sample"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.compileSdk.get().toInt()
    versionCode = 1
    versionName = "1.0.0"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  flavorDimensions += "sink"
  productFlavors {
    create("logcatOnly") { dimension = "sink" }
    create("withCrashlytics") { dimension = "sink" }
  }
}

kotlin { jvmToolchain(17) }

dependencies {
  implementation(project(":structlog-timber-core"))
  implementation(project(":structlog-timber-logcat"))
  implementation(libs.appcompat)

  "withCrashlyticsImplementation"(project(":structlog-timber-crashlytics"))
}
