/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import `in`.praj.kawa.tasks.KawaConfigure
import `in`.praj.kawa.tasks.KawaDownload
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Plugin to set up Kawa projects.
 */
class KawaPlugin: Plugin<Project> {
    private lateinit var project: Project
    private lateinit var settings: KawaSettings

    override fun apply(project: Project) {
        this.project = project

        setupExtensions()
        setupTasks()
    }

    private fun setupExtensions() {
        val extension = project.extensions.create(KAWA_EXTENSION, KawaExtension::class.java)
        project.afterEvaluate {
            settings = KawaSettings(project, extension)
        }
    }

    private fun setupTasks() {
        val downloadKawa  = register<KawaDownload>(DOWNLOAD_TASK)
        downloadKawa.configure {
            it.version.set(settings.version)
            it.outFile.set(settings.sourceTar)
        }

        val configureKawa = register<KawaConfigure>(CONFIGURE_TASK)
        configureKawa.configure {
            it.dependsOn(downloadKawa)
            it.version.set(settings.version)
            it.sourceTar.set(settings.sourceTar)
            it.distDir.set(settings.distDir)
        }
    }

    private inline fun <reified T : Task> register(name: String) =
            project.tasks.register(name, T::class.java)
}
