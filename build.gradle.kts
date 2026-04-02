plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.maven.publish) apply false
  alias(libs.plugins.kover)
  alias(libs.plugins.spotless)
}

dependencies { subprojects.forEach { kover(it) } }

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
