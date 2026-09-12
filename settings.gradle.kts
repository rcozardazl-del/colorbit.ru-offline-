// Автоматическое восстановление debug.keystore из base64 при распаковке ZIP архива
val autoKs = rootDir.resolve("debug.keystore")
val autoKsBase64 = rootDir.resolve("debug.keystore.base64")
if (!autoKs.exists() && autoKsBase64.exists()) {
  try {
    val clean = autoKsBase64.readText().replace("\n", "").replace("\r", "").trim()
    val decoded = java.util.Base64.getDecoder().decode(clean)
    autoKs.writeBytes(decoded)
  } catch (_: Throwable) {}
}

val autoEnv = rootDir.resolve(".env")
val autoEnvExample = rootDir.resolve(".env.example")
if (!autoEnv.exists()) {
  try {
    if (autoEnvExample.exists()) autoEnvExample.copyTo(autoEnv, overwrite = false)
    else autoEnv.writeText("")
  } catch (_: Throwable) {}
}

pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "Colorbit"

include(":app")
