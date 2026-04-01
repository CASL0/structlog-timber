plugins {
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.spotless)
}

spotless {
  kotlin {
    target("**/*.kt")
    targetExclude("**/build/**")
    ktfmt().googleStyle()
  }
  kotlinGradle {
    target("**/*.kts")
    targetExclude("**/build/**")
    ktfmt().googleStyle()
  }
}
