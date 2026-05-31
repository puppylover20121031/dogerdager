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
}
