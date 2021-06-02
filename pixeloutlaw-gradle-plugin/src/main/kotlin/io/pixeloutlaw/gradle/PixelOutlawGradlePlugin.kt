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
        target.allprojects { project ->
            // things that should go on every single project
            project.applyBaseConfiguration()

            // if the project is using Java, apply Java configuration
            project.pluginManager.withPlugin("java") {
                project.applyJavaConfiguration()
            }

            // if the project is using Kotlin, apply Kotlin configuration
            project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                project.applyKotlinConfiguration()
            }

            // setup maven central publication
            project.pluginManager.withPlugin("maven-publish") {
                project.publishToMavenCentral()
            }

            // make sure all publications are ready for maven central
            project.pluginManager.withPlugin("nebula.maven-publish") {
                project.configurePublicationsForMavenCentral()
            }
        }
    }
}
