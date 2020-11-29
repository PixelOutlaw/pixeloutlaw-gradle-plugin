plugins {
    `kotlin-dsl`
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("io.gitlab.arturbosch.detekt") version "1.14.2"
    id("org.jetbrains.dokka") version "1.4.10"
    id("nebula.release") version "15.3.0"
    id("com.gradle.plugin-publish") version "0.12.0"
}

group = "io.pixeloutlaw"

repositories {
    gradlePluginPortal()
    jcenter()
}

dependencies {
    // kotlin plugins
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:_")
    implementation("org.jetbrains.kotlin:kotlin-reflect:_")

    // dokka plugins
    implementation("org.jetbrains.dokka:dokka-core:_")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:_")

    // nebula plugins
    implementation("com.netflix.nebula:nebula-bintray-plugin:_")
    implementation("com.netflix.nebula:nebula-project-plugin:_")
    implementation("com.netflix.nebula:nebula-release-plugin:_")

    // other standard gradle plugins
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:_")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:_")
}

detekt {
    baseline = file("baseline.xml")
}

gradlePlugin {
    plugins {
        create("pixelOutlawMulti") {
            id = "io.pixeloutlaw.multi"
            displayName = "pixelOutlawMulti"
            description = "Common conventions for PixelOutlaw Gradle projects (multi module)."
            implementationClass = "io.pixeloutlaw.gradle.PixelOutlawMultiModuleGradlePlugin"
        }
        create("pixelOutlawSingle") {
            id = "io.pixeloutlaw.single"
            displayName = "pixelOutlawSingle"
            description = "Common conventions for PixelOutlaw Gradle projects (single module)."
            implementationClass = "io.pixeloutlaw.gradle.PixelOutlawSingleModuleGradlePlugin"
        }
        create("pixelOutlawSpigotBuildTools") {
            id = "io.pixeloutlaw.spigot.build"
            displayName = "pixelOutlawSpigotBuildTools"
            description = "Builds and installs versions of Spigot to Maven Local."
            implementationClass = "io.pixeloutlaw.gradle.spigot.PixelOutlawSpigotBuildToolsGradlePlugin"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
    dependsOn("ktlintFormat")
    kotlinOptions.jvmTarget = "1.8"
}
