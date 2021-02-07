package io.pixeloutlaw.gradle

import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.TestLoggerPlugin
import com.adarshr.gradle.testlogger.theme.ThemeType
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import io.codearte.gradle.nexus.NexusStagingExtension
import io.codearte.gradle.nexus.NexusStagingPlugin
import io.gitlab.arturbosch.detekt.DetektPlugin
import nebula.plugin.contacts.ContactsExtension
import nebula.plugin.contacts.ContactsPlugin
import nebula.plugin.responsible.NebulaResponsiblePlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Handles any shareable logic between single and multiple module projects.
 */
open class PixelOutlawGradlePlugin : Plugin<Project> {
    private companion object {
        val supportedJavaVersion = JavaVersion.VERSION_1_8
    }

    override fun apply(target: Project) {
        // don't do anything if we aren't the root project
        if (target != target.rootProject) {
            return
        }

        // Nexus Staging Plugin can only go on the root project
        target.pluginManager.apply(NexusStagingPlugin::class.java)
        target.configure<NexusStagingExtension> {
            packageGroup = "io.pixeloutlaw"
            username = System.getenv("OSSRH_USERNAME")
            password = System.getenv("OSSRH_PASSWORD")
        }

        // All of the other plugin configs are conditional, so we can go ahead
        // and just apply them to allprojects
        target.allprojects {
            applyJavaConfiguration()
            applyKotlinConfiguration()
            applyMavenPublishConfiguration()
            applyNebulaMavenPublishConfiguration()
        }
    }

    private fun Project.applyMavenPublishConfiguration() {
        withMavenPublish {
            pluginManager.apply(SigningPlugin::class.java)

            val (mvnName, mvnUrl) = if (version.toString().endsWith("-SNAPSHOT")) {
                "ossrhSnapshots" to uri("https://oss.sonatype.org/content/repositories/snapshots/")
            } else {
                "ossrhReleases" to uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            }

            configure<PublishingExtension> {
                repositories {
                    maven {
                        credentials {
                            username = System.getenv("OSSRH_USERNAME")
                            password = System.getenv("OSSRH_PASSWORD")
                        }
                        name = mvnName
                        url = mvnUrl
                    }
                }
            }
            configure<SigningExtension> {
                setRequired({
                    gradle.taskGraph.hasTask("publish")
                })
                val signingKey: String? by project
                val signingPassword: String? by project
                useInMemoryPgpKeys(signingKey, signingPassword)
            }
        }
    }

    private fun Project.applyNebulaMavenPublishConfiguration() {
        withNebulaMavenPublish {
            extensions.getByType(PublishingExtension::class.java).publications {
                withType(MavenPublication::class.java) {
                    extensions.getByType<SigningExtension>().sign(this)
                    pom {
                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("https://opensource.org/licenses/MIT")
                                distribution.set("repo")
                            }
                        }
                        withXml {
                            val root = asNode()
                            val dependencies = configurations.getByName("compileOnly").dependencies
                            if (dependencies.size > 0) {
                                val deps = root.children().find {
                                    it is groovy.util.Node && it.name().toString()
                                        .endsWith("dependencies")
                                } as groovy.util.Node? ?: root.appendNode("dependencies")
                                dependencies.forEach { dependency ->
                                    deps.appendNode("dependency").apply {
                                        appendNode("groupId", dependency.group)
                                        appendNode("artifactId", dependency.name)
                                        appendNode("version", dependency.version)
                                        appendNode("scope", "provided")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Project.applyKotlinConfiguration() {
        withKotlinJvmPlugin {
            pluginManager.apply(DetektPlugin::class.java)
            pluginManager.apply(DokkaPlugin::class.java)

            configure<SpotlessExtension> {
                kotlin {
                    target("src/**/*.kt")
                    ktlint("0.40.0")
                    trimTrailingWhitespace()
                    endWithNewline()
                    if (file("HEADER").exists()) {
                        licenseHeaderFile("HEADER")
                    }
                }
            }

            tasks.withType<KotlinCompile> {
                dependsOn("spotlessKotlinApply")
                kotlinOptions {
                    javaParameters = true
                    jvmTarget = supportedJavaVersion.toString()
                }
            }

            tasks.getByName("javadocJar", Jar::class) {
                dependsOn("dokkaJavadoc")
                from(buildDir.resolve("dokka/javadoc"))
            }
        }
    }

    private fun Project.applyJavaConfiguration() {
        withJavaPlugin {
            pluginManager.apply(ContactsPlugin::class.java)
            pluginManager.apply(NebulaResponsiblePlugin::class.java)
            pluginManager.apply(SpotlessPlugin::class.java)
            pluginManager.apply(TestLoggerPlugin::class.java)

            configure<ContactsExtension> {
                addPerson("topplethenunnery@gmail.com")
            }

            configure<SpotlessExtension> {
                java {
                    target("src/**/*.java")
                    googleJavaFormat()
                    trimTrailingWhitespace()
                    endWithNewline()
                }
            }

            tasks.withType<JavaCompile> {
                dependsOn("spotlessJavaApply")
                sourceCompatibility = supportedJavaVersion.toString()
                targetCompatibility = supportedJavaVersion.toString()
                options.compilerArgs.add("-parameters")
                options.isFork = true
                options.forkOptions.executable = "javac"
            }

            tasks.withType<Test> {
                useJUnitPlatform()
            }
        }
        pluginManager.withPlugin("com.adarshr.test-logger") {
            tasks.withType<Test> {
                extensions.getByType(TestLoggerExtension::class.java).apply {
                    theme = ThemeType.MOCHA
                    showSimpleNames = true
                    showStandardStreams = true
                    showFailedStandardStreams = true
                    showSkippedStandardStreams = false
                    showPassedStandardStreams = false
                }
            }
        }
    }
}
