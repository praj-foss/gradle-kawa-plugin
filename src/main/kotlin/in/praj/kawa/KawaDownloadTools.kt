/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
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
    val version: Property<String> = extension.version

    @Internal
    val tarballName: Provider<String> = version.map { "kawa-${it}.tar.gz" }

    @OutputFile
    val tarball: Provider<RegularFile> = extension.kawaBuildDir.map { it.file(tarballName.get()) }

    @TaskAction
    fun perform() {
        val downloadUrl = URL("ftp://ftp.gnu.org/pub/gnu/kawa/${tarballName.get()}")
        val target = tarball.get().asFile.toPath()
        val tmpTarget = target.resolveSibling("${tarballName.get()}.part")

        downloadUrl.openStream().use {
            Files.copy(it, tmpTarget, StandardCopyOption.REPLACE_EXISTING)
        }
        Files.move(tmpTarget, target, StandardCopyOption.REPLACE_EXISTING)
        println("Downloaded Kawa ${version.get()}")
    }
}