/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import `in`.praj.kawa.tasks.KawaCompile
import `in`.praj.kawa.tasks.KawaConfigure
import `in`.praj.kawa.tasks.KawaDownload
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware

/**
 * Plugin to set up Kawa projects.
 */
class KawaBasePlugin: Plugin<Project> {
    private lateinit var project: Project
    private lateinit var settings: KawaSettings
    internal lateinit var kawaExtension: KawaExtension
    internal lateinit var kawacExtension: KawacExtension

    override fun apply(project: Project) {
        this.project = project

        setupExtensions()
        setupTasks()
    }

    private fun setupExtensions() {
        kawaExtension = project.extensions
                .create(KAWA_EXTENSION, KawaExtension::class.java)
        kawacExtension = (kawaExtension as ExtensionAware).extensions
                .create(KAWAC_EXTENSION, KawacExtension::class.java)

        project.afterEvaluate {
            settings = KawaSettings(project, kawaExtension, kawacExtension)
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

        val compileKawa = register<KawaCompile>(COMPILE_TASK)
        compileKawa.configure {
            if (settings.distDir.exists().not())
                it.dependsOn(configureKawa)
            it.source = project.fileTree(settings.kawacSrcDir)
            it.classpath = project.fileTree(settings.distDir) { f -> f.include("*.jar") }
            it.destinationDir = settings.kawacDestDir
            it.language.set(settings.kawacLanguage)
            it.args.set(settings.kawacArgs)
            it.sourceCompatibility = JavaVersion.VERSION_1_8.toString()
            it.targetCompatibility = JavaVersion.VERSION_1_8.toString()
        }
    }

    private inline fun <reified T : Task> register(name: String) =
            project.tasks.register(name, T::class.java)
}
