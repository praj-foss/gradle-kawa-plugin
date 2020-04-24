/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.api.Project
import java.io.File

/**
 * Stores plugin-wide settings.
 */
internal class KawaSettings(
        private val project: Project,
        private val extension: KawaExtension
) {
    fun version(): String = extension.version

    private fun cacheDir(): File = project.file(extension.cacheDir)

    private fun baseDir(): File = cacheDir().resolve(version())

    fun sourceFile(): File = baseDir().resolve("kawa-${version()}.tar.gz")

    fun distDir(): File = baseDir().resolve("dist")
}