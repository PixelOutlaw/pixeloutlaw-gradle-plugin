package io.pixeloutlaw.gradle

import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.TestLoggerPlugin
import com.adarshr.gradle.testlogger.theme.ThemeType
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import io.github.gradlenexus.publishplugin.NexusPublishExtension
import io.github.gradlenexus.publishplugin.NexusPublishPlugin
import io.gitlab.arturbosch.detekt.DetektPlugin
import nebula.plugin.contacts.ContactsExtension
import nebula.plugin.contacts.ContactsPlugin
import nebula.plugin.responsible.NebulaResponsiblePlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Declare the minimum version of Java supported.
 */
internal val supportedJavaVersion = JavaVersion.VERSION_1_8

/**
 * Configures the project to publish to Maven Central.
 */
internal fun Project.publishToMavenCentral() {
    pluginManager.apply(SigningPlugin::class.java)

    // Signing will only occur if trying to publish the JAR.
    configure<SigningExtension> {
        setRequired({
            gradle.taskGraph.hasTask("publish")
        })
        val signingKey: String? by project
        val signingPassword: String? by project
        // Uses ASCII-armored keys (typically provided on GitHub Actions)
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(extensions.getByName<PublishingExtension>("publishing").publications.matching { it is MavenPublication })
    }
}

/**
 * Configures all Maven publications to be signed and have POM contents meet Maven Central requirements.
 */
internal fun Project.configurePublicationsForMavenCentral() {
    configure<PublishingExtension> {
        publications.withType<MavenPublication> {
            // POM contents for Maven Central
            pom {
                pom.mitLicense()
                pom.addCompileOnlyDependenciesAsProvided(this@configurePublicationsForMavenCentral)
            }
        }
    }
}

/**
 * Configures anything specifically related to Kotlin.
 */
internal fun Project.applyKotlinConfiguration() {
    pluginManager.apply(DetektPlugin::class.java)
    pluginManager.apply(DokkaPlugin::class.java)

    configure<SpotlessExtension> {
        kotlin {
            it.target("src/**/*.kt")
            it.ktlint("0.42.0")
            it.trimTrailingWhitespace()
            it.endWithNewline()
            if (file("HEADER").exists()) {
                it.licenseHeaderFile("HEADER")
            }
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            javaParameters = true
            jvmTarget = supportedJavaVersion.toString()
        }
    }

    val dokkaJavadoc = tasks.getByName<DokkaTask>("dokkaJavadoc")
    tasks.getByName("javadocJar", Jar::class) {
        dependsOn(dokkaJavadoc)
        from(dokkaJavadoc)
    }
}

/**
 * Configures anything specifically related to Java.
 */
internal fun Project.applyJavaConfiguration() {
    pluginManager.apply(JacocoPlugin::class.java)
    pluginManager.apply(SpotlessPlugin::class.java)

    configure<JacocoPluginExtension> {
        this.toolVersion = "0.8.7"
    }

    configure<SpotlessExtension> {
        java {
            it.target("src/**/*.java")
            it.googleJavaFormat()
            it.trimTrailingWhitespace()
            it.endWithNewline()
        }
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = supportedJavaVersion
        targetCompatibility = supportedJavaVersion
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
        options.isFork = true
        options.forkOptions.executable = "javac"
    }

    tasks.withType<JacocoReport> {
        reports {
            it.xml.isEnabled = true
        }
    }

    tasks.withType<Test> {
        finalizedBy("jacocoTestReport")
        useJUnitPlatform()
    }
}

/**
 * Configures anything that should always go on a project.
 */
internal fun Project.applyBaseConfiguration() {
    pluginManager.apply(ContactsPlugin::class.java)
    pluginManager.apply(NebulaResponsiblePlugin::class.java)
    pluginManager.apply(TestLoggerPlugin::class.java)

    configure<ContactsExtension> {
        addPerson("topplethenunnery@gmail.com")
    }
    configure<TestLoggerExtension> {
        theme = ThemeType.MOCHA
        showSimpleNames = true
        showStandardStreams = true
        showFailedStandardStreams = true
        showSkippedStandardStreams = false
        showPassedStandardStreams = false
    }
}

/**
 * Configures anything that should only go on the root project.
 */
internal fun Project.applyRootConfiguration() {
    // don't do anything if we aren't the root project
    if (this != rootProject) {
        return
    }

    allprojects {
        description = rootProject.description
        version = rootProject.version
    }

    pluginManager.apply(NexusPublishPlugin::class.java)
    configure<NexusPublishExtension> {
        repositories {
            it.sonatype { sonatype ->
                sonatype.username.set(System.getenv("OSSRH_USERNAME") ?: "N/A")
                sonatype.password.set(System.getenv("OSSRH_PASSWORD") ?: "N/A")
            }
        }
    }
}
