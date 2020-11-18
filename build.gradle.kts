plugins {
    kotlin("jvm") version "1.4.10" apply false
    id("io.pixeloutlaw.multi")
}

tasks.withType<Wrapper>().configureEach {
    version = "6.7.1"
    doLast {
        copy {
            from(propertiesFile)
            into(gradle.includedBuild("pixeloutlaw-gradle-plugin").projectDir.resolve("gradle/wrapper"))
        }
    }
}
