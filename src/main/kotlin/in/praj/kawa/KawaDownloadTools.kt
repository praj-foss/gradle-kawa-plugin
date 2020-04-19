/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject

/**
 * Task to download Kawa tools.
 */
open class KawaDownloadTools @Inject constructor(
        extension: KawaExtension
) : DefaultTask() {
    @Input
    val kawaDist: Provider<String> = extension.version
            .map { "kawa-${it}" }

    @OutputFile
    val tarball: Provider<File> = kawaDist
            .map { extension.kawaBuildDir.get().asFile.resolve("${it}.tar.gz") }

    @OutputDirectory
    val untarred: Provider<File> = kawaDist
            .map { extension.kawaBuildDir.get().asFile.resolve(it) }
    // Automatically creates the directory during get() if it doesn't exist

    @TaskAction
    fun perform() = tarball.get().let {
        if (it.exists().not())
            download(it)
        if (untarred.get().isEmpty())
            untar(it)
    }

    private fun download(target: File) {
        val tmpTarget = target.toPath().resolveSibling("${target.name}.part")
        URL("ftp://ftp.gnu.org/pub/gnu/kawa/${target.name}")
                .openStream()
                .use {
                    Files.copy(it, tmpTarget, StandardCopyOption.REPLACE_EXISTING)
                }
        Files.move(tmpTarget, target.toPath(), StandardCopyOption.ATOMIC_MOVE)
        println("Downloaded ${target.name}")
    }

    private fun untar(tar: File) {
        project.run {
            copy { it.from(tarTree(tar)).into(tar.parentFile) }
        }
    }

    /**
     * Extension method on a [File] to quickly check if
     * it's an empty directory.
     */
    private fun File.isEmpty() : Boolean =
            Files.list(this.toPath())
                    .findAny()
                    .isPresent
                    .not()
}
