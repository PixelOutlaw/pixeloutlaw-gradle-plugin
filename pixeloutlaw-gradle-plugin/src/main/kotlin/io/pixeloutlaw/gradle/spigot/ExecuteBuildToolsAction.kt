package io.pixeloutlaw.gradle.spigot

import org.gradle.api.file.FileSystemOperations
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import javax.inject.Inject

/**
 * Copies the BuildTools.jar and executes it.
 */
@Suppress("UnstableApiUsage")
abstract class ExecuteBuildToolsAction @Inject constructor(
    private val execOperations: ExecOperations,
    private val fileSystemOperations: FileSystemOperations
) : WorkAction<SpigotBuildToolsParameters> {
    override fun execute() {
        val versionDir = parameters.buildDir.dir(parameters.version)
        fileSystemOperations.copy {
            from(parameters.buildToolsJar)
            into(versionDir)
        }
        execOperations.javaexec {
            args = listOf(
                versionDir.get().file("BuildTools.jar").asFile.absolutePath,
                "--rev",
                parameters.version.get()
            )
            main = "-jar"
            workingDir = versionDir.get().asFile
        }
    }
}
