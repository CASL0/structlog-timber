plugins { alias(libs.plugins.android.library) }

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

dependencies {
  implementation(project(":structlog-timber-core"))
  implementation(libs.firebase.crashlytics)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
