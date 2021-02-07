package io.pixeloutlaw.gradle

import io.codearte.gradle.nexus.NexusStagingExtension
import io.codearte.gradle.nexus.NexusStagingPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Handles any shareable logic between single and multiple module projects.
 */
open class PixelOutlawGradlePlugin : Plugin<Project> {
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
            applyBaseConfiguration()
            applyTestLoggerPlugin()
            applyJavaConfiguration()
            applyKotlinConfiguration()
            applyMavenPublishConfiguration()
            applyNebulaMavenPublishConfiguration()
        }
    }
}
