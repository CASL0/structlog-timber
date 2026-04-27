plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kover)
  alias(libs.plugins.maven.publish)
}

android {
  namespace = "io.github.casl0.structlog.extension.timber.crashlytics"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlin { jvmToolchain(17) }
}

mavenPublishing {
  publishToMavenCentral()
  signAllPublications()
  coordinates(artifactId = "structlog-timber-crashlytics")
  pom {
    name.set("structlog-timber-crashlytics")
    description.set("Firebase Crashlytics sink for structlog-timber")
    inceptionYear.set("2026")
  }
}

dependencies {
  implementation(project(":structlog-timber-core"))
  implementation(libs.firebase.crashlytics)
  implementation(libs.kotlinx.serialization.json)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
