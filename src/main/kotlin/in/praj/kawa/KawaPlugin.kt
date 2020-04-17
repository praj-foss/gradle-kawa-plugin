/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.api.Project
import org.gradle.api.Plugin

/**
 * Plugin to set up Kawa Scheme projects.
 */
class KawaPlugin: Plugin<Project> {
    lateinit var project: Project

    override fun apply(project: Project) {
        this.project = project

        configureExtensions()
        configureTasks()
    }

    private fun configureExtensions() {
        project.extensions.create("kawa", KawaExtension::class.java, project)
    }

    private fun configureTasks() {
        project.tasks.create("downloadToolsKawa", KawaDownloadTools::class.java)
    }
}
