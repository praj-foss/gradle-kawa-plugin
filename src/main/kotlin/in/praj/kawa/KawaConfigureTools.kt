/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import javax.inject.Inject

/**
 * Task to configure and build the downloaded Kawa tools.
 */
open class KawaConfigureTools @Inject constructor(
        private val extension: KawaExtension,
        downloadTask: KawaDownloadTools
) : DefaultTask() {
    @InputFile
    val tarball: Provider<RegularFile> = downloadTask.tarball

    @OutputDirectory
    val kawaTools: Provider<Directory> = tarball.map {
        extension.kawaBuildDir.get().dir(it.asFile.name.substringBefore(".tar.gz")) }

    @TaskAction
    fun perform() {
        val toolsDir = kawaTools.get().asFile.toPath()
        if (Files.notExists(toolsDir))
            extractTools()

        // TODO: Configure ant here
    }

    private fun extractTools() {
        project.copy { it
                .from(project.tarTree(tarball))
                .into(extension.kawaBuildDir) }
    }
}