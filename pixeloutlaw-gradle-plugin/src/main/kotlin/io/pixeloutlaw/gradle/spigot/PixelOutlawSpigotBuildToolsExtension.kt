package io.pixeloutlaw.gradle.spigot

/**
 * Allows configuration of the Spigot Build Tools plugin.
 */
open class PixelOutlawSpigotBuildToolsExtension {
    /**
     * Which versions of Spigot to build and publish into Maven Local.
     */
    var versions: List<String> = emptyList()
}
