plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kover)
}

android {
  namespace = "io.github.casl0.structlog.extension.timber.logcat"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlin { jvmToolchain(17) }

  @Suppress("UnstableApiUsage") testOptions { unitTests.isIncludeAndroidResources = true }
}

dependencies {
  implementation(project(":structlog-timber-core"))

  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
}
