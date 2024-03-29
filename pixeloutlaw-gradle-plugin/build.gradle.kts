plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
    id("com.gradle.plugin-publish")
}

group = "io.pixeloutlaw"

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(gradleKotlinDsl())

    // kotlin plugins
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:_")
    implementation("org.jetbrains.kotlin:kotlin-reflect:_")

    // dokka plugins
    implementation("org.jetbrains.dokka:dokka-core:_")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:_")

    // nebula plugins
    implementation("com.netflix.nebula:nebula-project-plugin:_")
    implementation("com.netflix.nebula:gradle-contacts-plugin:_")

    // gradle nexus publish plugin
    implementation("io.github.gradle-nexus:publish-plugin:_")

    // test logger plugin
    implementation("com.adarshr:gradle-test-logger-plugin:_")

    // other standard gradle plugins
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:_")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:_")
}

detekt {
    baseline = file("baseline.xml")
}

gradlePlugin {
    plugins {
        create("pixelOutlawGradle") {
            id = "io.pixeloutlaw.gradle"
            displayName = "pixelOutlawGradle"
            description = "Common conventions for PixelOutlaw Gradle projects."
            implementationClass = "io.pixeloutlaw.gradle.PixelOutlawGradlePlugin"
        }
        create("pixelOutlawSpigotBuildTools") {
            id = "io.pixeloutlaw.spigot.build"
            displayName = "pixelOutlawSpigotBuildTools"
            description = "Builds and installs versions of Spigot to Maven Local."
            implementationClass = "io.pixeloutlaw.gradle.spigot.PixelOutlawSpigotBuildToolsGradlePlugin"
        }
    }
}

javaToolchains {
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withJavadocJar()
    withSourcesJar()
}

pluginBundle {
    website = "https://github.com/PixelOutlaw/pixeloutlaw-gradle-plugin"
    vcsUrl = "https://github.com/PixelOutlaw/pixeloutlaw-gradle-plugin"
    tags = listOf("kotlin", "pixeloutlaw", "convention", "spigot")
}

tasks.getByName("javadocJar", Jar::class) {
    dependsOn("dokkaJavadoc")
    from(buildDir.resolve("dokka/javadoc"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
