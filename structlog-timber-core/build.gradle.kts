plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kover)
  alias(libs.plugins.maven.publish)
}

android {
  namespace = "io.github.casl0.structlog.extension.timber"
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
  coordinates(artifactId = "structlog-timber-core")
  pom {
    name.set("structlog-timber-core")
    description.set("Structured logging core for Timber")
    inceptionYear.set("2026")
  }
}

dependencies {
  api(libs.timber)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
