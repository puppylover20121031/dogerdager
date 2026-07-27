plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    api(libs.gdx)
    api(libs.gdx.freetype)
    api(libs.visui)
    api(libs.gdx.controllers.core)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("com.badlogicgames.gdx:gdx-backend-headless:1.13.1")
}

tasks.test {
    useJUnitPlatform()
}
