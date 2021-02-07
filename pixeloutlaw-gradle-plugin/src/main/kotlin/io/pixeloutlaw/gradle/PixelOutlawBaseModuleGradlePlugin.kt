package io.pixeloutlaw.gradle

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import io.codearte.gradle.nexus.NexusStagingPlugin
import io.gitlab.arturbosch.detekt.DetektPlugin
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
open class PixelOutlawBaseModuleGradlePlugin : Plugin<Project> {
    private companion object {
        val supportedJavaVersion = JavaVersion.VERSION_1_8
    }

    override fun apply(target: Project) {
        target.withMavenPublish {
            target.pluginManager.apply(NexusStagingPlugin::class.java)
        }
    }

    internal fun applyMavenPublishConfiguration(target: Project) {
        target.withMavenPublish {
            target.pluginManager.apply(SigningPlugin::class.java)

            val (mvnName, mvnUrl) = if (target.version.toString().endsWith("-SNAPSHOT")) {
                "OSSRH" to target.uri("https://oss.sonatype.org/content/repositories/snapshots/")
            } else {
                "Sonatype Snapshots" to target.uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            }

            target.configure<PublishingExtension> {
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
            target.configure<SigningExtension> {
                setRequired({
                    target.gradle.taskGraph.hasTask("publish")
                })
                val signingKeyId: String? by project
                val signingKey: String? by project
                val signingPassword: String? by project
                useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            }
        }
    }

    internal fun applyNebulaMavenPublishConfiguration(target: Project) {
        target.withNebulaMavenPublish {
            target.extensions.getByType(PublishingExtension::class.java).publications {
                withType(MavenPublication::class.java) {
                    target.extensions.getByType<SigningExtension>().sign(this)
                    pom {
                        withXml {
                            val root = asNode()
                            val dependencies = target.configurations.getByName("compileOnly").dependencies
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

    internal fun applyKotlinConfiguration(target: Project) {
        target.withKotlinJvmPlugin {
            target.pluginManager.apply(DetektPlugin::class.java)
            target.pluginManager.apply(DokkaPlugin::class.java)

            target.configure<SpotlessExtension> {
                kotlin {
                    target("src/**/*.kt")
                    ktlint("0.40.0")
                    trimTrailingWhitespace()
                    endWithNewline()
                    if (target.file("HEADER").exists()) {
                        licenseHeaderFile("HEADER")
                    }
                }
            }

            target.tasks.withType<KotlinCompile> {
                dependsOn("spotlessKotlinApply")
                kotlinOptions {
                    javaParameters = true
                    jvmTarget = supportedJavaVersion.toString()
                }
            }

            target.tasks.getByName("javadocJar", Jar::class) {
                dependsOn("dokkaJavadoc")
                from(target.buildDir.resolve("dokka/javadoc"))
            }
        }
    }

    internal fun applyJavaConfiguration(target: Project) {
        target.withJavaPlugin {
            target.pluginManager.apply(NebulaResponsiblePlugin::class.java)
            target.pluginManager.apply(SpotlessPlugin::class.java)

            target.configure<SpotlessExtension> {
                java {
                    target("src/**/*.java")
                    googleJavaFormat()
                    trimTrailingWhitespace()
                    endWithNewline()
                }
            }

            target.tasks.withType<JavaCompile> {
                dependsOn("spotlessJavaApply")
                sourceCompatibility = supportedJavaVersion.toString()
                targetCompatibility = supportedJavaVersion.toString()
                options.compilerArgs.add("-parameters")
                options.isFork = true
                options.forkOptions.executable = "javac"
            }

            target.tasks.withType<Test> {
                useJUnitPlatform()
                testLogging {
                    events("passed", "skipped", "failed")
                }
            }
        }
    }
}
