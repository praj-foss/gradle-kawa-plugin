/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Plugin to set up Kawa projects.
 */

const val KAWA_EXTENSION_NAME  = "kawa"
const val DOWNLOAD_TOOLS_TASK  = "downloadToolsKawa"
const val CONFIGURE_TOOLS_TASK = "configureToolsKawa"

class KawaPlugin: Plugin<Project> {
    lateinit var project: Project
    lateinit var extension: KawaExtension

    override fun apply(project: Project) {
        this.project = project

        setupExtensions()
        setupTasks()
        applyConventions()
    }

    private fun setupExtensions() {
        extension = project.extensions.create(KAWA_EXTENSION_NAME, KawaExtension::class.java)
    }

    private fun setupTasks() {
        val dtk = register<KawaDownloadTools>(DOWNLOAD_TOOLS_TASK)

        register<KawaConfigureTools>(CONFIGURE_TOOLS_TASK)
                .configure { it.dependsOn(dtk) }
    }

    private fun applyConventions() {
        extension.apply {
            version.set("3.1.1")
            kawaBuildDir.set(project.buildDir.resolve("kawa"))
        }
    }

    private inline fun <reified T : Task> register(name: String) =
            project.tasks.register(name, T::class.java, extension)
}
