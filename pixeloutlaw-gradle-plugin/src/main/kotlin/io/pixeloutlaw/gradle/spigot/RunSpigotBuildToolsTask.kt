package io.pixeloutlaw.gradle.spigot

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

/**
 * Invokes Spigot BuildTools.jar for a specific version.
 */
open class RunSpigotBuildToolsTask @Inject constructor(private val workerExecutor: WorkerExecutor) : DefaultTask() {
    @get:InputFile
    var downloadedJar: File = project.buildDir.resolve("spigot-build-tools").resolve("BuildTools.jar")

    @Suppress("UnstableApiUsage")
    @TaskAction
    fun runSpigotBuildTools() {
        // Create a WorkQueue to submit work items
        val workQueue = workerExecutor.noIsolation()

        val buildTools = project.extensions.getByType<PixelOutlawSpigotBuildToolsExtension>()
        buildTools.versions.forEach {
            workQueue.submit(ExecuteBuildToolsAction::class.java) {
                buildDir.set(downloadedJar.parentFile)
                buildToolsJar.set(downloadedJar)
                version.set(it)
            }
        }
    }
}
