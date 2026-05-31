plugins {
    application
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.gdx.backend.lwjgl3)
    // libGDX ships its desktop natives in this classified jar; no
    // java.library.path wiring needed, the backend loads them itself.
    runtimeOnly(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
}

application {
    mainClass = "com.unpuppyable.dogerdager.lwjgl3.Lwjgl3Launcher"
}

tasks.named<JavaExec>("run") {
    // libGDX resolves Gdx.files.internal(..) against the working directory.
    workingDir = rootProject.file("assets")
}
