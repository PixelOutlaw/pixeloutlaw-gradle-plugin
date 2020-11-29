package io.pixeloutlaw.gradle.spigot

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.withGroovyBuilder
import java.io.File

/**
 * Task to download Spigot Build Tools to the build directory.
 */
open class DownloadSpigotBuildToolsTask : DefaultTask() {
    init {
        description = "Download Spigot BuildTools.jar into a build directory for use."
        group = "spigot"
    }

    @get:Input
    var downloadUrl: String =
        "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar"

    @get:OutputFile
    var downloadedJar: File = project.buildDir.resolve("spigot-build-tools").resolve("BuildTools.jar")

    @TaskAction
    fun download() {
        ant.withGroovyBuilder {
            "get"("src" to downloadUrl, "dest" to downloadedJar.toPath())
        }
    }
}
