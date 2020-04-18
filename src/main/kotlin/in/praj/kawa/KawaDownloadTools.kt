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

/**
 * Task to download Kawa tools.
 */
open class KawaDownloadTools : DefaultTask() {
    @Input
    val version: Property<String> = project.extensions.getByType(KawaExtension::class.java).version

    @Internal
    val tarballName: Provider<String> = version.map { "kawa-${it}.tar.gz" }

    @OutputFile
    val tarball: Provider<RegularFile> =
            tarballName.map { project.layout.buildDirectory.get().dir("kawa").file(it) }

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