plugins {
    kotlin("jvm") version "1.7.10" apply false
    id("io.pixeloutlaw.gradle")
}

tasks.withType<Wrapper>().configureEach {
    version = "7.1.1"
    doLast {
        copy {
            from(propertiesFile)
            into(gradle.includedBuild("pixeloutlaw-gradle-plugin").projectDir.resolve("gradle/wrapper"))
        }
    }
}
