plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "doger-dager"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":core", ":lwjgl3")
