plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kover)
  alias(libs.plugins.maven.publish)
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

mavenPublishing {
  publishToMavenCentral()
  signAllPublications()
  coordinates(artifactId = "structlog-timber-logcat")
  pom {
    name.set("structlog-timber-logcat")
    description.set("Logcat sink for structlog-timber")
    inceptionYear.set("2026")
  }
}

dependencies {
  implementation(project(":structlog-timber-core"))

  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
}
