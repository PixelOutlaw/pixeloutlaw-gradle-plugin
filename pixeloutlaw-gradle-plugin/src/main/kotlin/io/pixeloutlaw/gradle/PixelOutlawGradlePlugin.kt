package io.pixeloutlaw.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Handles any shareable logic between single and multiple module projects.
 */
open class PixelOutlawGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // don't do anything if we aren't the root project
        if (target != target.rootProject) {
            return
        }

        // this can only be ran on the root project
        target.applyRootConfiguration()

        // All of the other plugin configs are conditional, so we can go ahead
        // and just apply them to allprojects
        target.allprojects {
            // things that should go on every single project
            applyBaseConfiguration()

            // if the project is using Java, apply Java configuration
            pluginManager.withPlugin("java") {
                applyJavaConfiguration()
            }

            // if the project is using Kotlin, apply Kotlin configuration
            pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                applyKotlinConfiguration()
            }

            // setup maven central publication
            pluginManager.withPlugin("maven-publish") {
                publishToMavenCentral()
            }

            // make sure all publications are ready for maven central
            pluginManager.withPlugin("nebula.maven-publish") {
                configurePublicationsForMavenCentral()
            }
        }
    }
}
