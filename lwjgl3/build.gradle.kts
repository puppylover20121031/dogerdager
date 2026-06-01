plugins {
    application
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.lwjgl") {
            useVersion(libs.versions.lwjgl.get())
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.gdx.backend.lwjgl3)
    implementation(libs.gdx.controllers.desktop)
    runtimeOnly(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
    runtimeOnly(variantOf(libs.gdx.freetype.platform) { classifier("natives-desktop") })
}

application {
    mainClass = "com.unpuppyable.dogerdager.lwjgl3.Lwjgl3Launcher"
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.file("assets")
}

// Self-contained runnable jar: our classes + all deps (incl. natives) + assets.
tasks.register<Jar>("fatJar") {
    group = "distribution"
    archiveFileName.set("DogerDager.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.unpuppyable.dogerdager.lwjgl3.Lwjgl3Launcher"
    }
    from(sourceSets["main"].output)
    from(rootProject.file("assets"))
    val runtime = configurations.runtimeClasspath.get()
    dependsOn(runtime)
    from(runtime.filter { it.name.endsWith(".jar") }.map { zipTree(it) })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}
