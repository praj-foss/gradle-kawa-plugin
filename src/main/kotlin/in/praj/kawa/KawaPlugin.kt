/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin to setup Kawa projects and apply default configurations.
 */
class KawaPlugin : Plugin<Project> {
    private lateinit var project: Project
    private lateinit var basePlugin: KawaBasePlugin

    override fun apply(project: Project) {
        this.project = project
        basePlugin = project.plugins.apply(KawaBasePlugin::class.java)

        configureExtensions()
    }

    private fun configureExtensions() {
        basePlugin.kawaExtension.apply {
            cacheDir = project.buildDir.resolve("kawaCache")
        }

        basePlugin.kawacExtension.apply {
            srcDir   = project.projectDir.resolve("src")
            destDir  = project.buildDir.resolve("kawaClasses")
            language = "scheme"
            args     = "--warn-as-error"
        }
    }
}
