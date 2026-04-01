pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "structlog-timber"

include(":structlog-timber-core")

include(":structlog-timber-logcat")

include(":structlog-timber-crashlytics")
