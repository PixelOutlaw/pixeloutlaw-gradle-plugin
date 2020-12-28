package io.pixeloutlaw.gradle

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import io.gitlab.arturbosch.detekt.DetektPlugin
import nebula.plugin.bintray.BintrayPlugin
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
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Handles any shareable logic between single and multiple module projects.
 */
open class PixelOutlawBaseModuleGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // doesn't do anything
    }

    internal fun applyMavenPublishConfiguration(target: Project) {
        target.withMavenPublish {
            target.configure<PublishingExtension> {
                publications {
                    findByName("nebula")?.let { publication ->
                        (publication as MavenPublication)
                        publication.pom {
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
                    jvmTarget = "11"
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
            target.pluginManager.apply(BintrayPlugin::class.java)
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
                sourceCompatibility = JavaVersion.VERSION_11.toString()
                targetCompatibility = JavaVersion.VERSION_11.toString()
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
