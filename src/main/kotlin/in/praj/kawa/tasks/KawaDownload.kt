/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Task to download Kawa toolchain sources.
 */
abstract class KawaDownload : DefaultTask() {
    @get:Input
    abstract val version: Property<String>

    @get:OutputFile
    abstract val outFile: RegularFileProperty

    @Internal
    protected val downloadUrl: Provider<String> = version
            .map { "ftp://ftp.gnu.org/pub/gnu/kawa/kawa-${it}.tar.gz" }

    @TaskAction
    fun perform() {
        val url = downloadUrl.get()
        val out = outFile.get().asFile
        val tmp = out.resolveSibling("${out.name}.part")

        logger.lifecycle("Downloading Kawa sources from {}", url)
        URL(url).openStream().use {
            Files.copy(it, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        Files.move(tmp.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING)
        logger.debug("Saved Kawa sources to {}", out)
    }
}
