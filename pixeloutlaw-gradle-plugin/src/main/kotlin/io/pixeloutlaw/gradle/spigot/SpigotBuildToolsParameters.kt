package io.pixeloutlaw.gradle.spigot

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

/**
 * Worker action parameters for invoking Spigot Build Tools.
 */
@Suppress("UnstableApiUsage")
interface SpigotBuildToolsParameters : WorkParameters {
    /**
     * What is the root directory used by Spigot Build Tools?
     */
    val buildDir: DirectoryProperty

    /**
     * What file represents the BuildTools.jar?
     */
    val buildToolsJar: RegularFileProperty

    /**
     * Which version to build using the build tools?
     */
    val version: Property<String>
}
