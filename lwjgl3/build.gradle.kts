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
    runtimeOnly(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
}

application {
    mainClass = "com.unpuppyable.dogerdager.lwjgl3.Lwjgl3Launcher"
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.file("assets")
}
