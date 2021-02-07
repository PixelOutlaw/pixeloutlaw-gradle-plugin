plugins {
    kotlin("jvm") version "1.4.21" apply false
    id("io.pixeloutlaw.gradle")
    id("io.pixeloutlaw.spigot.build")
}

spigotBuildTools {
    versions = listOf("1.16.4")
}

tasks.withType<Wrapper>().configureEach {
    version = "6.8.2"
    doLast {
        copy {
            from(propertiesFile)
            into(gradle.includedBuild("pixeloutlaw-gradle-plugin").projectDir.resolve("gradle/wrapper"))
        }
    }
}
