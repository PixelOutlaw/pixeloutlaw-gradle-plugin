package io.pixeloutlaw.gradle.spigot

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

/**
 * Sets up tasks to download and run the Spigot BuildTools JAR.
 */
class PixelOutlawSpigotBuildToolsGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create<PixelOutlawSpigotBuildToolsExtension>("spigotBuildTools")
        val downloadTask = target.tasks.create<DownloadSpigotBuildToolsTask>("downloadSpigotBuildTools")
        target.tasks.create<RunSpigotBuildToolsTask>("runSpigotBuildTools").apply { dependsOn(downloadTask) }
    }
}
