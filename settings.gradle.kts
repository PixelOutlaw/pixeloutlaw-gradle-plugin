pluginManagement {
    includeBuild("pixeloutlaw-gradle-plugin")
}

rootProject.name = "pixeloutlaw-gradle"

gradle.allprojects {
    group = "io.pixeloutlaw"

    repositories {
        mavenCentral()
    }
}

include("sample-kotlin")
