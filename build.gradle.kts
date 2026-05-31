plugins {
    application
}

group = "com.unpuppyable"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt())
    }
}

dependencies {
    implementation(libs.slick2d.core)
    implementation(libs.eos4j)
}

// The sources live under src/ with package root game/, not the Gradle default
// src/main/java. res/ is read with new File("res/..") at runtime, so it stays a
// plain working-directory folder rather than a classpath resource set.
sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
    }
}

application {
    mainClass = "game.core.Game"
}

tasks.named<JavaExec>("run") {
    // Asset paths are relative to the project root.
    workingDir = rootDir
}
