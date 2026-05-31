plugins {
    application
}

group = "com.unpuppyable"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// The game depends only on the JDK (AWT/Swing + javax.sound.sampled); there are
// no external libraries. Sources live under src/ with package root game/, not
// the Gradle default src/main/java. res/ is read with new File("res/..") at
// runtime, so it stays a plain working-directory folder, not a resource set.
sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
    }
}

application {
    mainClass = "game.core.Game"
}

// With no dependencies the plain jar is fully self-contained; give it the entry
// point so `java -jar` runs the game (from a directory holding res/).
tasks.jar {
    manifest {
        attributes("Main-Class" to application.mainClass.get())
    }
}

tasks.named<JavaExec>("run") {
    // Asset paths are relative to the project root.
    workingDir = rootDir
}
