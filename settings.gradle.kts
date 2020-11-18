rootProject.name = "pixeloutlaw-gradle"

include("sample-kotlin")

includeBuild("pixeloutlaw-gradle-plugin")

gradle.allprojects {
    group = "io.pixeloutlaw"

    repositories {
        jcenter()
    }
}
