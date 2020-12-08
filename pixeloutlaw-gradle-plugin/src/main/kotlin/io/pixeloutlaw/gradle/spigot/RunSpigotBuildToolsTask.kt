package io.pixeloutlaw.gradle.spigot

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import org.gradle.process.ExecOperations
import org.jetbrains.dokka.utilities.ServiceLocator.toFile
import java.io.File
import javax.inject.Inject

/**
 * Invokes Spigot BuildTools.jar for a specific version.
 */
@Suppress("UnstableApiUsage")
open class RunSpigotBuildToolsTask @Inject constructor(
    private val execOperations: ExecOperations,
    private val fileSystemOperations: FileSystemOperations
) : DefaultTask() {
    @get:InputFile
    var buildToolsJar: File = project.buildDir.resolve("spigot-build-tools").resolve("BuildTools.jar")

    @Suppress("UnstableApiUsage")
    @TaskAction
    fun runSpigotBuildTools() {
        val buildTools = project.extensions.getByType<PixelOutlawSpigotBuildToolsExtension>()
        buildTools.versions.forEach {
            val mavenLocalDirectory = project.repositories.mavenLocal().url.toURL().toFile()
            val versionJar =
                mavenLocalDirectory.resolve("org/spigotmc/spigot/$it-R0.1-SNAPSHOT/spigot-$it-R0.1-SNAPSHOT.jar")
            if (versionJar.exists()) {
                logger.lifecycle("Skipping $it as Spigot JAR is found at ${versionJar.absolutePath}")
                return@forEach
            }
            val versionDir = buildToolsJar.parentFile.resolve(it)
            fileSystemOperations.copy {
                from(buildToolsJar)
                into(versionDir)
            }
            execOperations.javaexec {
                args = listOf(
                    buildToolsJar.absolutePath,
                    "--rev",
                    it
                )
                main = "-jar"
                workingDir = versionDir.absoluteFile
            }
        }
    }
}
